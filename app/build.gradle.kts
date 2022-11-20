import java.time.LocalDate

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    // Apply the application plugin to add support for building a CLI application in Java.
    id("com.android.application")
    id("org.jetbrains.dokka")
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        named("main") {
            includeNonPublic.set(true)
            outputDirectory.set(file("../documentation/"))
        }
    }
}

var applicationName = "OpenMusic"

android {
    compileSdkVersion = "android-33"
    buildToolsVersion = "30.0.3"
    namespace = "com.musicplayer.openmusic"

    defaultConfig {
        applicationId = "com.musicplayer.openmusic"
        minSdk = 24
        targetSdk = 33
        versionCode = 11
        versionName = "v1.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        applicationVariants.all {
            outputs.all {
                val formattedDate = LocalDate.now()//LocalDate.now().format(ofPattern("yyyy-MM-dd"))
                var outputFileName =
                    "${applicationName}_${buildType.name}_${formattedDate}_v${defaultConfig.versionName}.apk"
            }
        }
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
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
}


dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:29.0-jre")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    implementation("commons-io:commons-io:20030203.000550")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.karumi:dexter:6.2.3")
    implementation("com.gauravk.audiovisualizer:audiovisualizer:0.9.2")
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.media:media:1.6.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.5.4")
    implementation("com.github.woxthebox:draglistview:1.7.2")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.github.hendrawd:StorageUtil:1.0.0")
    implementation("com.github.sephiroth74:Tri-State-Checkbox:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("org.json:json:20220924")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")
    implementation("com.github.smart-fun:XmlToJson:1.5.1")

    val roomVersion = "2.4.3"

    implementation("androidx.room:room-runtime:${roomVersion}")

    kapt("androidx.room:room-compiler:${roomVersion}")

    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:${roomVersion}")
    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:${roomVersion}")
    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:${roomVersion}")
    // optional - Test helpers
    testImplementation("androidx.room:room-testing:${roomVersion}")
    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:2.5.0-beta02")

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.7.20")


}
