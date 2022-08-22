package com.jefisu.security.hash

interface HashService {
    fun generateSaltedHash(value: String, saltLength: Int = 32): SaltedHash

    fun verify(value: String, saltedHash: SaltedHash): Boolean
}