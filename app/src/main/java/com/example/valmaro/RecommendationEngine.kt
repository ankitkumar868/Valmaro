package com.example.valmaro

import com.example.valmaro.model.Product

object RecommendationEngine {

    fun recommendProducts(
        userNeed: String,
        products: List<Product>
    ): List<Product> {

        val query = userNeed.lowercase().trim()

        if (query.isEmpty()) return emptyList()

        // Budget detect karna
        val budgetRegex = Regex(
            """(?:under|below|less than|within|under|below|ke andar|tak|budget|₹|rs\.?)\s*₹?\s*(\d+)""",
            RegexOption.IGNORE_CASE
        )

        val budget = budgetRegex.find(query)
            ?.groupValues
            ?.get(1)
            ?.toDoubleOrNull()

        // Common words remove karna
        val stopWords = setOf(
            "i", "need", "want", "for", "the", "a", "an",
            "please", "show", "give", "me", "my",
            "mujhe", "chahiye", "chaiye", "hai",
            "ke", "ki", "ka", "koi", "under", "below",
            "less", "than", "within", "budget", "rupees",
            "rupaye", "tak", "andar"
        )

        val keywords = query
            .replace(Regex("[^a-zA-Z0-9 ]"), " ")
            .split(Regex("\\s+"))
            .filter {
                it.length >= 3 &&
                        !stopWords.contains(it) &&
                        it.toDoubleOrNull() == null
            }

        if (keywords.isEmpty()) return emptyList()

        // Similar words ko match karna
        val synonyms = mapOf(
            "shoe" to listOf("shoes", "sneakers", "footwear"),
            "shoes" to listOf("shoe", "sneakers", "footwear"),
            "running" to listOf("sports", "gym", "athletic"),
            "shirt" to listOf("shirts", "clothing", "clothes"),
            "clothes" to listOf("clothing", "shirt", "tshirt"),
            "watch" to listOf("watches", "smartwatch"),
            "watches" to listOf("watch", "smartwatch"),
            "smartwatch" to listOf("watch", "watches"),
            "cheap" to listOf("budget", "affordable", "low price"),
            "comfortable" to listOf("comfort", "soft")
        )

        val expandedKeywords = keywords.flatMap { keyword ->
            listOf(keyword) + (synonyms[keyword] ?: emptyList())
        }.distinct()

        val scoredProducts = products.mapNotNull { product ->

            if (product.stock <= 0) return@mapNotNull null

            val price = product.price
                .replace("₹", "")
                .replace(",", "")
                .trim()
                .toDoubleOrNull()

            // Budget se bahar products hatao
            if (budget != null && price != null && price > budget) {
                return@mapNotNull null
            }

            val name = product.name.lowercase()
            val category = product.category.lowercase()
            val description = product.description.lowercase()

            val productText = "$name $category $description"

            var score = 0

            for (keyword in expandedKeywords) {

                if (name.contains(keyword)) {
                    score += 20
                } else if (category.contains(keyword)) {
                    score += 15
                } else if (description.contains(keyword)) {
                    score += 5
                }
            }

            // Rating bonus
            val rating = product.rating.toDoubleOrNull() ?: 0.0
            score += rating.toInt()

            if (score > 0) {
                Triple(product, score, price ?: Double.MAX_VALUE)
            } else {
                null
            }
        }

        return scoredProducts
            .sortedWith(
                compareByDescending<Triple<Product, Int, Double>> {
                    it.second
                }.thenBy {
                    it.third
                }
            )
            .take(3)
            .map { it.first }
    }
}