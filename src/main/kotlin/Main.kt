package io.github.rigarenu

/**
 * TODO
 *  マスの画像 7, 8
 *  ウィンドウサイズ変更時の対応
 *  6x6, 7x7, 8x8への対応
 *  他ルールへの対応
 */

// 現在、日本語版、5x5盤面、ルール「V:バニラ」のみに対応
// ウィンドウが最前列かつ画面内に収まっている必要がある
fun main() {
    solve(4)
}

/**
 * 問題を解き続ける
 * エラーによる無限ループを防ぐためfor文で上限回数を指定する
 * @param numOfClear 解きたいクリア数
 */
fun solve(numOfClear: Int) {
    for(i in 1..numOfClear) {
        println("$i 回目")
        val mvWindowController = MVWindowController()
        val board = mvWindowController.make55Board()
        val confirmedGrid = BoardConfirmer(board).getConfirmedGrid()
        mvWindowController.doActiveWindow()
        confirmedGrid.forEach {
            // 数字ならクリック、地雷なら旗を立てる
            if (it.second == Grid.QUESTION) {
                mvWindowController.clickGridAt(it.first, true)
            } else if (it.second == Grid.FLAG) {
                mvWindowController.clickGridAt(it.first, false)
            }
        }
        mvWindowController.clickNextLevelButton()
    }
}