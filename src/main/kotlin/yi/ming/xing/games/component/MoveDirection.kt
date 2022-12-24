package yi.ming.xing.games.component

import com.almasb.fxgl.core.math.FXGLMath
import javafx.geometry.Point2D

enum class MoveDirection(val vector: Point2D) {

    UP(Point2D(0.0, -1.0)),
    DOWN(Point2D(0.0, 1.0)),
    LEFT(Point2D(-1.0, 0.0)),
    RIGHT(Point2D(1.0, 0.0)), ;

    companion object {
        private val upNextDirections = arrayOf(LEFT, RIGHT, DOWN)
        private val downNextDirections = arrayOf(UP, LEFT, RIGHT)
        private val leftNextDirections = arrayOf(UP, DOWN, LEFT)
        private val rightNextDirections = arrayOf(UP, DOWN, RIGHT)

        private val upAvoidDirections = arrayOf(LEFT, RIGHT)
        private val downAvoidDirections = arrayOf( LEFT, RIGHT)
        private val leftAvoidDirections = arrayOf(UP, DOWN)
        private val rightAvoidDirections = arrayOf(UP, DOWN)

        fun randomNext(i: MoveDirection): MoveDirection {
            return when (i) {
                UP -> FXGLMath.random(upNextDirections).get()
                DOWN -> FXGLMath.random(downNextDirections).get()
                LEFT -> FXGLMath.random(leftNextDirections).get()
                RIGHT -> FXGLMath.random(rightNextDirections).get()
            }
        }

        fun reverse(i: MoveDirection): MoveDirection {
            return when (i) {
                UP -> DOWN
                DOWN -> UP
                LEFT -> RIGHT
                RIGHT -> LEFT
            }
        }

        fun randomAvoid(i: MoveDirection): MoveDirection {
            return when (i) {
                UP -> FXGLMath.random(upAvoidDirections).get()
                DOWN -> FXGLMath.random(downAvoidDirections).get()
                LEFT -> FXGLMath.random(leftAvoidDirections).get()
                RIGHT -> FXGLMath.random(rightAvoidDirections).get()
            }
        }

        /**
         * 判断d方向是否可以躲避target方向
         */
        fun canAvoid(d: MoveDirection, target: MoveDirection):Boolean {
            return when (target) {
                UP -> upAvoidDirections.contains(d)
                DOWN -> downAvoidDirections.contains(d)
                LEFT -> leftAvoidDirections.contains(d)
                RIGHT -> rightAvoidDirections.contains(d)
            }
        }

    }
}
