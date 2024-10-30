package com.example.universalyoga

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class YogaClass(
    val id: Long,
    val courseId: Long,
    val date: String,
    val teacher: String,
    val comments: String? = null
) : Parcelable