package yi.games.pong

import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings

fun main(args: Array<String>){GameApplication.launch(PongGameApp::class.java, args)}
class PongGameApp:GameApplication() {
    override fun initSettings(settings: GameSettings?) {
        TODO("Not yet implemented")
    }
}
