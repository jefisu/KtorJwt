package com.jefisu.routes

import com.jefisu.data.requests.SignInRequest
import com.jefisu.data.requests.SignUpRequest
import com.jefisu.data.responses.AuthResponse
import com.jefisu.data.user.User
import com.jefisu.data.user.UserDataSource
import com.jefisu.security.hash.HashService
import com.jefisu.security.hash.SaltedHash
import com.jefisu.security.token.TokenClaim
import com.jefisu.security.token.TokenConfig
import com.jefisu.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    hashService: HashService,
    userDataSource: UserDataSource
) {
    post("signup") {
        val request = call.receiveOrNull<SignUpRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldsBlank = request.email.isBlank()
                || request.username.isBlank()
                || request.password.isBlank()
        val isPasswordTooShort = request.password.length < 8
        when {
            areFieldsBlank -> {
                call.respond(HttpStatusCode.Conflict, "Fill in all fields.")
                return@post
            }

            isPasswordTooShort -> {
                call.respond(HttpStatusCode.Conflict, "Is password too short.")
                return@post
            }
        }

        val isUsernameEmailNotAvailable = userDataSource.getUser(request.username, request.email) != null
        if (isUsernameEmailNotAvailable) {
            call.respond(HttpStatusCode.Conflict, "Existing user with this username and email.")
            return@post
        }

        val saltedHash = hashService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            email = request.email,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val wasAcknowledged = userDataSource.insertUser(user)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.Conflict, "An unexpected error has occurred.")
            return@post
        }

        call.respond(HttpStatusCode.OK, "Successful registered user.")
    }
}

fun Route.signIn(
    hashService: HashService,
    tokenService: TokenService,
    userDataSource: UserDataSource,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = call.receiveOrNull<SignInRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUser(request.login)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or email.")
            return@post
        }

        val isValidPassword = hashService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect password.")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )
        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(token)
        )
    }
}

fun Route.authenticateUser() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK, "You're authenticated!")
        }
    }
}

fun Route.getSecretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}