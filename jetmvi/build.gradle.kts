import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val publishGroupId: String by rootProject.extra
val publishVersion: String by rootProject.extra
val publishArtifactId = "jetmvi"

plugins {
	id("com.android.library")
	kotlin("android")
	`maven-publish`
	signing
}

android {
	compileSdk = 33
	buildToolsVersion = "33.0.0"
	namespace = "$publishGroupId.$publishArtifactId"

	publishing {
		singleVariant("release") {
			withSourcesJar()
		}
	}

	defaultConfig {
		minSdk = 16
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
}

tasks.withType(KotlinJvmCompile::class) {
	compilerOptions {
		jvmTarget.set(JVM_1_8)
		freeCompilerArgs.addAll("-Xexplicit-api=strict", "-Xjvm-default=all")
	}
}

dependencies {
	api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
	api("androidx.activity:activity-ktx:1.6.1")
	api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
	api("androidx.fragment:fragment-ktx:1.5.4")
}

afterEvaluate {
	publishing {
		publications {
			create<MavenPublication>("release") {
				groupId = publishGroupId
				artifactId = publishArtifactId
				version = publishVersion
				from(components.getByName("release"))

				pom {
					name.set(publishArtifactId)
					description.set("Experimental coroutines-based MVI micro framework for Android")
					url.set("https://github.com/solrudev/JetMVI")

					licenses {
						license {
							name.set("The Apache Software License, Version 2.0")
							url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
						}
					}

					developers {
						developer {
							id.set("solrudev")
							name.set("Ilya Fomichev")
						}
					}

					scm {
						connection.set("scm:git:github.com/solrudev/JetMVI.git")
						developerConnection.set("scm:git:ssh://github.com/solrudev/JetMVI.git")
						url.set("https://github.com/solrudev/JetMVI/tree/master")
					}
				}
			}
		}
	}
}

signing {
	val keyId = rootProject.extra["signing.keyId"] as String
	val key = rootProject.extra["signing.key"] as String
	val password = rootProject.extra["signing.password"] as String
	useInMemoryPgpKeys(keyId, key, password)
	sign(publishing.publications)
}