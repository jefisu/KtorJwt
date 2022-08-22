package com.jefisu.data.user

interface UserDataSource {

    suspend fun getUser(vararg logins: String): User?

    suspend fun insertUser(user: User): Boolean
}