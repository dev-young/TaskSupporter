package maple_tasks

import com.sun.jna.platform.win32.User32
import helper.HelperCore
import kotlinx.coroutines.delay
import leftBottom
import logI
import moveMouseSmoothly
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import rightBottom
import toMat
import winActive
import winGetPos
import winIsForeground
import winMove
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.KeyEvent
import kotlin.math.absoluteValue

open class MapleBaseTask {
    val helper: HelperCore = HelperCore()

    val itemDistance = 42  // 아이템 간격
    var inventoryKey = KeyEvent.VK_I
    var productionSkillKey = KeyEvent.VK_B

    var nextItemPoint: Point? = null
    var isNextItemInventoryExpanded = false //아이템 하나하나 확인하는 방식에서
    var nextItemPosition = 0 // 현재 확인중인 아이템 순서

    var isNumberLock = helper.toolkit.getLockingKeyState(KeyEvent.VK_NUM_LOCK)

    fun activateMaple(): Boolean {
        return User32.INSTANCE.winActive("MapleStory")
    }

    /**해당 좌표의 아이템이 빈칸인지 확인*/
    fun isItemEmpty(leftTop: Point): Boolean {
        val x1 = leftTop.x - 8
        val y1 = leftTop.y - 8
        helper.imageSearch(Point(x1, y1), 39, 39, "img\\emptyItem.png")?.let {
            return true
        }
        return false
    }

    /**해당 좌표의 아이템이 빈칸 혹은 사용불가능한 칸인지 확인*/
    fun isEmptyOrDisable(leftTop: Point): Boolean {
        val p = Point(leftTop.x - 7, leftTop.y - 7)
        helper.imageSearch(p, 39, 39, "img\\emptyItem.png")?.let {
            logI("빈칸 발견!")
            return true
        }

        helper.imageSearch(p, 39, 39, "img\\disableItem.png")?.let {
            logI("사용할 수 없는 칸 확인")
            return true
        }

        return false
    }

    /**해당 좌표의 아이템이 빈칸 혹은 사용불가능한 칸인지 확인*/
    fun checkEmptyOrDisable(leftTop: Point): Int {
        val p = Point(leftTop.x - 7, leftTop.y - 7)
        helper.imageSearch(p, 39, 39, "img\\emptyItem.png")?.let {
            logI("빈칸 발견!")
            return 1
        }

        helper.imageSearch(p, 39, 39, "img\\disableItem.png")?.let {
            logI("사용할 수 없는 칸 확인")
            return 2
        }

        return 0
    }

    val emptyItemTemplate = Imgcodecs.imread("img\\emptyItem.png")
    val disableItemTemplate = Imgcodecs.imread("img\\disableItem.png")

    /**checkEmptyOrDisable 빠르게 동작하는 버전
     * @param screenImg 전체 이미지
     * @param targetItem 전체 이미지 내에서 좌표
     * */
    fun checkEmptyOrDisableFast(screenImg: Mat, targetItem: Point): Int {
        // 검색할 이미지 범위 (해당위치에서 살짝 넓게)
        val source = screenImg.colRange(targetItem.x - 5, targetItem.x + emptyItemTemplate.rows() + 5)
            .rowRange(targetItem.y - 5, targetItem.y + emptyItemTemplate.cols() + 5)
        if (helper.imageSearchReturnBoolean(source, emptyItemTemplate)) {
            return 1
        }

        if (helper.imageSearchReturnBoolean(source, disableItemTemplate)) {
            return 2
        }

        return 0
    }

    /**해당 좌표의 아이템이 일반 아이템인지 잠재능력이 있는 아이템인지 확인 확인
     * 아이템에 따라 정확히 판단이 어려운 경우가 있습니다. (아이템이 아이템 칸 테두리까지 넘어오는 이미지인 경우 잠재능력이 있다고 판단한다.)*/
    fun checkItemIsNormal(leftTop: Point): Boolean {
        val x1 = leftTop.x - 7
        val y1 = leftTop.y - 7
        helper.imageSearch(Point(x1, y1), 10, 39, "img\\normalBorder.png", accuracy = 90.9)?.let {
            helper.imageSearch(Point(x1, y1), 39, 10, "img\\normalBorder2.png", accuracy = 90.9)?.let {
//                logI("일반 테두리 발견!")
                return true
            }
        }

        return false
    }

