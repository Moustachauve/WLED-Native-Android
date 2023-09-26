package ca.cgagnier.wlednativeandroid.repository

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.Version

@Database(
    entities = [
        Device::class,
        Version::class,
        Asset::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class DevicesDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun versionDao(): VersionDao

    companion object {
        @Volatile
        private var INSTANCE: DevicesDatabase? = null

        fun getDatabase(context: Context): DevicesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DevicesDatabase::class.java,
                    "devices_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}