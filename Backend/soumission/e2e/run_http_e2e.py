#!/usr/bin/env python3
"""End-to-end runner for Backend/soumission/submissions.http.

The file is executed in order with urllib; @variables, {{placeholders}} and the
IntelliJ response handlers (`> {% client.global.set(...) %}`) are supported.
Expected HTTP codes are read from the `### ... — 201` comments.

Blocks whose description starts with [SETUP] are skipped: they are fixtures for
running the .http file manually, endpoint by endpoint, in an HTTP client.

Usage (service must already be running):
    ./start-all.sh
    python3 Backend/soumission/e2e/run_http_e2e.py
    python3 Backend/soumission/e2e/run_http_e2e.py --base-url http://localhost:8083
"""

import argparse
import json
import re
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

DEFAULT_FILE = Path(__file__).resolve().parents[1] / "submissions.http"
VAR_RE = re.compile(r"^@([A-Za-z0-9_]+)\s*=\s*(.+?)\s*$")
PLACEHOLDER_RE = re.compile(r"\{\{\s*([A-Za-z0-9_]+)\s*\}\}")
REQUEST_RE = re.compile(r"^(GET|POST|PUT|PATCH|DELETE)\s+(\S+)\s*$")
STATUS_RE = re.compile(r"(?<!\d)([1-5]\d{2})(?!\d)")
SET_RE = re.compile(
    r'client\.global\.set\(["\']([^"\']+)["\']\s*,\s*response\.body(?:\.([A-Za-z0-9_$]+))?\s*\)'
)


def parse_file(path: Path):
    """Return global variables and (description, lines) blocks."""
    variables = {}
    blocks = []
    description = None
    lines = []

    for line in path.read_text(encoding="utf-8").splitlines():
        variable = VAR_RE.match(line)
        if variable:
            variables[variable.group(1)] = variable.group(2).strip()
        if line.startswith("###"):
            if description is not None:
                blocks.append((description, lines))
            description = line[3:].strip()
            lines = []
        elif description is not None:
            lines.append(line)
    if description is not None:
        blocks.append((description, lines))
    return variables, blocks


def parse_request(lines):
    """Extract method, url, headers, body and response handlers from one block."""
    method = url = None
    headers = {}
    body = []
    handlers = []
    mode = "start"

    for line in lines:
        stripped = line.strip()
        if not stripped:
            if mode == "headers":
                mode = "body"
            continue
        if stripped.startswith("@"):
            continue
        if method is None:
            match = REQUEST_RE.match(stripped)
            if match:
                method, url = match.group(1), match.group(2)
                mode = "headers"
                continue
        if stripped.startswith(">") or mode == "handler":
            mode = "handler"
            handlers.append(stripped)
            continue
        if mode == "headers" and ":" in stripped and not stripped.startswith("{"):
            name, value = stripped.split(":", 1)
            headers[name.strip()] = value.strip()
            continue
        body.append(line)

    if method is None:
        return None
    return method, url, headers, "\n".join(body).strip(), handlers


def resolve(value, variables, stack=()):
    """Recursively replace {{name}} with the current value of the variable."""
    def replace(match):
        name = match.group(1)
        if name in stack:
            raise ValueError(f"circular variable {name}")
        if name not in variables:
            raise ValueError(f"undefined variable {{{{{name}}}}}")
        return resolve(str(variables[name]), variables, stack + (name,))

    return PLACEHOLDER_RE.sub(replace, value)


def wait_for_service(base_url, attempts=10, delay=1):
    health = base_url.rstrip("/") + "/actuator/health"
    for _ in range(attempts):
        try:
            with urllib.request.urlopen(health, timeout=2) as response:
                if response.status == 200:
                    return True
        except (urllib.error.URLError, OSError):
            time.sleep(delay)
    return False


def run(path: Path, base_url=None, timeout=10):
    variables, blocks = parse_file(path)
    if base_url:
        variables["host"] = base_url.rstrip("/")
    host = str(variables.get("host", "http://localhost:8083"))

    if not wait_for_service(host):
        print(f"MS3 is not reachable at {host}/actuator/health")
        print("Start it with ./start-all.sh (or mvn spring-boot:run in Backend/soumission) first.")
        return 1

    passed = failed = skipped = 0
    for description, lines in blocks:
        if description.upper().startswith("[SETUP]"):
            skipped += 1
            continue
        request = parse_request(lines)
        if request is None:
            skipped += 1
            continue
        method, raw_url, raw_headers, raw_body, handlers = request

        try:
            url = resolve(raw_url, variables)
            if not url.startswith("http"):
                url = host.rstrip("/") + "/" + url.lstrip("/")
            headers = {key: resolve(value, variables) for key, value in raw_headers.items()}
            data = resolve(raw_body, variables).encode("utf-8") if raw_body else None

            status_match = STATUS_RE.search(description)
            expected = int(status_match.group(1)) if status_match else None

            req = urllib.request.Request(url, data=data, headers=headers, method=method)
            try:
                with urllib.request.urlopen(req, timeout=timeout) as response:
                    actual = response.status
                    payload = response.read().decode("utf-8", "replace")
            except urllib.error.HTTPError as error:
                actual = error.code
                payload = error.read().decode("utf-8", "replace")
        except (ValueError, OSError) as error:
            print(f"[ERROR] {description}: {error}")
            failed += 1
            continue

        ok = actual == expected if expected else 200 <= actual < 300
        label = f"{description}  -> {actual} (expected {expected})" if expected else f"{description}  -> {actual}"
        if ok:
            passed += 1
            print(f"[PASS] {label}")
        else:
            failed += 1
            print(f"[FAIL] {label}")
            print(f"       {payload[:400]}")

        if ok and handlers:
            try:
                body = json.loads(payload)
                for match in SET_RE.finditer("\n".join(handlers)):
                    name, json_path = match.group(1), match.group(2)
                    value = body
                    for key in (json_path or "").split("."):
                        if key:
                            value = value[key]
                    variables[name] = value
            except (ValueError, KeyError, TypeError) as error:
                print(f"[ERROR] response handler for {description}: {error}")
                failed += 1

    print(f"\n{passed} passed, {failed} failed, {skipped} non-request blocks skipped")
    return 0 if failed == 0 else 1


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--file", type=Path, default=DEFAULT_FILE, help="the .http file to execute")
    parser.add_argument("--base-url", help="override @host (e.g. http://localhost:8083)")
    parser.add_argument("--timeout", type=float, default=10, help="per-request timeout in seconds")
    args = parser.parse_args()
    sys.exit(run(args.file, args.base_url, args.timeout))


if __name__ == "__main__":
    main()
