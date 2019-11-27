package com.example.voicesimpletodo
import androidx.room.*
import com.example.voicesimpletodo.StringListTypeConverter

@Entity(tableName = "recordItem")
data class ItemEntity( // table within the database . field correspond to columns
    @PrimaryKey(autoGenerate = true)
    var id: Int,        // 実際のアイテムIDは1から開始｡
    var title: String = "",
    var description: String = "",
    @TypeConverters(StringListTypeConverter::class)
    var tag: MutableList<String>,
    var isOpened: Boolean = false,
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

@Database(entities = [ItemEntity::class], exportSchema = false, version = 5)
@TypeConverters(StringListTypeConverter::class)
abstract class MyDataBase : RoomDatabase() {
    abstract fun myDao(): MyDao
}

class TagState(
    val id : Int,
    val title: String,
    var isSelected : Boolean = true,
    var isUsing :Boolean = true)
