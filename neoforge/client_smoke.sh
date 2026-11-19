#!/usr/bin/env bash
# Dev client smoke test on a private virtual display; grabs a frame every 2 s into shots/<mc>/.
v=$1; disp=${2:-:56}
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
if [ "${FRESH:-1}" = 1 ]; then rm -rf versions/$v/run-client/saves/smoke; mkdir -p versions/$v/run-client/saves; if [ -d versions/$v/run-server/world ]; then cp -r versions/$v/run-server/world versions/$v/run-client/saves/smoke; fi; fi
# NeoForge early display uses SDL, which needs EGL + llvmpipe on Xvfb (plain GLX has no matching visual).
rm -f /tmp/.X${disp#:}-lock
Xvfb $disp -ac -screen 0 1280x720x24 +extension GLX +render -noreset >/dev/null 2>&1 & XP=$!
sleep 1
python3 prepare.py $v >/dev/null
(
  cd versions/$v && \
  DISPLAY=$disp \
  LIBGL_ALWAYS_SOFTWARE=1 \
  GALLIUM_DRIVER=llvmpipe \
  SDL_VIDEO_FORCE_EGL=1 \
  SDL_AUDIODRIVER=dummy \
  MESA_GL_VERSION_OVERRIDE=4.6 \
  MESA_GLSL_VERSION_OVERRIDE=460 \
  ./gradlew --no-daemon runClient > client.log 2>&1
) & GP=$!
i=0
while kill -0 $GP 2>/dev/null; do
  sleep 2; i=$((i+1))
  python3 -c "from PIL import ImageGrab; ImageGrab.grab(xdisplay='$disp').save('shots/$v/f%03d.png' % $i)" 2>/dev/null
  [ $i -gt 180 ] && { echo timeout; kill $GP 2>/dev/null; break; }
done
wait $GP 2>/dev/null
kill $XP 2>/dev/null
rg -e "CLIENTTEST|ERROR DISPLAY|OpenGL|GL_RENDERER|Backend library|Jarry's Holy Bible loaded" versions/$v/client.log | head -40
echo "shots: $(ls shots/$v/*.png 2>/dev/null | wc -l)"
