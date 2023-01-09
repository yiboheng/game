package yi.games.mario

import com.almasb.fxgl.animation.Interpolators
import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.app.scene.GameView
import com.almasb.fxgl.app.scene.LoadingScene
import com.almasb.fxgl.app.scene.SceneFactory
import com.almasb.fxgl.app.scene.Viewport
import com.almasb.fxgl.core.util.LazyValue
import com.almasb.fxgl.dsl.*
import com.almasb.fxgl.dsl.FXGL.Companion.despawnWithScale
import com.almasb.fxgl.dsl.FXGL.Companion.onCollisionOneTimeOnly
import com.almasb.fxgl.dsl.FXGL.Companion.setLevelFromMap
import com.almasb.fxgl.dsl.FXGL.Companion.spawnWithScale
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.components.CollidableComponent
import com.almasb.fxgl.entity.level.Level
import com.almasb.fxgl.input.UserAction
import com.almasb.fxgl.input.view.KeyView
import com.almasb.fxgl.physics.PhysicsComponent
import javafx.geometry.Point2D
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.util.Duration
import yi.games.mario.collision.PlayerButtonHandler
import yi.games.mario.component.PlayerComponent
import yi.games.mario.entity.EntityType
import yi.games.mario.entity.MarioEntityFactory
import yi.games.mario.scene.LevelEndScene
import yi.games.mario.scene.LevelEndScene.LevelTimeData
import yi.games.mario.scene.MainLoadingScene


fun main(args: Array<String>){GameApplication.launch(MarioGameApp::class.java, args)}

const val MAX_LEVEL = 5
const val STARTING_LEVEL = 0
class MarioGameApp:GameApplication() {
    private val levelEndScene = LazyValue { LevelEndScene() }
    private lateinit var player: Entity
    override fun initSettings(settings: GameSettings) {
        settings.width = 1280
        settings.height = 720
        settings.sceneFactory = object : SceneFactory() {
            override fun newLoadingScene(): LoadingScene {
                return MainLoadingScene()
            }
        }
    }

    override fun initInput() {
        getInput().addAction(object : UserAction("Left") {
            override fun onAction() {
                player.getComponent(PlayerComponent::class.java).left()
            }
            override fun onActionEnd() {
                player.getComponent(PlayerComponent::class.java).stop()
            }
        }, KeyCode.A)

        getInput().addAction(object : UserAction("Right") {
            override fun onAction() {
                player.getComponent(PlayerComponent::class.java).right()
            }
            override fun onActionEnd() {
                player.getComponent(PlayerComponent::class.java).stop()
            }
        }, KeyCode.D)

        getInput().addAction(object : UserAction("Jump") {
            override fun onActionBegin() {
                player.getComponent(PlayerComponent::class.java).jump()
            }
        }, KeyCode.W)

        getInput().addAction(object : UserAction("Use") {
            override fun onActionBegin() {
                getGameWorld().getEntitiesByType(EntityType.BUTTON)
                    .filter { it.hasComponent(CollidableComponent::class.java) && player.isColliding(it) }
                    .forEach {
                        it.removeComponent(CollidableComponent::class.java)
                        val keyEntity: Entity = it.getObject("keyEntity")
                        keyEntity.setProperty("activated", true)
                        val view = keyEntity.viewComponent.children[0] as KeyView
                        view.keyColor = Color.RED
                        makeExitDoor()
                    }
            }
        }, KeyCode.E)
    }

    override fun initGameVars(vars: MutableMap<String, Any>) {
        vars["level"] = STARTING_LEVEL
        vars["levelTime"] = 0.0
    }

    override fun initGame() {
        getGameWorld().addEntityFactory(MarioEntityFactory())
        nextLevel()
        // player must be spawned after call to nextLevel, otherwise player gets removed
        // before the update tick _actually_ adds the player to game world
        player = spawn("player", 50.0, 50.0)
        set("player", player)
        spawn("background")
        val viewport: Viewport = getGameScene().viewport
        viewport.setBounds(-1500, 0, 250 * 70, getAppHeight())
        viewport.bindToEntity(player, getAppWidth().toDouble() / 2, getAppHeight().toDouble() / 2)
        viewport.isLazy = true
    }

    override fun initPhysics() {
        getPhysicsWorld().setGravity(0.0, 760.0)
        getPhysicsWorld().addCollisionHandler(PlayerButtonHandler())
        onCollisionOneTimeOnly(EntityType.PLAYER, EntityType.EXIT_SIGN) { _, sign ->
            val texture = texture("exit_sign.png").brighter()
            texture.translateX = sign.x + 9
            texture.translateY = sign.y + 13
            val gameView = GameView(texture, 150)
            getGameScene().addGameView(gameView)
            runOnce({ getGameScene().removeGameView(gameView) }, Duration.seconds(1.6))
        }
        onCollisionOneTimeOnly(EntityType.PLAYER, EntityType.EXIT_TRIGGER) { _, _ ->
            makeExitDoor()
        }
        onCollisionOneTimeOnly(EntityType.PLAYER, EntityType.DOOR_BOT) { _, _ ->
            levelEndScene.get().onLevelFinish()
            // the above runs in its own scene, so fade will wait until
            // the user exits that scene
            getGameScene().viewport.fade { nextLevel() }
        }
        onCollisionBegin(EntityType.PLAYER, EntityType.KEY_PROMPT) { _, prompt ->
            val key: String = prompt.getString("key")
            val entity = getGameWorld().create("keyCode", SpawnData(prompt.x, prompt.y).put("key", key))
            spawnWithScale(entity, Duration.seconds(1.0), Interpolators.ELASTIC.EASE_OUT())
            runOnce({
                    despawnWithScale(entity, Duration.seconds(1.0), Interpolators.ELASTIC.EASE_IN())
                },
                Duration.seconds(2.5)
            )
        }
    }

    private fun makeExitDoor() {
        val doorTop = getGameWorld().getSingleton(EntityType.DOOR_TOP)
        val doorBot = getGameWorld().getSingleton(EntityType.DOOR_BOT)
        doorBot.getComponent(CollidableComponent::class.java).value = true
        doorTop.opacity = 1.0
        doorBot.opacity = 1.0
    }

    override fun onUpdate(tpf: Double) {
        inc("levelTime", tpf)
        if (player.y > getAppHeight()) {
            setLevel(geti("level"))
        }
    }

    private fun nextLevel() {
        if (geti("level") == MAX_LEVEL) {
            showMessage("You finished the demo!")
            return
        }
        inc("level", +1)
        setLevel(geti("level"))
    }

    private fun setLevel(levelNum: Int) {
        player.getComponent(PhysicsComponent::class.java).overwritePosition(Point2D(50.0, 50.0))
        player.zIndex = Int.MAX_VALUE
        set("levelTime", 0.0)
        val level: Level = setLevelFromMap("tmx/level$levelNum.tmx")
        val shortestTime = level.properties.getDouble("star1time")
        val levelTimeData = LevelTimeData(shortestTime * 2.4, shortestTime * 1.3, shortestTime)
        set("levelTimeData", levelTimeData)
    }
}
