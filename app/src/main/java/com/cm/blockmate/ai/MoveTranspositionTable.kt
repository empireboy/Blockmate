package com.cm.blockmate.ai

import com.cm.blockmate.models.Move

class MoveTranspositionTable
{
    private val _hashMap = HashMap<Int, Pair<Move?, Int>>() // Map position hashes to (move, depth)

    fun contains(hash: Int, depth: Int): Boolean
    {
        val entry = _hashMap[hash]

        return entry != null && entry.second >= depth
    }

    fun getEntry(hash: Int): Pair<Move?, Int>?
    {
        return _hashMap[hash]
    }

    fun store(hash: Int, move: Move?, depth: Int)
    {
        _hashMap[hash] = Pair(move, depth)
    }
}