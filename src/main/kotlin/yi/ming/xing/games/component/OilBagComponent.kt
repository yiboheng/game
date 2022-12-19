package yi.ming.xing.games.component

import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.components.ExpireCleanComponent
import com.almasb.fxgl.dsl.play
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.texture.AnimatedTexture
import com.almasb.fxgl.texture.AnimationChannel
import javafx.util.Duration


class OilBagComponent:Component() {

    private var animaBoom: AnimationChannel = AnimationChannel(FXGL.image("oil2.png"), 6, 60, 60, Duration.seconds(1.0), 1, 5)
    private var animaEmpty: AnimationChannel = AnimationChannel(FXGL.image("oil2.png"), 6, 60, 60, Duration.seconds(0.5), 0, 0)
    private var animaTexture: AnimatedTexture = AnimatedTexture(animaEmpty)

    private var boom = false

    init {
    }

    override fun onAdded() {
        entity.viewComponent.addChild(animaTexture)

    }

    override fun onUpdate(tpf: Double) {
        if(boom && animaTexture.animationChannel == animaEmpty){
            play("oilBoom.wav")
            animaTexture.loopAnimationChannel(animaBoom)
        }
    }

    fun boomAndClean(){
        boom = true
        entity.addComponent(ExpireCleanComponent(Duration.seconds(1.0)))
    }
}
