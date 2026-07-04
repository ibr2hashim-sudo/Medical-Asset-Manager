plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // ملاحظة: إذا كنت تستخدم Room، قد تحتاج أيضاً لإضافة id("kotlin-kapt") هنا
}

android {
    namespace = "com.example.yourprojectname"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.yourprojectname"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    // المكتبات الأساسية
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    
    // واجهة المستخدم، Material3، الأيقونات، و Coil
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    
    // مكتبة التنقل (Navigation)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // مكتبات الكاميرا (CameraX) - لحل أخطاء PreviewView و CameraSelector
    val camerax_version = "1.3.1"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    // مكتبات قاعدة البيانات (Room) - لحل أخطاء RoomDatabase و Entity
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:${room_version}")
    implementation("androidx.room:room-ktx:${room_version}")
    // ملاحظة: تأكد من إضافة kapt في ملف الـ plugins واستخدام k2 للتوليد إذا لزم الأمر
    // k2("androidx.room:room-compiler:${room_version}") 
}
