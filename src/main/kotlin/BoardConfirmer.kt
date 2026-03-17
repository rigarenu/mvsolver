package io.github.rigarenu

import io.github.rigarenu.Grid.EMPTY

/**
 * 引数の盤面を総当たりし、確定できるマスを特定するクラス
 *
 * ① 地雷の有無を1列に並べた組み合わせ一覧を作成
 *
 * ② それらを盤面に適用し、可能性のある盤面すべてを作成
 *
 * ③ 可能性のある盤面すべてにおいて同じ種類のマスが確定マスになる
 *
 * @param beforeBoard 解きたい盤面
 */
class BoardConfirmer(private val beforeBoard: Board) {
    /**
     * 盤面を総当たりし、確定したマスの一覧を返す
     * @return 確定したマスの一覧<<確定マスの座標>, 確定マスの状態>
     */
    fun getConfirmedGrid(): Set<Pair<Pair<Int, Int>, Grid>> {
        val result = HashSet<Pair<Pair<Int, Int>, Grid>>()

        // 残り空白マス
        val remainingEmpty = beforeBoard.getNumOfGrid(EMPTY)
        // 残り地雷数
        val remainingMines = beforeBoard.allMines - beforeBoard.getNumOfGrid(Grid.FLAG)

        // 地雷の有無を1列に並べた組み合わせ一覧
        // (残り空白マス C 残り地雷数)個の組み合わせになる
        val mineCombinationArraySet = generateCombinations(remainingEmpty, remainingMines)
        // 地雷の有無の組み合わせを適用した、可能性のある盤面のSet
        val mineCombinationBoardSet = generateCombinationBoardSet(mineCombinationArraySet)

        // 全可能性候補で同じマスが確定マスになる
        for (r in 1..beforeBoard.row - 1) {
            for (c in 1..beforeBoard.column - 1) {
                var allFlagFlag = true // 現在参照しているマスが全盤面でFlagかどうか
                var allNumFlag = true // 現在参照しているマスが全盤面で数字かどうか

                // 可能性のある盤面のSetすべてを走査
                mineCombinationBoardSet.forEach {
                    // 地雷(Flag)かそうでないか(Num)の2択になる
                    if (it.gridBoard[r][c] == Grid.FLAG) {
                        allNumFlag = false
                    } else {
                        allFlagFlag = false
                    }
                }

                // 現在参照しているマスが空欄で、地雷か数字のフラグを満たしていたら確定させる
                if (beforeBoard.gridBoard[r][c] == EMPTY) {
                    if (allFlagFlag) {
                        result.add(Pair(Pair(r, c), Grid.FLAG))
                    } else if (allNumFlag) {
                        result.add(Pair(Pair(r, c), Grid.QUESTION))
                    }
                }
            }
        }

        return result
    }

    /**
     * 地雷の有無を1列に並べた組み合わせSetをBooleanArrayで作成
     * @param n 地雷がある可能性のある空白マス
     * @param r 残り地雷数
     * @return 地雷の有無を1列に並べた組み合わせSet
     */
    private fun generateCombinations(n: Int, r: Int): Set<BooleanArray> {
        val result = HashSet<BooleanArray>()
        val current = BooleanArray(n)

        fun backtrack(start: Int, count: Int) {
            // r個選んだら結果に追加
            if (count == r) {
                result.add(current.copyOf())
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
     * 地雷の組み合わせを適用した、可能性のある盤面のSetを作成
     *
     * 計算過程を表示する
     * @param mineCombinationArraySet 地雷の有無を1列に並べた組み合わせSet
     * @return 地雷の全可能性の組み合わせを適用した盤面のSet
     */
    private fun generateCombinationBoardSet(mineCombinationArraySet: Set<BooleanArray>): Set<Board> {
        val result = HashSet<Board>()
        println("計算開始(組み合わせ総数: ${mineCombinationArraySet.size})")

        mineCombinationArraySet.forEach { mineCombinationArray ->
            // 地雷の有無の組み合わせを適用した盤面
            val mineCombinationBoard = beforeBoard.copy()
            var index = 0
            for (r in 1..beforeBoard.row - 1) {
                for (c in 1..beforeBoard.column - 1) {
                    if (mineCombinationBoard.gridBoard[r][c] == EMPTY) {
                        if (mineCombinationArray[index]) {
                            mineCombinationBoard.setGrid(Pair(r, c), Grid.FLAG)
                        } else {
                            mineCombinationBoard.setGrid(Pair(r, c), Grid.QUESTION)
                        }
                        index++
                    }
                }
            }

            // 盤面の数字で検証し、可能性があればSetに追加
            if (checkBoard(mineCombinationBoard)) {
                result.add(mineCombinationBoard)
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
        for (r in 1..board.row - 1) {
            for (c in 1..board.column - 1) {
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
     * boardの座標(point)の数字と、周囲8マスのGrid.Flagの数が一致しているかどうか
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
                if (board.gridBoard[i][j] == Grid.FLAG) {
                    count++
                }
            }
        }

        return board.gridBoard[point.first][point.second].num == count
    }
}