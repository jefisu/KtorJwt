package com.jefisu.plugins

import com.jefisu.data.user.MongoUserDataSource
import com.jefisu.routes.authenticateUser
import com.jefisu.routes.getSecretInfo
import com.jefisu.routes.signIn
import com.jefisu.routes.signUp
import com.jefisu.security.hash.SHA512HashService
import com.jefisu.security.token.JwtTokenService
import com.jefisu.security.token.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting(tokenConfig: TokenConfig) {

    val hashService by inject<SHA512HashService>()
    val tokeService by inject<JwtTokenService>()
    val userDataSource by inject<MongoUserDataSource>()

    routing {
        signUp(hashService, userDataSource)
        signIn(hashService, tokeService, userDataSource, tokenConfig)
        authenticateUser()
        getSecretInfo()
    }
}
