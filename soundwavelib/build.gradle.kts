import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.vanniktech.maven.publish)
}

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

mavenPublishing {
    coordinates("io.github.ultimatehandsomeboy666", "soundwavelib", "1.0.0")

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

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}

dependencies {
    implementation(libs.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
