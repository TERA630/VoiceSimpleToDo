package com.example.voicesimpletodo

import android.content.Context
import androidx.room.*

@Entity(tableName = "recordItem")
data class ItemEntity( // table within the database . field correspond to columns
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String = "",
    var description: String = "",
    var tag: String = "",
    var isParent: Boolean = false,
    var isClosed: Boolean = true,
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

//    @Query("SELECT * FROM recordItem")
//    suspend fun findTopLevel():List<ItemEntity>

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)
}

@Database(entities = [ItemEntity::class], exportSchema = false, version = 1)
abstract class MyDataBase : RoomDatabase() {
    abstract fun myDao(): MyDao

    companion object {
        private var instance: MyDataBase? = null

        fun getInstance(context: Context): MyDataBase? {
            if (instance == null) {
                synchronized(MyDataBase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, MyDataBase::class.java,
                        "MyDatabase.db"
                    )
                        .build()
                }
            }
            return instance
        }

        fun releaseInstance() {
            instance = null
        }
    }
}