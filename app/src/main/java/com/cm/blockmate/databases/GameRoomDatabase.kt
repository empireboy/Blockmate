package com.cm.blockmate.databases

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cm.blockmate.daos.IGameDao
import com.cm.blockmate.mappers.BoardMapper
import com.cm.blockmate.models.Board
import com.cm.blockmate.models.BoardEntity
import com.cm.blockmate.models.Tile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [BoardEntity::class], version = 1, exportSchema = false)
abstract class GameRoomDatabase : RoomDatabase()
{
    abstract fun gameDao(): IGameDao

    companion object
    {
        private const val DATABASE_NAME = "GAME_DATABASE"

        @Volatile
        private var gameRoomDatabaseInstance: GameRoomDatabase? = null

        fun getDatabase(context: Context): GameRoomDatabase?
        {
            if (gameRoomDatabaseInstance == null)
            {
                synchronized(GameRoomDatabase::class.java)
                {
                    if (gameRoomDatabaseInstance == null)
                    {
                        gameRoomDatabaseInstance = Room.databaseBuilder(
                            context.applicationContext,
                            GameRoomDatabase::class.java,
                            DATABASE_NAME
                        )
                            .build()
                    }
                }
            }

            return gameRoomDatabaseInstance
        }
    }
}