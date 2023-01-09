package yi.games.mario.scene

import com.almasb.fxgl.animation.Interpolators
import com.almasb.fxgl.dsl.*
import com.almasb.fxgl.dsl.FXGL.Companion.getSceneService
import com.almasb.fxgl.input.UserAction
import com.almasb.fxgl.scene.SubScene
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Insets
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.util.Duration


class LevelEndScene: SubScene() {
    private val width = 400.0
    private val height = 250.0
    private val textUserTime = getUIFactoryService().newText("", Color.WHITE, 24.0)
    private val gradeBox = HBox()
    private val levelFont = getAssetLoader().loadFont("level_font.ttf")
    private val isAnimationDone: BooleanProperty = SimpleBooleanProperty(false)
    init {
        val bg = Rectangle(width, height, Color.color(0.0, 0.0, 0.0, 0.85))
        bg.stroke = Color.BLUE
        bg.strokeWidth = 1.75
        bg.effect = DropShadow(28.0, Color.color(0.0, 0.0, 0.0, 0.9))
        VBox.setVgrow(gradeBox, Priority.ALWAYS)
        val textContinue = getUIFactoryService().newText("Tap to continue", Color.WHITE, 11.0)
        textContinue.visibleProperty().bind(isAnimationDone)
        animationBuilder(this)
            .repeatInfinitely()
            .autoReverse(true)
            .scale(textContinue)
            .from(Point2D(1.0, 1.0))
            .to(Point2D(1.25, 1.25))
            .buildAndPlay()
        val vbox = VBox(15.0, textUserTime, gradeBox, textContinue)
        vbox.alignment = Pos.CENTER
        vbox.padding = Insets(25.0)
        val root = StackPane(
            bg, vbox
        )
        root.translateX = (1280 / 2 - width / 2)
        root.translateY = (720 / 2 - height / 2)
        val textLevel = Text()
        textLevel.textProperty().bind(getip("level").asString("Level %d"))
        textLevel.font = levelFont.newFont(52.0)
        textLevel.rotate = -20.0
        textLevel.fill = Color.ORANGE
        textLevel.stroke = Color.BLACK
        textLevel.strokeWidth = 3.5
        textLevel.translateX = root.translateX - textLevel.layoutBounds.width / 3
        textLevel.translateY = root.translateY + 25
        contentRoot.children.addAll(root, textLevel)
        input.addAction(object : UserAction("Close Level End Screen") {
            override fun onActionBegin() {
                if (!isAnimationDone.value) return
                getSceneService().popSubScene()
            }
        }, MouseButton.PRIMARY)
    }

    fun onLevelFinish() {
        isAnimationDone.value = false
        val userTime = Duration.seconds(getd("levelTime"))
        val timeData: LevelTimeData = geto("levelTimeData")
        textUserTime.text = String.format("Your time: %.2f sec!", userTime.toSeconds())
        gradeBox.children.setAll(
            Grade(Duration.seconds(timeData.star1), userTime),
            Grade(Duration.seconds(timeData.star2), userTime),
            Grade(Duration.seconds(timeData.star3), userTime)
        )
        for (i in gradeBox.children.indices) {
            var builder = animationBuilder(this)
                .delay(Duration.seconds(i * 0.75))
                .duration(Duration.seconds(0.75))
                .interpolator(Interpolators.ELASTIC.EASE_OUT())
            // if last star animation
            if (i == gradeBox.children.size - 1) {
                builder = builder.onFinished { isAnimationDone.value = true }
            }
            builder.translate(gradeBox.children[i])
                .from(Point2D(0.0, -500.0))
                .to(Point2D(0.0, 0.0))
                .buildAndPlay()
        }
        getSceneService().pushSubScene(this)
    }

    class Grade(gradeTime: Duration, userTime: Duration) : VBox(15.0) {
        companion object {
            private val STAR_EMPTY = texture("star_empty.png", 65.0, 72.0).darker()
            private val STAR_FULL = texture("star_full.png", 65.0, 72.0)
        }
        init {
            HBox.setHgrow(this, Priority.ALWAYS)
            alignment = Pos.CENTER
            children.add(if (userTime.lessThanOrEqualTo(gradeTime)) STAR_FULL.copy() else STAR_EMPTY.copy())
            children.add(
                getUIFactoryService().newText(
                    String.format("<%.2f", gradeTime.toSeconds()),
                    Color.WHITE,
                    16.0
                )
            )
        }
    }

    class LevelTimeData
    /**
     * @param star1 in seconds
     * @param star2 in seconds
     * @param star3 in seconds
     */( val star1: Double, val star2: Double, val star3: Double)
}
