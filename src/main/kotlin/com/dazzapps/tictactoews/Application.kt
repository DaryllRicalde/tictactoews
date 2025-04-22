package com.dazzapps.tictactoews

import com.dazzapps.tictactoews.models.Game
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val game = Game()
    configureSockets()
    configureSerialization()
    configureRouting(game)
}
