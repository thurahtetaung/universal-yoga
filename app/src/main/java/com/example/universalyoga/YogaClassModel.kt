package com.example.universalyoga

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class representing a yoga class, parcelable for passing between activities
@Parcelize
data class YogaClass(
    val id: Long,
    val courseId: Long,
    val date: String,
    val teacher: String,
    val comments: String? = null
) : Parcelable