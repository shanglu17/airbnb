package com.example.airbnb.data

data class Listing(
    val id: Int,
    val title: String,
    val location: String,
    val pricePerNight: Int,
    val rating: Double,
    val reviewCount: Int,
    val imageUrl: String,
    val description: String
)
