plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // قم بحذف السطر الخاص بـ compose plugin مؤقتاً لنرى هل المشكلة منه
}

android {
    ...
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // هذا الإصدار هو الأكثر استقراراً وتوافقاً
    }
}


    buildFeatures {
        compose = true
    }
}

dependencies {
    // استخدم الـ BOM الخاص بـ Compose لتوحيد جميع الإصدارات
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // مكتبات UI الأساسية (بدون أرقام إصدارات لضمان التوافق)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // مكتبات الأندرويد الأساسية
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
}
