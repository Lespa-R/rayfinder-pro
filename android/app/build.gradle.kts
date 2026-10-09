import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Backend Lovable Cloud : mêmes URL et clé publique (publishable) que le site, lues dans le .env
// géré par Lovable à la racine du dépôt — rien n'est recopié dans le code Android.
val lovableEnv: Map<String, String> = rootProject.file("../.env").takeIf { it.exists() }
    ?.readLines()
    ?.mapNotNull { line ->
        val i = line.indexOf('=')
        if (line.isBlank() || line.trimStart().startsWith("#") || i < 0) null
        else line.substring(0, i).trim() to line.substring(i + 1).trim().removeSurrounding("\"")
    }
    ?.toMap()
    .orEmpty()

// Signature release : renseigner keystore.properties (non versionné), voir README.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "me.datagrow.rayfinderpro"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.datagrow.rayfinderpro"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"

        buildConfigField("String", "SUPABASE_URL", "\"${lovableEnv["VITE_SUPABASE_URL"].orEmpty()}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${lovableEnv["VITE_SUPABASE_PUBLISHABLE_KEY"].orEmpty()}\"")
        // Site publié par Lovable (mentions légales, confidentialité, suppression de compte, Safe Zone)
        buildConfigField("String", "SITE_URL", "\"https://rayfinder.datagrow.me\"")
    }

    signingConfigs {
        if (keystoreProps.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.06.00")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}
