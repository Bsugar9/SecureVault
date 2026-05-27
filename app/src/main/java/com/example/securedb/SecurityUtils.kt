package com.example.securedb

object SecurityUtils {
    /**
     * Overwrites the byte array with zeros to clear sensitive data from memory.
     */
    fun clear(bytes: ByteArray?) {
        if (bytes == null) return
        for (i in bytes.indices) {
            bytes[i] = 0
        }
    }
}
