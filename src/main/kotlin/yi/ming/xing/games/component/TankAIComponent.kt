package yi.ming.xing.games.component

import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.dsl.FXGL.Companion.getGameWorld
import com.almasb.fxgl.dsl.FXGL.Companion.run
import com.almasb.fxgl.dsl.components.ProjectileComponent
import com.almasb.fxgl.dsl.entityBuilder
import com.almasb.fxgl.dsl.getGameScene
import com.almasb.fxgl.dsl.runOnce
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.entity.state.EntityState
import com.almasb.fxgl.entity.state.StateComponent
import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.util.Duration
import yi.ming.xing.games.component.MoveDirection.*
import yi.ming.xing.games.entity.EntityType
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.math.abs


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
            }
            .filter{
                val dangerous = (it.x >= currentTankBox.getMinXWorld() && it.x<=currentTankBox.getMaxXWorld())
                        || (it.y >= currentTankBox.getMinYWorld() && it.y <= currentTankBox.getMaxYWorld())
                dangerous && entity.distanceBBox(it) < 300
            }
            .sortedBy { entity.distanceBBox(it) }

        if (enemyBulletList.isNotEmpty()) {
            val closestBullet = enemyBulletList[0]
            val bulletDirection = closestBullet.getPropertyOptional<MoveDirection>("direction").get()
            val randomAvoidDirection = MoveDirection.randomAvoid(bulletDirection)
            tankComponent!!.rotate(randomAvoidDirection)
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
        val vector = randomDirection.vector
        val width = abs(vector.x * 160) + 40
        val height = abs(vector.y * 160) + 40
        var findX = 0.0
        var findY = 0.0
        when (randomDirection) {
            UP -> {
                findX = tankBox.getMinXWorld()
                findY = tankBox.getMinYWorld() - height - 1
            }
            DOWN -> {
                findX = tankBox.getMinXWorld()
                findY = tankBox.getMaxYWorld() + 1
            }
            LEFT -> {
                findX = tankBox.getMinXWorld() - width -1
                findY = tankBox.getMinYWorld()
            }
            RIGHT -> {
                findX = tankBox.getMaxXWorld() + 1
                findY = tankBox.getMinYWorld()
            }
        }
        val findRectangle = Rectangle2D(findX, findY,  width, height,)
        val entitiesInRange = getGameWorld().getEntitiesInRange(findRectangle)
        val entityCount = entitiesInRange.count { it != tank.entity }
        if (entityCount == 0 && FXGLMath.randomBoolean()) {
            tank.move(randomDirection)
        }

        val rectangle = Rectangle(width, height, Color.TRANSPARENT)
        val markBox = VBox(rectangle, Text("$randomDirection"), Text("$entityCount"))
        markBox.border = Border(BorderStroke(Color.RED, BorderStrokeStyle.SOLID, null, BorderWidths.DEFAULT))
        val marker = entityBuilder()
            .at(findX, findY)
            .view(markBox)
            .buildAndAttach()
        runOnce({ marker.removeFromWorld()}, Duration.seconds(1.0))
    }

}
