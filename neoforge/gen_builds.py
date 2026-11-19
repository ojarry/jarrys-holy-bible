#!/usr/bin/env python3
"""Write per-version Gradle files and copy the wrapper."""
import shutil
from pathlib import Path
root = Path(__file__).resolve().parent
V = {
 "26.3":    dict(neo="26.3.0.113", java=25, rng="[26.3]"),
 "26.2":    dict(neo="26.2.0.95",  java=25, rng="[26.2]"),
 "26.1.2":  dict(neo="26.1.2.127", java=25, rng="[26.1.2]"),
 "1.21.11": dict(neo="21.11.48",   java=21, rng="[1.21.11]"),
 "1.21.10": dict(neo="21.10.64",   java=21, rng="[1.21.10]"),
}
TPL = (root / "build.gradle.tpl").read_text()
SET = (root / "settings.gradle.tpl").read_text()
# Prefer an existing Fabric wrapper (already cached); fall back to copying our own later.
wrapper_src = Path("/workspace/minecraft/bible-java/versions/26.3")
for mc, c in V.items():
    d = root / f"versions/{mc}"
    d.mkdir(parents=True, exist_ok=True)
    (d / "build.gradle").write_text(TPL.replace("@JAVA@", str(c["java"])))
    (d / "settings.gradle").write_text(SET)
    (d / "gradle.properties").write_text(f"""org.gradle.jvmargs=-Xmx3G
org.gradle.parallel=false
org.gradle.configuration-cache=false
org.gradle.daemon=false
minecraft_version={mc}
minecraft_version_range={c['rng']}
neo_version={c['neo']}
mod_version=1.0.0
maven_group=net.jarry.holybible
""")
    # wrapper
    wdst = d / "gradle/wrapper"
    wdst.mkdir(parents=True, exist_ok=True)
    shutil.copy2(wrapper_src / "gradlew", d / "gradlew")
    shutil.copy2(wrapper_src / "gradlew.bat", d / "gradlew.bat")
    shutil.copy2(wrapper_src / "gradle/wrapper/gradle-wrapper.jar", wdst / "gradle-wrapper.jar")
    # Keep the already-cached 9.8.2 distribution
    shutil.copy2(wrapper_src / "gradle/wrapper/gradle-wrapper.properties", wdst / "gradle-wrapper.properties")
    (d / "gradlew").chmod(0o755)
    print("generated", mc, "neoforge", c["neo"], "java", c["java"])
