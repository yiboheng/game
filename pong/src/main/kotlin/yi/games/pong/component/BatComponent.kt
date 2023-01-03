package yi.games.pong.component

import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.physics.PhysicsComponent

const val BAT_SPEED = 420.0
const val MOVABLE_MIN_Y = BAT_SPEED / 60

open class BatComponent:Component() {

    private var physics: PhysicsComponent? = null
    fun up() {
        if (entity.y >= MOVABLE_MIN_Y) {
            physics?.velocityY = -BAT_SPEED
        } else {
            stop()
        }
    }

    fun down() {
        if (entity.bottomY <= FXGL.getAppHeight() - MOVABLE_MIN_Y) {
            physics?.velocityY = BAT_SPEED
        } else {
            stop()
        }
    }

    fun stop() {
        physics?.setLinearVelocity(0.0, 0.0)
    }
}
