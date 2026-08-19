package io.github.rigarenu

import io.github.rigarenu.Grid.*

/**
 * 盤面を表すクラス
 * @param size 盤面のサイズ、上下左右の枠外も含めているので+2して渡す
 */
class Board(val size: Int) {
    // 枠外を含めた[row][column]の盤面、初期値は全てEMPTY
    val gridBoard = Array(size) {
        Array(size) {
            Empty
        }
    }

    init {
        // 外周1マスを枠外にする
        gridBoard[0] = Array(size) {
            Out
        }
        gridBoard[size - 1] = Array(size) {
            Out
        }
        for (i in 1..size - 2) {
            gridBoard[i][0] = Out
            gridBoard[i][size - 1] = Out
        }
    }

    override fun equals(other: Any?): Boolean {
        return gridBoard.contentDeepEquals((other as Board).gridBoard)
    }

    /**
     * 盤面をコンソールに表示
     */
    fun dispBoard() {
        for (r in 1..size - 2) {
            for (c in 1..size - 2) {
                when (gridBoard[r][c]) {
                    Empty -> print("_")
                    Num -> print("?")
                    Flag -> print("F")
                    Out -> {}
                }
                print(" ")
            }
            println()
        }
    }

    /**
     * point(row, column)にgridを入力
     * @param point 入力したい座標(row, column)
     * @param grid 入力したいGrid
     */
    fun setGrid(point: Pair<Int, Int>, grid: Grid) {
        gridBoard[point.first][point.second] = grid
    }
}