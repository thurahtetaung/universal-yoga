package com.example.universalyoga

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class YogaCourse(
    val id: Long,
    val type: String,
    val dayOfWeek: String,
    val timeOfDay: String,
    val duration: Int,
    val capacity: Int,
    val price: Double,
    val description: String? = null
) : Parcelable