package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ProjectDao
import com.example.data.model.ProjectEntity

@Database(
    entities = [ProjectEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "korva_engine.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val now = System.currentTimeMillis()
                        db.execSQL(
                            """
                            INSERT INTO projects (name, path, templateType, lastModified, fileSize, version, description, colorHex, scenesCount, scriptsCount)
                            VALUES ('Dark Village', '/KorvaProjects/Dark Village', '2D Project', $now, '12.4 MB', 'v1.0.0', 'مشروع لعبة 2D بأسلوب Pixel Art مع قرية مظلمة وتحكم كامل بالشخصية', '#8B5CF6', 3, 6)
                            """.trimIndent()
                        )
                    }
                }).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
