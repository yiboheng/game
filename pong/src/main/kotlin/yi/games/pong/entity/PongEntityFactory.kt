package yi.games.pong.entity

import com.almasb.fxgl.dsl.entityBuilder
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.EntityFactory
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.Spawns
import com.almasb.fxgl.entity.components.CollidableComponent
import com.almasb.fxgl.physics.BoundingShape
import com.almasb.fxgl.physics.HitBox
import com.almasb.fxgl.physics.PhysicsComponent
import com.almasb.fxgl.physics.box2d.dynamics.BodyType
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import yi.games.pong.component.BallComponent
import yi.games.pong.component.BatComponent
import yi.games.pong.component.EnemyBatComponent


class PongEntityFactory:EntityFactory {

    @Spawns("ball")
    fun newBall(data: SpawnData): Entity {
        val physics = PhysicsComponent()
        physics.setBodyType(BodyType.DYNAMIC)
        physics.setFixtureDef(FixtureDef().density(0.3f).restitution(1.0f))
        physics.setOnPhysicsInitialized { physics.setLinearVelocity((5 * 60).toDouble(), (-5 * 60).toDouble()) }
        return entityBuilder(data)
            .type(EntityType.BALL)
            .view(Circle(5.0, 5.0, 5.0))
            .bbox(HitBox(BoundingShape.circle(5.0)))
            .with(physics)
            .with(CollidableComponent(true))
            .with(BallComponent())
            .build()
    }

    @Spawns("bat")
    fun newBat(data: SpawnData): Entity {
        val isPlayer = data.get<Boolean>("isPlayer")
        val physics = PhysicsComponent()
        physics.setBodyType(BodyType.KINEMATIC)
        return entityBuilder(data)
            .type(if (isPlayer) EntityType.PLAYER_BAT else EntityType.ENEMY_BAT)
            .viewWithBBox(Rectangle(20.0, 60.0, Color.LIGHTGRAY))
            .with(CollidableComponent(true))
            .with(physics)
            .with(if (isPlayer) BatComponent() else EnemyBatComponent())
            .build()
    }
}
