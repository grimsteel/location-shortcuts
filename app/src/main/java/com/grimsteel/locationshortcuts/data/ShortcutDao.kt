package com.grimsteel.locationshortcuts.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class TypeWithCount(
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "count") val count: Int
)

@Dao
interface ShortcutDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(shortcut: Shortcut): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultiple(shortcut: List<Shortcut>)

    @Update
    suspend fun update(note: Shortcut)

    @Delete
    suspend fun delete(note: Shortcut)

    @Query("SELECT * FROM shortcuts WHERE id = :id")
    fun getItem(id: Int): Flow<Shortcut>

    @Query("SELECT * FROM shortcuts ORDER BY label ASC")
    fun getAllItems(): Flow<List<Shortcut>>

    @Query("SELECT * FROM shortcuts WHERE region = :region AND type = :type")
    fun get(region: String, type: String): Flow<List<Shortcut>>

    @Query("SELECT * FROM shortcuts WHERE region = COALESCE(:region, region) AND type = COALESCE(:type, type)")
    fun getAllForRegionAndType(region: String?, type: String?): Flow<List<Shortcut>>

    @Query("SELECT DISTINCT region FROM shortcuts ORDER BY label ASC")
    fun getAllRegions(): Flow<List<String>>

    @Query("SELECT DISTINCT type FROM shortcuts ORDER BY label ASC")
    fun getAllTypes(): Flow<List<String>>

    @Query("SELECT type, SUM(CASE WHEN region = :region THEN 1 ELSE 0 END) as count FROM shortcuts GROUP BY type ORDER BY count DESC")
    fun getAllTypesWithCountForRegion(region: String): Flow<List<TypeWithCount>>

    @Query("SELECT type, COUNT(id) as count FROM shortcuts GROUP BY type ORDER BY count DESC")
    fun getAllTypesWithCount(): Flow<List<TypeWithCount>>
}