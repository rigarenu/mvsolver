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

/**
 * 1マスずつクリックし総当たりで解く
 */
class RoundRobinSolver {
    // OpenCVでの比較に使用する画像が入ったディレクトリのパス
    private val resourceImagesDirectoryPath = Path("src\\main\\resources")

    // ウィンドウ操作用
    private val robot = Robot()

    private var windowImageStartPoint = Pair(0, 0)
    private lateinit var windowImage: BufferedImage
    private var size = 5
    private var board: Board
    private fun refreshWindowImage() {
        // ウィンドウハンドルの取得
        val hWnd = User32.INSTANCE.FindWindow(null, "Minesweeper Variants")

        // ウィンドウの座標を取得
        val rect = WinDef.RECT()
        User32.INSTANCE.GetWindowRect(hWnd, rect)

        // ウィンドウの範囲を計算
        // ウィンドウ全体の画像の始点座標(左上)
        windowImageStartPoint = Pair(rect.left, rect.top)
        // ウィンドウ全体の画像のサイズ
        val windowImageSize = Pair((rect.right - rect.left), (rect.bottom - rect.top))

        windowImage = robot.createScreenCapture(
            Rectangle(
                windowImageStartPoint.first,
                windowImageStartPoint.second,
                windowImageSize.first,
                windowImageSize.second
            )
        )
    }
    private fun refreshSize() {
        val image = windowImage.getSubimage(64, 572, 175, 22)

        val tesseract = Tesseract()
        tesseract.setDatapath("tessdata")
        tesseract.setLanguage("eng")

        val determinedStr = tesseract.doOCR(image).trim()
        // 盤面のサイズを初めて出た数字で判定、判定できなければ5にする
        val firstNum = determinedStr.find {
            it in '5'..'8'
        } ?: '5'
        size = firstNum.digitToInt()
    }
    private fun makeBoard(): Board {
        // 枠外の+2を含めた盤面の作成
        val result = Board(Rule.Vanilla, size + 2, 0)

        // 盤面左上の描画開始座標
        val startPoint = when (size) {
            6 -> Pair(370, 209)
            7 -> Pair(345, 184)
            8 -> Pair(320, 159)
            else -> {
                Pair(395, 234)
            }
        }

        for (row in 1..size) {
            for (column in 1..size) {
                val gridImage = windowImage.getSubimage(
                    startPoint.first + 50 * (column - 1),
                    startPoint.second + 50 * (row - 1),
                    50,
                    50
                )
                result.setGrid(Pair(row, column), determineGridWithOpenCV(gridImage))
            }
        }

        return result
    }

    init {
        // OpenCV使用のためのネイティブライブラリのロード（既にロード済みならスキップされる）
        OpenCV.loadLocally()
        refreshWindowImage()
        refreshSize()
        board = makeBoard()
    }

    // 正解したクリック順のマップ<座標, isLeft>
    private val collectLinkedMap = LinkedHashMap<Pair<Int, Int>, Boolean>()

    // ヒントで指定されたマスの座標
    private var hintGrid = Pair(0, 0)

    // 左と右を交互にクリックさせるフラグ
    private var leftRightFlag = true

    // ハマるのを防ぐためのカウンタ
    private var loopCounter = 0

    fun solve(numOfLoop: Int) {
        // アクティブウィンドウ化
        clickPointAt(windowImageStartPoint.first + 1, windowImageStartPoint.second + 1, true)

        for (i in 1..numOfLoop) {
            println("$i ループ目")
            println(loopCounter)
            // 右上のリセットをクリック
            clickPointAt(windowImageStartPoint.first + 920, windowImageStartPoint.second + 70, true)
            // ステージを最初からやり直しますか？の「はい」をクリック
            clickPointAt(windowImageStartPoint.first + 715, windowImageStartPoint.second + 485, true)

            collectLinkedMap.forEach {
                clickGridAndCheck(it.key.first, it.key.second, it.value)
            }

            if (hintGrid.first != 0) {
                if (clickGridAndCheck(hintGrid.first, hintGrid.second, leftRightFlag)) {
                    hintGrid = Pair(0, 0)
                }
            }

            if (hintGrid.first == 0) {
                val emptyGrid = getGrid(Grid.Empty)
                clickGridAndCheck(emptyGrid.first, emptyGrid.second, leftRightFlag)
            }

            leftRightFlag = !leftRightFlag

            // 次のステージへ
            val nextStageImage = windowImage.getSubimage(479, 467, 150, 34)
            if (isSameImage(nextStageImage, resourceImagesDirectoryPath / Path("next-level.png"))) {
                clickPointAt(windowImageStartPoint.first + 554, windowImageStartPoint.second + 484, true)
                collectLinkedMap.clear()
                hintGrid = Pair(0, 0)
                loopCounter = 0
            }

            // たまにハマるのでリセットさせる
            if(loopCounter++ > 100) {
                collectLinkedMap.clear()
                hintGrid = Pair(0, 0)
                loopCounter = 0
            }
        }
    }

