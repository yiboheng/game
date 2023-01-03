package yi.games.pong.component

import com.almasb.fxgl.core.math.FXGLMath.abs
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.getAppHeight
import com.almasb.fxgl.dsl.getAppWidth
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.physics.PhysicsComponent
import javafx.geometry.Point2D
import java.lang.Math.*
import kotlin.math.sign


class BallComponent:Component() {

    private val physics: PhysicsComponent? = null
    override fun onUpdate(tpf: Double) {
        limitVelocity()
        checkOffscreen()
    }

    private fun limitVelocity() {
        // don't move too slow in X direction
        if (abs(physics!!.velocityX) < 5 * 60) {
            physics.velocityX = sign(physics.velocityX) * 5 * 60
        }
        // don't move too fast in Y direction
        if (abs(physics.velocityY) > 5 * 60 * 2) {
            physics.velocityY = sign(physics.velocityY) * 5 * 60
        }
    }

    // we use a physics engine, so it is possible to push the ball through a wall to outside of the screen, hence the check
    private fun checkOffscreen() {
        val viewport = FXGL.getGameScene().viewport
        val visArea = viewport.visibleArea
        if (entity.boundingBoxComponent.isOutside(visArea)) {
            physics!!.overwritePosition(Point2D(getAppWidth() / 2.0, getAppHeight() / 2.0))
        }
    }
}
