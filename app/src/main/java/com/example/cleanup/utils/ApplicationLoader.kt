package com.example.cleanup.utils

import androidx.multidex.MultiDexApplication
import co.paystack.android.PaystackSdk


class ApplicationLoader : MultiDexApplication() {
//    val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    override fun onCreate() {
        super.onCreate()

        PaystackSdk.initialize(applicationContext)

    }
//
//    private fun initContacts() {
//        Contacts.initialize(this)
//    }
//
//
//    companion object {
//        @get:Synchronized
//        var instance: ApplicationLoader? = null
//            private set
//    }
}
