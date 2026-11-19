plugins {
	id 'java'
	id 'net.minecraftforge.gradle' version '[7.0.3,8)'
}

version = "${mod_version}+${minecraft_version}"
group = maven_group

base { archivesName = 'jarrys-holy-bible-forge' }

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

def common = file('../../common')

sourceSets {
	main {
		java.srcDirs = [
			file("${common}/src/main/java"),
			file("${common}/src/client/java"),
			file('src/main/java'),
			file('src/client/java')
		]
		resources.srcDirs = [
			file("${common}/src/main/resources"),
			file('src/main/resources')
		]
	}
	test {
		java.srcDirs = [file("${common}/src/test/java")]
	}
}

minecraft {
	mappings channel: 'official', version: minecraft_version

	runs {
		configureEach {
			systemProperty 'eventbus.api.strictRuntimeChecks', 'true'
		}

		register('client') {
			workingDir = layout.projectDirectory.dir('run-client')
			systemProperty 'jarrys_holy_bible.clientselftest', 'true'
			jvmArgs '-Xmx2G'
			args '--quickPlaySingleplayer', 'smoke'
		}

		register('server') {
			workingDir = layout.projectDirectory.dir('run-server')
			systemProperty 'jarrys_holy_bible.selftest', 'true'
			args '--nogui'
		}
	}
}

repositories {
	minecraft.mavenizer(it)
	maven fg.forgeMaven
	maven fg.minecraftLibsMaven
	mavenCentral()
}

dependencies {
	implementation minecraft.dependency("net.minecraftforge:forge:${minecraft_version}-${forge_version}")
	annotationProcessor "net.minecraftforge:eventbus-validator:${eventbus_validator}"
	testImplementation platform('org.junit:junit-bom:5.11.4')
	testImplementation 'org.junit.jupiter:junit-jupiter'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

test { useJUnitPlatform() }

def generateModMetadata = tasks.register('generateModMetadata', ProcessResources) {
	def replaceProperties = [
		minecraft_version      : minecraft_version,
		minecraft_version_range: minecraft_version_range,
		forge_version          : forge_version,
		forge_version_range    : forge_version_range,
		loader_version_range   : loader_version_range,
		mod_id                 : 'jarrys_holy_bible',
		mod_name               : "Jarry's Holy Bible",
		mod_license            : 'MIT',
		mod_version            : project.version
	]
	inputs.properties replaceProperties
	expand replaceProperties
	from "${common}/src/templates"
	into layout.buildDirectory.dir('generated/sources/modMetadata')
}
sourceSets.main.resources.srcDir generateModMetadata

tasks.withType(JavaCompile).configureEach {
	it.options.release = 21
	it.options.encoding = 'UTF-8'
	it.options.compilerArgs << '-Xlint:deprecation'
}

jar {
	from(file("${common}/LICENSE")) {
		rename { 'LICENSE.txt' }
	}
}

tasks.named('build') {
	doLast {
		copy {
			from tasks.named('jar', Jar)
			into file('../../dist')
		}
	}
}
