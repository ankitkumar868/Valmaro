package com.example.valmaro.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

data class PricePoint(
    val price: Double,
    val date: String
)

object PricePrediction {

    fun predictNext7Days(
        history: List<PricePoint>
    ): Double {

        if (history.size < 2) {
            return history.lastOrNull()?.price ?: 0.0
        }

        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        val sortedHistory = history.mapNotNull { point ->
            val date = formatter.parse(point.date)
            date?.let { Pair(it.time, point.price) }
        }.sortedBy { it.first }

        if (sortedHistory.size < 2) {
            return history.last().price
        }

        val first = sortedHistory.first()
        val last = sortedHistory.last()

        val days = (last.first - first.first) /
                (1000.0 * 60 * 60 * 24)

        if (days <= 0) {
            return last.second
        }

        val dailyChange = (last.second - first.second) / days

        val predictedPrice = last.second + (dailyChange * 7)

        return predictedPrice.coerceAtLeast(0.0)
    }
}