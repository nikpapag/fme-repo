#!/usr/bin/env bash
# Start the FME workshop Spring Boot app locally.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="${ROOT}/application"
cd "$APP_DIR"

# --- Locate Java (macOS often has JDK via Homebrew but not on PATH) ---
find_java_home() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    echo "$JAVA_HOME"
    return 0
  fi
  if command -v /usr/libexec/java_home &>/dev/null; then
    local jh
    jh="$(/usr/libexec/java_home 2>/dev/null || true)"
    if [[ -n "$jh" && -x "$jh/bin/java" ]]; then
      echo "$jh"
      return 0
    fi
    for ver in 21 17 11; do
      jh="$(/usr/libexec/java_home -v "$ver" 2>/dev/null || true)"
      if [[ -n "$jh" && -x "$jh/bin/java" ]]; then
        echo "$jh"
        return 0
      fi
    done
  fi
  local candidates=(
    /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
    /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
    /opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home
    /usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
    /usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
    /Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
    /Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
    /Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home
  )
  for c in "${candidates[@]}"; do
    if [[ -x "$c/bin/java" ]]; then
      echo "$c"
      return 0
    fi
  done
  return 1
}

setup_java() {
  local jh
  if ! jh="$(find_java_home)"; then
    echo "ERROR: Java 17+ not found."
    echo ""
    echo "Install JDK 17, then re-run:"
    echo "  brew install openjdk@17"
    echo "  echo 'export PATH=\"/opt/homebrew/opt/openjdk@17/bin:\$PATH\"' >> ~/.zshrc"
    echo "  source ~/.zshrc"
    echo ""
    echo "Or set JAVA_HOME manually:"
    echo "  export JAVA_HOME=\$(/usr/libexec/java_home -v 17)"
    return 1
  fi
  export JAVA_HOME="$jh"
  export PATH="${JAVA_HOME}/bin:${PATH}"
  echo "Using JAVA_HOME=${JAVA_HOME}"
  java -version 2>&1 | head -1
}

# Load .env from application/ if present
if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
  echo "Loaded environment from application/.env"
fi

if [[ -z "${SPLIT_SDK_KEY:-}" || "${SPLIT_SDK_KEY}" == "your-staging-server-side-sdk-key" ]]; then
  echo "ERROR: SPLIT_SDK_KEY is not set."
  echo ""
  echo "  1. cp application/.env.example application/.env"
  echo "  2. Edit application/.env and paste your Harness FME server-side SDK key"
  echo "  OR:  export SPLIT_SDK_KEY=\"your-key\" && ./run-local.sh"
  echo ""
  echo "Get the key: Harness → Feature Management & Experimentation → SDK API Keys"
  exit 1
fi

export SPLIT_SDK_KEY
export SERVER_PORT="${SERVER_PORT:-8080}"

run_maven() {
  if [[ -x ./mvnw ]]; then
    ./mvnw "$@"
  elif command -v mvn &>/dev/null; then
    mvn "$@"
  else
    echo "ERROR: Neither ./mvnw nor mvn found. Install Maven: brew install maven"
    exit 1
  fi
}

doctor() {
  echo "=== run-local.sh doctor ==="
  echo "ROOT=$ROOT"
  echo "APP_DIR=$APP_DIR"
  echo "SPLIT_SDK_KEY set: $([[ -n "${SPLIT_SDK_KEY:-}" ]] && echo yes || echo no)"
  echo ""
  echo "--- Java ---"
  if setup_java; then
    echo "OK"
  else
    echo "FAIL — install JDK 17+"
  fi
  echo ""
  echo "--- Maven ---"
  if [[ -x ./mvnw ]]; then
    echo "mvnw: present"
    if [[ -f .mvn/wrapper/maven-wrapper.properties ]]; then
      echo "maven-wrapper.properties: present"
    else
      echo "maven-wrapper.properties: MISSING"
    fi
  else
    echo "mvnw: missing"
  fi
  command -v mvn &>/dev/null && echo "system mvn: $(command -v mvn)" || echo "system mvn: not found"
  echo ""
  echo "--- Quick test ---"
  if setup_java 2>/dev/null; then
    ./mvnw -v 2>&1 | head -3 || mvn -v 2>&1 | head -3 || echo "Maven wrapper failed"
  fi
}

MODE="${1:-run}"

case "$MODE" in
  doctor)
    doctor
    exit 0
    ;;
  run|jar|build|test-api|test-ui)
    setup_java || exit 1
    ;;
  *)
    echo "Usage: ./run-local.sh [run|jar|build|test-api|test-ui|doctor]"
    echo "  run      - spring-boot:run (default)"
    echo "  jar      - build JAR and run it"
    echo "  build    - compile package only"
    echo "  test-api - API simulation integration test"
    echo "  test-ui  - Selenium UI test (needs Chrome)"
    echo "  doctor   - print environment diagnostics"
    exit 1
    ;;
esac

case "$MODE" in
  run)
    echo ""
    echo "Starting workshop app at http://localhost:${SERVER_PORT}"
    echo "  Login:    http://localhost:${SERVER_PORT}/"
    echo "  Simulate: http://localhost:${SERVER_PORT}/simulate"
    echo ""
    run_maven spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=${SERVER_PORT}"
    ;;
  jar)
    echo "Building JAR..."
    run_maven -q -DskipTests package
    echo ""
    echo "Starting JAR at http://localhost:${SERVER_PORT}"
    java -Dserver.port="${SERVER_PORT}" -jar target/workshop-0.0.1-SNAPSHOT.jar
    ;;
  build)
    run_maven -DskipTests package
    echo "Built: application/target/workshop-0.0.1-SNAPSHOT.jar"
    ;;
  test-api)
    run_maven test -Dtest=TrafficSimulationApiIT
    ;;
  test-ui)
    run_maven test -Dtest=TrafficSimulationSeleniumIT
    ;;
esac
