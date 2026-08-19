package io.github.rigarenu

/**
 * Normal Mode (ノーマルモード) で、1マスずつクリックし総当たりで解く
 */
class NormalModeSolver {
    private val mvWindow = MVWindow()

    /**
     * 1レベルを説き続ける
     *
     * @param numOfSolve 解く回数
     */
    fun solve(numOfSolve: Int) {
        for(i in 1 .. numOfSolve) {
            // 解けなかった場合は待機時間を少しずつ増やして解いてみる
            for(delay in 1000 .. 4000 step 1000) {
                solveOneBoard(delay.toLong())
                if(mvWindow.clickNextLevelButton()) {
                    break
                }
            }
        }
    }

    /**
     * 1盤面を解く
     *
     * @param delay 1クリックごとの待機時間
     */
    private fun solveOneBoard(delay: Long) {
        mvWindow.doActiveWindow()
        mvWindow.clickHintButton()
        mvWindow.resetBoard()

        val board = mvWindow.makeBoard()
        // クリックすべき正解一覧 <座標, 左/右クリック>
        val collectMap = HashMap<Pair<Int, Int>, Boolean>()
        for(row in 1 .. board.size - 1) {
            for(column in 1 .. board.size - 1) {
                if(board.gridBoard[row][column] == Grid.Empty) {
                    // 空欄マスの場合左クリックしてみる
                    mvWindow.clickGridAt(Pair(row, column), true)
                    Thread.sleep(delay)
                    // ポップアップが表示された(=間違っていた)場合、正しいのは右クリック
                    if(mvWindow.clickHintButton()) {
                        collectMap.put(Pair(row, column), false)
                    } else {
                        collectMap.put(Pair(row, column), true)
                    }
                }
            }
        }

        // ノーミスクリアのため盤面を一度リセット
        mvWindow.resetBoard()

        collectMap.forEach {
            mvWindow.clickGridAt(it.key , it.value)
        }

        Thread.sleep(2000)
    }
}