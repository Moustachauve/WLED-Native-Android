package ca.cgagnier.wlednativeandroid.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ca.cgagnier.wlednativeandroid.model.Device

@Database(entities = [Device::class], version = 1, exportSchema = false)
abstract class DevicesDatabase: RoomDatabase() {
    abstract fun deviceDao(): DeviceDao

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