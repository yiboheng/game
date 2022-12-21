package yi.ming.xing.games.component

import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.dsl.FXGL.Companion.getGameWorld
import com.almasb.fxgl.dsl.entityBuilder
import com.almasb.fxgl.dsl.runOnce
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.entity.components.BoundingBoxComponent
import javafx.geometry.Rectangle2D
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import yi.ming.xing.games.component.MoveDirection.*
import yi.ming.xing.games.entity.EntityType
import java.util.function.Consumer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class TankAIComponent() : Component() {

    private var tankComponent: TankComponent? = null
    var player: Entity? = null



    override fun onAdded() {
        tankComponent = entity.getComponent(TankComponent::class.java)
        tankComponent!!.registerOnCollisionFunction(Consumer { direction ->
            tankComponent!!.rotate(MoveDirection.randomNext(direction),true)
        })
    }

    private var lastAvoidTime = 0L
    private val avoidInterval = 200

    private var lastShootTime = 0L
    private val shootInterval = 200

    private var lastTravelTime = 0L
    private val travelInterval = 200
    override fun onUpdate(tpf: Double) {
        val currentTime = System.currentTimeMillis()

        tankComponent!!.moveForward()
        if (currentTime - lastTravelTime > travelInterval) {
            lastTravelTime = currentTime
            doTravel()
        }

        if (currentTime - lastAvoidTime > avoidInterval) {
            lastAvoidTime = currentTime
            doAvoidDanger()
        }

        if (currentTime - lastShootTime > shootInterval) {
            lastShootTime = currentTime
            doShoot()
        }
    }


    private fun doAvoidDanger(){
        val currentTankBox = entity.boundingBoxComponent
        val currentTankRole = entity.getPropertyOptional<String>("role").get()
        val enemyBulletList = getGameWorld().getEntitiesByType(EntityType.BULLET)
            .filter {
                val bulletRoleOpt = it.getPropertyOptional<String>("role")
                bulletRoleOpt.isPresent && bulletRoleOpt.get() != currentTankRole
            }.filter {
                entity.distanceBBox(it) < 300
            }
            .sortedBy { entity.distanceBBox(it) }

        if (enemyBulletList.isNotEmpty()) {
            val closestBullet = enemyBulletList[0]
            val bulletDirection = closestBullet.getPropertyOptional<MoveDirection>("direction").get()

            val bulletTraceBox = traceBox(
                closestBullet.boundingBoxComponent,
                bulletDirection,
                20,
                300,
                Color.GREEN,
                Duration.seconds(1.0)
            )
            val inDanger = getGameWorld().getEntitiesInRange(bulletTraceBox).any { it == entity }
            if (inDanger) {
                val randomAvoidDirection = MoveDirection.randomAvoid(bulletDirection)
                tankComponent!!.rotate(randomAvoidDirection)
            }
        }
    }

    private fun doShoot(){
        val currentTankBox = entity.boundingBoxComponent
        val currentTankRole = entity.getPropertyOptional<String>("role").get()
        val enemyTankDirections = getGameWorld().getEntitiesByType(EntityType.PLAYER)
            .filter {
                val enemyRoleOpt = it.getPropertyOptional<String>("role")
                enemyRoleOpt.isPresent && enemyRoleOpt.get() != currentTankRole
            }
            .filter{
                val canShoot = (it.x >= currentTankBox.getMinXWorld() && it.x<=currentTankBox.getMaxXWorld())
                        || (it.y >= currentTankBox.getMinYWorld() && it.y <= currentTankBox.getMaxYWorld())
                canShoot
            }
            .map {
                if((it.x >= currentTankBox.getMinXWorld() && it.x<=currentTankBox.getMaxXWorld())){
                    if(it.y < currentTankBox.getMinYWorld()){
                        MoveDirection.DOWN
                    } else {
                        UP
                    }
                }
                if ((it.y >= currentTankBox.getMinYWorld() && it.y <= currentTankBox.getMaxYWorld())){
                    if(it.x < currentTankBox.getMinXWorld()){
                        MoveDirection.RIGHT
                    } else {
                        MoveDirection.LEFT
                    }
                }
                tankComponent!!.direction
            }

        if (enemyTankDirections.isNotEmpty()) {
            tankComponent!!.rotate(enemyTankDirections[0])
            tankComponent!!.shoot()
        }
    }

    private fun doTravel() {
        val tank = tankComponent!!
        val tankBox = tank.entity.boundingBoxComponent
        val randomDirection = MoveDirection.randomAvoid(tank.direction)
        val traceBox = traceBox(tankBox, randomDirection, 40, 300, Color.RED, Duration.seconds(1.0))
        val entitiesInRange = getGameWorld().getEntitiesInRange(traceBox)
        val entityCount = entitiesInRange.count { it != tank.entity }
        if (entityCount == 0 && FXGLMath.randomBoolean()) {
            tank.move(randomDirection)
        }
    }

    private fun sniffAround() {
        val tank = tankComponent!!
        val tankBox = entity.boundingBoxComponent
        val tankMoveDirection = tank.direction

        val upTraceRange = traceBox(tankBox, UP, 40, 300, Color.GREEN, Duration.seconds(1.0))
        val rightTraceRange = traceBox(tankBox, RIGHT, 40, 300, Color.GREEN, Duration.seconds(1.0))
        val downTraceRange = traceBox(tankBox, DOWN, 40, 300, Color.GREEN, Duration.seconds(1.0))
        val leftTraceRange = traceBox(tankBox, LEFT, 40, 300, Color.GREEN, Duration.seconds(1.0))

        getGameWorld().getEntitiesInRange(upTraceRange)
    }

    private fun sniffResult(){

    }

    data class SniffResult(val hasEnemy:Boolean, val dangerIn)

    private fun traceBox(targetBox : BoundingBoxComponent, direction:MoveDirection, width: Int, height:Int, color: Color, cleanTime: Duration)
    : Rectangle2D{
        val minSide = min(width, height)
        val maxSide = max(width, height)
        val vector = direction.vector
        val width = abs(vector.x * (maxSide - minSide)) + minSide
        val height = abs(vector.y * (maxSide - minSide)) + minSide
        var findX = 0.0
        var findY = 0.0

        when (direction) {
            UP -> {
                findX = targetBox.getMinXWorld()
                findY = targetBox.getMinYWorld() - height - 1
            }
            DOWN -> {
                findX = targetBox.getMinXWorld()
                findY = targetBox.getMaxYWorld() + 1
            }
            LEFT -> {
                findX = targetBox.getMinXWorld() - width -1
                findY = targetBox.getMinYWorld()
            }
            RIGHT -> {
                findX = targetBox.getMaxXWorld() + 1
                findY = targetBox.getMinYWorld()
            }
        }

        val rectangle = Rectangle(width, height, Color.TRANSPARENT)
        val markBox = if(direction == UP || direction == DOWN) {VBox(rectangle)} else {HBox(rectangle)}
        markBox.border = Border(BorderStroke(color, BorderStrokeStyle.SOLID, null, BorderWidths.DEFAULT))
        val marker = entityBuilder()
            .at(findX, findY)
            .view(markBox)
            .buildAndAttach()
        runOnce({ marker.removeFromWorld()}, cleanTime)
        return Rectangle2D(findX, findY, width, height)
    }

}
