package yi.ming.xing.games.component

import com.almasb.fxgl.dsl.FXGL.Companion.getGameWorld
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.entity.state.EntityState
import com.almasb.fxgl.entity.state.StateComponent
import javafx.geometry.Point2D
import yi.ming.xing.games.entity.EntityType
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.math.abs


class TankAIComponent() : Component() {

    private var tankComponent: TankComponent? = null
    private var stateComponent: StateComponent? = null
    var player: Entity? = null

    var avoidState: EntityState? = null
    private var portalState: EntityState? = null


    override fun onAdded() {
        tankComponent = entity.getComponent(TankComponent::class.java)
        tankComponent!!.registerOnCollisionFunction(Consumer { direction ->
            tankComponent!!.rotate(MoveDirection.randomNext(direction),true)
        })
        stateComponent = StateComponent()
        entity.addComponent(stateComponent)

        portalState = PortalState(entity)
        tankComponent!!.moveForward()
    }

    override fun onUpdate(tpf: Double) {

        val currentTankRole = entity.getPropertyOptional<String>("role").get()
        val closestBulletOpt = getGameWorld().getClosestEntity(entity) {
            val targetRoleOpt = it.getPropertyOptional<String>("role")
            if (it.type != EntityType.BULLET || targetRoleOpt.isEmpty) {
                false
            } else {
                targetRoleOpt.get() != currentTankRole
            }
        }
        if (closestBulletOpt.isEmpty) {
            tankComponent!!.moveForward()
        } else {
            val closestBullet = closestBulletOpt.get()
            val distance = entity.distanceBBox(closestBullet)
            val dx = abs(entity.x - closestBullet.x)
            val dy = abs(entity.y - closestBullet.y)
            if(distance < 200 && (dx < 5 || dy < 5)){
                val bulletDirection = closestBullet.getPropertyOptional<MoveDirection>("direction").get()
                val randomAvoidDirection = MoveDirection.randomAvoid(bulletDirection)
                tankComponent!!.rotate(randomAvoidDirection)
            } else {
                tankComponent!!.moveForward()
            }
        }
    }


    class PortalState(val entity: Entity):EntityState(){
        override fun onUpdate(tpf: Double) {
            entity.getComponent(TankComponent::class.java).moveForward()
        }
    }

    class AvoidState(private val currentTankEntity: Entity, private val bulletEntity: Entity):EntityState(){

        override fun onEnteredFrom(prevState: EntityState?) {
            val bulletDirection = bulletEntity.getPropertyOptional<MoveDirection>("direction").get()
            val randomAvoidDirection = MoveDirection.randomAvoid(bulletDirection)
            currentTankEntity.getComponent(TankComponent::class.java).rotate(randomAvoidDirection)
        }

        override fun onUpdate(tpf: Double) {
            currentTankEntity.getComponent(TankComponent::class.java).moveForward()
        }
    }

}
