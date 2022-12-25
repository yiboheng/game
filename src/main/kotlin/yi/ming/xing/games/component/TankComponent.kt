package yi.ming.xing.games.component

import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.newLocalTimer
import com.almasb.fxgl.dsl.play
import com.almasb.fxgl.dsl.spawn
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.physics.PhysicsComponent
import com.almasb.fxgl.physics.box2d.dynamics.BodyType
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef
import com.almasb.fxgl.texture.AnimatedTexture
import com.almasb.fxgl.texture.AnimationChannel
import javafx.geometry.Point2D
import javafx.util.Duration
import yi.ming.xing.games.component.MoveDirection.*
import yi.ming.xing.games.entity.EntityType
import java.util.Optional
import java.util.function.Consumer
import kotlin.math.abs


class TankComponent( var direction: MoveDirection) : Component() {

    private var defaultSpeed: Int = 150
    var moveSpeed = 0
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
        updateInternal1(tpf)
    }

    private fun updateInternal1(tpf: Double) {
        val vx = direction.vector.x
        val vy = direction.vector.y

        val dx = vx * moveSpeed * tpf
        val dy = vy * moveSpeed * tpf
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
        val distance = entity.distanceBBox(other)
        if(distance < 1){
            val forwardX = direction.vector.x
            val forwardY = direction.vector.y
            if (other.type == EntityType.PLAYER) {
                val e1 = entity.getPropertyOptional<String>("name").get()
                val e2 = other.getPropertyOptional<String>("name").get()
                val otherTank = other.getComponent(TankComponent::class.java)
                val otherTankDirection = otherTank.direction
                val otherTankSpeed = otherTank.moveSpeed
                val knockX = otherTankDirection.vector.x
                val knockY = otherTankDirection.vector.y
                println(" ==== tank knock tank ==== ")
                println("$e1 knock $e2 , $e1 tank direction = $direction, speed = $moveSpeed ; $e2 tank direction = $otherTankDirection, speed=$otherTankSpeed")
                val factor = 3
                if(moveSpeed < 1 && otherTankSpeed > 1){
                    println("case 1")
                    entity.translate(knockX*factor, knockY*factor)
                    other.translate(-knockX*factor, -knockY*factor)
                } else if (moveSpeed > 1 && otherTankSpeed < 1) {
                    println("case 2")
                    entity.translate(-forwardX*factor, -forwardY*factor)
                    other.translate(forwardX*factor, forwardY*factor)
                } else if (moveSpeed > 1 && otherTankSpeed > 1){
                    println("case 3")
                    entity.translate((knockX+forwardX)*factor, (knockY+forwardY)*factor)
                    other.translate((knockX+forwardX)*factor, (knockY+forwardY)*factor)
                }
                println(" ========= ")
                otherTank.setSpeed(0)
                onCollisionFunction?.accept(direction)
            } else {
                entity.translate(-forwardX*5, -forwardY*5)
                onCollisionFunction?.accept(direction)
            }
        }
        setSpeed(0)
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
