package yi.games.tank.component

import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.texture.AnimatedTexture
import com.almasb.fxgl.texture.AnimationChannel
import javafx.geometry.Point2D
import javafx.util.Duration


class AnimationComponent:Component() {

    private var speed = 0

    private var texture: AnimatedTexture? = null
    private var animIdle: AnimationChannel? = null
    private  var animWalk:AnimationChannel? = null

    init {
        animIdle = AnimationChannel(FXGL.image("newdude.png"), 4, 32, 42, Duration.seconds(1.0), 1, 1)
        animWalk = AnimationChannel(FXGL.image("newdude.png"), 4, 32, 42, Duration.seconds(1.0), 0, 3)
        texture = AnimatedTexture(animIdle!!)
    }

    override fun onAdded() {
        entity.transformComponent.scaleOrigin = Point2D(16.0, 21.0)
        entity.viewComponent.addChild(texture!!)
    }

    override fun onUpdate(tpf: Double) {
        entity.translateX(speed * tpf)
        if (speed != 0) {
            if (texture!!.animationChannel == animIdle) {
                texture!!.loopAnimationChannel(animWalk!!)
            }
            speed = (speed * 0.9).toInt()
            if (FXGLMath.abs(speed.toFloat()) < 1) {
                speed = 0
                texture!!.loopAnimationChannel(animIdle!!)
            }
        }
    }


    fun moveRight() {
        speed = 150
        getEntity().scaleX = 1.0
    }

    fun moveLeft() {
        speed = -150
        getEntity().scaleX = -1.0
    }

    var isLeft = false;
    fun moveSelf( x1:Int, x2:Int){

        if(isLeft){
            moveLeft()
        } else {
            moveRight()
        }

        if(getEntity().x > x2){
            isLeft = true
        } else if (getEntity().x < x1) {
            isLeft = false
        }
    }
}
