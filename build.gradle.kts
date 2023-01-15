import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        java.srcDirs("src")
        java.destinationDirectory.set(buildDir.resolve("classes"))
    }

    test {
        java.srcDirs("test")
        java.destinationDirectory.set(buildDir.resolve("tests"))
    }
}

configurations {
    create("client")
    all {
        resolutionStrategy.cacheDynamicVersionsFor(60, TimeUnit.SECONDS)
    }
}

fun get(url: String): String {
    return URL(url).readText().trim()
}

fun getLatestBattlecodeVersion(): String {
    return Regex("\\\"release_version_public\\\":\\\"(.*?)\\\"")
        .find(get("https://api.battlecode.org/api/episode/e/bc23/?format=json"))!!
        .groups[1]!!
        .value
}

fun getLatestExamplefuncsplayer(): String {
    return get("https://raw.githubusercontent.com/battlecode/battlecode23/master/example-bots/src/main/examplefuncsplayer/RobotPlayer.java")
        .replace("System.out.", "// System.out.")
        .replace("e.printStackTrace()", "// e.printStackTrace()") + "\n"
}

val replayPath = project.property("replayPath").toString()
    .replace("%TEAM_A%", project.property("teamA").toString())
    .replace("%TEAM_B%", project.property("teamB").toString())

val clientType = with(System.getProperty("os.name").toLowerCase()) {
    when {
        startsWith("windows") -> "win"
        startsWith("mac") -> "mac"
        else -> "linux"
    }
}

val clientName = "battlecode23-client-$clientType"

val currentBattlecodeVersion = file("version.txt").readText().trim()
val examplefuncsplayerFile = file("src/examplefuncsplayer/RobotPlayer.java")

val classLocation = sourceSets["main"].output.classesDirs.asPath

repositories {
    mavenCentral()
    maven("https://releases.battlecode.org/maven")
}

dependencies {
    implementation("org.battlecode:battlecode23:$currentBattlecodeVersion")
    implementation("org.battlecode:battlecode23:$currentBattlecodeVersion:javadoc")
    add("client", "org.battlecode:$clientName:$currentBattlecodeVersion")
}

task("checkForUpdates") {
    group = "battlecode"
    description = "Checks for Battlecode updates."

    doLast {
        val latestBattlecodeVersion = getLatestBattlecodeVersion()
        if (currentBattlecodeVersion != latestBattlecodeVersion) {
            print("\n\n\nBATTLECODE UPDATE AVAILABLE ($currentBattlecodeVersion -> $latestBattlecodeVersion)\n\n\n")
        }

        if (examplefuncsplayerFile.readText() != getLatestExamplefuncsplayer()) {
            print("\n\n\nEXAMPLEFUNCSPLAYER UPDATE AVAILABLE\n\n\n")
        }
    }
}

task("update") {
    group = "battlecode"
    description = "Updates to the latest Battlecode version."

    doLast {
        val latestBattlecodeVersion = getLatestBattlecodeVersion()
        if (currentBattlecodeVersion == latestBattlecodeVersion) {
            println("Already using the latest Battlecode version ($currentBattlecodeVersion)")
        } else {
            file("version.txt").writeText(latestBattlecodeVersion + "\n")
            println("Updated Battlecode from $currentBattlecodeVersion to $latestBattlecodeVersion, please reload the Gradle project")
        }

        val latestExamplefuncsplayer = getLatestExamplefuncsplayer()
        if (examplefuncsplayerFile.readText() == latestExamplefuncsplayer) {
            println("Already using the latest examplefuncsplayer")
        } else {
            examplefuncsplayerFile.writeText(latestExamplefuncsplayer)
            println("Updated examplefuncsplayer to the latest version")
        }
    }
}

task<Copy>("unpackClient") {
    group = "battlecode"
    description = "Unpacks the client."

    dependsOn(configurations["client"], "checkForUpdates")

    from(configurations["client"].map { if (it.isDirectory) it else zipTree(it) })
    into("client/")
}

task<JavaExec>("run") {
    group = "battlecode"
    description = "Runs a match without starting the client."

    dependsOn("build")

    mainClass.set("battlecode.server.Main")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("-c=-")
    jvmArgs = listOf(
        "-Dbc.server.wait-for-client=${project.findProperty("waitForClient") ?: "false"}",
        "-Dbc.server.websocket=${project.findProperty("waitForClient") ?: "false"}",
        "-Dbc.server.mode=headless",
        "-Dbc.server.map-path=maps",
        "-Dbc.engine.robot-player-to-system-out=${project.property("outputVerbose")}",
        "-Dbc.server.debug=false",
        "-Dbc.engine.debug-methods=${project.property("debug")}",
        "-Dbc.engine.enable-profiler=${project.property("enableProfiler")}",
        "-Dbc.engine.show-indicators=${project.property("showIndicators")}",
        "-Dbc.game.team-a=${project.property("teamA")}",
        "-Dbc.game.team-b=${project.property("teamB")}",
        "-Dbc.game.team-a.url=$classLocation",
        "-Dbc.game.team-b.url=$classLocation",
        "-Dbc.game.team-a.package=${project.property("teamA")}",
        "-Dbc.game.team-b.package=${project.property("teamB")}",
        "-Dbc.game.maps=${project.property("maps")}",
        "-Dbc.server.save-file=${replayPath.replace("%MAP%", project.property("maps").toString())}"
    )
}

task("listMaps") {
    group = "battlecode"
    description = "Lists all available maps."

    doLast {
        val officialMapFiles =
            zipTree(sourceSets["main"].compileClasspath.first { it.toString().contains("battlecode23-") })
        val customMapFiles = fileTree(file("maps"))

        val maps = (officialMapFiles + customMapFiles)
            .filter { it.name.endsWith(".map23") }
            .map { it.name.substringBeforeLast(".map23") }
            .distinct()
            .sortedBy { it.toLowerCase() }

        println("Maps (${maps.size}):")
        for (map in maps) {
            println(map)
        }
    }
}

task<Zip>("createSubmission") {
    group = "battlecode"
    description = "Creates a submission zip."

    dependsOn("build")

    from(file("src").absolutePath)
    include("camel_case/**/*")
    archiveBaseName.set(DateTimeFormatter.ofPattern("yyyy-MM-dd_kk-mm-ss").format(LocalDateTime.now()))
    destinationDirectory.set(project.buildDir.resolve("submissions"))
}

tasks.named("build") {
    group = "battlecode"

    dependsOn("unpackClient")
}
