package com.cm.blockmate.ai

class TranspositionTable {
    private val _table = HashMap<Int, Pair<Float, Int>>() // Map position hashes to (score, depth)

    fun contains(hash: Int, depth: Int): Boolean {
        val entry = _table[hash]
        return entry != null && entry.second >= depth
    }

    fun getScore(hash: Int): Pair<Float, Int>? {
        return _table[hash]
    }

    fun store(hash: Int, score: Float, depth: Int) {
        _table[hash] = Pair(score, depth)
    }
}