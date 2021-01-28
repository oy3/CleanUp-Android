package com.example.cleanup.data

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserInfo(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val fullname: String? = null,
    val phonenumber: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null
)