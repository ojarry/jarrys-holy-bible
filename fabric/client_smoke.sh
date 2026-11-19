#!/usr/bin/env bash
# Dev client smoke test on a private virtual display; grabs a frame every 2 s into shots/<mc>/.
v=$1; disp=${2:-:55}
# Use JAVA_HOME if set; otherwise whatever `java` is on PATH.
if [ -n "${JAVA_HOME:-}" ]; then
  export PATH="$JAVA_HOME/bin:$PATH"
elif ! command -v java >/dev/null 2>&1; then
  echo "Java not found on PATH. Set JAVA_HOME to a JDK (21+ for Minecraft 1.21.x, 25+ for 26.x)." >&2
  exit 1
fi
cd "$(dirname "$0")"
mkdir -p versions/$v/run-client shots/$v && rm -f shots/$v/*.png
printf 'onboardAccessibility:false\nguiScale:2\nmaxFps:30\nnarrator:0\ntutorialStep:none\nsoundCategory_master:0.0\n' > versions/$v/run-client/options.txt
if [ "${FRESH:-1}" = 1 ]; then rm -rf versions/$v/run-client/saves/smoke; mkdir -p versions/$v/run-client/saves; cp -r versions/$v/run-server/world versions/$v/run-client/saves/smoke; fi
Xvfb $disp -screen 0 1280x720x24 +extension GLX +render -noreset >/dev/null 2>&1 & XP=$!
sleep 1
python3 prepare.py $v >/dev/null
( cd versions/$v && DISPLAY=$disp LIBGL_ALWAYS_SOFTWARE=1 ./gradlew --no-daemon runClient > client.log 2>&1 ) & GP=$!
i=0
while kill -0 $GP 2>/dev/null; do
  sleep 2; i=$((i+1))
  python3 -c "from PIL import ImageGrab; ImageGrab.grab(xdisplay='$disp').save('shots/$v/f%03d.png' % $i)" 2>/dev/null
  [ $i -gt 300 ] && { echo timeout; pkill -f "run-client" ; break; }
done
kill $XP 2>/dev/null
grep -E "CLIENTTEST|Exception|OpenGL|GL_RENDERER|Backend library" versions/$v/client.log | head -30
