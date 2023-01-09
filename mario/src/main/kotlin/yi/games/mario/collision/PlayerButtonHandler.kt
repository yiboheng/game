package yi.games.mario.collision

import com.almasb.fxgl.dsl.getGameWorld
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.physics.CollisionHandler
import yi.games.mario.entity.EntityType

class PlayerButtonHandler : CollisionHandler(EntityType.PLAYER, EntityType.BUTTON) {
    override fun onCollisionBegin(player: Entity, btn: Entity) {
        val keyEntity: Entity = btn.getObject("keyEntity")
        if (!keyEntity.isActive) {
            keyEntity.setProperty("activated", false)
            getGameWorld().addEntity(keyEntity)
        }
        keyEntity.isVisible = true
    }

    override fun onCollisionEnd(player: Entity, btn: Entity) {
        val keyEntity: Entity = btn.getObject("keyEntity")
        if (!keyEntity.getBoolean("activated")) {
            keyEntity.isVisible = false
        }
    }
}
