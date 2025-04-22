package com.dazzapps.tictactoews

import com.dazzapps.tictactoews.models.Game
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting(game: Game) {
    routing {
        socket(game)
        // Static plugin. Try to access `/static/index.html`
        // staticResources("/static", "static")
    }
}
