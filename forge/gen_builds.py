#!/usr/bin/env python3
"""Write per-version Gradle files and copy the official MDK wrapper for that game version."""
import shutil
from pathlib import Path
root = Path(__file__).resolve().parent
MDK = Path("/tmp/forge-mdk")
V = {
 "1.21.11": dict(forge="61.2.5",  ev="7.0.5", loader="[61,)", frange="[61,)", mrange="[1.21.11,1.22)"),
 "1.21.10": dict(forge="60.1.18", ev="7.0.1", loader="[60,)", frange="[60,)", mrange="[1.21.10,1.22)"),
}
TPL = (root / "build.gradle.tpl").read_text()
SET = (root / "settings.gradle.tpl").read_text()
for mc, c in V.items():
    d = root / f"versions/{mc}"
    d.mkdir(parents=True, exist_ok=True)
    (d / "build.gradle").write_text(TPL)
    (d / "settings.gradle").write_text(SET)
    (d / "gradle.properties").write_text(f"""org.gradle.jvmargs=-Xmx3G
org.gradle.parallel=false
org.gradle.configuration-cache=false
org.gradle.daemon=false
org.gradle.caching=true
net.minecraftforge.gradle.merge-source-sets=true
minecraft_version={mc}
minecraft_version_range={c['mrange']}
forge_version={c['forge']}
forge_version_range={c['frange']}
loader_version_range={c['loader']}
eventbus_validator={c['ev']}
mod_version=1.0.0
maven_group=net.jarry.holybible
""")
    mdk = MDK / mc
    wdst = d / "gradle/wrapper"
    wdst.mkdir(parents=True, exist_ok=True)
    shutil.copy2(mdk / "gradlew", d / "gradlew")
    shutil.copy2(mdk / "gradlew.bat", d / "gradlew.bat")
    shutil.copy2(mdk / "gradle/wrapper/gradle-wrapper.jar", wdst / "gradle-wrapper.jar")
    shutil.copy2(mdk / "gradle/wrapper/gradle-wrapper.properties", wdst / "gradle-wrapper.properties")
    (d / "gradlew").chmod(0o755)
    print("generated", mc, "forge", c["forge"], "eventbus-validator", c["ev"])