    /**
     * 引数の座標をクリックし、正しいマスをクリックしたか判定、対応する
     *
     * 誤った場合はヒント座標を更新し盤面をリセット、falseを返す
     *
     * 正しかった場合はtrueを返す
     * @param row クリックしたい横座標
     * @param column クリックしたい縦座標
     * @param isLeft 左クリックしたいならtrue、右ならfalse
     */
    private fun clickGridAndCheck(row: Int, column: Int, isLeft: Boolean): Boolean {
        // 盤面左上の描画開始座標
        val boardStartPoint = when (size) {
            6 -> Pair(370 + windowImageStartPoint.first, 209 + windowImageStartPoint.second)
            7 -> Pair(345 + windowImageStartPoint.first, 184 + windowImageStartPoint.second)
            8 -> Pair(320 + windowImageStartPoint.first, 159 + windowImageStartPoint.second)
            else -> {
                Pair(395 + windowImageStartPoint.first, 234 + windowImageStartPoint.second)
            }
        }
        clickPointAt(
            boardStartPoint.first + (column - 1) * 50 + 25,
            boardStartPoint.second + (row - 1) * 50 + 25,
            isLeft
        )

        refreshWindowImage()
        val hintImage = windowImage.getSubimage(500, 470, 128, 28)
        if (isSameImage(hintImage, resourceImagesDirectoryPath / Path("hint.png"))) {
            // 誤ったマスをクリックした場合、ヒント座標を取得
            // 「ヒント」ボタンをクリック
            clickPointAt(windowImageStartPoint.first + 564, windowImageStartPoint.second + 484, true)
            // ヒント座標の取得
            hintGrid = getGrid(Grid.Exclamation)
            return false
        } else {
            // 正しいマスをクリックした場合
            collectLinkedMap.put(Pair(row, column), isLeft)
            return true
        }
    }

    /**
     * 引数のウィンドウ上の座標をクリックする
     * @param x クリックしたいx座標
     * @param y クリックしたいy座標
     * @param isLeft 左クリックしたいならtrue、右ならfalse
     */
    private fun clickPointAt(x: Int, y: Int, isLeft: Boolean) {
        robot.mouseMove(x, y)

        // 左クリック
        var inputEvent = InputEvent.BUTTON1_DOWN_MASK
        if (!isLeft) {
            // 右クリック
            inputEvent = InputEvent.BUTTON3_DOWN_MASK
        }

        robot.mousePress(inputEvent)
        robot.delay(50)
        robot.mouseRelease(inputEvent)
        robot.delay(size / 2 * 100) // 盤面が大きいほど待機も伸ばす
    }

    /**
     * 引数のマスを取得する
     * @return 引数のマスの座標
     */
    private fun getGrid(grid: Grid): Pair<Int, Int> {
        refreshWindowImage()
        val hintBoard = makeBoard()
        for (row in 1..board.size - 2) {
            for (column in 1..board.size - 2) {
                if (hintBoard.gridBoard[row][column] == grid) {
                    return Pair(row, column)
                }
            }
        }

        return Pair(0, 0)
    }

    /**
     * 引数の画像をOpenCVで判別し、一致したGridを返す
     * @param image 判別したいマスの画像
     */
    private fun determineGridWithOpenCV(image: BufferedImage): Grid {
        var result = Grid.Unknown
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

        return if (score > 0.97) true
        else false
    }
}