    suspend fun scrollInventory(count: Int) {
        TODO("Not yet implemented")
    }

    /**수행시 인벤토리에 첫칸부터 수행될때마다 다음칸으로 마우스를 이동시킨다.*/
    suspend fun findNextItem() {
        HelperCore().apply {
            if (nextItemPoint == null) {
                isNextItemInventoryExpanded = isInventoryExpanded()
                nextItemPoint = findFirstItemInInventory() ?: return
                nextItemPoint!!.apply {
                    x += 10
                    y += 10
                }
            }

            moveMouseSmoothly(nextItemPoint!!)
            nextItemPosition++

            if (isNextItemInventoryExpanded) {
                //확장된 인벤토리인 경우
                if (nextItemPosition % 32 == 0) {
                    nextItemPoint?.apply {
                        setLocation(x + itemDistance, y - (itemDistance * 7))
                    }
                    return@apply
                }
            }
            nextItemPoint!!.apply {
                if (nextItemPosition % 4 == 0) {
                    setLocation(x - (itemDistance * 3), y + itemDistance)
                } else {
                    setLocation(x + itemDistance, y)
                }
            }


        }
        logI(nextItemPoint.toString())
    }


    fun findInventory(expanded: Boolean = false): Point? {
        val img = if (expanded) "collapseBtn.png" else "meso.png"
        val p = helper.imageSearch("img\\$img")
        return p
    }

    fun isInventoryExpanded(): Boolean {
        val collapseBtn = helper.imageSearch("img\\collapseBtn.png")
        return collapseBtn != null
    }

    /**인벤토리에서 첫번째 아이템 확인 */
    suspend fun findFirstItemInInventory(moveMouse: Boolean = false): Point? {
        var point: Point? = null
        val collapseBtn = helper.imageSearch("img\\collapseBtn.png")
        if (collapseBtn == null) {
            val mesoBtn = helper.imageSearch("img\\meso.png")
            if (mesoBtn == null) {
                logI("인벤토리를 찾을 수 없습니다.")
                return null
            } else {
                mesoBtn.let {
                    it.x = it.x + 4
                    it.y = it.y - 252
                }
                point = mesoBtn
            }
        } else {
            collapseBtn.let {
                it.x = it.x - 160
                it.y = it.y - 340
            }
            point = collapseBtn
        }
        if (moveMouse)
            helper.moveMouseSmoothly(point, 100)

        return point
    }

    /**축소된 인벤토리에서 첫번째 아이템 확인 */
    suspend fun findFirstItemFromCollapsed(moveMouse: Boolean = false): Point? {
        var point: Point? = findInventory(false)
        point?.let {
            //첫번째 칸의 좌상단 좌표
            it.x = it.x + 4
            it.y = it.y - 252
//            log("첫째칸 좌상단 위치: $it")
            if (moveMouse)
                helper.moveMouseSmoothly(it, 100)
        }
        return point
    }

    /**확장된 인벤토리에서 첫번째 아이템 확인 */
    suspend fun findFirstItemFromExpanded(moveMouse: Boolean = false): Point? {
        var point: Point? = findInventory(true)
        point?.let {
            //첫번째 칸의 좌상단 좌표
            it.x = it.x - 160
            it.y = it.y - 340
//            log("첫째칸 좌상단 위치: $it")
            if (moveMouse)
                helper.moveMouseSmoothly(it, 100)
        }
        return point
    }

    /**텍스트를 입력을 할 수 있는 곳에서 텍스트를 지운다.*/
    suspend fun clearText() {
        helper.apply {
            keyPress(KeyEvent.VK_DELETE)
            send(KeyEvent.VK_END)
            repeat(100) {
                kotlinx.coroutines.delay(1)
                send(KeyEvent.VK_BACK_SPACE)
//                keyPress(KeyEvent.VK_DELETE)

            }
        }
    }

    fun clearText2() {
        helper.apply {
            if (isNumberLock) {
                toolkit.setLockingKeyState(KeyEvent.VK_NUM_LOCK, false)
                isNumberLock = false
            }

            autoDelay = 10
            send(KeyEvent.VK_HOME)
            keyPress(KeyEvent.VK_SHIFT)
            send(KeyEvent.VK_END)
            keyRelease(KeyEvent.VK_SHIFT)
            keyPress(KeyEvent.VK_DELETE)
            autoDelay = 0
        }
    }

