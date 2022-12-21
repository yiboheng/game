package yi.ming.xing.games

import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.dsl.*
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.SpawnData
import javafx.geometry.Point2D
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import yi.ming.xing.games.component.MoveDirection
import yi.ming.xing.games.component.TankAIComponent
import yi.ming.xing.games.component.TankComponent
import yi.ming.xing.games.entity.EntityType
import yi.ming.xing.games.entity.TankEntityFactory
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

fun main() {
    GameApplication.launch(TankGameApp::class.java, emptyArray())
}

class TankGameApp : GameApplication() {

    private var p1: Entity? = null
    private var p2: Entity? = null

    private val idleEnemyCounter = AtomicInteger(1)

    override fun initSettings(settings: GameSettings) {
        with(settings) {
            width = 1600
            height = 800
            title = "TankWar"
        }
    }

    override fun initInput() {

        onKey(KeyCode.W, "p1_up"){
            p1!!.getComponent(TankComponent::class.java).moveUp()
        }
        onKey(KeyCode.S, "p1_down"){
            p1!!.getComponent(TankComponent::class.java).moveDown()
        }
        onKey(KeyCode.A, "p1_left"){
            p1!!.getComponent(TankComponent::class.java).moveLeft()
        }
        onKey(KeyCode.D, "p1_right"){
            p1!!.getComponent(TankComponent::class.java).moveRight()
        }
        onKeyDown(KeyCode.J, "p1_shoot"){
            p1!!.getComponent(TankComponent::class.java).shoot()
        }

        onKey(KeyCode.UP, "p2_up"){
            p2!!.getComponent(TankComponent::class.java).moveUp()
        }
        onKey(KeyCode.DOWN, "p2_down"){
            p2!!.getComponent(TankComponent::class.java).moveDown()
        }
        onKey(KeyCode.LEFT, "p2_left"){
            p2!!.getComponent(TankComponent::class.java).moveLeft()
        }
        onKey(KeyCode.RIGHT, "p2_right"){
            p2!!.getComponent(TankComponent::class.java).moveRight()
        }
        onKeyDown(KeyCode.NUMPAD5, "p2_shoot"){
            p2!!.getComponent(TankComponent::class.java).shoot()
        }
    }

    override fun initGame() {
//        run({
//            spawnTarget()
//        }, Duration.seconds(1.0))

        getGameWorld().addEntityFactory(TankEntityFactory())

        getGameScene().setBackgroundRepeat(FXGL.image("ground.png"))

        val appWidth = getAppWidth() * 1.0
        val halfAppWidth = getAppWidth() / 2.0
        val appHeight = getAppHeight() * 1.0
        val halfAppHeight = getAppHeight() / 2.0

        //添加中段河流
        val halfRiverWidth = 50.0
        val riverWidth = halfRiverWidth * 2

        val edgeWidth = 20.0

        spawnEdges()

        //添加一块砖
        spawnBricks()

        //添加一个油桶
        spawn("oilBoom", 350.0,350.0)

        //添加玩家1
        p1 = spawn("player", SpawnData(x = appWidth / 4, y = appHeight - 100).also {
            it.put("direction", MoveDirection.UP)
            it.put("name", "p1")
        })

        //添加玩家2
        p2 = spawn("player", SpawnData(x = 3*appWidth / 4, y = appHeight - 100).also {
            it.put("direction", MoveDirection.UP)
            it.put("name", "p2")
        })

        run({
            if(idleEnemyCounter.get()>0){
                spawnEnemy()
            }
        }, Duration.seconds(2.0))


    }

    override fun initUI() {

    }

