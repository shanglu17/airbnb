package com.example.airbnb.data

data class Listing(
    val id: Int,
    val title: String,
    val location: String,
    val pricePerNight: Int,
    val rating: Double,
    val reviewCount: Int,
    val imageUrls: List<String>,
    val description: String,
    val tags: List<String>,
    val isSuperHost: Boolean,
    val guestCount: Int,
    val bedroomCount: Int,
    val bathroomCount: Int,
    val amenities: List<String>,
    val hostName: String,
    val hostSince: String,
    val cleaningFee: Int,
    val serviceFee: Int,
    val supportsFreeCancellation: Boolean,
    val isNearbyArea: Boolean,
    val isFlexibleDateAvailable: Boolean,
    val reviews: List<Review>
)

data class Review(
    val id: Int,
    val author: String,
    val rating: Double,
    val date: String,
    val comment: String
)
