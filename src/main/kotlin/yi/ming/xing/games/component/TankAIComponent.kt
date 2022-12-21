package yi.ming.xing.games.component

import com.almasb.fxgl.dsl.FXGL.Companion.getGameWorld
import com.almasb.fxgl.dsl.entityBuilder
import com.almasb.fxgl.dsl.runOnce
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.entity.components.BoundingBoxComponent
import javafx.geometry.Rectangle2D
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
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

    override fun onAdded() {
        tankRole = entity.getPropertyOptional<String>("role").get()
        tank = entity.getComponent(TankComponent::class.java)
        tank.registerOnCollisionFunction(Consumer { direction ->
            tank.rotate(MoveDirection.randomNext(direction),true)
        })

    }

    private var lastSniffTime = 0L
    private val sniffInterval = 200
    override fun onUpdate(tpf: Double) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastSniffTime > sniffInterval) {
            lastSniffTime = currentTime
            doSniffAround()
        }

        tank.moveForward()
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
            if(closestResult.dangerDistance < 200){
                if(MoveDirection.canAvoid(tank.direction, closestResult.dangerDirection)){
                    tank.moveForward()
                } else {
                    val avoidDirection = MoveDirection.randomAvoid(closestResult.dangerDirection)
                    tank.move(avoidDirection)
                }
            }
            return
        }

        // 攻击敌人
        val enemyResults = sniffResults.filter { it.hasEnemy }.sortedBy { it.enemyDistance }
        if (enemyResults.isNotEmpty()) {
            val closestResult = enemyResults[0]
            tank.move(closestResult.sniffDirection)
            tank.shoot()
            return
        }

        // 变更行进方向
        val noEdgeResults = sniffResults.filter { it.edgeDistance < 200}
        if (noEdgeResults.isNotEmpty()) {
            if(Random.nextInt(20) == 1){
                val closestResult = noEdgeResults[0]
                tank.move(closestResult.sniffDirection)
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

        val entitiesInRange = getGameWorld().getEntitiesInRange(range)
        for (it in entitiesInRange) {
            if (it.type == EntityType.EDGE || it.type == EntityType.BRICK) {
                hasEdge = true
                edgeDistance = entity.distanceBBox(it)
                continue
            }
            val itRole = it.getPropertyOptional<String>("role")
            if(it.type == EntityType.PLAYER && itRole.isPresent && itRole.get()!=tankRole){
                hasEnemy = true
                enemyDirection = it.getComponent(TankComponent::class.java).direction
                enemyDistance = entity.distanceBBox(it)
                continue
            }
            if(it.type == EntityType.BULLET && itRole.isPresent && itRole.get()!=tankRole){
                hasDanger = true
                dangerDirection = it.getPropertyOptional<MoveDirection>("direction").get()
                dangerDistance = entity.distanceBBox(it)
                continue
            }
        }
        return SniffResult(hasEnemy, enemyDirection, enemyDistance,
            hasDanger, dangerDirection, dangerDistance, hasEdge, edgeDistance, sniffDirection)
    }

    data class SniffResult(val hasEnemy:Boolean, val enemyDirection:MoveDirection, val enemyDistance: Double,
                           val hasDanger:Boolean, val dangerDirection:MoveDirection, val dangerDistance: Double,
                           val hasEdge:Boolean, val edgeDistance: Double, val sniffDirection:MoveDirection)

    private fun traceBox(targetBox : BoundingBoxComponent, direction:MoveDirection, width: Int, height:Int, color: Color, cleanTime: Duration)
    : Rectangle2D{
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

//        val rectangle = Rectangle(markBoxWidth, markBoxHeight, Color.TRANSPARENT)
//        val markBox = if(direction == UP || direction == DOWN) {VBox(rectangle)} else {HBox(rectangle)}
//        markBox.border = Border(BorderStroke(color, BorderStrokeStyle.SOLID, null, BorderWidths.DEFAULT))
//        val marker = entityBuilder()
//            .at(markBoxX, markBoxY)
//            .view(markBox)
//            .buildAndAttach()
//        runOnce({ marker.removeFromWorld()}, cleanTime)
        return Rectangle2D(markBoxX, markBoxY, markBoxWidth, markBoxHeight)
    }

}
