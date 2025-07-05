package com.dazzapps.tictactoews

import com.dazzapps.tictactoews.models.Game
import com.dazzapps.tictactoews.models.Move
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

const val MESSAGE_DELIMITER = "#"

fun Route.socket(game: Game) {
    route("/play") {
        webSocket {
            val player = game.connectPlayer(this)
            if(player == null) {
                println("LOG: Cannot connect player")
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Max number of players reached"))
                return@webSocket
            }

            try {
                incoming.consumeEach { frame ->
                    if(frame is Frame.Text) {
                        val move = extractMove(frame.readText())
                        game.makeMove(player, move)
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
            } finally {
                game.disconnectPlayer(player)
            }
        }
    }
}

/**
 * Assume that the message received will be in the form of make_move#{...}
 * the chars after "#" is a valid JSON string
 */
private fun extractMove(rawMessage: String): Move {
    val type = rawMessage.substringBefore(MESSAGE_DELIMITER)
    val content = rawMessage.substringAfter(MESSAGE_DELIMITER)
    return if(type == "make_move") {
        Json.decodeFromString<Move>(content)
    } else {
        Move(-1, -1)
    }
}