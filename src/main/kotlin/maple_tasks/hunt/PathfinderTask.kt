package maple_tasks.hunt

import helper.HWKey
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.delay
import logH
import tornadofx.c

class PathfinderTask(
    var limit: HuntRange,
    var attackDelayMin: SimpleIntegerProperty
) : HuntBaseTask() {
    init {
        Platform.runLater {
            attackDelayMin.value = 930
            limit.left.value = 30
            limit.right.value = 190
        }
    }
    var attackCode1 = HWKey.VK_1 //블디
    var attackCode2 = HWKey.VK_SHIFT //미스텔
    var attackCode3 = HWKey.VK_END //어썰트
    var attackCode4 = HWKey.VK_HOME //레조넌스
    var attackCodeMove = HWKey.VK_X //트랜지션

    var buffList = arrayListOf<Buff>().apply {
        add(Buff(HWKey.VK_2, 220))
    }


    //안개 낀 숲
    suspend fun startMistyForest() {
        while (true) {
            var currentPosition = getCharacterPos()
            delay(1)
            while (limit.left.value < currentPosition?.x ?: 100) {
                //왼쪽으로 이동
                leftPress()
                attack()
                if(helper.random.nextBoolean())
                    leftRelease()
//                buff()
                checkRuneAndVioleta()
                delay(10)
                currentPosition = getCharacterPos()
            }
            leftRelease()
            send(HWKey.VK_RIGHT, 100, 120)
//            moveAttack(directionKey2 = HWKey.VK_RIGHT)
            while (limit.right.value > currentPosition?.x ?: 100) {
                //오른쪽으로 이동
                rightPress()
                currentPosition?.let {
                    if(it.y > 122){ //1층인 거
                        delay(800)
                        moveAttack()
                    }
                }
                attack()
                if(helper.random.nextBoolean())
                    rightRelease()
//                buff()
                checkRuneAndVioleta()
                delay(10)
                currentPosition = getCharacterPos()
            }
            rightRelease()
            send(HWKey.VK_LEFT, 100, 120)
        }
    }

    suspend fun attack() {
        if(helper.random.nextInt(100) < 85) {
            attack3()
            attack1()
        } else {
            attack4()
            attack2()
        }

    }


    var lastAttack = 0L
    suspend fun attack1() {
        val current = System.currentTimeMillis()
        if (current - lastAttack > attackDelayMin.value) {
            doubleJump(50, 70)
            helper.delayRandom(20, 40)
            send(attackCode1)
            lastAttack = current
            helper.delayRandom(30, 60)
        }
    }   // 더블점프 공격

    suspend fun attack2() {
        val current = System.currentTimeMillis()
        if (current - lastAttack > attackDelayMin.value) {
            doubleJump(50, 100)
            helper.delayRandom(30, 60)
            send(attackCode1)
            helper.delayRandom(400, 500)
            send(attackCode1)
            helper.delayRandom(400, 500)
            lastAttack = current
        }
    }   // 더블점프 공격 후 점프공격

    //어설트
    var attack3Time = 0L
    suspend fun attack3() {
        val current = System.currentTimeMillis()
        if (current - attack3Time > 20000) {
            doubleJump()
            helper.delayRandom(55, 100)
            send(attackCode3)
            attack3Time = current
            helper.delayRandom(200, 300)
        }
    }

    //레조넌스
    var attack4Time = 0L
    suspend fun attack4() {
        val current = System.currentTimeMillis()
        if (current - attack4Time > 10000) {
            doubleJump()
            helper.delayRandom(55, 100)
            send(attackCode4)
            attack4Time = current
            helper.delayRandom(50, 100)
        }
    }

    //트렌지션
    val moveAttackDelay = 1500
    var moveAttackTime = 0L
    suspend fun moveAttack(directionKey1: Int = HWKey.VK_UP) {
        val current = System.currentTimeMillis()
        if (current - moveAttackTime > moveAttackDelay) {
            helper.delayRandom(50, 80)
            jump()
            helper.delayRandom(50, 80)
            helper.keyPress(directionKey1)
            helper.delayRandom(50, 80)
            jump()
            helper.delayRandom(200, 250)
            send(attackCodeMove)

            helper.delayRandom(25, 50)
            helper.keyRelease(directionKey1)

            moveAttackTime = current
        }
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