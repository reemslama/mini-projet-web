#!/usr/bin/env bash
# Launch all backend services of the Arène project.
#   Eureka peer1 (8761) and peer2 (8762)
#   MS1 Challenges (8081), MS2 Participants (8082, 8092)
#   MS4 Evaluation (8084, 8087)
#   MS3 Soumission (8083)
#   MS5 Verification (8085)
#   MS6 Reputation (8086, 8096)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LOG_DIR="$SCRIPT_DIR/logs"
RUN_DIR="$SCRIPT_DIR/run"
MS2_DIR="$REPO_ROOT/Backend/ms2-participants"
WAIT_TIMEOUT="${WAIT_TIMEOUT:-300}"
SKIP_DB=0

usage() {
    cat <<EOF
Usage: scripts/start-all.sh [--skip-db]

  --skip-db      Do not check/start MySQL (use when MS2/MS4 are not needed).

Environment:
  WAIT_TIMEOUT   Seconds to wait per service port (default: 300).
EOF
}

for arg in "$@"; do
    case "$arg" in
        --skip-db) SKIP_DB=1 ;;
        -h|--help) usage; exit 0 ;;
        *) printf 'Unknown option: %s\n' "$arg" >&2; usage >&2; exit 1 ;;
    esac
done

mkdir -p "$LOG_DIR" "$RUN_DIR"

info() { printf '[start-all] %s\n' "$*"; }
warn() { printf '[start-all] WARNING: %s\n' "$*" >&2; }

