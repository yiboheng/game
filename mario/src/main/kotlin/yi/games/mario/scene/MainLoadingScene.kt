package yi.games.mario.scene

import com.almasb.fxgl.animation.Interpolators
import com.almasb.fxgl.app.scene.LoadingScene
import com.almasb.fxgl.dsl.*
import com.almasb.fxgl.dsl.FXGL.Companion.centerText
import javafx.geometry.Rectangle2D
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration


class MainLoadingScene: LoadingScene() {
    init {
        val appWidth = getAppWidth().toDouble()
        val appHeight = getAppHeight().toDouble()
        val bg = Rectangle(appWidth, appHeight, Color.AZURE)
        val text = getUIFactoryService().newText("Loading level", Color.BLACK, 46.0)
        centerText(text, appWidth / 2, appHeight / 3 + 25)
        val hBox = HBox(5.0)
        for (i in 0..2) {
            val textDot = getUIFactoryService().newText(".", Color.BLACK, 46.0)
            hBox.children.add(textDot)
            animationBuilder(this)
                .autoReverse(true)
                .delay(Duration.seconds(i * 0.5))
                .repeatInfinitely()
                .fadeIn(textDot)
                .buildAndPlay()
        }
        hBox.translateX = (appWidth / 2 - 20)
        hBox.translateY = (appHeight / 2)
        val texture = texture("player.png")
            .subTexture(Rectangle2D(0.0, 0.0, 32.0, 42.0))
        texture.translateX = appWidth / 2 - 32 / 2
        texture.translateY = appHeight / 2 - 42 / 2
        animationBuilder(this)
            .duration(Duration.seconds(1.25))
            .repeatInfinitely()
            .autoReverse(true)
            .interpolator(Interpolators.EXPONENTIAL.EASE_IN_OUT())
            .rotate(texture)
            .from(0.0)
            .to(360.0)
            .buildAndPlay()
        contentRoot.children.setAll(bg, text, hBox, texture)
    }
}