    /**소비 탭 클릭*/
    suspend fun clickConsumeTab(): Point? {
        helper.apply {
            return imageSearchAndClick("img\\consumeTab.png", maxTime = 100)
        }
    }

    /**인벤토리창을 연다. */
    suspend fun openInventory(repeatDelay: Int = 800):Boolean {

        var tryCount = 0
        while (findInventory() == null) {
            tryCount++
            if(tryCount > 50) return false
            helper.send(inventoryKey)
            delay(repeatDelay.toLong())
        }
        return true
    }

    /**인벤토리창을 닫는다. */
    suspend fun closeInventory(repeatDelay: Int = 800): Boolean {
        var tryCount = 0
        while (findInventory() != null) {
            tryCount++
            if(tryCount > 50) return false
            helper.send(inventoryKey)
            delay(repeatDelay.toLong())
        }
        return true
    }

    /**전문기술 창을 연다.*/
    suspend fun openProductionSkill(): Point? {
        val searchBtn = helper.imageSearch("img\\meister\\searchBtn.png")

        if (searchBtn == null) {
            helper.apply {
                openInventory()
                send(productionSkillKey)
                delayRandom(200, 400)
                val p = imageSearch("img\\meister\\productionSkill.png")?.let {
                    smartClick(Point(it.x + 4, it.y + 21), 28, 10, maxTime = 150)
                    it
                }
            }
            var tryCount = 0
            while (findInventory() != null) {
                closeInventory(1000)
                delay(500) //전문기술창 로딩 (시간이 생각보다 오래 걸린다.)
                if(tryCount++ > 40)
                    return null
            }
            delay(500)

            return openProductionSkill()
        } else {
            return searchBtn
        }
    }

    /**전문기술 창을 닫는다.*/
    suspend fun closeProductionSkill() {
        val searchBtn = helper.imageSearch("img\\meister\\searchBtn.png")

        if (searchBtn != null) {
            helper.send(productionSkillKey)
            delay(500)
            closeProductionSkill()
        }
    }

    /**마지막 아이템의 위치를 찾는다. */
    suspend fun findLastItem(): Point? {
        helper.apply {
            val items = findItems()
            return if (items.isEmpty()) null
            else items.last()
        }
    }

    var lastUsedImg: Mat? = null   //마지막으로 사용된 이미지(Mat 형식)

    /**인벤토리 정보를 생성하여 반환 */
    suspend fun getInventory(): Inventory {
//        val items = arrayListOf<Point>()
        val inventory = Inventory()
        moveMouseLB()
        helper.apply {
            var vx: Int //현재 아이템 x
            var vy: Int  //현재 아이템 y
            val point: Point = findFirstItemInInventory() ?: return@apply soundBeep().also {
                logI("인벤토리 첫칸을 찾는데 실패했습니다.")
            }
            point.let {
                vx = it.x + 2
                vy = it.y + 2
                logI("첫째칸 좌상단 위치: $vx, $vy")
            }

            val isInventoryExpanded = isInventoryExpanded()
            val repeatCount = if (isInventoryExpanded) 128 else 24

            val itemPosList = arrayListOf<Point>()
            for (i in 1..repeatCount) {
                itemPosList.add(Point(vx, vy))
                if (isInventoryExpanded) {
                    //확장된 인벤토리인 경우
                    if (i % 32 == 0) {
                        vx += itemDistance
                        vy -= (itemDistance * 7)
                        continue
                    }
                }

                if (i % 4 == 0) {
                    vx -= (itemDistance * 3)
                    vy += itemDistance
                } else {
                    vx += itemDistance
                }
            }

            //0,0 ~ 인벤토리가 다 보이는곳까지의 이미지
            val screenImg = if (isInventoryExpanded)
                createScreenCapture(Rectangle(0, 0, point.x + 700, point.y + 370)).toMat()
            else
                createScreenCapture(Rectangle(0, 0, point.x + 190, point.y + 330)).toMat()
            lastUsedImg = screenImg

            var lastPosition = 0
            kotlin.run {
                itemPosList.forEachIndexed { index, point ->
                    kotlinx.coroutines.delay(1)
//                    logI("${index+1}번째 : 일반아이템? ${checkItemIsNormal(point)}")
                    val checkResult = checkEmptyOrDisableFast(screenImg, point)
                    if (checkResult > 0) {
                        lastPosition = index - 1
//                        logI("lastPosition: $lastPosition")

                        if (checkResult == 2)
                            return@run

                        if (checkResult == 1) {
                            inventory.list.add(Inventory.Item(point, true))
                        }
                    } else {
                        inventory.list.add(Inventory.Item(point, false))
                    }
                }
            }

            if (lastPosition < 0) {
                logI("아이템을 찾을 수 없습니다.")
            }

            //아이템이 있는곳에 Mat 저장
            inventory.list.forEachIndexed { index, item ->
                if (!item.isEmpty) {
                    item.setMetFrom(screenImg)
                }
            }
        }
        return inventory
    }

