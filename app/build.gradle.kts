import java.util.Properties

plugins {
    id("com.android.application")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}
val properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())
android {
    namespace = "com.example.holymoly"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.holymoly"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "KARLO_API_KEY", properties.getProperty("karlo"))
        buildConfigField("String", "GEMINI_API_KEY", properties.getProperty("gemini"))
        buildConfigField("String", "POLLY_API_KEY", properties.getProperty("polly"))

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures{
        buildConfig = true
        viewBinding = true
    }
}


dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    //동그란 사진
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // Firestore
    implementation("com.google.firebase:firebase-firestore:24.4.0")
    implementation("androidx.activity:activity:1.8.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    // firebase auth
    implementation("com.google.firebase:firebase-auth:23.0.0")
    // firebase storage - 파이어베이스 이미지 저장 공간
    implementation("com.google.firebase:firebase-storage:21.0.0")

    // Gemini API 관련
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    // Required for one-shot operations (to use `ListenableFuture` from Guava Android)
    implementation("com.google.guava:guava:32.0.1-android")
    // Required for streaming operations (to use `Publisher` from Reactive Streams)
    implementation("org.reactivestreams:reactive-streams:1.0.4")

    implementation ("org.json:json:20210307")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")

    //color picker
    implementation ("com.github.yukuku:ambilwarna:2.0.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    //나 혼자 만들기 레이아웃
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    // Amazon Polly
    implementation ("com.amazonaws:aws-android-sdk-polly:2.22.2")

    implementation ("androidx.palette:palette:1.0.0")
}
