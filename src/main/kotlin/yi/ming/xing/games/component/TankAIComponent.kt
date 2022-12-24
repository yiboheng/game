package yi.ming.xing.games.component

import com.almasb.fxgl.dsl.FXGL.Companion.getGameWorld
import com.almasb.fxgl.dsl.newLocalTimer
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.entity.components.BoundingBoxComponent
import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import javafx.scene.paint.Color
import javafx.util.Duration
import yi.ming.xing.games.component.MoveDirection.*
import yi.ming.xing.games.entity.EntityType
import java.util.function.Consumer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


class TankAIComponent() : Component() {

    private lateinit var tank: TankComponent
    private lateinit var tankRole: String

    private val sniffTimer = newLocalTimer()
    private val sniffInterval = Duration.seconds(0.2)
    override fun onAdded() {
        tankRole = entity.getPropertyOptional<String>("role").get()
        tank = entity.getComponent(TankComponent::class.java)
        tank.registerOnCollisionFunction(Consumer { direction ->
            tank.rotate(MoveDirection.randomNext(direction),true)
        })
    }

    override fun onUpdate(tpf: Double) {
        tank.moveForward()
        if(!sniffTimer.elapsed(sniffInterval)){
            return
        }
        sniffTimer.capture()
        doSniffAround()
    }

    private fun doSniffAround() {
        val sniffResults = sniffAround()
        handleSniffResults(sniffResults)
    }

    private fun handleSniffResults(sniffResults: List<SniffResult>) {
        // 躲避危险
        val dangerResults = sniffResults.filter { it.hasDanger }.sortedBy { it.dangerDistance }
        if (dangerResults.isNotEmpty()) {
            val closestResult = dangerResults[0]
            val reverseDangerDirection = MoveDirection.reverse(closestResult.dangerDirection)
            tank.rotate(reverseDangerDirection)
            tank.shoot()
            if(closestResult.dangerDistance < 200){
                markBox(closestResult.dangerPosition, 5.0,5.0, Color.TRANSPARENT, Color.RED, Duration.seconds(1.0))
                val avoidDirection = MoveDirection.randomAvoid(closestResult.dangerDirection)
                tank.move(avoidDirection)
            }
            return
        }

        // 攻击敌人
        val enemyResults = sniffResults.filter { it.hasEnemy }.sortedBy { it.enemyDistance }
        if (enemyResults.isNotEmpty()) {
            val closestResult = enemyResults[0]
            markBox(closestResult.enemyPosition, 5.0,5.0, Color.TRANSPARENT, Color.YELLOW, Duration.seconds(1.0))
            tank.move(closestResult.sniffDirection)
            tank.shoot()
            return
        }

        // 变更行进方向
        val noEdgeResults = sniffResults.filter { it.edgeDistance > 200}.sortedByDescending { it.edgeDistance }
        if (noEdgeResults.isNotEmpty()) {
            val closestResult = noEdgeResults[0]
            if(Random.nextInt(15) == 1){
                markBox(closestResult.edgePosition, 5.0,5.0, Color.GRAY, Color.BLACK, Duration.seconds(1.0))
                tank.move(closestResult.sniffDirection)
            } else {
                markBox(closestResult.edgePosition, 5.0,5.0, Color.TRANSPARENT, Color.BLACK, Duration.seconds(1.0))
            }
            return
        }
    }

    private fun sniffAround(): List<SniffResult> {
        val sniffWidth = 40
        val sniffHeight = 400
        val tankBox = entity.boundingBoxComponent
        val upTraceRange = traceBox(tankBox, UP, sniffWidth, sniffHeight, Color.GREEN, Duration.seconds(1.0))
        val rightTraceRange = traceBox(tankBox, RIGHT, sniffWidth, sniffHeight, Color.GREEN, Duration.seconds(1.0))
        val downTraceRange = traceBox(tankBox, DOWN, sniffWidth, sniffHeight, Color.GREEN, Duration.seconds(1.0))
        val leftTraceRange = traceBox(tankBox, LEFT, sniffWidth, sniffHeight, Color.GREEN, Duration.seconds(1.0))

        val upSniffResult = sniffRange(upTraceRange, UP)
        val rightSniffResult = sniffRange(rightTraceRange, RIGHT)
        val downSniffResult = sniffRange(downTraceRange, DOWN)
        val leftSniffResult = sniffRange(leftTraceRange, LEFT)

        return listOf(upSniffResult, rightSniffResult, downSniffResult, leftSniffResult)
    }

