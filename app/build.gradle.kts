import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) load(file.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    jacoco
    id("org.sonarqube") version "6.0.1.5171"
}

sonar {
    properties {
        property("sonar.projectKey",   "VinceDS99_OPC-Project-16-Eventorias-Maintenance")
        property("sonar.organization", "vinceds99")
        property("sonar.host.url",     "https://sonarcloud.io")
        property("sonar.sources",      "src/main/java")
        property("sonar.tests",        "src/test/java,src/androidTest/java")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${project.projectDir}/app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
        )
        property(
            "sonar.exclusions",
            "**/R.class,**/R\$*.class,**/BuildConfig.*,**/Manifest*.*," +
                    "**/*_HiltModules*,**/*_Factory*,**/*Hilt*," +
                    "**/*ComposableSingletons*"
        )
    }
}

android {
    namespace = "com.openclassrooms.eventorias"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.openclassrooms.eventorias"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.openclassrooms.eventorias.HiltTestRunner"

        buildConfigField(
            "String",
            "MAPS_API_KEY",
            "\"${localProperties.getProperty("MAPS_API_KEY", "")}\""
        )
    }

    signingConfigs {
        // Ne crée la config release que si keystore.properties existe (local uniquement)
        val storeFilePath = keystoreProperties.getProperty("storeFile", "")
        if (storeFilePath.isNotEmpty()) {
            create("release") {
                storeFile     = rootProject.file(storeFilePath)
                storePassword = keystoreProperties.getProperty("storePassword", "")
                keyAlias      = keystoreProperties.getProperty("keyAlias", "")
                keyPassword   = keystoreProperties.getProperty("keyPassword", "")
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            // Utilise la config release si elle existe, sinon null (CI sans keystore)
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.firebase.storage)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("io.mockk:mockk:1.13.10")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    androidTestImplementation("com.google.dagger:hilt-android-testing:2.55")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.55")
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    // Dépend de l'exécution des tests unitaires debug
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // Fichiers à exclure de la couverture (générés, non testables)
    val excludes = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",

        // Hilt
        "**/*_HiltModules*",
        "**/*_Factory*",
        "**/*_MembersInjector*",
        "**/hilt_aggregated_deps/**",
        "**/*Hilt*",

        // Compose généré
        "**/*ComposableSingletons*",
        "**/*_Impl*"
    )

    // Classes Kotlin compilées
    val debugTree = fileTree(
        "${layout.buildDirectory.get()}/tmp/kotlin-classes/debug"
    ) { exclude(excludes) }

    sourceDirectories.setFrom(files("${project.projectDir}/src/main/java"))
    classDirectories.setFrom(files(debugTree))

    // Fichier d'exécution produit par testDebugUnitTest
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec"
            )
        }
    )
}