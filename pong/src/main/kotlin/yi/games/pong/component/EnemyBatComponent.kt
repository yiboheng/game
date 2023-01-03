package yi.games.pong.component

import com.almasb.fxgl.entity.Entity
import yi.games.pong.entity.EntityType

class EnemyBatComponent:BatComponent() {
    private var ball: Entity? = null
    override fun onUpdate(tpf: Double) {
        if (ball == null) {
            ball = entity.world
                .getSingletonOptional(EntityType.BALL)
                .orElse(null)
        } else {
            moveAI()
        }
    }

    private fun moveAI() {
        val bat = entity
        val isBallToLeft: Boolean = ball!!.rightX <= bat.x
        if (ball!!.y < bat.y) {
            if (isBallToLeft) up() else down()
        } else if (ball!!.y > bat.y) {
            if (isBallToLeft) down() else up()
        } else {
            stop()
        }
    }
}
