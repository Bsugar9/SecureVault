package com.example.securedb

data class Category(val id: Int, val name: String)

data class EntryModel(
    val id: Int, 
    val categoryId: Int, 
    val title: String, 
    val password: String,
    val iconId: Int = 0 // 0: Lock, 1: Email, 2: Bank, 3: Social, 4: Card
)
