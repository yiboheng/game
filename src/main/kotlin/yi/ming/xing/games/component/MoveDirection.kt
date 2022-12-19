package yi.ming.xing.games.component

import com.almasb.fxgl.core.math.FXGLMath
import javafx.geometry.Point2D

enum class MoveDirection(val vector: Point2D) {

    UP(Point2D(0.0, -1.0)),
    DOWN(Point2D(0.0, 1.0)),
    LEFT(Point2D(-1.0, 0.0)),
    RIGHT(Point2D(1.0, 0.0)), ;

    companion object {
        fun randomNext(i: MoveDirection): MoveDirection {
            return when (i) {
                UP -> FXGLMath.random(arrayOf(LEFT, RIGHT, DOWN)).get()
                DOWN -> FXGLMath.random(arrayOf(UP, LEFT, RIGHT)).get()
                LEFT -> FXGLMath.random(arrayOf(UP, DOWN, LEFT)).get()
                RIGHT -> FXGLMath.random(arrayOf(UP, DOWN, RIGHT)).get()
            }
        }

        fun randomAvoid(i: MoveDirection): MoveDirection {
            return when (i) {
                UP -> FXGLMath.random(arrayOf(LEFT, RIGHT)).get()
                DOWN -> FXGLMath.random(arrayOf( LEFT, RIGHT)).get()
                LEFT -> FXGLMath.random(arrayOf(UP, DOWN)).get()
                RIGHT -> FXGLMath.random(arrayOf(UP, DOWN)).get()
            }
        }

    }
}
