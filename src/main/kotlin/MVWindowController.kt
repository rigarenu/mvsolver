package io.github.rigarenu

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import net.sourceforge.tess4j.Tesseract
import nu.pattern.OpenCV
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.div

// キャプチャやクリックを行う
private val robot = Robot()

/**
 * 「Minesweeper Variants」のウィンドウの操作を行う
 * 映っている数字の判別や、そこからの盤面の作成も行う
 */
class MVWindowController {
    private val mvWindow = MVWindow()

    // OpenCVでの比較に使用する画像が入ったディレクトリのパス
    private val resourceImagesDirectoryPath = Path("src\\main\\resources")

    /**
     * キャプチャした画像から盤面を作成
     * @return 作成した盤面
     */
    fun make55Board(): Board {
        val result = Board(7, 7, getNumOfAllMines(mvWindow))

        // 1マスの画像の大きさ
        val gridImageWidth = mvWindow.boardImageSize.first / 5

        // 画像を5x5に分割
        for (i in 0..4) {
            for (j in 0..4) {
                val gridImage =
                    mvWindow.boardImage.getSubimage(
                        gridImageWidth * j,
                        gridImageWidth * i,
                        gridImageWidth,
                        gridImageWidth
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
        robot.mouseMove(mvWindow.windowImagePoint.first + 1, mvWindow.windowImagePoint.second + 1)
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
        robot.delay(100)
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }

    /**
     * 引数のマス目を右または左クリックする
     * @param point クリックしたい座標(row, column)
     * @param isLeft 左クリックしたいならtrue、右ならfalse
     */
    fun clickGridAt(point: Pair<Int, Int>, isLeft: Boolean) {
        // クリックする座標を取得
        val clickPoint = mvWindow.getGridPoint(point)
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
        robot.delay(100)
    }

    /**
     * クリア問題数を取得
     */
    fun getNumOfClear(): Int {
        return determineNumberWithTesseract(mvWindow.numOfClearImage)
    }

    /**
     * 「次のステージへ」が表示されているか判定し、表示されていたらクリック
     */
    fun clickNextLevelButton() {
        if (isSameImage(mvWindow.nextLevelImage, resourceImagesDirectoryPath / Path("next-level.png"))) {
            robot.mouseMove(
                mvWindow.windowImagePoint.first + mvWindow.lengthToNextLeveImage.first + 8,
                mvWindow.windowImagePoint.second + mvWindow.lengthToNextLeveImage.second + 8
            )
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            robot.delay(100)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            robot.delay(100)
        }
    }

    /**
     * 画像を.pngで保存
     */
    fun saveImage() {
        val image = mvWindow.windowImage
        ImageIO.write(image, "png", File("image.png"))
    }

    /**
     * 引数の画像に表示されている数字をtesseractで判別し取得。判定できなかった場合は-1を返す
     * @param image 判別したい画像
     * @return 画像の数字。判別できなかった場合は-1
     */
    private fun determineNumberWithTesseract(image: BufferedImage): Int {
        val tesseract = Tesseract()

        // 学習データが入っているフォルダのパスを指定
        tesseract.setDatapath("tessdata")

        // 言語を指定 (数字なら eng でOK)
        tesseract.setLanguage("eng")

        // 数字だけに限定
        tesseract.setVariable("tessedit_char_whitelist", "0123456789")

        return try {
            tesseract.doOCR(image).trim().toInt()
        } catch (e: Exception) {
            // 処理できなかった場合
            -1
        }
    }

    /**
     * 引数の画像をOpenCVで判別し、一致したGridを返す
     * @param image 判別したいマスの画像
     */
    private fun determineGridWithOpenCV(image: BufferedImage): Grid {
        var result = Grid.UNKNOWN
        val fileList = Files.list(resourceImagesDirectoryPath)

        fileList.forEach {
            if (isSameImage(image, it)) {
                result = Grid.valueOf(it.toString().substringAfterLast("\\").substringBeforeLast("."))
            }
        }

        return result
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

        return if (score > 0.95) true
        else false
    }

    /**
     * 地雷総数を取得
     * @return 地雷総数
     */
    private fun getNumOfAllMines(mvWindow: MVWindow): Int {
        return determineNumberWithTesseract(mvWindow.numOfAllMinesImage)
    }
}

/**
 * 「Minesweeper Variants」のウィンドウを表すクラス
 * 画面のキャプチャや切り抜きを行う
 */
private class MVWindow {
    init {
        // OpenCV使用のためのネイティブライブラリのロード（既にロード済みならスキップされる）
        OpenCV.loadLocally()
    }

    // ウィンドウ名
    private val windowTitle = "Minesweeper Variants"

    // ウィンドウ全体の画像 --------
    val windowImage: BufferedImage
        get() {
            // ウィンドウハンドルの取得
            val hWnd = User32.INSTANCE.FindWindow(null, windowTitle)

            // ウィンドウの座標を取得
            val rect = WinDef.RECT()
            User32.INSTANCE.GetWindowRect(hWnd, rect)

            // ウィンドウの範囲を計算
            // ウィンドウ全体の画像の始点座標(左上)
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

    // ウィンドウの始点座標(左上)
    var windowImagePoint = Pair(0, 0)

    // 地雷総数画像 --------
    val numOfAllMinesImage: BufferedImage
        get() {
            return windowImage.getSubimage(
                lengthToNumOfAllMinesImage.first,
                lengthToNumOfAllMinesImage.second,
                numOfAllMinesImageSize.first,
                numOfAllMinesImageSize.second
            )
        }

    // 地雷総数画像の始点座標(左上)までの長さ
    private val lengthToNumOfAllMinesImage = Pair(145, 54)

    // 地雷総数画像のサイズ
    private val numOfAllMinesImageSize = Pair(45, 20)

    // 盤面画像 --------
    val boardImage: BufferedImage
        get() {
            return windowImage.getSubimage(
                lengthToBoardImage.first,
                lengthToBoardImage.second,
                boardImageSize.first,
                boardImageSize.second
            )
        }

    // 盤面画像の始点座標(左上)までの長さ
    private val lengthToBoardImage = Pair(395, 234)

    // 盤面画像のサイズ
    val boardImageSize = Pair(250, 250)

    // クリア問題数画像 --------
    val numOfClearImage: BufferedImage
        get() {
            return windowImage.getSubimage(
                lengthToNumOfClearImage.first,
                lengthToNumOfClearImage.second,
                numOfClearImageSize.first,
                numOfClearImageSize.second
            )
        }

    // クリア問題数画像の始点座標(左上)までの長さ
    private val lengthToNumOfClearImage = Pair(939, 573)

    // クリア問題数画像のサイズ
    private val numOfClearImageSize = Pair(48, 18)

    // ポップアップの右上の[x]ボタン --------
    val closeImage: BufferedImage
        get() {
            return windowImage.getSubimage(
                lengthToCloseImage.first,
                lengthToCloseImage.second,
                closeImageSize.first,
                closeImageSize.second
            )
        }

    // ポップアップの右上の[x]ボタンの画像の始点座標(左上)までの長さ
    private val lengthToCloseImage = Pair(905, 129)

    // ポップアップの右上の[x]ボタン画像のサイズ
    private val closeImageSize = Pair(30, 34)

    // ポップアップの「次のステージへ」ボタン --------
    val nextLevelImage: BufferedImage
        get() {
            return windowImage.getSubimage(
                lengthToNextLeveImage.first,
                lengthToNextLeveImage.second,
                nextLeveImageSize.first,
                nextLeveImageSize.second
            )
        }

    // ポップアップの「次のステージへ」ボタンの画像の始点座標(左上)までの長さ
    val lengthToNextLeveImage = Pair(479, 467)

    // ポップアップの「次のステージへ」ボタンの画像のサイズ
    private val nextLeveImageSize = Pair(150, 34)

    /**
     * マス目の中心の座標を取得
     * @param point 調べたいマス目(row, column)
     * @return ウィンドウ上での座標(x, y)
     */
    fun getGridPoint(point: Pair<Int, Int>): Pair<Int, Int> {
        // 盤面左上の座標
        val boardPoint = plusPoint(windowImagePoint, lengthToBoardImage)

        val gridSize = boardImageSize.first / 5

        val gridXPoint = boardPoint.first + gridSize * (point.second - 1) + gridSize / 2
        val gridYPoint = boardPoint.second + gridSize * (point.first - 1) + gridSize / 2

        return Pair(gridXPoint, gridYPoint)
    }

    /**
     * 2つの座標の和を返す
     * @param point1 加算したい座標
     * @param point2 加算したい座標
     * @return 加算結果
     */
    private fun plusPoint(point1: Pair<Int, Int>, point2: Pair<Int, Int>): Pair<Int, Int> {
        return Pair(point1.first + point2.first, point1.second + point2.second)
    }
}