    /**인벤토리 첫칸부터 빈칸이 나오기 전까지 아이템의 목록을 반환한다.
     * @param untilBlank false로 할 경우 모든 아이템의 위치를 반환한다. */
    suspend fun findItems(
        untilBlank: Boolean = true,
        blankList: ArrayList<Point>? = null,
        capturedImg: Array<Mat>? = null
    ): ArrayList<Point> {
        val items = arrayListOf<Point>()
        blankList?.clear()
        moveMouseLB()
        helper.apply {
            var vx: Int //현재 아이템 x
            var vy: Int  //현재 아이템 y
            val point: Point = findFirstItemInInventory() ?: return@apply soundBeep().also {
                logI("인벤토리 첫칸을 찾는데 실패했습니다.")
            }
            point.let {
                vx = it.x + 2
                vy = it.y + 2
                logI("첫째칸 좌상단 위치: $vx, $vy")
            }

            val isInventoryExpanded = isInventoryExpanded()
            val repeatCount = if (isInventoryExpanded) 128 else 24

            val itemPosList = arrayListOf<Point>()
            for (i in 1..repeatCount) {
                itemPosList.add(Point(vx, vy))
                if (isInventoryExpanded) {
                    //확장된 인벤토리인 경우
                    if (i % 32 == 0) {
                        vx += itemDistance
                        vy -= (itemDistance * 7)
                        continue
                    }
                }

                if (i % 4 == 0) {
                    vx -= (itemDistance * 3)
                    vy += itemDistance
                } else {
                    vx += itemDistance
                }
            }

            //0,0 ~ 인벤토리가 다 보이는곳까지의 이미지
            val screenImg = if (isInventoryExpanded)
                createScreenCapture(Rectangle(0, 0, point.x + 700, point.y + 370)).toMat()
            else
                createScreenCapture(Rectangle(0, 0, point.x + 190, point.y + 330)).toMat()
            lastUsedImg = screenImg
            capturedImg?.let { it[0] = screenImg }

            var lastPosition = 0
            kotlin.run {
                itemPosList.forEachIndexed { index, point ->
                    kotlinx.coroutines.delay(1)
//                    logI("${index+1}번째 : 일반아이템? ${checkItemIsNormal(point)}")
                    val checkResult = checkEmptyOrDisableFast(screenImg, point)
                    if (checkResult > 0) {
                        lastPosition = index - 1
//                        logI("lastPosition: $lastPosition")

                        if (checkResult == 2 || untilBlank)
                            return@run

                        if (checkResult == 1) {
                            blankList?.add(point)
                        }
                    } else {
                        items.add(point)
                    }
                }
            }

            if (lastPosition < 0) {
                logI("아이템을 찾을 수 없습니다.")
            }
        }
        return items
    }

