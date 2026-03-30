package io.github.rigarenu

/**
 * TODO
 *  マスの画像 8
 *  ウィンドウサイズ変更時の対応
 *  複数ルールへの対応
 */

/**
 * 日本語版、ルール「V:バニラ」のみに対応
 * ウィンドウが最前列かつ画面内に収まっている必要がある
 */
fun main() {
    solve(4)
}

/**
 * 問題を解き続ける
 *
 * エラーによる無限ループを防ぐためfor文で上限回数を指定する
 *
 * ① 盤面をウィンドウから取得
 *
 * ② 盤面を解析し、確定したマスの一覧を取得
 *
 * ③ 確定したマスをクリック
 * @param numOfLoop 解きたいクリア数
 */
fun solve(numOfLoop: Int) {
    for(i in 1..numOfLoop) {
        println("$i ループ目")
        val mvWindowController = MVWindowController()

        // ① 盤面をウィンドウから取得
        val board = mvWindowController.makeBoard()

        // ② 盤面を解析し、確定したマスの一覧を取得
        val solver = VanillaSolver(board)
        val confirmedGrid = solver.confirm()

        // ③ 確定したマスをクリック
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