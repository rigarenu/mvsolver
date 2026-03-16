package io.github.rigarenu

/**
 * マス目の状態を表す
 */
enum class Grid(val num: Int) {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),

    // プログラムで確認できなかったマス目
    UNKNOWN(-1),

    // 空欄
    EMPTY(-2),

    // ?記号
    QUESTION(-3),

    // 旗記号
    FLAG(-4),

    // 枠外
    OUT(-5)
}