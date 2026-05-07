package com.example.airbnb.data

object MockData {
    val supportedCities = listOf("All", "Shanghai", "Beijing", "Shenzhen", "Hangzhou")

    val listings: List<Listing> = List(40) { index ->
        val id = index + 1
        val city = supportedCities.drop(1)[index % 4]
        val basePrice = 398 + (index % 8) * 66
        Listing(
            id = id,
            title = "Cozy Loft #$id",
            location = city,
            pricePerNight = basePrice,
            rating = 4.5 + ((index % 5) * 0.1),
            reviewCount = 60 + index * 3,
            imageUrls = List(4) { pic ->
                "https://picsum.photos/seed/airbnb${id}_${pic + 1}/1200/800"
            },
            description = "A modern and comfortable stay with great transport access, workspace, and city view.",
            tags = buildList {
                if (index % 2 == 0) add("Guest favorite")
                if (index % 3 == 0) add("Self check-in")
                if (index % 4 == 0) add("Great location")
            },
            isSuperHost = index % 3 != 0,
            guestCount = 2 + index % 4,
            bedroomCount = 1 + index % 3,
            bathroomCount = 1 + index % 2,
            amenities = listOf("Wifi", "Kitchen", "Washer", "Air conditioning", "Dedicated workspace")
                .shuffled()
                .take(4),
            hostName = listOf("Alex", "Mia", "Jason", "Luna")[index % 4],
            hostSince = "Host since ${2018 + (index % 6)}",
            cleaningFee = 80 + (index % 4) * 20,
            serviceFee = 60 + (index % 3) * 15,
            reviews = List(4) { reviewIndex ->
                Review(
                    id = id * 10 + reviewIndex,
                    author = listOf("Sofia", "Noah", "Emma", "Leo")[reviewIndex % 4],
                    rating = 4.5 + (reviewIndex % 3) * 0.1,
                    date = "${2024 + reviewIndex}-0${(reviewIndex % 8) + 1}",
                    comment = "Great place in $city. Clean room and responsive host."
                )
            }
        )
    }

    fun findListing(id: Int): Listing? = listings.firstOrNull { it.id == id }
}
