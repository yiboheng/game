package yi.games.pong

import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.core.collection.PropertyChangeListener
import com.almasb.fxgl.dsl.*
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.physics.CollisionHandler
import com.almasb.fxgl.physics.HitBox
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import yi.games.pong.component.BatComponent
import yi.games.pong.entity.EntityType
import yi.games.pong.entity.PongEntityFactory


fun main(args: Array<String>){GameApplication.launch(PongGameApp::class.java, args)}
class PongGameApp:GameApplication() {

    private lateinit var playerBat: BatComponent
    override fun initSettings(settings: GameSettings) {
        with(settings) {
            title = "Pong"
            version = "1.0"
        }
    }

    override fun initInput() {
        onKeyDown(KeyCode.W, "UP"){
            playerBat.up()
        }
        onKeyUp(KeyCode.W){
            playerBat.stop()
        }
        onKeyDown(KeyCode.S, "DOWN"){
            playerBat.down()
        }
        onKeyUp(KeyCode.S){
            playerBat.stop()
        }
    }

    override fun initGameVars(vars: MutableMap<String, Any>) {
        vars["player1score"] = 0
        vars["player2score"] = 0
    }

    override fun initGame() {
        getWorldProperties().addListener("player1score", PropertyChangeListener<Int> { oldVal, newVal ->
            if(newVal > 10){
                showGameOver("Player 1")
            }
        })
        getWorldProperties().addListener("player2score", PropertyChangeListener<Int> { oldVal, newVal ->
            if(newVal > 10){
                showGameOver("Player 2")
            }
        })
        getGameWorld().addEntityFactory(PongEntityFactory())
        getGameScene().setBackgroundColor(Color.rgb(0, 0, 5))
        initGameBounds()
        initGameObjects()
    }

    override fun initPhysics() {
        with(getPhysicsWorld()) {
            setGravity(0.0, 0.0)

        }
        getPhysicsWorld().addCollisionHandler(object : CollisionHandler(EntityType.BALL, EntityType.WALL) {
            override fun onHitBoxTrigger(a: Entity, b: Entity, boxA: HitBox, boxB: HitBox) {
                if (boxB.name == "LEFT") {
                    inc("player2score", +1)
                } else if (boxB.name == "RIGHT") {
                    inc("player1score", +1)
                }
            }
        })

    }


    private fun showGameOver(winner: String){
        getDialogService().showMessageBox("$winner won! Demo over \n Thanks for playing"){
            getGameController().exit()
        }
    }

    private fun initGameBounds(){
        entityBuilder()
            .type(EntityType.WALL)
            .collidable()
            .buildScreenBoundsAndAttach(150.0)
    }

    private fun initGameObjects(){
        spawn("ball", (getAppWidth() / 2 - 5).toDouble(), (getAppHeight() / 2 - 5).toDouble())
        val bat1 = spawn(
            "bat", SpawnData(
                (getAppWidth() / 4).toDouble(),
                (getAppHeight() / 2 - 30).toDouble()
            )
                .put("isPlayer", true)
        )
        val bat2 = spawn(
            "bat", SpawnData(
                (3 * getAppWidth() / 4 - 20).toDouble(),
                (getAppHeight() / 2 - 30).toDouble()
            )
                .put("isPlayer", false)
        )

        playerBat = bat1.getComponent(BatComponent::class.java)
    }
}
