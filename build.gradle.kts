import java.util.Properties

val publishGroupId by extra("io.github.solrudev")
val publishVersion by extra("0.1.5")
group = publishGroupId
version = publishVersion

plugins {
	id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
	id("org.jetbrains.dokka") version "1.8.20"
}

subprojects {
	apply(plugin = "org.jetbrains.dokka")
}

buildscript {
	dependencies {
		classpath("com.android.tools.build:gradle:8.0.2")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
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

val secretPropertiesFile = file("local.properties")
if (secretPropertiesFile.exists()) {
	Properties().run {
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
	this.repositories {
		sonatype {
			stagingProfileId.set(sonatypeStagingProfileId)
			username.set(ossrhUsername)
			password.set(ossrhPassword)
			nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
			snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
		}
	}
}