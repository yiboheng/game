package yi.ming.xing.games.component

import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.newLocalTimer
import com.almasb.fxgl.dsl.play
import com.almasb.fxgl.dsl.spawn
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.texture.AnimatedTexture
import com.almasb.fxgl.texture.AnimationChannel
import javafx.geometry.Point2D
import javafx.util.Duration
import yi.ming.xing.games.component.MoveDirection.*
import java.util.Optional
import java.util.function.Consumer
import kotlin.math.abs


class TankComponent( var direction: MoveDirection) : Component() {

    private var defaultSpeed: Int = 150
    private var moveSpeed = 0

    private val animaRun = AnimationChannel(FXGL.image("tank.png"), 4, 40, 40, Duration.seconds(1.0), 0, 3)
    private val animaStop = AnimationChannel(FXGL.image("tank.png"), 4, 40, 40, Duration.seconds(1.0), 0, 0)
    private var animaTexture = AnimatedTexture(animaStop)
    private val shootTimer = newLocalTimer()
    private val shootInterval = Duration.seconds(0.5)
    init {

    }

    override fun onAdded() {
        entity.viewComponent.addChild(animaTexture)
        entity.transformComponent.scaleOrigin = Point2D(20.0, 20.0)
        entity.transformComponent.rotationOrigin = Point2D(20.0, 20.0)
        rotate(direction,  true)
    }

    override fun onUpdate(tpf: Double) {

        val dx = direction.vector.x * moveSpeed * tpf
        val dy = direction.vector.y * moveSpeed * tpf
        val absDx = abs(dx)
        val absDy = abs(dy)

        if (absDx >= 1) {
            entity.translateX(dx)
        }
        if (absDy >= 1) {
            entity.translateY(dy)
        }

        if ((absDx >= 1 || absDy >= 1) && animaTexture.animationChannel == animaStop) {
            animaTexture.loopAnimationChannel(animaRun)
        }
        if ((absDx < 1 && absDy < 1) && animaTexture.animationChannel == animaRun) {
            animaTexture.loopAnimationChannel(animaStop)
        }

        moveSpeed = (moveSpeed * 0.8).toInt()
    }

    fun moveRight() {
        move(RIGHT)
    }

    fun moveLeft() {
        move(LEFT)
    }

    fun moveDown() {
        move(DOWN)
    }

    fun moveUp() {
        move(UP)
    }

    fun moveForward() {
        move(direction)
    }

    fun move(direction: MoveDirection){
        rotate(direction)
        setSpeed(defaultSpeed)
    }

    fun onCollision(other: Entity) {
        setSpeed(0)

        val distance = entity.distanceBBox(other)
        if(distance < 1){
            val backX = -direction.vector.x*5
            val backY = -direction.vector.y*5
            println("stop back direction = $backX, $backY")
            onCollisionFunction?.accept(direction)
            entity.translate(backX, backY)
        }
    }

    private var onCollisionFunction : Consumer<MoveDirection>? = null

    fun registerOnCollisionFunction(f:Consumer<MoveDirection>){
        onCollisionFunction = f
    }

    fun setSpeed(speed: Int){
        moveSpeed = speed
    }

    fun rotate(direction: MoveDirection, forceRotate:Boolean = false){
        if (forceRotate || this.direction != direction) {
            this.direction = direction
            entity.rotateToVector(direction.vector)
        }
    }


    fun shoot(): Optional<Entity> {
        if( !shootTimer.elapsed(shootInterval)){
            return Optional.empty()
        }
        shootTimer.capture()
        play("tankFire.wav")
        var x = entity.position.x
        var y = entity.position.y
        when(direction){
            UP -> {x +=17; y -=15}
            DOWN -> {x +=17; y+=50}
            LEFT -> {y +=17; x-=15}
            RIGHT -> {x +=50; y +=17}
        }
        val bullet = spawn("bullet", SpawnData(x, y).also {
            it.put("direction", direction)
            it.put("speed", 400)
            it.put("damage", 1)
            it.put("role", entity.getPropertyOptional<String>("role").get())
        })
        return Optional.of(bullet)
    }
}
