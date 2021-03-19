group = "org.leafygreens"
version = run {
  val baseVersion =
    project.findProperty("project.version") ?: error("project.version must be set in gradle.properties")
  when ((project.findProperty("release") as? String)?.toBoolean()) {
    true -> baseVersion
    else -> "$baseVersion-SNAPSHOT"
  }
}

plugins {
  kotlin("jvm")
  id("io.gitlab.arturbosch.detekt")
  idea
  `maven-publish`
}

repositories {
  jcenter()
}

dependencies {
  // Align versions of all Kotlin components
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  // TESTING
  val junitVersion = "5.6.2"
  val mockkVersion = "1.10.0"
  val truthVersion = "1.0.1"

  // Use JUnit Jupiter API
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

  // Mockk
  testImplementation("io.mockk:mockk:$mockkVersion")

  // Truth
  testImplementation("com.google.truth:truth:$truthVersion")
}

tasks {
  test {
    useJUnitPlatform()
  }
  withType<AbstractTestTask> {
    testLogging {
      setExceptionFormat("full")
    }
    afterSuite(
      KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
        if (desc.parent == null) { // will match the outermost suite
          println(
            "Results: ${result.resultType} (${result.testCount} tests, " +
                "${result.successfulTestCount} successes, " +
                "${result.failedTestCount} failures, ${result.skippedTestCount} skipped)"
          )
        }
      })
    )
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
      jvmTarget = "14"
    }
  }
}

detekt {
  toolVersion = "1.16.0-RC2"
  config = files("${rootProject.projectDir}/detekt.yml")
  buildUponDefaultConfig = true
}

dependencies {
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.16.0-RC2")
}

publishing {
  repositories {
    maven {
      name = "GithubPackages"
      url = uri("https://maven.pkg.github.com/lg-backbone/skelegro")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
//  publications {
//    create<MavenPublication>("library") {
//      from(components["kotlin"])
//      artifact(tasks["shadowJar"])
//    }
//  }
}
