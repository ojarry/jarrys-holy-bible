#!/usr/bin/env bash
# Builds, unit-tests and dev-server smoke-tests every target version, one at a time.
set -u
# Use JAVA_HOME if set; otherwise whatever `java` is on PATH.
if [ -n "${JAVA_HOME:-}" ]; then
  export PATH="$JAVA_HOME/bin:$PATH"
elif ! command -v java >/dev/null 2>&1; then
  echo "Java not found on PATH. Set JAVA_HOME to a JDK (21+ for Minecraft 1.21.x, 25+ for 26.x)." >&2
  exit 1
fi
cd "$(dirname "$0")"
python3 gen_builds.py || exit 1
python3 prepare.py "$@" || exit 1
VERS=("$@"); [ ${#VERS[@]} -eq 0 ] && VERS=(1.21.11 1.21.10)
for v in "${VERS[@]}"; do
  echo "===== $v"
  ( cd versions/$v && rm -rf run-server/world && ./gradlew --no-daemon --max-workers=2 clean build runServer > build.log 2>&1; echo "gradle rc=$?" )
  if grep -q "SELFTEST PASS" versions/$v/build.log && ! grep -qE "SELFTEST FAIL" versions/$v/build.log; then echo "SMOKE $v: PASS"; else echo "SMOKE $v: FAIL"; fi
  grep -E "error:|warning:|FAILED|BUILD (SUCCESSFUL|FAILED)|SELFTEST|Exception" versions/$v/build.log | head -60
  python3 - "$v" <<'PY'
import sys, glob, xml.etree.ElementTree as ET
t=f=e=0
for x in glob.glob(f"versions/{sys.argv[1]}/build/test-results/test/*.xml"):
    r=ET.parse(x).getroot(); t+=int(r.get("tests")); f+=int(r.get("failures")); e+=int(r.get("errors"))
print(f"unit tests: {t} run, {f} failures, {e} errors")
PY
done
