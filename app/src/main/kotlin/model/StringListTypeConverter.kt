package com.example.voicesimpletodo.model

import androidx.room.TypeConverter

class StringListTypeConverter {

    @TypeConverter
    fun fromStringList(strings:List<String>?):String?{
        if(strings == null ) return null
        val buffer = StringBuilder()
        val result = strings.joinTo(buffer,",")
        return result.toString()
    }

    @TypeConverter
    fun toStringList(string:String?):List<String>?{
        if(string == null ) return null
        return  string.split(",")
    }
}