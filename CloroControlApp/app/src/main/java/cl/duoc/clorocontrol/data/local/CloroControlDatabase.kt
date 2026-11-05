package cl.duoc.clorocontrol.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MeasurementEntity::class,
        AlertEntity::class,
        CorrectiveActionEntity::class,
        SyncQueueEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CloroControlDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao
    abstract fun alertDao(): AlertDao
    abstract fun correctiveActionDao(): CorrectiveActionDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: CloroControlDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE measurements ADD COLUMN clientId TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE measurements SET clientId = 'legacy-measurement-' || id WHERE clientId = ''")
                db.execSQL("UPDATE measurements SET synced = 1")
                db.execSQL("ALTER TABLE corrective_actions ADD COLUMN clientId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE corrective_actions ADD COLUMN synced INTEGER NOT NULL DEFAULT 1")
                db.execSQL("UPDATE corrective_actions SET clientId = 'legacy-action-' || id WHERE clientId = ''")
                db.execSQL("UPDATE corrective_actions SET synced = 1")
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS sync_queue (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        eventType TEXT NOT NULL,
                        eventClientId TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        attempts INTEGER NOT NULL DEFAULT 0,
                        lastError TEXT
                    )""".trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_sync_queue_eventType_eventClientId " +
                        "ON sync_queue(eventType, eventClientId)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Backfill any interim v2 rows before creating unique client IDs.
                db.execSQL("UPDATE measurements SET clientId = 'legacy-measurement-' || id WHERE clientId = ''")
                db.execSQL("UPDATE corrective_actions SET clientId = 'legacy-action-' || id WHERE clientId = ''")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_measurements_clientId ON measurements(clientId)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_corrective_actions_clientId ON corrective_actions(clientId)"
                )
            }
        }

        fun getInstance(context: Context): CloroControlDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CloroControlDatabase::class.java,
                    "clorocontrol_local.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
