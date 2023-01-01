package yi.games.tank.component

import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.newLocalTimer
import com.almasb.fxgl.dsl.play
import com.almasb.fxgl.dsl.spawn
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.physics.PhysicsComponent
import com.almasb.fxgl.texture.AnimatedTexture
import com.almasb.fxgl.texture.AnimationChannel
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import javafx.util.Duration
import yi.games.tank.component.MoveDirection.*
import java.util.*
import java.util.function.Consumer


class TankComponent( var direction: MoveDirection) : Component() {

    private var defaultSpeed = 150.0
    var moveSpeed = 0.0
    private val animaRun = AnimationChannel(FXGL.image("tank.png"), 4, 40, 40, Duration.seconds(1.0), 0, 3)
    private val animaStop = AnimationChannel(FXGL.image("tank.png"), 4, 40, 40, Duration.seconds(1.0), 0, 0)
    private var animaTexture = AnimatedTexture(animaStop)
    private val shootTimer = newLocalTimer()
    private val shootInterval = Duration.seconds(0.5)

    private lateinit var physics : PhysicsComponent
    override fun onAdded() {
        physics = entity.getComponent(PhysicsComponent::class.java)
        entity.viewComponent.addChild(animaTexture)
        entity.transformComponent.scaleOrigin = Point2D(20.0, 20.0)
        entity.transformComponent.rotationOrigin = Point2D(20.0, 20.0)
        physics.setOnPhysicsInitialized {
            rotate(direction,  true)
        }

        val colorOpt = entity.getPropertyOptional<Color>("color")
        if (colorOpt.isPresent) {
            val color = colorOpt.get()
            animaTexture.toColor(color)
        }
    }

    override fun onUpdate(tpf: Double) {
        val normalizedVec2 = physics.linearVelocity.normalize()
        if (normalizedVec2 != direction.vector) {
            return
        }

        val speed = physics.linearVelocity.magnitude()
        if(speed >= 2 && animaTexture.animationChannel == animaStop){
            animaTexture.loopAnimationChannel(animaRun)
        }
        if(speed < 2 && animaTexture.animationChannel == animaRun){
            physics.linearVelocity = Point2D(0.0,0.0)
            animaTexture.loopAnimationChannel(animaStop)
        }
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
        onCollisionFunction?.accept(direction)
    }

    private var onCollisionFunction : Consumer<MoveDirection>? = null

    fun registerOnCollisionFunction(f:Consumer<MoveDirection>){
        onCollisionFunction = f
    }


    fun setSpeed(speed: Double){
        physics.linearVelocity = direction.vector.multiply(speed)
    }

    fun rotate(direction: MoveDirection, forceRotate:Boolean = false){
        this.direction = direction
        physics.overwriteAngle(direction.angle)
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
