package yi.games.mario.entity

import com.almasb.fxgl.dsl.*
import com.almasb.fxgl.dsl.components.LiftComponent
import com.almasb.fxgl.dsl.views.ScrollingBackgroundView
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.EntityFactory
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.Spawns
import com.almasb.fxgl.entity.components.CollidableComponent
import com.almasb.fxgl.entity.components.IrremovableComponent
import com.almasb.fxgl.input.view.KeyView
import com.almasb.fxgl.physics.BoundingShape
import com.almasb.fxgl.physics.HitBox
import com.almasb.fxgl.physics.PhysicsComponent
import com.almasb.fxgl.physics.box2d.dynamics.BodyType
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef
import javafx.geometry.Point2D
import javafx.scene.CacheHint
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.util.Duration
import yi.games.mario.component.PlayerComponent


class MarioEntityFactory:EntityFactory {
    @Spawns("background")
    fun newBackground(data: SpawnData): Entity {
        return entityBuilder()
            .view(ScrollingBackgroundView(image("background/forest.png"),
                getAppWidth().toDouble(), getAppHeight().toDouble()))
            .zIndex(-1)
            .with(IrremovableComponent())
            .build()
    }

    @Spawns("platform")
    fun newPlatform(data: SpawnData): Entity {
        return entityBuilder(data)
            .type(EntityType.PLATFORM)
            .bbox(HitBox(BoundingShape.box(data.get("width"), data.get("height"))))
            .with(PhysicsComponent())
            .build()
    }

    @Spawns("exitTrigger")
    fun newExitTrigger(data: SpawnData): Entity {
        return entityBuilder(data)
            .type(EntityType.EXIT_TRIGGER)
            .bbox(HitBox(BoundingShape.box(data.get("width"), data.get("height"))))
            .with(CollidableComponent(true))
            .build()
    }

    @Spawns("doorTop")
    fun newDoorTop(data: SpawnData): Entity {
        return entityBuilder(data)
            .type(EntityType.DOOR_TOP)
            .opacity(0.0)
            .build()
    }

    @Spawns("doorBot")
    fun newDoorBot(data: SpawnData): Entity {
        return entityBuilder(data)
            .type(EntityType.DOOR_BOT)
            .bbox(HitBox(BoundingShape.box(data.get("width"), data.get("height"))))
            .opacity(0.0)
            .with(CollidableComponent(false))
            .build()
    }

    @Spawns("player")
    fun newPlayer(data: SpawnData): Entity {
        val physics = PhysicsComponent()
        physics.setBodyType(BodyType.DYNAMIC)
        physics.addGroundSensor(HitBox("GROUND_SENSOR",
            Point2D(16.0, 38.0), BoundingShape.box(6.0, 8.0)))
        // this avoids player sticking to walls
        physics.setFixtureDef(FixtureDef().friction(0.0f))
        return entityBuilder(data)
            .type(EntityType.PLAYER)
            .bbox(HitBox(Point2D(5.0, 5.0), BoundingShape.circle(12.0)))
            .bbox(HitBox(Point2D(10.0, 25.0), BoundingShape.box(10.0, 17.0)))
            .with(physics)
            .with(CollidableComponent(true))
            .with(IrremovableComponent())
            .with(PlayerComponent())
            .build()
    }

    @Spawns("exitSign")
    fun newExit(data: SpawnData): Entity {
        return entityBuilder(data)
            .type(EntityType.EXIT_SIGN)
            .bbox(HitBox(BoundingShape.box(data.get("width"), data.get("height"))))
            .with(CollidableComponent(true))
            .build()
    }

    @Spawns("keyPrompt")
    fun newPrompt(data: SpawnData): Entity {
        return entityBuilder(data)
            .type(EntityType.KEY_PROMPT)
            .bbox(HitBox(BoundingShape.box(data.get("width"), data.get("height"))))
            .with(CollidableComponent(true))
            .build()
    }

    @Spawns("keyCode")
    fun newKeyCode(data: SpawnData): Entity {
        val key = data.get<String>("key")
        val keyCode = KeyCode.getKeyCode(key)
        val lift = LiftComponent()
        lift.isGoingUp = true
        lift.yAxisDistanceDuration(6.0, Duration.seconds(0.76))
        val view = KeyView(keyCode, Color.YELLOW, 24.0)
        view.isCache = true
        view.cacheHint = CacheHint.SCALE
        return entityBuilder(data)
            .view(view)
            .with(lift)
            .zIndex(100)
            .build()
    }

    @Spawns("button")
    fun newButton(data: SpawnData): Entity {
        val keyEntity = getGameWorld().create("keyCode",
            SpawnData(data.x, data.y - 50).put("key", "E"))
        keyEntity.viewComponent.isVisible = false
        return entityBuilder(data)
            .type(EntityType.BUTTON)
            .viewWithBBox(texture("button.png", 20.0, 18.0))
            .with(CollidableComponent(true))
            .with("keyEntity", keyEntity)
            .build()
    }
}
