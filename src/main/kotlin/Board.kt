package io.github.rigarenu

import io.github.rigarenu.Grid.*

/**
 * 盤面を表すクラス
 * @param row 盤面の行数(縦)、上下の枠外も含めているので+2して渡す
 * @param column 盤面の列数(横)、左右の枠外も含めているので+2して渡す
 * @param allMines 地雷総数
 */
class Board(val row: Int, val column: Int, val allMines: Int) {
    // 枠外を含めた[row][column]の盤面、初期値は全てEMPTY
    val gridBoard = Array(row) {
        Array(column) {
            EMPTY
        }
    }

    init {
        // 外周1マスを枠外にする
        gridBoard[0] = Array(column) {
            OUT
        }
        gridBoard[row - 1] = Array(column) {
            OUT
        }
        for (i in 1..row - 2) {
            gridBoard[i][0] = OUT
            gridBoard[i][column - 1] = OUT
        }
    }

    /**
     * コピーを作成
     * @return コピー
     */
    fun copy(): Board {
        val result = Board(row, column, allMines)

        for (i in 0 until row) {
            for (j in 0 until column) {
                result.gridBoard[i][j] = gridBoard[i][j]
            }
        }

        return result
    }

    /**
     * 盤面をコンソールに表示
     */
    fun dispBoard() {
        for (r in 1..row - 2) {
            for (c in 1..column - 2) {
                when (gridBoard[r][c]) {
                    UNKNOWN -> print("x")
                    EMPTY -> print("_")
                    QUESTION -> print("?")
                    FLAG -> print("F")
                    OUT -> print("o")
                    else -> print(gridBoard[r][c].num)
                }
                print(" ")
            }
            println()
        }
    }

    /**
     * point(row, column)にgridを入力
     * @param point 入力したい座標
     * @param grid 入力したいGrid
     */
    fun setGrid(point: Pair<Int, Int>, grid: Grid) {
        gridBoard[point.first][point.second] = grid
    }

    /**
     * 盤面の引数のマスの数を取得
     * @param grid 調べたいマスの種類
     * @return マスの数
     */
    fun getNumOfGrid(grid: Grid): Int {
        var result = 0
        gridBoard.forEach { array ->
            array.forEach {
                if (it == grid) {
                    result++
                }
            }
        }
        return result
    }
}