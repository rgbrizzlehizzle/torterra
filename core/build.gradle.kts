plugins {
  id("org.leafygreens.torterra.kotlin-common-conventions")
  kotlin("kapt")
  `java-library`
}

repositories {
  jcenter()
}

dependencies {
  implementation("com.squareup.moshi:moshi:1.11.0")
  implementation("com.squareup.moshi:moshi-kotlin:1.11.0")
  kapt("com.squareup.moshi:moshi-kotlin-codegen:1.11.0")
}
