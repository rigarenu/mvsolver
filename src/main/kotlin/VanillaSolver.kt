package io.github.rigarenu

import io.github.rigarenu.Grid.*
import kotlin.math.max
import kotlin.math.min

/**
 * ルール「V:バニラ」を解くプログラム
 * @param beforeBoard 解きたい盤面
 */
class VanillaSolver(private val beforeBoard: Board) {
    // 可能性のある盤面のSet
    private val possibleBoardSet = HashSet<Board>()
    // 数字が隣にない空白マスを枠外とした盤面
    private val outBoard = makeOutBoard()
    // 数字が隣にない空白マスの数
    private val numOfNotNextToNum = countNumOfNotNextToNum()

    /**
     * 盤面を総当たりし、確定したマスの一覧を返す
     * @return 確定したマスの一覧<<確定マスの座標>, 確定マスの状態>
     */
    fun confirm(): Set<Pair<Pair<Int, Int>, Grid>> {
        val result = HashSet<Pair<Pair<Int, Int>, Grid>>()

        // 確定した地雷数
        val numOfConfirmedMines = beforeBoard.getNumOfGrid(Flag)
        // 検証する地雷数の最低値(数字が隣にない空白マスが全て地雷の場合)、最低値は0
        val minMine = max(beforeBoard.numOfAllMines - numOfNotNextToNum - numOfConfirmedMines, 0)
        // 検証する地雷数の最高値(数字が隣にない空白マスが全て地雷でない場合)
        val maxMine = min(beforeBoard.numOfAllMines - numOfConfirmedMines, outBoard.getNumOfGrid(Empty))

        println("$minMine ～ $maxMine の地雷数のパターンを検証します。")
        for (numOfMine in minMine..maxMine) {
            println("${outBoard.getNumOfGrid(Empty)} C $numOfMine パターンの組み合わせを検証中...")
            possibleBoardSet.addAll(makePossibleBoardSet(outBoard, outBoard.getNumOfGrid(Empty), numOfMine))
        }

        result.addAll(makeConfirmedGridSet())

        // 結果が0の場合、全パターンでの検証を行う
        if(result.isEmpty()) {
            println("空白マスを除外した計算結果が0だったので、全パターンでの解析を開始します。")
            possibleBoardSet.clear()
            val numOfRemainingMines = beforeBoard.numOfAllMines - numOfConfirmedMines
            possibleBoardSet.addAll(makePossibleBoardSet(beforeBoard, beforeBoard.getNumOfGrid(Empty), numOfRemainingMines))
            result.addAll(makeConfirmedGridSet())
        }

        return result
    }

    /**
     * 可能性のある盤面のSetでの、全マスが同じ確定したマスの一覧を取得
     * @return 確定したマスの一覧<<確定マスの座標>, 確定マスの状態>
     */
    private fun makeConfirmedGridSet(): Set<Pair<Pair<Int, Int>, Grid>> {
        val result = HashSet<Pair<Pair<Int, Int>, Grid>>()

        // 全可能性候補で、あるマスが全て空欄なら数字、すべて地雷なら地雷が確定
        for (r in 1..beforeBoard.size - 1) {
            loop@ for (c in 1..beforeBoard.size - 1) {
                var allNumFlag = true // 現在参照しているマスが全盤面で空欄(=地雷でない)かどうか
                var allFlagFlag = true // 現在参照しているマスが全盤面で地雷かどうか

                possibleBoardSet.forEach {
                    when (it.gridBoard[r][c]) {
                        Empty -> {
                            // 空欄が出現した時点で、そこが地雷の可能性はなくなる
                            allFlagFlag = false
                        }

                        Flag -> {
                            // 地雷が出現した時点で、そこが数字の可能性はなくなる
                            allNumFlag = false
                        }

                        else -> {
                            // 空欄または地雷でないなら今後そのマスの調査は不要
                            continue@loop
                        }
                    }
                }

                // 現在参照しているマスが地雷か数字のフラグを満たしていたら確定
                // 空欄を前提にしないと、すでに確定されたマスも候補に入れてしまう
                if (beforeBoard.gridBoard[r][c] == Empty) {
                    if (allNumFlag) {
                        result.add(Pair(Pair(r, c), Question))
                    } else if (allFlagFlag) {
                        result.add(Pair(Pair(r, c), Flag))
                    }
                }
            }
        }

        return result
    }

