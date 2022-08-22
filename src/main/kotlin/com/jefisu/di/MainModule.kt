package com.jefisu.di

import com.jefisu.data.user.MongoUserDataSource
import com.jefisu.security.hash.SHA512HashService
import com.jefisu.security.token.JwtTokenService
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mainModule = module {
    single {
        KMongo.createClient()
            .coroutine
            .getDatabase("jwt-auth_db")
    }
    single {
        SHA512HashService()
    }
    single {
        JwtTokenService()
    }
    single {
        MongoUserDataSource(get())
    }
}