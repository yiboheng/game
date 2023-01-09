package yi.games.mario.component

import com.almasb.fxgl.dsl.image
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.physics.PhysicsComponent
import com.almasb.fxgl.texture.AnimatedTexture
import com.almasb.fxgl.texture.AnimationChannel
import javafx.beans.value.ObservableValue
import javafx.geometry.Point2D
import javafx.util.Duration


class PlayerComponent:Component() {
    private var physics: PhysicsComponent? = null
    private lateinit var texture: AnimatedTexture
    private lateinit var animIdle: AnimationChannel
    private lateinit var  animWalk: AnimationChannel
    private var jumps = 2
    fun PlayerComponent() {
        val image = image("player.png")
        animIdle = AnimationChannel(image, 4, 32, 42, Duration.seconds(1.0), 1, 1)
        animWalk = AnimationChannel(image, 4, 32, 42, Duration.seconds(0.66), 0, 3)
        texture = AnimatedTexture(animIdle)
        texture.loop()
    }

    override fun onAdded() {
        entity.transformComponent.scaleOrigin = Point2D(16.0, 21.0)
        entity.viewComponent.addChild(texture)
        physics!!.onGroundProperty()
            .addListener { _: ObservableValue<out Boolean>?, _: Boolean?, isOnGround: Boolean ->
                if (isOnGround) {
                    jumps = 2
                }
            }
    }

    override fun onUpdate(tpf: Double) {
        if (physics!!.isMovingX) {
            if (texture.animationChannel != animWalk) {
                texture.loopAnimationChannel(animWalk)
            }
        } else {
            if (texture.animationChannel != animIdle) {
                texture.loopAnimationChannel(animIdle)
            }
        }
    }

    fun left() {
        getEntity().scaleX = -1.0
        physics!!.velocityX = -170.0
    }

    fun right() {
        getEntity().scaleX = 1.0
        physics!!.velocityX = 170.0
    }

    fun stop() {
        physics!!.velocityX = 0.0
    }

    fun jump() {
        if (jumps == 0) return
        physics!!.velocityY = -300.0
        jumps--
    }
}
