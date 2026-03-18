package io.github.rigarenu

import io.github.rigarenu.Grid.*

/**
 * 盤面を表すクラス
 * @param size 盤面のサイズ、上下左右の枠外も含めているので+2して渡す
 * @param allMines 地雷総数
 */
class Board(val rule: Rule, val size: Int, val allMines: Int) {
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

    /**
     * コピーを作成
     * @return コピー
     */
    fun copy(): Board {
        val result = Board(rule, size, allMines)

        for (i in 0 until size) {
            for (j in 0 until size) {
                result.gridBoard[i][j] = gridBoard[i][j]
            }
        }

        return result
    }

    /**
     * 盤面をコンソールに表示
     */
    fun dispBoard() {
        for (r in 1..size - 2) {
            for (c in 1..size - 2) {
                when (gridBoard[r][c]) {
                    Unknown -> print("x")
                    Empty -> print("_")
                    Question -> print("?")
                    Flag -> print("F")
                    Out -> print("o")
                    else -> print(gridBoard[r][c].num)
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