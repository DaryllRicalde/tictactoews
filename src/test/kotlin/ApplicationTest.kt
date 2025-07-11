package com.dazzapps

import com.dazzapps.tictactoews.models.Field
import com.dazzapps.tictactoews.models.Game
import com.dazzapps.tictactoews.models.GameState
import org.junit.Assert.assertTrue
import kotlin.test.Test

class ApplicationTest {

    @Test
    fun playerMustWinViaColumns() {
        val columnWonField: Field = arrayOf(
            arrayOf('X', 'O', 'O'),
            arrayOf('X', null, 'O'),
            arrayOf('X', 'O', 'O')
        )
        val state = GameState(field = columnWonField)
        val game = Game(state)
        assertTrue(game.playerWonWithColumns('X'))
    }

    @Test
    fun playerMustWinViaRows() {
        val rowWonField: Field = arrayOf(
            arrayOf('X', 'X', 'X'),
            arrayOf('O', 'O', 'O'),
            arrayOf('O', 'O', 'O')
        )
        val state = GameState(field = rowWonField)
        val game = Game(state)
        assertTrue(game.playerWonWithRows('X'))
    }

    @Test
    fun playerMustWinViaDiagonals() {
        val diagonalWonField: Field = arrayOf(
            arrayOf('X', 'O', 'O'),
            arrayOf('O', 'X', 'O'),
            arrayOf('O', 'O', 'X')
        )
        val state = GameState(field = diagonalWonField)
        val game = Game(state)
        assertTrue(game.playerWonWithDiagonals('X'))
    }
}
