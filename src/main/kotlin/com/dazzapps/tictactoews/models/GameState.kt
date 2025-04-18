package com.dazzapps.tictactoews.models

import kotlinx.serialization.Serializable

typealias Player = Char
typealias Field = Array<Array<Char?>>

@Serializable
data class GameState(
    val playerAtTurn: Player? = null,
    val field: Field = newGame(),
    val winningPlayer: Player? = null,
    val isBoardFull: Boolean = false,
    val connectedPlayers: List<Player> = emptyList()
) {
    companion object {
        fun newGame(): Field =
            arrayOf(
                arrayOf(null, null, null),
                arrayOf(null, null, null),
                arrayOf(null, null, null)
            )
    }

    /**
     * Auto-generated
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (playerAtTurn != other.playerAtTurn) return false
        if (winningPlayer != other.winningPlayer) return false
        if (isBoardFull != other.isBoardFull) return false
        if (!field.contentDeepEquals(other.field)) return false
        if (connectedPlayers != other.connectedPlayers) return false

        return true
    }

    /**
     * Auto-generated
     */
    override fun hashCode(): Int {
        var result = playerAtTurn?.hashCode() ?: 0
        result = 31 * result + (winningPlayer?.hashCode() ?: 0)
        result = 31 * result + isBoardFull.hashCode()
        result = 31 * result + field.contentDeepHashCode()
        result = 31 * result + connectedPlayers.hashCode()
        return result
    }
}
