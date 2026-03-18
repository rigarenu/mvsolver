package io.github.rigarenu

/**
 * TODO
 *  マスの画像 7, 8
 *  ウィンドウサイズ変更時の対応
 *  複数ルールへの対応
 */

/**
 * 日本語版、ルール「V:バニラ」のみに対応
 * ウィンドウが最前列かつ画面内に収まっている必要がある
 * サイズ6以上だとOutOfMemoryErrorが発生する可能性あり
 */
fun main() {
    solve(4)
}

/**
 * 問題を解き続ける
 *
 * エラーによる無限ループを防ぐためfor文で上限回数を指定する
 * @param numOfClear 解きたいクリア数
 * @param size 盤面のサイズ
 */
fun solve(numOfClear: Int) {
    for(i in 1..numOfClear) {
        println("$i 回目")
        val mvWindowController = MVWindowController()
        val board = mvWindowController.makeBoard()
        val confirmedGrid = BoardConfirmer(board).getConfirmedGrid()
        mvWindowController.doActiveWindow()
        confirmedGrid.forEach {
            // 数字ならクリック、地雷なら旗を立てる
            if (it.second == Grid.Question) {
                mvWindowController.clickGridAt(it.first, true)
            } else if (it.second == Grid.Flag) {
                mvWindowController.clickGridAt(it.first, false)
            }
        }
        mvWindowController.clickNextLevelButton()
    }
}