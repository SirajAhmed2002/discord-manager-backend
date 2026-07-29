plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
    jacoco
}

group = "ch.zhaw.it.pm4"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }

    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
}

extra["snippetsDir"] = file("build/generated-snippets")
extra["springAiVersion"] = "1.0.0-M6"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Enable when used
    // implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // JsonWebToken
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")


    implementation("net.dv8tion:JDA:5.5.1")
    implementation("com.github.lavalink-devs:lavaplayer:2.2.3")
    implementation("com.github.lavalink-devs:youtube-source:1.13.2")

    implementation("org.seleniumhq.selenium:selenium-java:4.32.0")
    implementation("dev.arbjerg:lavaplayer:2.0.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

openApi {
    apiDocsUrl.set("http://localhost:8081/v3/api-docs")
    outputDir.set(layout.buildDirectory.dir("docs"))
    outputFileName.set("openapi.yaml")
    waitTimeInSeconds.set(10)
}

tasks.bootJar{
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

jacoco{
    toolVersion = "0.8.10"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn("test")
    reports {
        html.required.set(true) // Generate HTML reports
        xml.required.set(true)  // Optionally generate XML reports
        csv.required.set(false) // Disable CSV reports (optional)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.test {
    outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.asciidoctor {
    inputs.dir(project.extra["snippetsDir"]!!)
    dependsOn(tasks.test)
}
