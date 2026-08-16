package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val path: String,
    val templateType: String = "2D Game Engine",
    val lastModified: Long = System.currentTimeMillis(),
    val fileSize: String = "12.4 MB",
    val version: String = "v1.0.0",
    val description: String = "مشروع كورفا للألعاب والتطبيقات التفاعلية",
    val isFavorite: Boolean = false,
    val colorHex: String = "#00E5C9",
    val scenesCount: Int = 3,
    val scriptsCount: Int = 8
)