port_open() {
    (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null
}

wait_for_port() {
    local label="$1" port="$2" limit="${3:-$WAIT_TIMEOUT}" pid="${4:-}"
    local log_name="${5:-$label}" start=$SECONDS
    while ! port_open "$port"; do
        if [[ -n "$pid" ]] && ! kill -0 "$pid" 2>/dev/null; then
            warn "$label exited before opening port $port (see $LOG_DIR/$log_name.log)"
            return 1
        fi
        if (( SECONDS - start >= limit )); then
            warn "$label did not open port $port within ${limit}s (see $LOG_DIR/$log_name.log)"
            return 1
        fi
        sleep 2
    done
    info "$label is listening on http://localhost:$port"
}

wait_service() {
    local name="$1" label="$2" port="$3" pid=''
    [[ -f "$RUN_DIR/$name.pid" ]] && pid=$(cat "$RUN_DIR/$name.pid")
    wait_for_port "$label" "$port" "$WAIT_TIMEOUT" "$pid" "$name"
}

ensure_host_aliases() {
    local alias missing=()
    for alias in eureka1; do
        if ! getent hosts "$alias" >/dev/null 2>&1; then
            missing+=("$alias")
        fi
    done

    if (( ${#missing[@]} == 0 )); then
        info "host alias eureka1 is already configured"
        return 0
    fi

    info "adding ${missing[*]} to /etc/hosts"
    if [[ $(id -u) -eq 0 ]]; then
        printf '127.0.0.1 %s\n' "${missing[*]}" >> /etc/hosts
    elif command -v sudo >/dev/null 2>&1; then
        printf '127.0.0.1 %s\n' "${missing[*]}" | sudo tee -a /etc/hosts >/dev/null
    else
        warn "cannot edit /etc/hosts automatically; add this line as root:"
        warn "  127.0.0.1 eureka1"
        return 1
    fi

    for alias in "${missing[@]}"; do
        if ! getent hosts "$alias" >/dev/null 2>&1; then
            warn "host alias $alias is still not resolvable"
            return 1
        fi
    done
    info "host aliases configured"
}

ensure_mysql() {
    if port_open 3306; then
        info "MySQL is already listening on port 3306"
        return 0
    fi

    if ! command -v docker >/dev/null 2>&1 || ! docker info >/dev/null 2>&1; then
        warn "MySQL is not running on port 3306 and Docker is unavailable."
        warn "MS2 and MS4 need MySQL (user root, empty password)."
        return 1
    fi

    if docker ps -a --format '{{.Names}}' 2>/dev/null | grep -qx 'arene-mysql'; then
        info "starting existing arene-mysql container"
        docker start arene-mysql >/dev/null
    else
        info "starting MySQL 8 container 'arene-mysql'"
        docker run -d --name arene-mysql \
            -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
            -e MYSQL_DATABASE=forge_evaluation \
            -p 3306:3306 mysql:8 >/dev/null
    fi
    wait_for_port "MySQL" 3306 120
}

# Hibernate cannot auto-detect the dialect on recent MariaDB releases.
detect_db_dialect_arg() {
    local client version=''
    for client in mariadb mysql; do
        if command -v "$client" >/dev/null 2>&1; then
            version=$("$client" -uroot -N -B -e 'SELECT VERSION()' 2>/dev/null || true)
            [[ -n "$version" ]] && break
        fi
    done
    if [[ "$version" == *MariaDB* ]]; then
        printf '%s' '--spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect'
    fi
}

start_service() {
    local name="$1" dir="$2" port="$3" run_args="$4"
    local pid_file="$RUN_DIR/$name.pid"
    local old_pid
    local -a mvn_cmd

    if [[ -f "$pid_file" ]]; then
        old_pid=$(cat "$pid_file")
        if [[ "$old_pid" =~ ^[0-9]+$ ]] && kill -0 "$old_pid" 2>/dev/null; then
            info "$name is already running (PID $old_pid)"
            return 0
        fi
    fi

    if port_open "$port"; then
        warn "port $port is already in use; skipping $name"
        return 0
    fi

    if [[ -x "$dir/mvnw" ]]; then
        mvn_cmd=("$dir/mvnw")
    elif [[ -f "$dir/mvnw" ]]; then
        mvn_cmd=(sh "$dir/mvnw")
    else
        mvn_cmd=(mvn)
    fi

    ( cd "$dir" && "${mvn_cmd[@]}" spring-boot:run "-Dspring-boot.run.arguments=$run_args" ) \
        > "$LOG_DIR/$name.log" 2>&1 &
    printf '%s\n' "$!" > "$pid_file"
    info "started $name (PID $!, log: $LOG_DIR/$name.log)"
}

# Both MS2 instances run from one jar: starting two `mvn spring-boot:run`
# processes in the same directory makes them fight over target/classes.
ensure_ms2_jar() {
    local jar
    jar=$(find "$MS2_DIR/target" -maxdepth 1 -name 'ms2-participants-*.jar' \
        ! -name '*original*' -print -quit 2>/dev/null || true)
    if [[ -n "$jar" ]]; then
        printf '%s' "$jar"
        return 0
    fi
    info "building MS2 jar (shared by both instances)"
    if ! ( cd "$MS2_DIR" && mvn -q -DskipTests package ) > "$LOG_DIR/ms2-build.log" 2>&1; then
        warn "MS2 build failed (see $LOG_DIR/ms2-build.log)"
        return 1
    fi
    jar=$(find "$MS2_DIR/target" -maxdepth 1 -name 'ms2-participants-*.jar' \
        ! -name '*original*' -print -quit 2>/dev/null || true)
    [[ -n "$jar" ]] || { warn "MS2 jar not found after build"; return 1; }
    printf '%s' "$jar"
}

start_ms2_instance() {
    local name="$1" port="$2" jar="$3"
    local pid_file="$RUN_DIR/$name.pid" old_pid

    if [[ -f "$pid_file" ]]; then
        old_pid=$(cat "$pid_file")
        if [[ "$old_pid" =~ ^[0-9]+$ ]] && kill -0 "$old_pid" 2>/dev/null; then
            info "$name is already running (PID $old_pid)"
            return 0
        fi
    fi
    if port_open "$port"; then
        warn "port $port is already in use; skipping $name"
        return 0
    fi

    setsid java -jar "$jar" "--server.port=$port" $DB_DIALECT_ARG \
        > "$LOG_DIR/$name.log" 2>&1 < /dev/null &
    printf '%s\n' "$!" > "$pid_file"
    info "started $name (PID $!, log: $LOG_DIR/$name.log)"
}

if ! command -v java >/dev/null 2>&1; then
    printf '[start-all] ERROR: Java is not installed or not on PATH.\n' >&2
    exit 1
fi

info "project root: $REPO_ROOT"
ensure_host_aliases || {
    warn "Eureka requires the eureka1 host alias."
    exit 1
}

DB_DIALECT_ARG=''
if (( SKIP_DB == 0 )); then
    if ! ensure_mysql; then
        warn "continuing without MySQL; MS2 and MS4 will fail to start until a database is available"
    else
        DB_DIALECT_ARG=$(detect_db_dialect_arg)
    fi
fi

# Discovery servers first, so clients can register as soon as they are up.
start_service eureka-1 "$REPO_ROOT/Backend/eureka-server" 8761 "--spring.profiles.active=peer1"
wait_service eureka-1 "Eureka peer1" 8761 || true
start_service eureka-2 "$REPO_ROOT/Backend/eureka-server" 8762 "--spring.profiles.active=peer2"
wait_service eureka-2 "Eureka peer2" 8762 || true

start_service ms1-challenges "$REPO_ROOT/Backend/ms1-challenges" 8081 ""
# Start MS2 instances sequentially: DataInitializer seeds only when the table is
# empty, so launching both at once causes a duplicate-key failure on one of them.
rm -f "$RUN_DIR/ms2-participants.pid"
if MS2_JAR=$(ensure_ms2_jar); then
    start_ms2_instance ms2-participants-1 8082 "$MS2_JAR"
    wait_service ms2-participants-1 "MS2 Participants 1" 8082 || true
    start_ms2_instance ms2-participants-2 8092 "$MS2_JAR"
    wait_service ms2-participants-2 "MS2 Participants 2" 8092 || true
fi
start_service ms4-evaluation-1 "$REPO_ROOT/Backend/ms4-evaluation" 8084 "--server.port=8084 $DB_DIALECT_ARG"
start_service ms4-evaluation-2 "$REPO_ROOT/Backend/ms4-evaluation" 8087 "--server.port=8087 $DB_DIALECT_ARG"
start_service ms3-soumission "$REPO_ROOT/Backend/soumission" 8083 ""
start_service ms5-verification "$REPO_ROOT/Backend/ms5-verification" 8085 ""
start_service ms6-reputation-1 "$REPO_ROOT/Backend/ms6-reputation" 8086 "--server.port=8086"
start_service ms6-reputation-2 "$REPO_ROOT/Backend/ms6-reputation" 8096 "--server.port=8096"

wait_service ms1-challenges "MS1 Challenges" 8081 || true
wait_service ms4-evaluation-1 "MS4 Evaluation 1" 8084 || true
wait_service ms4-evaluation-2 "MS4 Evaluation 2" 8087 || true
wait_service ms3-soumission "MS3 Soumission" 8083 || true
wait_service ms5-verification "MS5 Verification" 8085 || true
wait_service ms6-reputation-1 "MS6 Reputation 1" 8086 || true
wait_service ms6-reputation-2 "MS6 Reputation 2" 8096 || true

printf '\n'
info "services requested:"
printf '  Eureka 8761     http://localhost:8761  (registry: http://localhost:8761/eureka/apps)\n'
printf '  Eureka 8762     http://localhost:8762  (peer2, profile peer2)\n'
printf '  MS1 (8081)      health http://localhost:8081/actuator/health  docs http://localhost:8081/swagger-ui.html\n'
printf '  MS2 (8082)      health http://localhost:8082/actuator/health  docs http://localhost:8082/swagger-ui.html\n'
printf '  MS2 (8092)      health http://localhost:8092/actuator/health\n'
printf '  MS6 (8086)      health http://localhost:8086/actuator/health  docs http://localhost:8086/swagger-ui.html\n'
printf '  MS6 (8096)      health http://localhost:8096/actuator/health  docs http://localhost:8096/swagger-ui.html\n'
printf '  MS4 (8084)      health http://localhost:8084/actuator/health  docs http://localhost:8084/swagger-ui.html\n'
printf '  MS4 (8087)      health http://localhost:8087/actuator/health  docs http://localhost:8087/swagger-ui.html\n'
printf '  MS3 (8083)      health http://localhost:8083/actuator/health  docs http://localhost:8083/swagger-ui.html\n'
printf '  MS5 (8085)      health http://localhost:8085/actuator/health  docs http://localhost:8085/swagger-ui.html\n'
printf '  MS5 OpenAPI     http://localhost:8085/v3/api-docs  |  eureka http://localhost:8761/eureka/apps/MS5-VERIFICATION\n'
printf '  MS5 requests    %s/Backend/ms5-verification/verifications.http\n' "$REPO_ROOT"
printf '  Logs            %s\n' "$LOG_DIR"
printf '  Stop all        scripts/stop-all.sh\n'
