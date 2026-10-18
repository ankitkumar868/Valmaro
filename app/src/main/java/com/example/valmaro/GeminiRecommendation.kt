package com.example.valmaro

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.example.valmaro.model.Product

object GeminiRecommendation {

    private val model = Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).generativeModel("gemini-3.8-flash")

    suspend fun getRecommendations(
        userNeed: String,
        products: List<Product>
    ): String {

        val productList = products
            .filter { it.stock > 0 }
            .map {
                """
                ID: ${it.productId}
                Name: ${it.name}
                Category: ${it.category}
                Price: ${it.price}
                Rating: ${it.rating}
                Description: ${it.description}
                """.trimIndent()
            }
            .joinToString("\n\n")

        val prompt = """
            You are Valmaro's AI shopping assistant.

            User requirement:
            $userNeed

            Available products:
            $productList

            Instructions:
            1. Recommend only 2 or 3 suitable products.
            2. Select products only from the provided list.
            3. Never invent product names or IDs.
            4. Respect the user's budget.
            5. Prefer relevant products with good ratings and affordable prices.
            6. If no suitable product exists, return an empty list.

            Return only valid JSON in this format:
            {
              "productIds": ["id1", "id2", "id3"],
              "message": "Short explanation"
            }
        """.trimIndent()

        val response = model.generateContent(prompt)

        return response.text ?: ""
    }
}