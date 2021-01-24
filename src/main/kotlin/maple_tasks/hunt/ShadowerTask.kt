package maple_tasks.hunt

import helper.HWKey
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.delay

class ShadowerTask(
    var limit: HuntRange,
    var attackDelayMin: SimpleIntegerProperty
) : HuntBaseTask() {
    init {
        Platform.runLater {
            attackDelayMin.value = 1700
            limit.left.value = 40
            limit.right.value = 130
        }
        ropeConnectKey = HWKey.VK_V
    }

    var attackCode1 = HWKey.VK_CONTROL //암살
    var attackCode2 = HWKey.VK_SHIFT //메잌
    var attackCode3 = HWKey.VK_F10 //부스
    var attackCode4 = HWKey.VK_HOME //서든레이든
    var attackCode5 = HWKey.VK_PAGE_DOWN //절개
    var attackCodeMove = HWKey.VK_X //어썰트

    var buffList = arrayListOf<Buff>().apply {
        add(Buff(HWKey.VK_1, 185, delayBefore = 800))
        add(Buff(HWKey.VK_2, 185))
        add(Buff(HWKey.VK_3, 185))
        add(Buff(HWKey.VK_8, 100, 200, 200, withStop = false))
//        add(Buff(HWKey.VK_INSERT, 62, delayBefore = 1000))
        add(Buff(HWKey.VK_PAGE_UP, 62))
        add(Buff(attackCode5, 15, 200, 200, withStop = false))
        add(Buff(HWKey.VK_DELETE, 25, 200, 200, withStop = false))
    }

    suspend fun waitZen() {
        waitZen(limit.zenDelay.value)
    }

    suspend fun startHorizontal() {
        limit.zenDelay.value = 7500
        Platform.runLater {
            attackDelayMin.value = 1700
            limit.left.value = 55
            limit.right.value = 145
        }
        while (true) {
            delay(1)
            while (limit.left.value < getCharacterPos()?.x ?: 100) {
                //왼쪽으로 이동
                leftPress()
                attack()
                if (helper.random.nextBoolean())
                    leftRelease()
                buff()
//                checkRuneBountyAndVioleta()
                delay(50)
            }
            leftRelease()
            waitZen()
            moveAttack(HWKey.VK_RIGHT)
            while (limit.right.value > getCharacterPos()?.x ?: 100) {
                //오른쪽으로 이동
                rightPress()
                attack()
                if (helper.random.nextBoolean())
                    rightRelease()
                buff()
//                checkRuneBountyAndVioleta()
                delay(50)
            }
            rightRelease()
            waitZen()
            moveAttack(HWKey.VK_LEFT)
        }
    }

    //본색을 드러내는곳1
    suspend fun startLachelein1() {
        limit.zenDelay.value = 1500
        Platform.runLater {
            attackDelayMin.value = 450
            limit.left.value = 50
            limit.right.value = 150
        }

        //3층 95,  2층 114
        while (true) {
            var currentPosition = getCharacterPos()
            delay(1)
            while (limit.left.value < currentPosition?.x ?: 100) {
                //왼쪽으로 이동
                leftPress()
                attack()
                if (helper.random.nextBoolean())
                    leftRelease()
                buff()
                delay(50)
                currentPosition = getCharacterPos()
            }
            leftRelease()
            waitZen()
            currentPosition?.let {
                if (it.y > 115) {
                    if (!moveAttack(HWKey.VK_RIGHT)) {
                        rightPress()
                        helper.delayRandom(80, 100)
                        ropeConnect()
                        rightRelease()
                        helper.delayRandom(700, 900)
                    }
                } else {
                    downJump(500)
                }
            }



            while (limit.right.value > currentPosition?.x ?: 100) {
                //오른쪽으로 이동
                rightPress()
                attack()
                if (helper.random.nextBoolean())
                    rightRelease()
                buff()
                delay(50)
                currentPosition = getCharacterPos()
            }
            rightRelease()
            waitZen()
            currentPosition?.let {
                if (it.y > 115) {
                    if (!moveAttack(HWKey.VK_LEFT)) {
                        leftPress()
                        helper.delayRandom(80, 100)
                        ropeConnect()
                        leftRelease()
                        helper.delayRandom(700, 900)
                    }
                } else {
                    downJump(500)
                }
            }

        }
    }

    //물과 햇살의 숲
    suspend fun startArcana1() {
        limit.zenDelay.value = 1500
        Platform.runLater {
            attackDelayMin.value = 450
            limit.left.value = 50
            limit.right.value = 150
        }

        while (true) {
            var currentPosition = getCharacterPos()
            delay(1)
            while (limit.left.value < currentPosition?.x ?: 100) {
                //왼쪽으로 이동
                leftPress()
                if (currentPosition != null && currentPosition.y < 105 && currentPosition.x > 93 && currentPosition.x < 130) {
                    attack3()
                }
                attack()
                if (helper.random.nextBoolean())
                    leftRelease()

                buff()
                delay(50)
                currentPosition = getCharacterPos()
            }
            leftRelease()
            waitZen()
            currentPosition?.let {
                if (it.y > 115) {
                    if (!moveAttack(HWKey.VK_RIGHT)) {
                        rightPress()
                        helper.delayRandom(80, 100)
                        ropeConnect()
                        rightRelease()
                        helper.delayRandom(700, 900)
                    }
                } else {
                    downJump(500)
                }
            }

            while (limit.right.value > currentPosition?.x ?: 100) {
                //오른쪽으로 이동
                rightPress()
                if (currentPosition != null && currentPosition.y < 105 && currentPosition.x > 83 && currentPosition.x < 117) {
                    attack3()
                }
                attack()
                if (helper.random.nextBoolean())
                    rightRelease()

                buff()
                delay(50)
                currentPosition = getCharacterPos()
            }
            rightRelease()
            waitZen()
            currentPosition?.let {
                if (it.y > 115) {
                    if (!moveAttack(HWKey.VK_LEFT)) {
                        leftPress()
                        helper.delayRandom(80, 100)
                        ropeConnect()
                        leftRelease()
                        helper.delayRandom(700, 900)
                    }
                } else {
                    downJump(500)
                }
            }

        }
    }

    suspend fun attack() {
        attack4()   //서든
        if (helper.random.nextInt(100) < 85)
            attack2()   //암메
        else
            attack1()
    }

    var isAttacking = false
    var lastAttack = 0L

    suspend fun attack1() {
        if (!isAttacking) {
            isAttacking = true
            doubleJump2()
            helper.delayRandom(60, 100)
            send(attackCode1)
            helper.delayRandom(5, 15)
            send(attackCode2)
            helper.delayRandom(attackDelayMin.value, attackDelayMin.value + 80)
            isAttacking = false
        }
    }   // 더블점프 공격

    suspend fun attack2() {
        if (!isAttacking) {
            isAttacking = true
            doubleJump2(10, 20)
            helper.delayRandom(5, 10)
            send(attackCode1)
            helper.delayRandom(5, 15)
            send(attackCode2)

            if (helper.random.nextInt(100) > 72) {
                helper.delayRandom(500, 550)
                jump()
                helper.delayRandom(60, 90)
                send(attackCode1)
                helper.delayRandom(5, 15)
                send(attackCode2)
            }
            helper.delayRandom(attackDelayMin.value, attackDelayMin.value + 80)
            isAttacking = false
        }
    }   // 더블점프 공격 후 점프공격

    suspend fun attack1_() {
        val current = System.currentTimeMillis()
        if (current - lastAttack > attackDelayMin.value) {
            doubleJump2()
            helper.delayRandom(60, 100)
            send(attackCode1)
            helper.delayRandom(5, 15)
            send(attackCode2)
            lastAttack = current
        }
    }   // 더블점프 공격

    suspend fun attack2_() {
        val current = System.currentTimeMillis()
        if (current - lastAttack > attackDelayMin.value) {
            doubleJump2()
            helper.delayRandom(30, 50)
            send(attackCode1)
            helper.delayRandom(5, 15)
            send(attackCode2)

            if (helper.random.nextBoolean()) {
                helper.delayRandom(500, 550)
                jump()
                helper.delayRandom(60, 90)
                send(attackCode1)
                helper.delayRandom(5, 15)
                send(attackCode2)
            }
            lastAttack = current
        }
    }   // 더블점프 공격 후 점프공격

    suspend fun attack3() {
        if (!isAttacking) {
            isAttacking = true
            jump()
            helper.delayRandom(60, 90)
            send(attackCode1)
            helper.delayRandom(5, 15)
            send(attackCode2)
            helper.delayRandom(attackDelayMin.value, attackDelayMin.value + 80)
            isAttacking = false
        }
    }   // 점프 공격

    //써든레이드
    var attack4Time = 0L
    suspend fun attack4() {
        val current = System.currentTimeMillis()
        if (current - attack4Time > 35000) {
            doubleJump2()
            helper.delayRandom(55, 100)
            send(attackCode4)
            attack4Time = current
        }
    }

    //어썰트공격
    val moveAttackDelay = 20 * 1000
    var moveAttackTime = 0L
    suspend fun moveAttack(directionKey: Int = HWKey.VK_UP): Boolean {
        val current = System.currentTimeMillis()
        if (current - moveAttackTime > moveAttackDelay) {
            jump()
            helper.delayRandom(80, 90)
            upPress()
            helper.keyPress(directionKey)
            send(attackCodeMove)
            helper.keyRelease(directionKey)
            upRelease()

            moveAttackTime = current
            return true
        }
        return false
    }

    val buffCheckDelay = 5000   //버프 확인 주기
    var buffCheckTime = 0L
    suspend fun buff() {
        val current = System.currentTimeMillis()
        if (current - buffCheckTime > buffCheckDelay) {
            buffList.forEach {
                it.useIfEnable(this, current)
            }
            buffCheckTime = current
        }

    }
}