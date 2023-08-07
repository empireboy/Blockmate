package com.cm.blockmate.repositories

import android.content.Context
import android.util.Log
import com.cm.blockmate.daos.IGameDao
import com.cm.blockmate.databases.GameRoomDatabase
import com.cm.blockmate.models.BoardEntity

class GameRepository(context: Context)
{
    private var _gameDao: IGameDao

    init
    {
        val gameRoomDatabase = GameRoomDatabase.getDatabase(context)

        _gameDao = gameRoomDatabase!!.gameDao()
    }

    fun getBoard(): BoardEntity?
    {
        return _gameDao.getBoard()
    }

    fun insertBoard(boardEntity: BoardEntity)
    {
        _gameDao.insertBoard(boardEntity)
    }

    fun updateBoard(boardEntity: BoardEntity)
    {
        boardEntity.id = 1

        _gameDao.updateBoard(boardEntity)
    }

    fun deleteAllBoards()
    {
        _gameDao.deleteAllBoards()
    }
}