    /**
     * 数字が隣にない空白マスを枠外とした盤面を作成
     * @return 数字が隣にない空白マスを枠外とした盤面
     */
    private fun makeOutBoard(): Board {
        val result = beforeBoard.copy()

        val size = beforeBoard.size
        for (r in 1..size - 2) {
            for (c in 1..size - 2) {
                // そこが空白マスか
                if (beforeBoard.gridBoard[r][c] == Empty) {
                    // 周囲8マスに数字がなければ計算外なのでOutを代入
                    if (!isNumberAround(r, c)) {
                        result.setGrid(Pair(r, c), Out)
                    }
                }
            }
        }

        return result
    }

    /**
     * 引数の盤面から数字が隣にない空白マスの数を数える
     * @return 数字が隣にない空白マスの数
     */
    private fun countNumOfNotNextToNum(): Int {
        var result = 0

        val size = beforeBoard.size
        for (r in 1..size - 2) {
            for (c in 1..size - 2) {
                // そこが空白マスか
                if (beforeBoard.gridBoard[r][c] == Empty) {
                    // 周囲8マスに数字がなければカウンタを増やす
                    if (!isNumberAround(r, c)) {
                        result++
                    }
                }
            }
        }

        return result
    }

    /**
     * 周囲8マスに数字があるか
     * @param r 調べたい行座標
     * @param c 調べたい列座標
     * @return 周囲8マスに数字があるならtrue
     */
    private fun isNumberAround(r: Int, c: Int): Boolean {
        for (y in r - 1..r + 1) {
            for (x in c - 1..c + 1) {
                // 自身のマスは除外
                if (!(y == r && x == c)) {
                    if (beforeBoard.gridBoard[y][x].num >= 0 || beforeBoard.gridBoard[y][x] == Question) {
                        return true
                    }
                }
            }
        }

        return false
    }

    /**
     * 引数の盤面から可能性のある盤面のSetを作成
     * @param board 調べたい盤面
     * @param n 空白マスの数
     * @param r 地雷の数
     * @return 可能性のある盤面
     */
    private fun makePossibleBoardSet(board: Board, n: Int, r: Int): Set<Board> {
        val result = HashSet<Board>()
        val current = BooleanArray(n)

        fun backtrack(start: Int, count: Int) {
            // r個選んだら結果に追加
            if (count == r) {
                // currentから盤面を作成
                val currentBoard = makeBoardFromArray(board, current.copyOf())
                // その盤面が合法か検証し、合法ならresultに追加
                if(checkBoard(currentBoard)) {
                    result.add(currentBoard)
                }
                return
            }

            // 残りの要素を探索
            for (i in start until n) {
                // 残りの枠でr個に足りない場合は枝刈り（最適化）
                if (n - i < r - count) break

                current[i] = true
                backtrack(i + 1, count + 1)
                current[i] = false // 状態を戻す（バックトラッキング）
            }
        }

        backtrack(0, 0)
        return result
    }

    /**
     * 引数の盤面から地雷の組み合わせを適用した盤面を作成
     * @param board 調べたい盤面
     * @param booleanArray 地雷の有無を一列に並べた配列
     * @return 作成した盤面
     */
    private fun makeBoardFromArray(board: Board, booleanArray: BooleanArray): Board {
        val result = board.copy()

        var index = 0
        for (r in 1..beforeBoard.size - 1) {
            for (c in 1..beforeBoard.size - 1) {
                if (result.gridBoard[r][c] == Empty) {
                    if (booleanArray[index]) {
                        result.setGrid(Pair(r, c), Flag)
                    } else {
                        result.setGrid(Pair(r, c), Empty)
                    }
                    index++
                }
            }
        }

        return result
    }

    /**
     * 引数の盤面が合法か検証
     * @param board 調べたい盤面
     * @return 合法ならtrue
     */
    private fun checkBoard(board: Board): Boolean {
        for (r in 1..board.size - 1) {
            for (c in 1..board.size - 1) {
                // 現在参照しているマスが数字なら検証する
                if (board.gridBoard[r][c].num >= 0) {
                    if (!checkNum(board, Pair(r, c))) {
                        return false
                    }
                }
            }
        }
        return true
    }

    /**
     * boardの座標(point)の数字と、周囲8マスの地雷の数が一致しているかどうか
     * @param board 調べたい盤面
     * @param point 調べたい座標
     * @return 引数のマスの数字と周囲の地雷数が一致していればtrue
     */
    private fun checkNum(board: Board, point: Pair<Int, Int>): Boolean {
        // 周囲の地雷数カウント
        var count = 0

        for (i in point.first - 1..point.first + 1) {
            for (j in point.second - 1..point.second + 1) {
                // 自身のマスは調べない
                if (i == point.first && j == point.second) continue
                if (board.gridBoard[i][j] == Flag) {
                    count++
                }
            }
        }

        return board.gridBoard[point.first][point.second].num == count
    }
}