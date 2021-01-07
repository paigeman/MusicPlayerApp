package org.edu.ncu.part1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBUtils(val context: Context,name: String,version: Int):SQLiteOpenHelper(context,name,null,version) {

    private val createUser = "create table User (" +
            " id integer primary key autoincrement," +
            "username varchar(255)," +
            "userpwd varchar(255))"

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createUser)
    }

}