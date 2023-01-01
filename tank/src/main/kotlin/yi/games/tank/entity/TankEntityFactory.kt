package yi.games.tank.entity

import com.almasb.fxgl.dsl.EntityBuilder
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.components.HealthIntComponent
import com.almasb.fxgl.dsl.entityBuilder
import com.almasb.fxgl.dsl.texture
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.EntityFactory
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.Spawns
import com.almasb.fxgl.physics.BoundingShape
import com.almasb.fxgl.physics.PhysicsComponent
import com.almasb.fxgl.physics.box2d.dynamics.BodyDef
import com.almasb.fxgl.physics.box2d.dynamics.BodyType
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef
import javafx.scene.effect.BlendMode.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import yi.games.tank.component.BoomComponent
import yi.games.tank.component.MoveDirection.RIGHT
import yi.games.tank.component.MoveDirection.UP
import yi.games.tank.component.OilBagComponent
import yi.games.tank.component.TankComponent
import yi.games.tank.entity.EntityType.*

class TankEntityFactory:EntityFactory {

    private val tankBboxWidth = 40.0
    private val tankBboxHeight = 40.0

    @Spawns("player")
    fun newPlayer(data: SpawnData): Entity {
        return newTank(data)
            .type(PLAYER)
            .with("role", "player")
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
            .bbox(BoundingShape.box(16.0, 5.0))
            .collidable()
            .with(PhysicsComponent().also {
                it.setBodyDef(BodyDef().also { bodyDef ->
                    bodyDef.isBullet = true
                    bodyDef.type = BodyType.DYNAMIC
                    bodyDef.linearDamping = 0.2f
                    bodyDef.angularDamping = 0.2f
                    bodyDef.isFixedRotation = true
                })
                it.setFixtureDef(FixtureDef().also { fixtureDef ->
                    fixtureDef.density = 1000f
                    fixtureDef.friction = 0.1f
                })
                it.setOnPhysicsInitialized {
                    it.linearVelocity = direction.vector.multiply(1000.0)
                    it.overwriteAngle(direction.angle)
                }
            })
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
            .with(PhysicsComponent())
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
            .with(PhysicsComponent().also {
                it.setFixtureDef(FixtureDef().also {fixtureDef ->
                    fixtureDef.density = 1000f
                    fixtureDef.friction = 0.7f
                })
                it.setBodyDef(BodyDef().also {bodyDef ->
                    bodyDef.type = BodyType.DYNAMIC
                    bodyDef.linearDamping = 2f
                    bodyDef.angularDamping = 0.2f
                    bodyDef.isFixedRotation = true
                })
            })
            .with(TankComponent(direction))
            .collidable()
            .with(HealthIntComponent(health))
    }

}