    private fun sniffRange(range: Rectangle2D, sniffDirection: MoveDirection):SniffResult{
        var hasEdge = false
        var hasEnemy = false
        var hasDanger = false
        var enemyDirection = RIGHT
        var dangerDirection = RIGHT
        var edgeDistance = 999999.9
        var enemyDistance = 999999.9
        var dangerDistance = 999999.9
        var edgePosition = Point2D(999999.9,999999.9)
        var enemyPosition = Point2D(999999.9,999999.9)
        var dangerPosition = Point2D(999999.9,999999.9)

        val entitiesInRange = getGameWorld().getEntitiesInRange(range)
        for (it in entitiesInRange) {
            if (it.type == EntityType.EDGE || it.type == EntityType.BRICK) {

                hasEdge = true
                edgeDistance = entity.distanceBBox(it)
                edgePosition = it.position
                continue
            }
            val itRole = it.getPropertyOptional<String>("role")
            if(it.type == EntityType.PLAYER && itRole.isPresent && itRole.get()!=tankRole){
                hasEnemy = true
                enemyDirection = it.getComponent(TankComponent::class.java).direction
                enemyDistance = entity.distanceBBox(it)
                enemyPosition = it.position
                continue
            }
            if(it.type == EntityType.BULLET && itRole.isPresent && itRole.get()!=tankRole){
                hasDanger = true
                dangerDirection = it.getPropertyOptional<MoveDirection>("direction").get()
                dangerDistance = entity.distanceBBox(it)
                dangerPosition = it.position
                continue
            }
        }
        return SniffResult(hasEnemy, enemyDirection, enemyDistance,enemyPosition,
            hasDanger, dangerDirection, dangerDistance, dangerPosition,
            hasEdge, edgeDistance, edgePosition, sniffDirection)
    }

    data class SniffResult(val hasEnemy:Boolean, val enemyDirection:MoveDirection, val enemyDistance: Double, val enemyPosition:Point2D,
                           val hasDanger:Boolean, val dangerDirection:MoveDirection, val dangerDistance: Double, val dangerPosition:Point2D,
                           val hasEdge:Boolean, val edgeDistance: Double, val edgePosition:Point2D, val sniffDirection:MoveDirection)

    private fun traceBox(targetBox : BoundingBoxComponent, direction:MoveDirection,
                         width: Int, height:Int, color: Color, cleanTime: Duration): Rectangle2D{
        val minSide = min(width, height)
        val maxSide = max(width, height)
        val vector = direction.vector
        val markBoxWidth = abs(vector.x * (maxSide - minSide)) + minSide
        val markBoxHeight = abs(vector.y * (maxSide - minSide)) + minSide
        var markBoxX = 0.0
        var markBoxY = 0.0

        when (direction) {
            UP -> {
                markBoxX = targetBox.getMinXWorld()
                markBoxY = targetBox.getMinYWorld() - markBoxHeight - 1
            }
            DOWN -> {
                markBoxX = targetBox.getMinXWorld()
                markBoxY = targetBox.getMaxYWorld() + 1
            }
            LEFT -> {
                markBoxX = targetBox.getMinXWorld() - markBoxWidth -1
                markBoxY = targetBox.getMinYWorld()
            }
            RIGHT -> {
                markBoxX = targetBox.getMaxXWorld() + 1
                markBoxY = targetBox.getMinYWorld()
            }
        }

        markBox(Point2D(markBoxX, markBoxY), markBoxWidth, markBoxHeight, Color.TRANSPARENT, color, cleanTime)
        return Rectangle2D(markBoxX, markBoxY, markBoxWidth, markBoxHeight)
    }

    private fun markBox(position:Point2D, width:Double, height:Double, fillColor:Color, borderColor:Color, cleanTime: Duration) {
//        val markBox = VBox(Rectangle(width, height, fillColor))
//        markBox.border = Border(BorderStroke(borderColor, BorderStrokeStyle.SOLID, null, BorderWidths.DEFAULT))
//        val marker = entityBuilder()
//            .at(position)
//            .view(markBox)
//            .buildAndAttach()
//        runOnce({ marker.removeFromWorld()}, cleanTime)
    }

}
