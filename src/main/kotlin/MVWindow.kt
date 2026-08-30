package io.github.rigarenu

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import net.sourceforge.tess4j.Tesseract
import nu.pattern.OpenCV
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.io.path.Path
import io.github.rigarenu.ConstantNum.*
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.nio.file.Path

/**
 * 「Minesweeper Variants」のウィンドウの操作を行うクラス
 *
 * 映っている数字の判別や、そこからの盤面の作成、クリックなどを行う
 */
class MVWindow {
    // キャプチャやクリックを行う
    private val robot = Robot()

    // ウィンドウ全体における画像の始点座標
    var windowImagePoint = Pair(0, 0)
        private set

    // ウィンドウ全体の画像 --------
    private val windowImage: BufferedImage
        get() {
            // ウィンドウハンドルの取得
            val hWnd = User32.INSTANCE.FindWindow(null, "Minesweeper Variants")

            // ウィンドウの座標を取得
            val rect = WinDef.RECT()
            User32.INSTANCE.GetWindowRect(hWnd, rect)

            // ウィンドウの範囲を計算
            windowImagePoint = Pair(rect.left, rect.top)
            // ウィンドウ全体の画像のサイズ
            val windowImageSize = Pair((rect.right - rect.left), (rect.bottom - rect.top))

            return robot.createScreenCapture(
                Rectangle(
                    windowImagePoint.first,
                    windowImagePoint.second,
                    windowImageSize.first,
                    windowImageSize.second
                )
            )
        }

    init {
        // OpenCV使用のためのネイティブライブラリのロード（既にロード済みならスキップされる）
        OpenCV.loadLocally()
    }

    /**
     * 画像を.pngで保存
     */
    fun saveImage() {
        ImageIO.write(windowImage, "png", File("image.png"))
    }

