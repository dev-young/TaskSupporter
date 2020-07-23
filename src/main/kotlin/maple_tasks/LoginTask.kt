package maple_tasks

import helper.HelperCore
import logI
import moveMouseSmoothly
import java.awt.Point
import java.awt.event.KeyEvent

class LoginTask : MapleBaseTask() {


    suspend fun login(id: String, pw: String): Boolean {
        helper.apply {
            smartClickTimeMax = 100

            val loginBtn = imageSearch("img\\login.png")
            if (loginBtn == null) {
                logI("로그인 버튼 찾기 실패")
                return false
            }

            val idPoint = Point(loginBtn.x - 30, loginBtn.y-10)
            val pwPoint = Point(loginBtn.x - 30, loginBtn.y+15)

            copyToClipboard(id)
            smartClick(idPoint, randomRangeX = 10, randomRangeY = 5)
            simpleClick()
            delayRandom(50,60)
            clearText()
            paste()
            delayRandom(50,60)

            copyToClipboard(pw)
            send(KeyEvent.VK_TAB)
//            smartClick(pwPoint, randomRangeX = 10, randomRangeY = 5)
//            simpleClick()
            delayRandom(50,60)
            clearText()
            paste()
            delayRandom(50,60)

            sendEnter()
        }

        return false
    }




}