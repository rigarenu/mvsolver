package io.github.rigarenu

/**
 * マス目の状態を表す
 */
enum class Grid(val num: Int) {
    Zero(0),
    One(1),
    Two(2),
    Three(3),
    Four(4),
    Five(5),
    Six(6),
    Seven(7),
    Eight(8),

    // プログラムで確認できなかったマス目
    Unknown(-1),

    // 空欄
    Empty(-2),

    // ?記号
    Question(-3),

    // 旗記号
    Flag(-4),

    // 枠外
    Out(-5)
}