package com.example.cleanup.data

import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable
import java.util.*

@IgnoreExtraProperties
data class Bookings(
    val amount: String? = null,
    var bookedTime: Date? = null,
    val cleaningDuration: String? = null,
    val customerAddress: String? = null,
    val customerAltPhone: String? = null,
    val customerEmail: String? = null,
    val customerLocation: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val date: String? = null,
    val frequency: String? = null,
    val needCleaningProduct: String? = null,
    val numberOfBath: String? = null,
    val numberOfRooms: String? = null,
    val paystackId: String? = null,
    val specialInstruction: String? = null,
    val time: String? = null,
    val typeOfService: String? = null,
    val status: String? = null,
    val userId: String? = null
) : Serializable


//class Bookings {
//    var numberOfBath: String? = null
//        private set
//    var frequency: String? = null
//        private set
//    var date: String? = null
//        private set
//    var customerAddress: String? = null
//        private set
//
//
//    constructor() {
//        //empty constructor needed
//    }
//
//    constructor(
//        numberOfBath: String?,
//        frequency: String?,
//        date: String?,
//        customerAddress: String?
//    ) {
//        this.numberOfBath = numberOfBath
//        this.frequency = frequency
//        this.date = date
//        this.customerAddress = customerAddress
//    }
//
//}