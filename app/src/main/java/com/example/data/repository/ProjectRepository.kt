package com.example.data.repository

import android.content.Context
import com.example.data.dao.ProjectDao
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val context: Context
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    fun searchProjects(query: String): Flow<List<ProjectEntity>> {
        return if (query.isBlank()) {
            projectDao.getAllProjects()
        } else {
            projectDao.searchProjects(query)
        }
    }

    suspend fun getProjectById(id: Long): ProjectEntity? = withContext(Dispatchers.IO) {
        projectDao.getProjectById(id)
    }

    suspend fun ensureDefaultProjects() = withContext(Dispatchers.IO) {
        try {
            if (projectDao.getProjectCount() == 0) {
                val defaultDir = getDefaultProjectsDirectory()
                val darkVillageDir = File(defaultDir, "Dark Village")
                if (!darkVillageDir.exists()) {
                    darkVillageDir.mkdirs()
                }
                val defaultProject = ProjectEntity(
                    name = "Dark Village",
                    path = darkVillageDir.absolutePath,
                    templateType = "2D Project",
                    lastModified = System.currentTimeMillis(),
                    fileSize = "12.4 MB",
                    version = "v1.0.0",
                    description = "مشروع لعبة 2D بأسلوب Pixel Art مع قرية مظلمة وتحكم كامل بالشخصية",
                    colorHex = "#8B5CF6",
                    scenesCount = 3,
                    scriptsCount = 6
                )
                projectDao.insertProject(defaultProject)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun createNewProject(
        name: String,
        basePath: String,
        templateType: String
    ): ProjectEntity = withContext(Dispatchers.IO) {
        val sanitizedName = name.trim().ifEmpty { "Project_${System.currentTimeMillis() % 10000}" }
        val projectFolder = File(basePath, sanitizedName)
        
        // Ensure directories exist
        try {
            if (!projectFolder.exists()) {
                projectFolder.mkdirs()
            }
            File(projectFolder, "scenes").mkdirs()
            File(projectFolder, "scripts").mkdirs()
            File(projectFolder, "assets").mkdirs()
            File(projectFolder, "build").mkdirs()

            // Create initial Korva Project metadata file
            val manifestFile = File(projectFolder, "project.korva")
            manifestFile.writeText(
                """
                {
                  "engine": "korva",
                  "version": "1.0.0",
                  "name": "$sanitizedName",
                  "template": "$templateType",
                  "created_at": "${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}",
                  "entry_scene": "scenes/main.scene"
                }
                """.trimIndent()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val color = when (templateType) {
            "2D Game Engine" -> "#00E5C9"
            "3D Scene Studio" -> "#38BDF8"
            "Physics Sandbox" -> "#FBBF24"
            else -> "#A855F7"
        }

        val project = ProjectEntity(
            name = sanitizedName,
            path = projectFolder.absolutePath,
            templateType = templateType,
            lastModified = System.currentTimeMillis(),
            fileSize = "4.2 MB",
            version = "v1.0.0",
            description = "مشروع تفاعلي مبني بواسطة محرك Korva Engine",
            colorHex = color,
            scenesCount = if (templateType.contains("3D")) 1 else 2,
            scriptsCount = 4
        )

        val generatedId = projectDao.insertProject(project)
        project.copy(id = generatedId)
    }

    suspend fun openExistingProjectFolder(folderPath: String): ProjectEntity = withContext(Dispatchers.IO) {
        val folder = File(folderPath)
        val name = if (folder.exists() && folder.name.isNotBlank()) folder.name else "مشروع مستورد"
        val project = ProjectEntity(
            name = name,
            path = folder.absolutePath,
            templateType = "2D Game Engine",
            lastModified = System.currentTimeMillis(),
            fileSize = "8.5 MB",
            version = "v1.0.0",
            description = "تم استيراده من الذاكرة المحلية",
            colorHex = "#38BDF8"
        )
        val id = projectDao.insertProject(project)
        project.copy(id = id)
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project.copy(lastModified = System.currentTimeMillis()))
    }

    suspend fun deleteProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(project)
        try {
            val dir = File(project.path)
            if (dir.exists()) {
                dir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteProjectById(id: Long) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(id)
    }

    fun getDefaultProjectsDirectory(): String {
        val externalDir = context.getExternalFilesDir(null)
        val baseDir = if (externalDir != null) {
            File(externalDir, "KorvaProjects")
        } else {
            File(context.filesDir, "KorvaProjects")
        }
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        return baseDir.absolutePath
    }
}
