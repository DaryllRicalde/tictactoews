package com.dazzapps.tictactoews.models

import com.dazzapps.tictactoews.main
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.VisibleForTesting
import java.util.concurrent.ConcurrentHashMap

class Game(gameState: GameState = GameState()) {

    @VisibleForTesting
    val state = MutableStateFlow(gameState)
    private val playerSockets = ConcurrentHashMap<Player, WebSocketSession>()
    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var delayGameJob: Job? = null

    init {
        state.onEach(::broadcast).launchIn(gameScope)
    }

    /**
     * Connect a player if [GameState.connectedPlayers] is not full. Otherwise returns null
     *
     * Player 1 is Player 'X', and Player 2 is Player 'O'
     */
    fun connectPlayer(session: WebSocketSession): Player? {
        // Check if game is full
        if (state.value.connectedPlayers.isFull()) {
            return null
        }
        val isPlayerX = state.value.connectedPlayers.isEmpty()
        val player = if (isPlayerX) 'X' else 'O'
        if (playerHasNotConnectedBefore(player)) {
            playerSockets[player] = session
        }
        state.update {
            it.copy(
                connectedPlayers = it.connectedPlayers + player,
                playerAtTurn = if(isPlayerX) player else it.playerAtTurn
            )
        }
        return player
    }

    fun disconnectPlayer(player: Player) {
        if (playerInGame(player)) {
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
        if (fieldPositionEmpty(move.xCoordinate, move.yCoordinate) && gameInProgress() && isTurn(
                player
            )
        ) {
            state.update {
                val updatedField = state.value.field.also { field ->
                    field[move.xCoordinate][move.yCoordinate] = player
                }
                val isBoardFull = allFieldsIsNonEmpty(updatedField)
                if (isBoardFull || gameWonByPlayer(player)) {
                    startNewGameDelayed()
                    return
                }
                val newState = it.copy(
                    playerAtTurn = player.opponent,
                    field = updatedField,
                    isBoardFull = isBoardFull
                )
                newState
            }
        }
    }

    private fun gameWonByPlayer(player: Player): Boolean =
        playerWonWithDiagonals(player) || playerWonWithColumns(player) || playerWonWithRows(player)

    @VisibleForTesting
    fun playerWonWithRows(player: Player): Boolean =
        state.value.field.rows.any { row ->
            row.all { it == player}
        }

    @VisibleForTesting
    fun playerWonWithColumns(player: Player): Boolean =
        state.value.field.columns.any { col ->
             col.all { it == player}
        }

    @VisibleForTesting
    fun playerWonWithDiagonals(player: Player): Boolean =
        state.value.field.diagonals.any { diagonal ->
            diagonal.all { it == player }
        }

    private fun startNewGameDelayed() {
        delayGameJob?.cancel()
        delayGameJob = gameScope.launch {
            delay(DEFAULT_START_GAME_DELAY_IN_MILLIS)
            state.update { it.createNewGameWithCurrentConnectedPlayers() }
        }
    }

    private fun fieldPositionEmpty(x: Int, y: Int) = state.value.field[x][y] == null

    private fun gameInProgress() = state.value.winningPlayer == null

    private fun isTurn(player: Player) = state.value.playerAtTurn == player

    private fun allFieldsIsNonEmpty(field: Field) = field.all { innerField ->
        innerField.all { it != null }
    }

    private fun playerHasNotConnectedBefore(player: Player): Boolean =
        !playerSockets.containsKey(player)

    private fun playerInGame(player: Player) = state.value.connectedPlayers.contains(player)

    companion object {
        const val DEFAULT_START_GAME_DELAY_IN_MILLIS = 5000L
    }
}

private fun List<Player>.isFull(): Boolean = this.size == 2

private val Player.opponent: Player
    get() = if (this == 'X') 'O' else 'X'

private val Field.rows: Field
    get() = this

private val Field.columns: List<List<Player?>>
    get() = (this.indices).map { colIndex ->
        this.map { row -> row[colIndex]}
    }

private val Field.diagonals: List<List<Player?>>
    get() = this.run {
        val size = this.size
        val mainDiagonal = List(size) { i -> this[i][i] }
        val antiDiagonal = List(size) { i -> this[i][this.lastIndex - i]}
        listOf(mainDiagonal, antiDiagonal)
    }
