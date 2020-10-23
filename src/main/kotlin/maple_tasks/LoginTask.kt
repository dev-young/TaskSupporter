package maple_tasks

import logI
import java.awt.Point
import java.awt.event.KeyEvent
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class LoginTask : MapleBaseTask() {


    suspend fun login(id: String, pw: String, fileName: String = "", description:String = ""): Boolean {
        helper.apply {
            smartClickTimeMax = 100
            moveMouseLB()
            val loginBtn = imageSearch("img\\login.png") ?: let {
                logOut()
                imageSearch("img\\login.png")
            }
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

            if (fileName.isNotEmpty()) {
                imageSearchAndClickUntilFind("img\\$fileName", 60.0, maxTime = 100)
                simpleClick()
                sendEnter()
                sendEnter()
            }

            //로그인 기록 남기기
            saveLog("$description  [$id]")
        }

        return true
    }

    private fun saveLog(id: String) {
        val file = File("loginLog.txt")
        val bw = BufferedWriter(FileWriter(file, true))
        val simpleDateFormat = SimpleDateFormat("YYYY-MM-dd  HH:mm:ss")
        // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
        // 이미 파일이 존재할 경우 덮어쓰기로 저장
        try {
            bw.write("${simpleDateFormat.format(Date())} > $id")
            bw.newLine()
        } catch (e: FileNotFoundException) {
            logI("FileNotFound: $id")
        }

        bw.flush()
        bw.close()
    }

    /**로그인창 로딩 대기*/
    suspend fun waitLoadingLogin(maxTimeSec: Int = 5): Boolean {
        return waitLoading("img\\login.png", maxTimeSec)
    }

    /**로그인 후 채널 선택창 로딩 대기 */
    suspend fun waitLoadingChannel(maxTimeSec: Int = 20): Boolean {
        return waitLoading("img\\serverImg.png", maxTimeSec)
    }


    /**3번 채널로 이동
     * @param wordNumber 월드 순서 (스카니아:1 ~ 엘슘:7 ~ 리부트2:14) */
    suspend fun intoChannel(wordNumber: Int): Boolean {
        helper.apply {
            val mouseMoveTime = 200
            moveMouseLB()
            val wordBetween = 32    //월드간 간격   (1: 620, 55)
            val titlePoint = imageSearchUntilFind("img\\serverImg.png") ?: return false
            val wordPoint = Point(titlePoint.x + 620, titlePoint.y + 55 + ((wordNumber - 1) * wordBetween))
            val channelPoint = Point(titlePoint.x + 400, titlePoint.y + 245)

            smartClick(wordPoint, 20, 5, maxTime = mouseMoveTime)

            smartClick(channelPoint, 20, 5, maxTime = 5000)
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
    suspend fun waitLoadingCharacter(maxTimeSec: Int = 20): Boolean {
        return waitLoading("img\\selectCharacter.png", maxTimeSec)
    }

    /**캐릭터 선택후 접속 대기 */
    suspend fun waitLoadingGame(maxTimeSec: Int = 45): Boolean {
        return waitLoading("img\\menu.png", maxTimeSec)
    }

    suspend fun clearAd() {
        helper.apply {
            moveMouseLB()

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
            moveMouseLB()
            var tryCount = 0
            while (imageSearch(img, 80.0) == null){
                kotlinx.coroutines.delay(1000)

                activateMaple()

                tryCount++
                if(tryCount > maxTimeSec)
                    return false
            }
        }
        return true
    }

    suspend fun logOut() {
        helper.apply {
            moveMouseLB()
            if(imageSearch("img\\menu.png") != null) {
                backToCharacter()
                waitLoadingCharacter()
                delayRandom(500, 600)
            }

            moveMouseLB()
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