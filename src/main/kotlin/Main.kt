package io.github.rigarenu

/**
 * 言語を「English」、モードを「Normal Mode」にする
 *
 * ウィンドウが最前列かつ画面内に収まっている必要がある
 */
fun main() {
    // 1ステージのみを繰り返し説きたい場合は解きたいステージに入り「solve()」を実行
    NormalModeSolver().solve(100)

    // 全ステージを解きたい場合はステージ選択画面で「solveAll()」を実行、アンロックされていないステージは実行できないので注意。
//    NormalModeSolver().solveAll(1)
}