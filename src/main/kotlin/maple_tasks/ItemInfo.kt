package maple_tasks

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import maple_tasks.AdditionalOptionTask.Companion.EAR
import maple_tasks.AdditionalOptionTask.Companion.EYE
import maple_tasks.AdditionalOptionTask.Companion.FACE
import maple_tasks.AdditionalOptionTask.Companion.POCKET
import java.io.Serializable

/**아이템 정보를 담는 class*/
class ItemInfo(
    @Expose val job: String,
    @Expose val category: String,
    @Expose val option: LinkedHashMap<String, Int>
) : Serializable {
    @Expose
    var name = ""

    @Expose
    var price = 0L

    @Expose
    var priceText = ""

    private var priceSimple : String? = null

    @Expose
    var dateText = ""
    var dateTextSimple = ""

    private var gradeKey = ""

    @Expose
    private var grade: Pair<String, Int>? = null
    private var uid: String? = null

    @Expose var isUpgraded : Boolean? = null

    fun getUid(): String {
        if (uid == null) {
            if (category == AdditionalOptionTask.BELT)
                uid = "$job>$name$category>$option>$price>$dateText"
            else
                uid = "$job>$category>$option>$price>$dateText"
        }
        return uid!!
    }

    /**추옵이 몇급인지 반환
     * <"STR", 120> 반환*/
    fun getGrade(): Pair<String, Int> {
        if (grade != null) return grade!!

        val statOrder = hashMapOf(
            Pair(UpgradeItemTask.STR, 4),
            Pair(UpgradeItemTask.LUK, 3),
            Pair(UpgradeItemTask.DEX, 2),
            Pair(UpgradeItemTask.INT, 1)
        )
        val tempOptions = hashMapOf<String, Int>()
        when (job) {
            AdditionalOptionTask.WORRIER -> {
                var str = option[UpgradeItemTask.STR] ?: 0
                str += (option[UpgradeItemTask.ALL] ?: 0) * 10
                str += option[UpgradeItemTask.ATT]?.let { it * 4 } ?: 0

                var hp = option[UpgradeItemTask.HP] ?: 0
                hp += option[UpgradeItemTask.ATT]?.let { it * 140 } ?: 0

                if (hp > 2300 || str < 90) {
                    if (hp > 1000 && str < 50)
                        return Pair(UpgradeItemTask.HP, hp)
                    if (hp > 2900 && str < 79)
                        return Pair(UpgradeItemTask.HP, hp)
                    if (hp > 3100 && str < 83)
                        return Pair(UpgradeItemTask.HP, hp)
                    if (hp > 3540 && str < 91)
                        return Pair(UpgradeItemTask.HP, hp)

                    return Pair(UpgradeItemTask.STR, str)

                } else
                    return Pair(UpgradeItemTask.STR, str)

            }

            AdditionalOptionTask.MAGICIAN -> {
                var int = option[UpgradeItemTask.INT] ?: 0
                int += (option[UpgradeItemTask.ALL] ?: 0) * 10
                int += option[UpgradeItemTask.SPELL]?.let { it * 4 } ?: 0
                return Pair(UpgradeItemTask.INT, int)
            }

            AdditionalOptionTask.ARCHER -> {
                var dex = option[UpgradeItemTask.DEX] ?: 0
                dex += (option[UpgradeItemTask.ALL] ?: 0) * 10
                dex += option[UpgradeItemTask.ATT]?.let { it * 4 } ?: 0
                return Pair(UpgradeItemTask.DEX, dex)
            }

            AdditionalOptionTask.THIEF -> {
                var luc = option[UpgradeItemTask.LUK] ?: 0
                luc += (option[UpgradeItemTask.ALL] ?: 0) * 10
                luc += option[UpgradeItemTask.ATT]?.let { it * 4 } ?: 0
                return Pair(UpgradeItemTask.LUK, luc)
            }

            AdditionalOptionTask.PIRATE -> {
                var dex = option[UpgradeItemTask.DEX] ?: 0
                dex += (option[UpgradeItemTask.ALL] ?: 0) * 10
                dex += option[UpgradeItemTask.ATT]?.let { it * 4 } ?: 0

                var str = option[UpgradeItemTask.STR] ?: 0
                str += option[UpgradeItemTask.ALL]?.let { it * 10 } ?: 0
                str += option[UpgradeItemTask.ATT]?.let { it * 4 } ?: 0

                if (dex - str > 2) {
                    return Pair(UpgradeItemTask.DEX, dex)
                } else {
                    return Pair(UpgradeItemTask.STR, str)
                }
            }
            else -> {
                // hp옵션이 1추 이상인경우를 판단하기 위한 기준
                val targetHP = when (category) {
                    FACE -> 2300
                    EYE -> 2100
                    EAR -> 2700
                    POCKET -> 2900
                    else -> 3150
                }
                // hp옵션이 1추 이상일 때 스텟 최대치가 targetStat 보다 작은지 비교하기 위해 사용하는 변수
                val targetStat = when (category) {
                    FACE, EYE -> 50
                    else -> 71
                }
                var hp = 0  //tempOptions
                option.forEach { (t, u) ->
                    when (t) {
                        UpgradeItemTask.ALL -> {
                            val v = 10 * u
                            tempOptions[UpgradeItemTask.STR] = tempOptions[UpgradeItemTask.STR]?.let { it + v } ?: v
                            tempOptions[UpgradeItemTask.INT] = tempOptions[UpgradeItemTask.INT]?.let { it + v } ?: v
                            tempOptions[UpgradeItemTask.DEX] = tempOptions[UpgradeItemTask.DEX]?.let { it + v } ?: v
                            tempOptions[UpgradeItemTask.LUK] = tempOptions[UpgradeItemTask.LUK]?.let { it + v } ?: v
                        }
                        UpgradeItemTask.ATT -> {
                            val v = 4 * u
                            tempOptions[UpgradeItemTask.STR] = tempOptions[UpgradeItemTask.STR]?.let { it + v } ?: v
                            tempOptions[UpgradeItemTask.DEX] = tempOptions[UpgradeItemTask.DEX]?.let { it + v } ?: v
                            tempOptions[UpgradeItemTask.LUK] = tempOptions[UpgradeItemTask.LUK]?.let { it + v } ?: v
                            hp += (140 * u)
                        }
                        UpgradeItemTask.SPELL -> {
                            val v = 4 * u
                            tempOptions[UpgradeItemTask.INT] = tempOptions[UpgradeItemTask.INT]?.let { it + v } ?: v
                        }
                        UpgradeItemTask.HP -> {
                            hp += u
                        }
                        else -> {
                            tempOptions[t] = tempOptions[t]?.let { it + u } ?: u
                        }
                    }
                }
                var max = 0
                var optionName = ""
                for ((t, u) in tempOptions) {
                    if (u > max) {
                        optionName = t
                        max = u
                    } else if (u == max && optionName != UpgradeItemTask.STR) {
                        //수치가 같을때 옵션 우선순위 적용
                        if (statOrder[t]!! > statOrder[optionName]!!) {
                            optionName = t
                        }
                    }
                }
                if (hp > 0 && max < 10)
                    return Pair(UpgradeItemTask.HP, hp)

                return if (hp < targetHP || max > targetStat)
                    Pair(optionName, max)
                else
                    Pair(UpgradeItemTask.HP, hp)

            }
        }
    }

    /**<스텟><값> 형태의 스트링 반환 (값의 자리수를 4자리로 맞춘다 -> 정렬을 위해서)*/
    fun getGradeKey(): String {
        if (gradeKey.isNullOrEmpty()) {
            gradeKey = "${getGrade().first} ${
                getGrade().second.toString().let {
                    var temp = ""
                    val space = 4 - (it.length)
                    for (i in 1..space) {
                        temp += " "
                    }
                    temp + it
                }
            }"
        }
        return gradeKey
    }

    fun getAllInfo(): String {
        val upgraded = if(isUpgraded == true) "강화된 " else ""
        return "$upgraded${getCategory1()}[${getGradeKey()}]  ${getSimplePrice()}  [${getSimpleDate()}]$option"
    }

    fun getCategory1(): String {
        if (job == AdditionalOptionTask.COMMON)
            return if (category == AdditionalOptionTask.BELT) "[$category][$name]"
            else "[$category]"
        return "[$job][$category]"
    }

    fun getInfoText(): String {
        return "${getCategory1()}[${getGrade().first} ${getGrade().second}]$option"
    }

    fun getSimplePrice(): String {
        if (priceSimple.isNullOrEmpty()) {
            val p = price / 10000
            priceSimple = if (p > 9999) {
                "${p / 10000}억 ${p % 10000}만"
            } else {
                String.format("%4s만", p).replace(" ", "_")
            }
        }
        return priceSimple!!
    }

    fun getSimpleDate(): String {
        if (dateTextSimple.isNullOrEmpty())
            dateTextSimple = dateText.substring(5).replace('-', '.')
        return dateTextSimple
    }

    fun getPriceAndDateAndOption(): String {
        val upgraded = if(isUpgraded == true) "강화됨 " else ""
        return "[$priceText][${getSimpleDate()}]$upgraded$option"
    }

    fun toDB(): String {
        return gson.toJson(this)
    }

    companion object {
        private val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        fun fromJson(json: String): ItemInfo {
            return gson.fromJson(json, ItemInfo::class.java)
        }
    }

}