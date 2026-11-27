#!/usr/bin/env bash

set -euo pipefail

# Always resolve collections relative to the repository, not the caller's cwd.
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

COLLECTIONS=(
    "$ROOT_DIR/postman/collections/MS3 Soumissions"
    "$ROOT_DIR/postman/collections/MS5 Verification"
)

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Make the script usable even if the caller's shell lost the Node/Postman PATH entry.
ensure_postman() {
    if command -v postman >/dev/null 2>&1; then
        return 0
    fi

    local candidate postman_dir
    for candidate in "$HOME"/Downloads/node-*/bin/postman \
                     "$HOME"/.nvm/versions/node/*/bin/postman \
                     /usr/local/bin/postman /usr/bin/postman; do
        if [[ -x "$candidate" ]]; then
            postman_dir="$(dirname "$candidate")"
            export PATH="$postman_dir:$PATH"
            log "[INFO] postman found at $candidate (added $postman_dir to PATH for this script)"
            return 0
        fi
    done

    log "[ERROR] postman CLI not found. Install it with: npm install -g postman-cli@1.94.0"
    return 127
}

lint_collection() {
    local collection="$1"

    log "Checking collection: ${collection#"$ROOT_DIR/"}"

    if [[ ! -d "$collection" ]]; then
        log "[ERROR] Directory not found: $collection"
        return 1
    fi

    if postman collection lint "$collection" -f error; then
        log "[SUCCESS] Lint passed."
        return 0
    fi

    log "[ERROR] Lint failed."
    return 1
}

lint_all() {
    local passed=0 failed=0 collection

    log "Starting lint for all collections..."

    for collection in "${COLLECTIONS[@]}"; do
        echo
        if lint_collection "$collection"; then
            passed=$((passed + 1))
        else
            failed=$((failed + 1))
        fi
    done

    echo
    log "========== FINAL SUMMARY =========="
    log "Total  : ${#COLLECTIONS[@]}"
    log "Passed : $passed"
    log "Failed : $failed"
    log "==================================="

    (( failed == 0 ))
}

show_menu() {
    if [[ -t 1 ]]; then
        clear || true
    fi

    echo "======================================"
    echo "       POSTMAN COLLECTION LINTER"
    echo "======================================"
    echo
    echo "  1) Lint MS3 Soumissions"
    echo "  2) Lint MS5 Verification"
    echo "  3) Lint ALL collections"
    echo "  0) Exit"
    echo
    echo "======================================"
}

usage() {
    cat <<EOF
Usage: scripts/test-postman.sh [OPTION]

  --all     Lint both collections (default when run without a terminal)
  --ms3     Lint the MS3 Soumissions collection
  --ms5     Lint the MS5 Verification collection
  --help    Show this help

With no arguments and an interactive terminal, a menu is shown.
EOF
}

interactive_menu() {
    local choice

    while true; do
        show_menu
        if ! read -rp "Select an option: " choice; then
            echo
            log "No input available; exiting."
            return 0
        fi

        case "$choice" in
            1) lint_collection "${COLLECTIONS[0]}" ;;
            2) lint_collection "${COLLECTIONS[1]}" ;;
            3) lint_all ;;
            0)
                log "Exiting Postman Linter."
                return 0
                ;;
            *)
                log "[WARNING] Invalid option. Try again."
                ;;
        esac

        echo
        if [[ -t 0 ]]; then
            read -rp "Press Enter to return to menu..." || true
        else
            break
        fi
    done
}

main() {
    ensure_postman

    if (( $# > 0 )); then
        case "$1" in
            --all)   lint_all ;;
            --ms3)   lint_collection "${COLLECTIONS[0]}" ;;
            --ms5)   lint_collection "${COLLECTIONS[1]}" ;;
            --help|-h) usage ;;
            *)
                log "[ERROR] Unknown option: $1"
                usage
                return 2
                ;;
        esac
        return $?
    fi

    if [[ -t 0 ]]; then
        interactive_menu
    else
        lint_all
    fi
}

main "$@"
