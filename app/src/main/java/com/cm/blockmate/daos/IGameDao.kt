package com.cm.blockmate.daos

import androidx.room.*
import com.cm.blockmate.models.BoardEntity

@Dao
interface IGameDao
{
    @Query("SELECT * FROM boardTable LIMIT 1")
    fun getBoard(): BoardEntity?

    @Insert
    fun insertBoard(boardEntity: BoardEntity)

    @Update
    fun updateBoard(boardEntity: BoardEntity)

    @Query("DELETE FROM boardTable")
    fun deleteAllBoards()
}