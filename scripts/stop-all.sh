#!/usr/bin/env bash
# Stop every service started by scripts/start-all.sh.
set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUN_DIR="$ROOT_DIR/run"

if [[ ! -d "$RUN_DIR" ]]; then
    printf '[stop-all] nothing to stop\n'
    exit 0
fi

shopt -s nullglob
for pid_file in "$RUN_DIR"/*.pid; do
    name=$(basename "$pid_file" .pid)
    pid=$(cat "$pid_file")

    if [[ "$pid" =~ ^[0-9]+$ ]] && kill -0 "$pid" 2>/dev/null; then
        kill "$pid" 2>/dev/null || true
        for _ in {1..10}; do
            kill -0 "$pid" 2>/dev/null || break
            sleep 0.2
        done
        if kill -0 "$pid" 2>/dev/null; then
            kill -9 "$pid" 2>/dev/null || true
        fi
        printf '[stop-all] stopped %s (PID %s)\n' "$name" "$pid"
    else
        printf '[stop-all] %s was not running\n' "$name"
    fi
    rm -f "$pid_file"
done
