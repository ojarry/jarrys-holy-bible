import os
V = {
 "26.3":    dict(api="0.166.2+26.3",   plugin="net.fabricmc.fabric-loom",       java=25, dep="~26.3",  remap=False),
 "26.2":    dict(api="0.161.0+26.2",   plugin="net.fabricmc.fabric-loom",       java=25, dep="~26.2",  remap=False),
 "26.1.2":  dict(api="0.155.3+26.1.2", plugin="net.fabricmc.fabric-loom",       java=25, dep="~26.1.2", remap=False),
 "1.21.11": dict(api="0.141.6+1.21.11",plugin="net.fabricmc.fabric-loom-remap", java=21, dep="1.21.11", remap=True),
 "1.21.10": dict(api="0.138.4+1.21.10",plugin="net.fabricmc.fabric-loom-remap", java=21, dep="1.21.10", remap=True),
}
TPL = open("build.gradle.tpl").read()
for mc, c in V.items():
    d = f"versions/{mc}"
    impl = "modImplementation" if c["remap"] else "implementation"
    s = TPL.replace("@PLUGIN@", c["plugin"]).replace("@IMPL@", impl).replace("@JAVA@", str(c["java"])) \
           .replace("@MAPPINGS@", "mappings loom.officialMojangMappings()" if c["remap"] else "")
    open(f"{d}/build.gradle","w").write(s)
    open(f"{d}/gradle.properties","w").write(f"""org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=false
org.gradle.configuration-cache=false
minecraft_version={mc}
loader_version=0.20.1
loom_version=1.19-SNAPSHOT
mod_version=1.0.0
maven_group=net.jarry.holybible
fabric_api_version={c['api']}
mc_dep={c['dep']}
java_min={c['java']}
""")
    open(f"{d}/settings.gradle","w").write("""pluginManagement {
	repositories {
		maven { name = 'Fabric'; url = 'https://maven.fabricmc.net/' }
		mavenCentral()
		gradlePluginPortal()
	}
}
rootProject.name = 'jarrys-holy-bible'
""")
