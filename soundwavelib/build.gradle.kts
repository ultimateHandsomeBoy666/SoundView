plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
    signing
}

group = "io.github.ultimatehandsomeboy666"
version = "1.0.0"

android {
    namespace = "com.soundwave.lib"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                pom {
                    name.set("SoundWave")
                    description.set("A SoundView that dances with volume changing")
                    url.set("https://github.com/ultimateHandsomeBoy666/SoundView")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("ultimateHandsomeBoy666")
                            name.set("bullfrog")
                            email.set("jiujiuli@qq.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/ultimateHandsomeBoy666/SoundWave.git")
                        developerConnection.set("scm:git:ssh://github.com/ultimateHandsomeBoy666/SoundWave.git")
                        url.set("https://github.com/ultimateHandsomeBoy666/SoundWave/tree/main")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "Sonatype"
                val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                
                credentials {
                    username = project.properties["ossrhUsername"] as String?
                    password = project.properties["ossrhPassword"] as String?
                }
            }
        }
    }
}

// 6. 配置 GPG 签名
signing {
    val signingKeyId = project.properties["signing.keyId"] as String?
    val signingPassword = project.properties["signing.password"] as String?
    val signingKey = project.properties["signing.secretKeyRingFile"] as String?
    
    if (signingKeyId != null) {
        sign(publishing.publications)
    }
}

dependencies {
    implementation(libs.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