    @Deprecated("느린 방식이다.")
    private suspend fun findItems_Old(
        untilBlank: Boolean = true,
        blankList: ArrayList<Point> = arrayListOf()
    ): List<Point> {
        val items = arrayListOf<Point>()
        blankList.clear()
        helper.apply {
            var vx: Int //현재 아이템 x
            var vy: Int  //현재 아이템 y
            val point: Point = findFirstItemInInventory() ?: return@apply soundBeep().also {
                logI("인벤토리 첫칸을 찾는데 실패했습니다.")
            }
            point.let {
                vx = it.x + 2
                vy = it.y + 2
                logI("첫째칸 좌상단 위치: $vx, $vy")
            }
            kotlinx.coroutines.delay(1)

            val isInventoryExpanded = isInventoryExpanded()
            val repeatCount = if (isInventoryExpanded) 128 else 24

            val itemPosList = arrayListOf<Point>()
            for (i in 1..repeatCount) {
                itemPosList.add(Point(vx, vy))
                if (isInventoryExpanded) {
                    //확장된 인벤토리인 경우
                    if (i % 32 == 0) {
                        vx += itemDistance
                        vy -= (itemDistance * 7)
                        continue
                    }
                }

                if (i % 4 == 0) {
                    vx -= (itemDistance * 3)
                    vy += itemDistance
                } else {
                    vx += itemDistance
                }
            }

            var lastPosition = 0
            kotlin.run {
                itemPosList.forEachIndexed { index, point ->
                    kotlinx.coroutines.delay(1)
//                    logI("${index+1}번째 : 일반아이템? ${checkItemIsNormal(point)}")
                    val checkResult = checkEmptyOrDisable(point)
                    if (checkResult > 0) {
                        lastPosition = index - 1
//                        logI("lastPosition: $lastPosition")

                        if (checkResult == 2 || untilBlank)
                            return@run

                        if (checkResult == 1) {
                            blankList.add(point)
                        }
                    } else {
                        items.add(point)
                    }
                }
            }

            if (lastPosition < 0) {
                logI("아이템을 찾을 수 없습니다.")
            }
        }
        return items
    }

    /**미확인 아이템 감정하기*/
    suspend fun appraiseItems(untilBlank: Boolean) {
        val items = findItems(untilBlank)
        helper.apply {
            val appraiseBtn = imageSearch("img\\appraiseBtn.png") ?: imageSearch("img\\appraiseBtn2.png") ?: return

            items.forEach {
                smartClick(appraiseBtn, randomRangeX = 10, randomRangeY = 10, maxTime = 100)
                delayRandom(30, 60)
                simpleClick()
                smartClick(it, randomRangeY = 15, randomRangeX = 15, minTime = 400, maxTime = 450)
                sendEnter()
                delayRandom(30, 60)
                sendEnter()
                delayRandom(30, 60)
                sendEnter()
                delayRandom(30, 60)
                sendEnter()
            }
        }
    }

    /**캐릭터의 현재 좌표를 찾는다. (상대 좌표) */
    fun findCharacter(): Point? {
        helper.apply {
            // TODO: 상대좌표 반환 기능을 코어에 포함시키자
            //미니맵 위치에서 검색 수행
            val startPoint = User32.INSTANCE.winGetPos().toRectangle().location
            val myPosition = imageSearch(startPoint, 300, 200, "img\\myChar.png")
//            logI("abs: ${myPosition.toString()}")
            myPosition?.apply {
                x -= startPoint.x
                y -= startPoint.y
            }

//            logI("rel: ${myPosition.toString()}")

            return myPosition
        }

        return null

    }

    /**캐릭터의 위치를 미니맵의 좌표로 이동시킨다.
     * @param destination 캐릭터의 상대 좌표
     * @param range 오차범위 지정 (픽셀)  ex) range = 3 -> x-3 ~ x+3 */
    suspend fun moveCharacter(destination: Point, range: Int = 1): Boolean {
        helper.apply {
            //y좌표 이동
            var tryCount = 0
            while (true) {
                var current = findCharacter()
                if (current != null) {
                    val dy = destination.y - current.y
                    if (dy.absoluteValue <= range) {
                        break
                    }

                    if (dy > 0) {    //밑으로 가야하는 경우
                        moveDownFloor()
                    } else {    //위로 가야하는 경우
                        moveUpFloor()
                    }
                }
                tryCount++
                if(tryCount > 10){
                    releaseAll()
                    return false
                }
            }
            delayRandom(1000,2000)

            //x좌표 이동
            while (true) {
                var current = findCharacter()
                if (current != null) {
                    val dx = destination.x - current.x
                    if (dx.absoluteValue <= range) {
                        releaseAll()
                        break
                    }

                    if (dx < 0) {    //왼쪽으로 가야하는 경우
                        if(pressedKeySet.contains(KeyEvent.VK_RIGHT))
                            keyRelease(KeyEvent.VK_RIGHT)
                        keyPress(KeyEvent.VK_LEFT)
                    } else {    //오른쪽으로 가야하는 경우
                        if(pressedKeySet.contains(KeyEvent.VK_LEFT))
                            keyRelease(KeyEvent.VK_LEFT)
                        keyPress(KeyEvent.VK_RIGHT)
                    }
                    kotlinx.coroutines.delay(10)
                }
                tryCount++
                if(tryCount > 1200000){
                    releaseAll()
                    return false
                }
            }

        }

        return true
    }

