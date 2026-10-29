package cl.duoc.clorocontrol.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MeasurementEntity::class, AlertEntity::class, CorrectiveActionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CloroControlDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao
    abstract fun alertDao(): AlertDao
    abstract fun correctiveActionDao(): CorrectiveActionDao

    companion object {
        @Volatile private var INSTANCE: CloroControlDatabase? = null

        fun getInstance(context: Context): CloroControlDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                CloroControlDatabase::class.java,
                "clorocontrol_local.db"
            ).build().also { INSTANCE = it }
        }
    }
}
