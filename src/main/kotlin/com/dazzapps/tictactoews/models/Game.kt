package com.dazzapps.tictactoews.models

import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class Game {

    private val state =  MutableStateFlow(GameState())
    private val playerSockets = ConcurrentHashMap<Player, WebSocketSession>()
    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        state.onEach(::broadcast).launchIn(gameScope)
    }

    /**
     * Connect a player if [GameState.connectedPlayers] is not full
     *
     * Player 1 is Player 'X', and Player 2 is Player 'O'
     */
    fun connectPlayer(session: WebSocketSession): Player? {
        // Check if game is full
        if(state.value.connectedPlayers.isFull()) {
            return null
        }
        val isPlayerX = state.value.connectedPlayers.isEmpty()
        val player = if(isPlayerX) 'X' else 'O'
        if(playerHasNotConnectedBefore(player)) {
            playerSockets[player] = session
        }
        state.update {
            it.copy(
                connectedPlayers = it.connectedPlayers + player
            )
        }
        return player
    }

    fun disconnectPlayer(player: Player) {
        if(playerInGame(player)) {
            playerSockets.remove(player)
            state.update {
                it.copy(connectedPlayers = it.connectedPlayers - player)
            }
        }
    }

    suspend fun broadcast(state: GameState) {
        playerSockets.values.forEach { socket ->
            socket.send(
                Json.encodeToString(state)
            )
        }
    }

    /**
     * A player can make a move if the following conditions are satisfied:
     * 1. The field position they want to make a move on is empty
     * 2. Game is still in progress i.e. no winner
     * 3. It is their turn
     *
     * The game will restart if:
     * - The player makes a move that completes the board
     * - The player makes a move that makes them win
     */
    fun makeMove(player: Player, move: Move) {
        if(fieldPositionEmpty(move.xCoordinate, move.yCoordinate) && gameInProgress() && isTurn(player)) {

        }
    }

    private fun fieldPositionEmpty(x: Int, y: Int) = state.value.field[x][y] == null

//    private fun gameInProgress()

    private fun playerHasNotConnectedBefore(player: Player): Boolean =
        !playerSockets.containsKey(player)

    private fun playerInGame(player: Player) = state.value.connectedPlayers.contains(player)
}

private fun List<Player>.isFull(): Boolean = this.size == 2
