package com.example.airbnb.data

object MockData {
    val listings: List<Listing> = List(40) { index ->
        val id = index + 1
        Listing(
            id = id,
            title = "Cozy Loft #$id",
            location = listOf("Shanghai", "Beijing", "Shenzhen", "Hangzhou")[index % 4],
            pricePerNight = 398 + (index % 8) * 66,
            rating = 4.5 + ((index % 5) * 0.1),
            reviewCount = 60 + index * 3,
            imageUrl = "https://picsum.photos/seed/airbnb$id/1200/800",
            description = "A modern and comfortable stay with great transport access, workspace, and city view."
        )
    }
}