    /**밑점프 사용*/
    suspend fun moveDownFloor(afterDelay: Int = 500) {
        helper.apply {
            releaseAll()
            keyPress(KeyEvent.VK_DOWN)
            delayRandom(200, 300)
            send(KeyEvent.VK_ALT)
            delayRandom(100, 200)
            keyRelease(KeyEvent.VK_DOWN)
            delayRandom(afterDelay, afterDelay + 100)
        }
    }

    /**윗점프 사용*/
    suspend fun moveUpFloor(afterDelay: Int = 200) {
        helper.apply {
            keyPress(KeyEvent.VK_ALT)
            delayRandom(50, 60)   //첫 점프에서 키간 딜레이가 있어야한다.
            keyRelease(KeyEvent.VK_ALT)
            delayRandom(50, 100)
            keyPress(KeyEvent.VK_UP)
            delayRandom(50, 100)
            keyPress(KeyEvent.VK_ALT)
            delayRandom(800, 1000)
            keyRelease(KeyEvent.VK_ALT)
            delayRandom(50, 100)
            keyRelease(KeyEvent.VK_UP)
            delayRandom(afterDelay, afterDelay + 100)
        }
    }

    /**광클*/
    suspend fun startAutoClick(windowTitle: String) {
        logI("광클 시작!")
        while (User32.INSTANCE.winIsForeground(windowTitle)) {
            helper.simpleClick()
        }
        logI("광클 종료!")
    }

    /**광클*/
    suspend fun startAutoSend(windowTitle: String, keyCode: Int) {
        logI("광클 시작!")
        while (User32.INSTANCE.winIsForeground(windowTitle)) {
            delay(10)
            helper.send(keyCode)
        }
        logI("광클 종료!")
    }

    suspend fun startAutoSpaceAndEnter(windowTitle: String) {
        logI("광클 시작!")
        var count = 0
        while (User32.INSTANCE.winIsForeground(windowTitle)) {
            count++
            delay(10)
            helper.send(KeyEvent.VK_SPACE)

            if (count % 15 == 0)
                helper.sendEnter()
        }
        logI("광클 종료!")
    }

    /**첫번째 빈칸이 나올때까지 아이템 버리기 */
    suspend fun dropItemUntilBlank(dropDelay: Int) {
        openInventory()
        helper.apply {
            delayRandom(100, 150)
            val items = findItems()
            if (items.isNotEmpty()) {
                val dropPoint = Point(items[0].x - 70, items[0].y)
                items.forEach {
                    smartClick(it, 20, 20, maxTime = 80)
                    smartClick(dropPoint, 20, 20, maxTime = 80)
                    moveMouseSmoothly(it, 50)
                    delayRandom(dropDelay, dropDelay)
                }
            }

        }
    }

    suspend fun pressZ(time: Long) {
        val t = time.toInt() * 1000
        helper.apply {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < t) {
                send(KeyEvent.VK_Z)
                delayRandom(50, 150)
            }

        }
    }

    /**마우스 좌표를 메이플창의 왼쪽 하단으로 이동시킨다. */
    fun moveMouseLB(time: Int = 80) {
        helper.user32.winGetPos().leftBottom().let {
            it.x = it.x + 8
            it.y = it.y - 8
            helper.moveMouseSmoothly(it, t = time)
        }
    }

    /**마우스 좌표를 메이플창의 오른쪽 하단으로 이동시킨다. */
    fun moveMouseRB2(time: Int = 80) {
        helper.user32.winGetPos().rightBottom().let {
            it.x = it.x - 10
            it.y = it.y - 10
            helper.moveMouseSmoothly(it, t = time)
        }
    }

    /**아이템 정렬*/
    suspend fun sortItems() {
        moveMouseLB()
        val mesoBtn = findInventory()
        mesoBtn?.let {
            val sortBtn = Point(it.x + 144, it.y)
            helper.smartClick(sortBtn, 5, 5, maxTime = 50)
            helper.smartClick(sortBtn, 5, 5, maxTime = 50)
            delay(800)
            helper.smartClick(sortBtn, 5, 5, maxTime = 50)
            helper.smartClick(sortBtn, 5, 5, maxTime = 50)
        }
    }

    fun moveWindow(point: Point) {
        User32.INSTANCE.FindWindow(null, "MapleStory")?.let {
            helper.user32.winMove(point, hwnd_ = it)
        }
    }
}