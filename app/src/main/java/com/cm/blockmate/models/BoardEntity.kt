package com.cm.blockmate.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cm.blockmate.enums.GameState
import com.cm.blockmate.enums.Player

@Entity(tableName = "boardTable")
data class BoardEntity(
    @ColumnInfo(name = "tileJson")
    val tileJson: String,

    @ColumnInfo(name = "gameState")
    val gameState: GameState,

    @ColumnInfo(name = "turnState")
    val turnState: Player,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null
)