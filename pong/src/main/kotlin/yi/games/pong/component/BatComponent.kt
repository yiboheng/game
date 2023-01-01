package yi.games.pong.component

import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.physics.PhysicsComponent


class BatComponent:Component() {
    private val BAT_SPEED = 420.0
    private var physics: PhysicsComponent? = null
    fun up() {
        if (entity.y >= BAT_SPEED / 60) physics!!.velocityY = -BAT_SPEED else stop()
    }

    fun down() {
        if (entity.bottomY <= FXGL.getAppHeight() - BAT_SPEED / 60) physics!!.velocityY = BAT_SPEED else stop()
    }

    fun stop() {
        physics!!.setLinearVelocity(0.0, 0.0)
    }
}