    override fun initPhysics() {
        onCollisionBegin(EntityType.BULLET, EntityType.BRICK) { bullet, brick ->
            spawn("boom", bullet.position)
            bullet.removeFromWorld()
            brick.removeFromWorld()
        }

        onCollisionBegin(EntityType.BULLET, EntityType.EDGE) { bullet, _ ->
            spawn("boom", bullet.position)
            bullet.removeFromWorld()
        }

        onCollisionBegin(EntityType.BULLET, EntityType.PLAYER) { bullet, player ->

            val playerRole = player.getPropertyOptional<String>("role")
            val bulletRole = bullet.getPropertyOptional<String>("role")
            if (bulletRole != playerRole) {
                spawn("boom", bullet.position)
                bullet.removeFromWorld()
                player.removeFromWorld()
                idleEnemyCounter.incrementAndGet()
            }
            val nameOpt = player.getPropertyOptional<String>("name")
            if (nameOpt.isPresent) {
                getDialogService().showMessageBox("${nameOpt.get()} 败北"){
                    getGameController().exit()
                }
            }
        }

        onCollisionBegin(EntityType.PLAYER, EntityType.PLAYER) { player, ps ->
            player.getComponent(TankComponent::class.java).onCollision(ps)
            ps.getComponent(TankComponent::class.java).onCollision(ps)
        }

        onCollisionBegin(EntityType.PLAYER, EntityType.EDGE) { player, edge ->
            val edgeBox = edge.boundingBoxComponent
            val tankBox = player.boundingBoxComponent
            edge.viewComponent.addChild(Rectangle(10.0,10.0, Color.RED).also {

            })
            println("\n knock edge =========== >" +
                    " \n distanceBox = ${player.distanceBBox(edge)}, distance = ${player.distance(edge)}," +
                    "\n tank minX = ${tankBox.getMinXWorld()}, minY = ${tankBox.getMinYWorld()}, maxX = ${tankBox.getMaxXWorld()}, maxY = ${tankBox.getMaxYWorld()}" +
                    "\n edge minX = ${edgeBox.getMinXWorld()}, minY = ${edgeBox.getMinYWorld()}, maxX = ${edgeBox.getMaxXWorld()}, maxY = ${edgeBox.getMaxYWorld()}")
            player.getComponent(TankComponent::class.java).onCollision(edge)
        }
        onCollisionBegin(EntityType.PLAYER, EntityType.BRICK) { player, brick ->
            println("knock brick")
            player.getComponent(TankComponent::class.java).onCollision(brick)
        }

    }

    private fun spawnEnemy() {
        val enemySpawnPoints = arrayOf(Point2D(100.0,30.0), Point2D(1400.0, 30.0))
        val spawnPoint = enemySpawnPoints[Random.nextInt(enemySpawnPoints.size)]
        val enemy = spawn("enemy", SpawnData(spawnPoint).also {
            it.put("direction", MoveDirection.DOWN)
        })
        enemy.addComponent(TankAIComponent())
        idleEnemyCounter.decrementAndGet()
    }

    private fun spawnEdges(){
        spawnEdgeLine(Point2D(0.0, 0.0), Point2D(getAppWidth().toDouble(), 0.0))
        spawnEdgeLine(Point2D(0.0, 20.0), Point2D(0.0, getAppHeight().toDouble()))
        spawnEdgeLine(Point2D(0.0, getAppHeight().toDouble()-20), Point2D(getAppWidth().toDouble(), getAppHeight().toDouble()-20))
        spawnEdgeLine(Point2D(getAppWidth().toDouble()-20, 20.0), Point2D(getAppWidth().toDouble()-20, getAppHeight().toDouble()-20))
    }

    private fun spawnBricks(){
        spawnBrickLine(Point2D(20.0, 160.0), Point2D(480.0, 160.0))
        spawnBrickLine(Point2D(680.0, 160.0), Point2D(1280.0, 160.0))
        spawnBrickLine(Point2D(1480.0, 160.0), Point2D(1760.0, 160.0))
        spawnBrickLine(Point2D(280.0, 360.0), Point2D(880.0, 360.0))
        spawnBrickLine(Point2D(1080.0, 360.0), Point2D(1680.0, 360.0))

        spawnBrickLine(Point2D(20.0, 560.0), Point2D(480.0, 560.0))
        spawnBrickLine(Point2D(680.0, 560.0), Point2D(1280.0, 560.0))
        spawnBrickLine(Point2D(1480.0, 560.0), Point2D(1760.0, 560.0))
    }

    private fun spawnEdgeLine(p1 : Point2D, p2: Point2D){
        spawnLine("edge", p1, p2, 20,20)
    }
    private fun spawnBrickLine(p1 : Point2D, p2: Point2D){
        spawnLine("brick", p1, p2, 40,40)
    }
    private fun spawnLine(entityName:String, p1 : Point2D, p2: Point2D, specWidth: Int, specHeight: Int){
        var x = min(p1.x, p2.x)
        var y = min(p1.y, p2.y)
        val maxX = max(p1.x, p2.x)
        val maxY = max(p1.y, p2.y)
        do{
            println("brick $x, $y")
            spawn(entityName, x, y)
            x +=specWidth
            y +=specHeight
            if (x > maxX) {
                x = maxX
            }
            if (y > maxY) {
                y = maxY
            }
        } while (x !=maxX || y !=maxY)

    }
}




