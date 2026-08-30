package io.github.rigarenu

/**
 * 盤面のサイズなどの定数群
 */
enum class ConstantNum(val num: Int) {
    // 1マスの画像の大きさ
    GridImageSize(50),

    // 盤面5のサイズと始点座標までの長さ
    Board5Size(250),
    Start5Width(395),
    Start5Height(234),

    // 盤面6のサイズと始点座標までの長さ
    Board6Size(300),
    Start6Width(370),
    Start6Height(209),

    // 盤面7のサイズと始点座標までの長さ
    Board7Size(350),
    Start7Width(345),
    Start7Height(184),

    // 盤面8のサイズと始点座標までの長さ
    Board8Size(400),
    Start8Width(320),
    Start8Height(159),

    // ポップアップ上の「Hint」または「Next Level」ボタンのサイズと始点座標までの長さ
    PopupImageWidth(130),
    PopupImageHeight(36),
    StartPopupImageWidth(499),
    StartPopupImageHeight(466),

    // V5の座標
    V5X(520),
    V5Y(80),

    // Q5の座標
    Q5X(480),
    Q5Y(160),

    // M5の座標
    M5X(560),
    M5Y(160),

    //+5の座標
    Plus5X(480),
    Plus5Y(480),

    // #5の座標
    Hash5X(560),
    Hash5Y(480),

    // #+5の座標
    HashPlus5X(520),
    HashPlus5Y(560)
}