package com.example.voicesimpletodo

import androidx.room.*

@Entity(tableName = "recordItem")
data class ItemEntity( // table within the database . field correspond to columns
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String = "",
    var description: String = "",
    var tag: String = "",
    var isParent: Boolean = false,
    var isOpened: Boolean = false,
    var isChild: Boolean = false,
    var isChildOf: Int = 0
)

@Dao
interface MyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Query("SELECT * FROM recordItem")
    suspend fun findAll(): List<ItemEntity>

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)
}

@Database(entities = [ItemEntity::class], exportSchema = false, version = 2)
abstract class MyDataBase : RoomDatabase() {
    abstract fun myDao(): MyDao

    companion object {
        private var instance: MyDataBase? = null
    }
}