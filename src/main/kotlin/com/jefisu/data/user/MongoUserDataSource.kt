package com.jefisu.data.user

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoUserDataSource(
    db: CoroutineDatabase
) : UserDataSource {

    private val users = db.getCollection<User>()

    override suspend fun getUser(vararg logins: String): User? {
        if (logins.getOrNull(1) != null) {
            users
                .findOne(User::email eq logins[1])
                ?.let { return it }
        }

        users
            .findOne(User::username eq logins.first())
            ?.let { return it }
        return users.findOne(User::email eq logins.first())
    }

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }
}