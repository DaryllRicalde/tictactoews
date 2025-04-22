package com.dazzapps.tictactoews.models

import kotlinx.serialization.Serializable

@Serializable
data class Move(val xCoordinate: Int, val yCoordinate:Int)
