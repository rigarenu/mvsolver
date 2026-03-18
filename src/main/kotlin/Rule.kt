package io.github.rigarenu

/**
 * ルールを表す
 */
enum class Rule(val char: Char) {
    Vanilla('V'),
    Quad('Q'),
    Multiple('M'),
    Connected('C'),
    Liar('L'),
    Triplet('T'),
    Wall('W'),
    Outside('O'),
    Negation('N'),
    Dual('D'),
    Cross('X'),
    Snake('S'),
    Partition('P'),
    Balance('B'),
    Eyesight('E')
}