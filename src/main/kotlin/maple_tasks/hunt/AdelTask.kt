package maple_tasks.hunt

import helper.HWKey
import kotlinx.coroutines.delay

class AdelTask(var limit : HuntRange) : HuntBaseTask() {
    suspend fun startHorizontal() {
        while (true) {
            delay(1)
            while (limit.left.value < getCharacterPos()?.x ?: 100) {
                //왼쪽으로 이동
                leftPress()
                attack()
                delay(1)
            }
            leftRelease()

            delay(1)
            while (limit.right.value > getCharacterPos()?.x ?: 100) {
                //오른쪽으로 이동
                rightPress()
                attack()
                delay(1)
            }
            rightRelease()
        }
    }
    val jumpDelayMin = 1300
    var lastJump = 0L
    override suspend fun doubleJump(delayMin: Int, delayMax: Int) {
        val current = System.currentTimeMillis()
        if(current - lastJump > jumpDelayMin){
            super.doubleJump2(delayMin, delayMax)
            lastJump = current
        }
    }

    val attackDelayMin = 1000
    var lastAttack = 0L
    suspend fun attack(){
        val current = System.currentTimeMillis()
        if(current - lastAttack > attackDelayMin){
            doubleJump2()
            helper.delayRandom(10, 50)
            send(HWKey.VK_CONTROL)
            lastAttack = current
        }
    }


}