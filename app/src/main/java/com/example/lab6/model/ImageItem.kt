package com.example.lab6.model

import android.net.Uri

/**
 * Модель данных для изображения в галерее
 * @property uri URI изображения
 * @property description Описание изображения
 */
data class ImageItem(
    val uri: Uri,
    var description: String? = null
) 