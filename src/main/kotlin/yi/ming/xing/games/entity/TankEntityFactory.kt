package yi.ming.xing.games.entity

import com.almasb.fxgl.dsl.EntityBuilder
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.components.HealthIntComponent
import com.almasb.fxgl.dsl.components.ProjectileComponent
import com.almasb.fxgl.dsl.entityBuilder
import com.almasb.fxgl.dsl.texture
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.EntityFactory
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.Spawns
import com.almasb.fxgl.entity.action.ActionComponent
import com.almasb.fxgl.entity.state.StateComponent
import com.almasb.fxgl.physics.BoundingShape
import com.almasb.fxgl.physics.HitBox
import com.almasb.fxgl.physics.PhysicsComponent
import javafx.geometry.Point2D
import javafx.scene.effect.BlendMode
import javafx.scene.effect.BlendMode.COLOR_BURN
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import yi.ming.xing.games.entity.EntityType.*
import yi.ming.xing.games.component.BoomComponent
import yi.ming.xing.games.component.MoveDirection.RIGHT
import yi.ming.xing.games.component.MoveDirection.UP
import yi.ming.xing.games.component.OilBagComponent
import yi.ming.xing.games.component.TankComponent

class TankEntityFactory:EntityFactory {

    private val tankBboxWidth = 40.0
    private val tankBboxHeight = 40.0

    @Spawns("player")
    fun newPlayer(data: SpawnData): Entity {
        return newTank(data)
            .type(PLAYER)
            .with("role", "player")
            .view(Rectangle(tankBboxWidth, tankBboxHeight, data.get("color")).also { it.blendMode = COLOR_BURN })
            .with("name", data.get("name"))
            .build()
    }

    @Spawns("enemy")
    fun newEnemy(data: SpawnData): Entity {
        return newTank(data)
            .type(PLAYER)
            .view(Rectangle(tankBboxWidth, tankBboxHeight, Color.RED).also { it.blendMode = COLOR_BURN })
            .with("role", "enemy")
            .build()
    }

    @Spawns("bullet")
    fun newBullet(data: SpawnData): Entity {
        var direction = RIGHT
        var speed = 50.0
        var damage = 1
        var role = "enemy"
        if(data.hasKey("direction")){
            direction = data.get("direction")
        }
        if(data.hasKey("speed")){
            speed = data.get("speed")
        }
        if(data.hasKey("damage")){
            damage = data.get("damage")
        }
        if(data.hasKey("role")){
            role = data.get("role")
        }

        return entityBuilder(data)
            .type(BULLET)
            .view("bullet.png")
            .viewWithBBox(Rectangle(1.0,1.0, Color.TRANSPARENT))
            .collidable()
            .with(ProjectileComponent(direction.vector, speed))
            .with("damage", damage)
            .with("role", role)
            .build()
    }

    @Spawns("boom")
    fun newBoom(data: SpawnData): Entity {
        return entityBuilder(data)
            .at(data.x-18, data.y-20)
            .with(BoomComponent())
            .build()
    }

    @Spawns("oilBoom")
    fun newOilBoom(data: SpawnData): Entity {
        return entityBuilder(data)
            .type(OIL)
            .viewWithBBox(Rectangle(60.0, 60.0, Color.TRANSPARENT))
            .collidable()
            .with(OilBagComponent())
            .build()
    }

    @Spawns("brick")
    fun newBrick(data: SpawnData): Entity {
        return entityBuilder(data)
            .type(BRICK)
            .viewWithBBox(texture("brick.png", 40.0,40.0))
            .opacity(0.5)
            .collidable()
            .build()
    }

    @Spawns("edge")
    fun newEdge(data: SpawnData): Entity {
        return entityBuilder(data)
            .type(EDGE)
            .viewWithBBox(texture("edge.png", 20.0, 20.0))
            .collidable()
            .build()
    }


    private fun newTank(data: SpawnData): EntityBuilder {
        var direction = UP
        var health = 1
        if (data.hasKey("direction")) {
            direction = data.get("direction")
        }
        if (data.hasKey("health")) {
            health = data.get("health")
        }

        return FXGL.entityBuilder(data)
            .bbox(BoundingShape.box(tankBboxWidth, tankBboxHeight))
            .collidable()
            .with(TankComponent(direction))
            .with(HealthIntComponent(health))
    }

}
