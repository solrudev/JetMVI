import java.util.*

val publishGroupId by extra("io.github.solrudev")
val publishVersion by extra("0.1.1")
group = publishGroupId
version = publishVersion

plugins {
	id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

buildscript {
	repositories {
		google()
		mavenCentral()
	}
	dependencies {
		classpath("com.android.tools.build:gradle:7.3.1")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21")
	}
}

allprojects {
	repositories {
		google()
		mavenCentral()
	}
}

tasks.register<Delete>("clean").configure {
	delete(rootProject.buildDir)
}

val ossrhUsername by extra("")
val ossrhPassword by extra("")
val sonatypeStagingProfileId by extra("")
extra["signing.keyId"] = ""
extra["signing.password"] = ""
extra["signing.key"] = ""

val secretPropertiesFile = project.rootProject.file("local.properties")
if (secretPropertiesFile.exists()) {
	with(Properties()) {
		secretPropertiesFile.inputStream().use(::load)
		forEach { name, value -> extra[name as String] = value }
	}
}

extra["ossrhUsername"] = System.getenv("OSSRH_USERNAME") ?: extra["ossrhUsername"]
extra["ossrhPassword"] = System.getenv("OSSRH_PASSWORD") ?: extra["ossrhPassword"]
extra["sonatypeStagingProfileId"] = System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: extra["sonatypeStagingProfileId"]
extra["signing.keyId"] = System.getenv("SIGNING_KEY_ID") ?: extra["signing.keyId"]
extra["signing.password"] = System.getenv("SIGNING_PASSWORD") ?: extra["signing.password"]
extra["signing.key"] = System.getenv("SIGNING_KEY") ?: extra["signing.key"]

nexusPublishing {
	repositories {
		sonatype {
			stagingProfileId.set(sonatypeStagingProfileId)
			username.set(ossrhUsername)
			password.set(ossrhPassword)
			nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
			snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
		}
	}
}