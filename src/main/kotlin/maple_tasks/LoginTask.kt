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

    suspend fun login(
        id: String,
        pw: String,
        fileName: String = "",
        wordNumber: Int = 0,
        characterIndex: Int = 0,
        description: String = ""
    ): Boolean {
        helper.apply {
            smartClickTimeMax = 100
            moveMouseLB()
            val loginBtn = imageSearch("img\\login.png") ?: let {
                logOut(true)
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

            val idPoint = Point(loginBtn.x - 55, loginBtn.y - 95)
            val pwPoint = Point(loginBtn.x - 55, loginBtn.y + 53)

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

            var log: String

            if (fileName.isNotEmpty()) {
                imageSearchAndClickUntilFind("img\\$fileName", 60.0, maxTime = 100)
                simpleClick()
                sendEnter()
                sendEnter()

                log = fileName
            } else
                log = id


            if (wordNumber > 0 && waitLoadingChannel()) {
                intoChannel(wordNumber)
                log = "$log > ${wordNumber}번 서버"

                if (characterIndex > 0 && waitLoadingCharacter()) {
                    selectCharacter(characterIndex)
                    log = "$log > ${characterIndex}번째 캐릭터"
                }
            }

            logI("$log 접속 완료")


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
            val firstWorld = imageSearchUntilFind("img\\serverImg.png") ?: return false
            val worldPoint = Point(firstWorld.x, firstWorld.y + ((wordNumber - 1) * wordBetween))
            val channelPoint = Point(firstWorld.x - 330, firstWorld.y + 260)

            smartClick(worldPoint, 20, 5, maxTime = mouseMoveTime)

            imageSearchUntilFind("img\\serverImg.png", repeatCount = 40) ?: logI("채널 목록을 못찾음.")

            smartClick(channelPoint, 20, 5, maxTime = 200)
            sendEnter()
            simpleClick()

            return true
        }
    }

    suspend fun backToCharacter(fastMode: Boolean = false): Boolean {
        var trycount = 0
        while (getSelectCharacterPoint() == null) {
            helper.apply {

                imageSearchAndClick("img\\menuCollapse.png", accuracy = 50.0, maxTime = 200)?.let {
                    kotlinx.coroutines.delay(50)
                    simpleClick()
                    delayRandom(500, 800)
                }

                val menu = imageSearchAndClick("img\\option.png", 90.0, maxTime = 100)?.also {
                    if (!fastMode) delayRandom(800, 1000)

                    smartClick(
                        Point(it.x - 14, it.y - 41),
                        30,
                        8,
                        if (fastMode) 50 else 100,
                        if (fastMode) 80 else 200
                    )
                    simpleClick()
                    if (fastMode) delayRandom(50, 100)
                    else delayRandom(800, 1000)
                    sendEnter()
                    delayRandom(100, 200)
                    sendEnter()
                    delayRandom(100, 200)
                    sendEnter()
                }

                if (fastMode) kotlinx.coroutines.delay(50)
                else kotlinx.coroutines.delay(1500)


            }
            activateMaple()
            trycount++
            if (trycount > 10) return false
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
                val between = 122
                val x = selectChar.x - 740 + ((characterIndex % 6) * between)
                val y = selectChar.y - if (characterIndex > 5) 1 else 230
                val character = Point(x, y)

                smartClick(character, 5, 5, maxTime = 200)
                delayRandom(100, 150)
                sendEnter()
                smartClick(selectChar, 5, 5, maxTime = 100)
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

            if (imageSearch("img\\closeAd.png") == null) {
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
    suspend fun waitLoading(img: String, maxTimeSec: Int = 10): Boolean {
        helper.apply {
            moveMouseLB()
            var tryCount = 0
            val maxTry = maxTimeSec * 10
            while (imageSearch(img, 80.0) == null) {
                kotlinx.coroutines.delay(100)

                activateMaple()

                tryCount++
                if (tryCount > maxTry)
                    return false
            }
        }
        return true
    }

    suspend fun logOut(fastMode: Boolean = false) {
        helper.apply {
            moveMouseLB()
            if (imageSearch("img\\menu.png") != null || imageSearch("img\\menuCollapse.png") != null) {
                backToCharacter(fastMode)
                waitLoadingCharacter()
                if (!fastMode) delayRandom(500, 600)
            }

            moveMouseLB()
            if (imageSearchAndClick("img\\backToLogin.png", 80.5, maxTime = 100) != null) {
                simpleClick()
                if (fastMode) delayRandom(100, 200)
                else delayRandom(1000, 1110)

                sendEnter()
                sendEnter()
            } else {
                send(KeyEvent.VK_ESCAPE)
                if (fastMode) delayRandom(100, 200)
                else delayRandom(500, 600)
                sendEnter()
            }

            waitLoadingLogin()

        }

    }

}