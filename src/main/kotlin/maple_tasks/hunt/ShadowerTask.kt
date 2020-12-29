package maple_tasks.hunt

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.delay
import logH
import tornadofx.c
import java.awt.event.KeyEvent

class ShadowerTask(var limit : HuntRange,
                   var attackDelayMin: SimpleIntegerProperty
) : HuntBaseTask() {
    init {
        Platform.runLater {
            attackDelayMin.value = 1700
            limit.left.value = 40
            limit.right.value = 130
        }
    }
    var attackCode1 = KeyEvent.VK_CONTROL //암살
    var attackCode2 = KeyEvent.VK_SHIFT //메잌
    var attackCode3 = KeyEvent.VK_F10 //부스
    var attackCode4 = KeyEvent.VK_HOME //서든레이든
    var attackCode5 = KeyEvent.VK_PAGE_DOWN //절개
    var attackCodeMove = KeyEvent.VK_X //어썰트

    var buffList = arrayListOf<Buff>().apply {
        add(Buff(KeyEvent.VK_1, 185, delayBefore = 800))
        add(Buff(KeyEvent.VK_2, 185))
        add(Buff(KeyEvent.VK_3, 185))
//        add(Buff(KeyEvent.VK_INSERT, 62, delayBefore = 1000))
        add(Buff(KeyEvent.VK_PAGE_UP, 62))
        add(Buff(attackCode5, 15, 200, 200, withStop = false))
        add(Buff(KeyEvent.VK_DELETE, 25, 200, 200, withStop = false))
    }

    suspend fun waitZen(){
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
                if(helper.random.nextBoolean())
                    leftRelease()
                buff()
//                checkRuneBountyAndVioleta()
                delay(50)
            }
            leftRelease()
            waitZen()
            moveAttack(KeyEvent.VK_RIGHT)
            while (limit.right.value > getCharacterPos()?.x ?: 100) {
                //오른쪽으로 이동
                rightPress()
                attack()
                if(helper.random.nextBoolean())
                    rightRelease()
                buff()
//                checkRuneBountyAndVioleta()
                delay(50)
            }
            rightRelease()
            waitZen()
            moveAttack(KeyEvent.VK_LEFT)
        }
    }

    suspend fun attack() {
        attack4()   //서든
        if(helper.random.nextInt(100) < 81)
            attack2()   //암메
        else
            attack1()
    }

    val jumpDelayMin = 1300
    var lastJump = 0L
    override suspend fun doubleJump(delayMin: Int, delayMax: Int) {
        val current = System.currentTimeMillis()
        if (current - lastJump > jumpDelayMin) {
            super.doubleJump2()
            lastJump = current
        }
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
            helper.delayRandom(500, 550)
            isAttacking = false
        }
    }   // 더블점프 공격

    suspend fun attack2() {
        if (!isAttacking) {
            isAttacking = true
            doubleJump2()
            helper.delayRandom(30, 50)
            send(attackCode1)
            helper.delayRandom(5, 15)
            send(attackCode2)

            if (helper.random.nextBoolean()){
                helper.delayRandom(500, 550)
                jump()
                helper.delayRandom(60, 90)
                send(attackCode1)
                helper.delayRandom(5, 15)
                send(attackCode2)
            }

            helper.delayRandom(500, 550)
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

            if (helper.random.nextBoolean()){
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
        val current = System.currentTimeMillis()
        if (current - lastAttack > attackDelayMin.value) {
            jump()
            helper.delayRandom(55, 100)
            send(attackCode1)
            send(attackCode2)
            lastAttack = current
        }
    }   // 점프 공격

    //써든레이드
    var attack4Time = 0L
    suspend fun attack4(){
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
    suspend fun moveAttack(directionKey:Int = KeyEvent.VK_UP){
        val current = System.currentTimeMillis()
        if(current - moveAttackTime > moveAttackDelay) {
            jump()
            helper.delayRandom(55, 100)
            upPress()
            helper.keyPress(directionKey)
            send(attackCodeMove)
            helper.keyRelease(directionKey)
            upRelease()

            moveAttackTime = current
        }
    }

    val buffCheckDelay = 5000   //버프 확인 주기
    var buffCheckTime = 0L
    suspend fun buff(){
        val current = System.currentTimeMillis()
        if(current - buffCheckTime > buffCheckDelay) {
            buffList.forEach {
                it.useIfEnable(this, current)
            }
            buffCheckTime = current
        }

    }

    override suspend fun doubleJump2() {
        send(jumpKey)
        helper.delayRandom(24, 50)
        send(jumpKey2)
    }
}