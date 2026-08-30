package io.github.rigarenu

import io.github.rigarenu.ConstantNum.*
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

/**
 * Normal Mode (ノーマルモード) で、1マスずつクリックし総当たりで解く
 */
class NormalModeSolver {
    private val mvWindow = MVWindow()

    init {
        mvWindow.doActiveWindow()
    }

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
     * 全ステージを引数の問題数ずつ解く
     *
     * @param numOfSolve 1ステージ当たりの解く回数
     */
    fun solveAll(numOfSolve: Int) {
        // V5 - V8
        var pointX = mvWindow.windowImagePoint.first + V5X.num
        var pointY = mvWindow.windowImagePoint.second + V5Y.num
        for(i in 1 .. 4) {
            enterAndSolve(Pair(pointX, pointY), numOfSolve)
            pointX += 40
        }

        // Q5 - B8
        pointX = mvWindow.windowImagePoint.first + Q5X.num
        pointY = mvWindow.windowImagePoint.second + Q5Y.num
        for(i in 1 .. 7) {
            for(j in 1 .. 4) {
                enterAndSolve(Pair(pointX, pointY), numOfSolve)
                pointX -= 40
            }
            pointX = mvWindow.windowImagePoint.first + Q5X.num
            pointY += 40
        }

        // M5 - E8
        pointX = mvWindow.windowImagePoint.first + M5X.num
        pointY = mvWindow.windowImagePoint.second + M5Y.num
        for(i in 1 .. 7) {
            for(j in 1 .. 4) {
                enterAndSolve(Pair(pointX, pointY), numOfSolve)
                pointX += 40
            }
            pointX = mvWindow.windowImagePoint.first + M5X.num
            pointY += 40
        }

        // +5 - +8
        pointX = mvWindow.windowImagePoint.first + Plus5X.num
        pointY = mvWindow.windowImagePoint.second + Plus5Y.num
        for(i in 1 .. 4) {
            enterAndSolve(Pair(pointX, pointY), numOfSolve)
            pointX -= 40
        }

        // #5 - #8
        pointX = mvWindow.windowImagePoint.first + Hash5X.num
        pointY = mvWindow.windowImagePoint.second + Hash5Y.num
        for(i in 1 .. 4) {
            enterAndSolve(Pair(pointX, pointY), numOfSolve)
            pointX += 40
        }

        // #+5 - #+8
        pointX = mvWindow.windowImagePoint.first + HashPlus5X.num
        pointY = mvWindow.windowImagePoint.second + HashPlus5Y.num
        for(i in 1 .. 4) {
            enterAndSolve(Pair(pointX, pointY), numOfSolve)
            pointX += 40
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

    /**
     * ステージ選択画面から引数の座標をクリックしステージに入り、引数の問題数解いてステージ選択画面に戻る
     *
     * @param point 解きたいステージの座標
     * @param numOfSolve 1ステージ当たりの解く回数
     */
    private fun enterAndSolve(point: Pair<Int, Int>, numOfSolve: Int) {
        val robot = Robot()
        robot.mouseMove(point.first, point.second)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        robot.delay(100)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
        robot.delay(100)

        robot.keyPress(KeyEvent.VK_ENTER)
        robot.delay(100)
        robot.keyRelease(KeyEvent.VK_ENTER)
        robot.delay(100)

        solve(numOfSolve)

        robot.keyPress(KeyEvent.VK_ESCAPE)
        robot.delay(100)
        robot.keyRelease(KeyEvent.VK_ESCAPE)
        robot.delay(100)
    }
}