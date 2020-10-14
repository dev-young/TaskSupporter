package maple_tasks

import logI
import java.awt.Point
import java.awt.event.KeyEvent

class LoginTask : MapleBaseTask() {


    suspend fun login(id: String, pw: String, startFirstID:Boolean =false): Boolean {
        helper.apply {
            smartClickTimeMax = 100

            val loginBtn = imageSearch("img\\login.png")
            if (loginBtn == null) {
                logI("로그인 버튼 찾기 실패")
                return false
            }

            if (id.contains('@')) {
                imageSearchAndClick("img\\emailId.png", maxTime = 100)?.let {
                    delayRandom(200, 300)
                }
            } else {
                imageSearchAndClick("img\\mapleId.png", maxTime = 100)?.let {
                    delayRandom(200, 300)
                }
            }

            val idPoint = Point(loginBtn.x - 30, loginBtn.y - 10)
            val pwPoint = Point(loginBtn.x - 30, loginBtn.y + 15)

            copyToClipboard(id)
            smartClick(idPoint, randomRangeX = 10, randomRangeY = 5)
            simpleClick()
            delayRandom(50, 60)
            clearText()
            paste()
            delayRandom(50, 60)

            copyToClipboard(pw)
            send(KeyEvent.VK_TAB)
//            smartClick(pwPoint, randomRangeX = 10, randomRangeY = 5)
//            simpleClick()
            delayRandom(50, 60)
            clearText()
            paste()
            delayRandom(50, 60)

            sendEnter()

            if (id.contains('@') && startFirstID) {
                delayRandom(400, 500)
                send(KeyEvent.VK_DOWN)
                delayRandom(500, 600)
                sendEnter()
            }
        }

        return false
    }

    /**로그인창 로딩 대기*/
    suspend fun waitLoadingLogin(maxTimeSec: Int = 5): Boolean {
        return waitLoading("img\\login.png", maxTimeSec)
    }

    /**로그인 후 채널 선택창 로딩 대기 */
    suspend fun waitLoadingChannel(maxTimeSec: Int = 10): Boolean {
        return waitLoading("img\\serverImg.png", maxTimeSec)
    }


    /**3번 채널로 이동
     * @param wordNumber 월드 순서 (스카니아:1 ~ 엘슘:7 ~ 리부트2:14) */
    suspend fun intoChannel(wordNumber: Int): Boolean {
        helper.apply {
            val mouseMoveTime = 200
            moveMouseRB()
            val wordBetween = 32    //월드간 간격   (1: 620, 55)
            val titlePoint = imageSearchUntilFind("img\\serverImg.png") ?: return false
            val wordPoint = Point(titlePoint.x + 620, titlePoint.y + 55 + ((wordNumber - 1) * wordBetween))
            val channelPoint = Point(titlePoint.x + 400, titlePoint.y + 245)

            smartClick(wordPoint, 20, 5, maxTime = mouseMoveTime)

            smartClick(channelPoint, 20, 5, maxTime = 1000)
            sendEnter()
            simpleClick()

            return true
        }
    }

    suspend fun backToCharacter(): Boolean {
        var trycount = 0
        while (getSelectCharacterPoint() == null) {
            helper.apply {

                val menu = imageSearchAndClick("img\\option.png", 90.0, maxTime = 300)?.also {
                    delayRandom(800, 1000)

                    smartClick(Point(it.x - 14, it.y - 41), 30, 8, 100, 200)
                    simpleClick()
                    delayRandom(800, 1000)
                    sendEnter()
                    sendEnter()
                }

                if (menu == null) {
                    imageSearchAndClick("img\\menuCollapse.png", maxTime = 300)?.let {
                        delayRandom(1000, 1500)
                    }
                }

                kotlinx.coroutines.delay(300)

            }

            trycount++
            if(trycount > 5) return false
        }
        return true
    }


    private fun getSelectCharacterPoint(): Point? {
        return helper.imageSearch("img\\selectCharacter.png")
    }

    suspend fun selectCharacter(characterIndex_: Int) {
        val characterIndex = characterIndex_ - 1
        helper.apply {
            getSelectCharacterPoint()?.let { selectChar ->
                val between = 125
                val x = selectChar.x + 70 + ((characterIndex%4) * between)
                val y = selectChar.y + if(characterIndex > 3) 400 else 200
                val character = Point(x, y)

                smartClick(character, 5, 5, maxTime = 200)
                delayRandom(400, 500)
                sendEnter()
                smartClick(Point(selectChar.x+600, selectChar.y+400), 5, 5, maxTime = 200)
            }
        }

    }

    /**캐릭터창 로딩 대기 */
    suspend fun waitLoadingCharacter(maxTimeSec: Int = 10): Boolean {
        return waitLoading("img\\selectCharacter.png", maxTimeSec)
    }

    /**캐릭터 선택후 접속 대기 */
    suspend fun waitLoadingGame(maxTimeSec: Int = 25): Boolean {
        return waitLoading("img\\menu.png", maxTimeSec)
    }

    suspend fun clearAd() {
        helper.apply {
            moveMouseRB()

            if(imageSearch("img\\closeAd.png") == null) {
                //처음에 광고가 안뜬 경우 (썬데이메이플)
                send(KeyEvent.VK_ESCAPE)
                delayRandom(1000, 1100)
            }
            imageSearchAndClickUntilFind("img\\closeAd.png", 95.5, maxTime = 200)
            delayRandom(1000, 1100)
            send(KeyEvent.VK_ESCAPE)

            imageSearchAndClick("img\\closeAd.png")
        }
    }

    /**로딩 대기 */
    suspend fun waitLoading(img:String, maxTimeSec: Int = 10): Boolean {
        helper.apply {
            moveMouseRB()
            var tryCount = 0
            while (imageSearch(img, 90.0) == null){
                kotlinx.coroutines.delay(1000)

                tryCount++
                if(tryCount > maxTimeSec)
                    return false
            }
        }
        return true
    }

    suspend fun logOut() {
        helper.apply {
            moveMouseRB()
            if(imageSearch("img\\menu.png") != null) {
                backToCharacter()
                waitLoadingCharacter()
                delayRandom(500, 600)
            }

            moveMouseRB()
            if (imageSearchAndClick("img\\backToLogin.png", 80.5, maxTime = 200) != null) {
                simpleClick()
                delayRandom(1000, 1110)
                sendEnter()
                sendEnter()
            } else {
                send(KeyEvent.VK_ESCAPE)
                delayRandom(500, 600)
                sendEnter()
            }

            waitLoadingLogin()

        }

    }

}