plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    id("org.sonarqube") version "4.4.1.3373"
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
                    "**/*_HiltModules*,**/*_Factory*,**/*Hilt*,**/*ComposableSingletons*"
        )
    }
}