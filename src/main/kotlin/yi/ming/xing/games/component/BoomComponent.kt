package yi.ming.xing.games.component

import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.components.ExpireCleanComponent
import com.almasb.fxgl.dsl.play
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.texture.AnimatedTexture
import com.almasb.fxgl.texture.AnimationChannel
import javafx.util.Duration


class BoomComponent:Component() {
    private var animaBoom: AnimationChannel = AnimationChannel(FXGL.image("boom2.png"), 6, 40, 40, Duration.seconds(0.5), 1, 5)
    private var animaEmpty: AnimationChannel = AnimationChannel(FXGL.image("boom2.png"), 6, 40, 40, Duration.seconds(0.1), 0, 0)
    private var animaTexture: AnimatedTexture = AnimatedTexture(animaEmpty)

    init {
    }

    override fun onAdded() {
        entity.viewComponent.addChild(animaTexture)
        entity.addComponent(ExpireCleanComponent(Duration.seconds(0.5)))
    }

    override fun onUpdate(tpf: Double) {
        if (animaTexture.animationChannel == animaEmpty) {
            play("bulletBoom.wav")
            animaTexture.loopAnimationChannel(animaBoom)
        }
    }

    fun reBoom(){
        animaTexture.loopAnimationChannel(animaEmpty)
    }
}