    /**
     * キャプチャした画像から盤面を作成
     * @return 作成した盤面
     */
    fun makeBoard(): Board {
        val boardSize = determineBoardSize()

        // 枠外の+2を含めた盤面の作成
        val result = Board(boardSize + 2)

        val boardImage = when (boardSize) {
            5 -> {
                windowImage.getSubimage(Start5Width.num, Start5Height.num, Board5Size.num, Board5Size.num)
            }

            6 -> {
                windowImage.getSubimage(Start6Width.num, Start6Height.num, Board6Size.num, Board6Size.num)
            }

            7 -> {
                windowImage.getSubimage(Start7Width.num, Start7Height.num, Board7Size.num, Board7Size.num)
            }

            8 -> {
                windowImage.getSubimage(Start8Width.num, Start8Height.num, Board8Size.num, Board8Size.num)
            }

            else -> {
                throw Exception()
            }
        }

        // 画像をsizeに分割
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                val gridImage =
                    boardImage.getSubimage(
                        GridImageSize.num * j,
                        GridImageSize.num * i,
                        GridImageSize.num,
                        GridImageSize.num
                    )

                result.setGrid(Pair(i + 1, j + 1), determineGridWithOpenCV(gridImage))
            }
        }

        return result
    }

    /**
     * ウィンドウが非アクティブだとクリックが反応しないのでアクティブにする
     */
    fun doActiveWindow() {
        windowImage // 一度ウィンドウの位置を読み込み場所を特定する
        robot.mouseMove(windowImagePoint.first + 1, windowImagePoint.second + 1)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        robot.delay(100)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }

    /**
     * 引数のマス目を左または右クリックし、画像班別の邪魔にならないようマウスを左上に移動
     * @param point クリックしたい座標(row, column)
     * @param isLeft 左クリックしたいならtrue、右ならfalse
     */
    fun clickGridAt(point: Pair<Int, Int>, isLeft: Boolean) {
        // クリックする座標を取得
        val clickPoint = getGridPoint(point, determineBoardSize())
        robot.mouseMove(clickPoint.first, clickPoint.second)
        // 左クリック
        var inputEvent = InputEvent.BUTTON1_DOWN_MASK
        if (!isLeft) {
            // 右クリック
            inputEvent = InputEvent.BUTTON3_DOWN_MASK
        }

        robot.mousePress(inputEvent)
        robot.delay(100)
        robot.mouseRelease(inputEvent)

        robot.mouseMove(0, 0)
    }

    /**
     * ポップアップ上の「Hint」ボタンが表示されているならポップアップを閉じてtrueを返す。表示されていなければ何もせずfalseを返す
     *
     * @return ポップアップ上の「Hint」ボタンが表示されているならポップアップを閉じてtrueを返す。表示されていなければ何もせずfalseを返す
     */
    fun clickHintButton(): Boolean {
        val hintImage = windowImage.getSubimage(
            StartPopupImageWidth.num,
            StartPopupImageHeight.num,
            PopupImageWidth.num,
            PopupImageHeight.num
        )
        if (isSameImage(hintImage, Path("src\\main\\resources\\Hint.png"))) {
            robot.keyPress(KeyEvent.VK_ESCAPE)
            robot.delay(100)
            robot.keyRelease(KeyEvent.VK_ESCAPE)
            robot.delay(100)
            return true
        }
        return false
    }

    /**
     * ポップアップ上の「Next Level」ボタンが表示されているなら押してtrueを返す。表示されていなければ何もせずfalseを返す
     *
     * @return ポップアップ上の「Next Level」ボタンが表示されているなら押してtrueを返す。表示されていなければ何もせずfalseを返す
     */
    fun clickNextLevelButton(): Boolean {
        val hintImage = windowImage.getSubimage(
            StartPopupImageWidth.num,
            StartPopupImageHeight.num,
            PopupImageWidth.num,
            PopupImageHeight.num
        )
        if (isSameImage(hintImage, Path("src\\main\\resources\\NextLevel.png"))) {
            robot.mouseMove(windowImagePoint.first + StartPopupImageWidth.num + 10,  windowImagePoint.second+ StartPopupImageHeight.num + 10)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            robot.delay(100)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            robot.delay(100)
            return true
        }
        return false
    }

    /**
     * 盤面をリセット
     */
    fun resetBoard() {
        robot.keyPress(KeyEvent.VK_R)
        robot.delay(100)
        robot.keyRelease(KeyEvent.VK_R)
        robot.delay(100)
    }

    /**
     * 画像から盤面のサイズを判定、判定できなければエラーを返す
     * @return 盤面のサイズ
     */
    private fun determineBoardSize(): Int {
        val tesseract = Tesseract()

        // 学習データが入っているフォルダのパスを指定
        tesseract.setDatapath("tessdata")

        // 言語を指定
        tesseract.setLanguage("eng")

        // 盤面のサイズを初めて出た数字で判定
        val sizeImage = windowImage.getSubimage(64, 572, 175, 22)
        val determinedStr = tesseract.doOCR(sizeImage).trim()
        val firstNum = determinedStr.find {
            it in '5'..'8'
        } ?: throw Exception()

        return firstNum.digitToInt()
    }

    /**
     * 引数の画像に一致したGridを返す。判別できなければ数字として扱う
     * @param image 判別したいマスの画像
     */
    private fun determineGridWithOpenCV(image: BufferedImage): Grid {
        return when {
            isSameImage(image, Path("src\\main\\resources\\Empty.png")) -> {
                Grid.Empty
            }

            isSameImage(image, Path("src\\main\\resources\\Flag.png")) -> {
                Grid.Flag
            }

            else -> {
                Grid.Num
            }
        }
    }

    /**
     * 引数の画像をOpenCVで判別し、同じと判断できたらtrueを返す
     * @param image 判別したい画像
     * @param path 保存してある判別対象の画像のパス
     * @return 同じと判断できたらtrue
     */
    private fun isSameImage(image: BufferedImage, path: Path): Boolean {
        // 判別したい画像を1度保存しMatに変換
        ImageIO.write(image, "png", File("temp.png"))
        val matImage = Imgcodecs.imread("temp.png", Imgcodecs.IMREAD_GRAYSCALE)

        // 判別対象も同様にMatに変換
        val targetImage = Imgcodecs.imread(path.toString(), Imgcodecs.IMREAD_GRAYSCALE)

        // サイズが異なる場合は、ターゲットのサイズに合わせる（比較のため必須）
        val resizedImage = Mat()
        Imgproc.resize(targetImage, resizedImage, matImage.size())

        // テンプレートマッチングで類似度計算
        val resultMat = Mat()
        Imgproc.matchTemplate(matImage, resizedImage, resultMat, Imgproc.TM_CCOEFF_NORMED)

        // 最大のスコア（類似度）を取得
        val score = Core.minMaxLoc(resultMat).maxVal

        // メモリ解放
        resizedImage.release()
        resultMat.release()

        return if (score > 0.97) true
        else false
    }

    /**
     * マス目の中心の座標を取得
     * @param point 調べたいマス目(row, column)
     * @param boardSize 盤面のサイズ
     * @return ウィンドウ上での座標(x, y)
     */
    private fun getGridPoint(point: Pair<Int, Int>, boardSize: Int): Pair<Int, Int> {
        val lengthToStart = when (boardSize) {
            5 -> Pair(Start5Width.num, Start5Height.num)
            6 -> Pair(Start6Width.num, Start6Height.num)
            7 -> Pair(Start7Width.num, Start7Height.num)
            8 -> Pair(Start8Width.num, Start8Height.num)
            else -> {
                throw Exception()
            }
        }
        // 盤面左上の座標
        val boardPoint =
            Pair(windowImagePoint.first + lengthToStart.first, windowImagePoint.second + lengthToStart.second)

        val gridXPoint = boardPoint.first + 50 * (point.second - 1) + 25
        val gridYPoint = boardPoint.second + 50 * (point.first - 1) + 25

        return Pair(gridXPoint, gridYPoint)
    }
}