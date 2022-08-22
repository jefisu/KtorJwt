package com.jefisu

import com.jefisu.di.mainModule
import com.jefisu.plugins.configureMonitoring
import com.jefisu.plugins.configureRouting
import com.jefisu.plugins.configureSecurity
import com.jefisu.plugins.configureSerialization
import com.jefisu.security.token.TokenConfig
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(Koin) {
        modules(mainModule)
    }

    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 1000L * 60L * 60L * 24L * 365L,
        secret = System.getenv("JWT_SECRET")
    )

    configureSerialization()
    configureMonitoring()
    configureSecurity(tokenConfig)
    configureRouting(tokenConfig)
}