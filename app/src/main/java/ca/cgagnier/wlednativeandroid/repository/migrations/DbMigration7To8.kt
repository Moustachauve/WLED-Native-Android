package ca.cgagnier.wlednativeandroid.repository.migrations

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

class DbMigration7To8 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        // Copy data from Device (StatefulDevice) to device2 (Device)
        // We filter out devices with unknown MAC addresses because 'macAddress'
        // is the Primary Key in the new table and must be unique/valid
        db.execSQL("""
            INSERT OR IGNORE INTO device2 (macAddress, address, isHidden, customName, originalName)
            SELECT 
                macAddress, 
                address, 
                isHidden, 
                CASE WHEN isCustomName = 1 THEN name ELSE NULL END, 
                CASE WHEN isCustomName = 0 THEN name ELSE NULL END
            FROM device
            WHERE macAddress IS NOT NULL AND macAddress != '__unknown__'
        """.trimIndent())
    }
}