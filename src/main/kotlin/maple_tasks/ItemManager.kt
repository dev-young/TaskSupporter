package maple_tasks

import logI
import java.io.*
import java.util.*
import kotlin.math.max

class ItemManager {
    companion object {
        private val comparator = kotlin.Comparator<String> { o1, o2 ->
            if (o1[0] == o2[0]) {
                -o1.compareTo(o2)
            } else {
                if (o1[0] == 'H')
                    return@Comparator 1
                if (o2[0] == 'H')
                    return@Comparator -1

                -o1[0].compareTo(o2[0])
            }
        }
        val categoryMap = linkedMapOf<String, SortedMap<String, ArrayList<ItemInfo>>>().apply {
            AdditionalOptionTask.JOBS.forEach { job ->
                AdditionalOptionTask.CATES.forEach {
                    put("[$job][$it]", TreeMap<String, ArrayList<ItemInfo>>(comparator))
                }
            }
        }

        val itemMap = hashMapOf<String, ItemInfo>()
        val sortedList = arrayListOf<ItemInfo>()
    }


    fun add(info: ItemInfo) {
        if (!itemMap.contains(info.getUid())) {
            val cate1 = info.getCategory1()
            val gradeKey = info.getGradeKey()
            val gradeMap =
                categoryMap.computeIfAbsent(cate1) {
                    TreeMap<String, ArrayList<ItemInfo>>(comparator).apply {
                        put(gradeKey, arrayListOf())
                    }
                }
            val list = gradeMap.computeIfAbsent(gradeKey) { arrayListOf() }
            list.add(info)

            itemMap[info.getUid()] = info
        }
    }

    /**정보들을 가져와서 중복안되도록 저장한다. */
    fun addAll(infoList: Collection<ItemInfo>) {
        infoList.forEach { info ->
            add(info)
        }
    }

    fun getSortedList(): ArrayList<ItemInfo> {
        sortedList.clear()
        categoryMap.values.forEach { gradeMap ->
            gradeMap.values.forEach {
                it.sortWith(kotlin.Comparator { o1, o2 -> -o1.price.compareTo(o2.price) })
                sortedList.addAll(it)
            }
        }

        return sortedList
    }

    fun clear() {
        categoryMap.values.forEach { it.clear() }
        itemMap.clear()
        sortedList.clear()
    }

    fun search(info: ItemInfo): ArrayList<ItemInfo> {
        val result = arrayListOf<ItemInfo>()

        val cateList = arrayListOf<String>()
        if (info.category == AdditionalOptionTask.BELT) {
            // 두 종류의 벨트를 다 검색할 경우 주석 해제
//            info.name = "쿰의벨"
//            cateList.add(info.getCategory1())
//            info.name = "골든클"
//            cateList.add(info.getCategory1())
            cateList.add(info.getCategory1())   // 두 종류의 벨트를 다 검색할 경우 주석
        } else
            cateList.add(info.getCategory1())
        val gradeKey = info.getGradeKey()

        cateList.forEach { cate1 ->
            val gradeMap = categoryMap[cate1]
            gradeMap?.let {
                gradeMap.computeIfAbsent(gradeKey) { arrayListOf() }
                val keys = gradeMap.keys.toList()
                for (i in keys.indices) {
                    val key = keys[i]
                    if (key == gradeKey) {
                        intArrayOf(i - 1, i, i + 1).forEach {
                            if (it > -1 && it < keys.size) {
                                gradeMap[keys[it]]?.let {
                                    it.sortWith(kotlin.Comparator { o1, o2 -> -o1.price.compareTo(o2.price) })
                                    result.addAll(it)
                                }

                            }
                        }
                        break
                    }

                }
            }
        }


        return result
    }

    /**한단계 높은 추옵은 최근 10개까지만 표시
     * 해당 추옵은 최근 20개까지만 표시
     * 한단계 낮은 추옵은 최근 15개까지만 표시*/
    fun smartSearch(info: ItemInfo): ArrayList<ItemInfo> {
        val result = arrayListOf<ItemInfo>()

        val gradeKey = info.getGradeKey()

        info.getCategory1().let { cate1 ->
            val gradeMap = categoryMap[cate1]

            gradeMap?.let {
                gradeMap.computeIfAbsent(gradeKey) { arrayListOf() }
                val keys = gradeMap.keys.toList()
                for (i in keys.indices) {
                    val key = keys[i]
                    if (key == gradeKey) {
                        intArrayOf(i - 1, i, i + 1).forEachIndexed { index, i ->
                            if (i > -1 && i < keys.size) {
                                gradeMap[keys[i]]?.let {
                                    val temp = arrayListOf<ItemInfo>()
                                    var maxCount = 20
                                    if (index == 0) maxCount = 10   //더 높은 추옵인 경우
                                    else if (index == 2) maxCount = 15  //추옵이 더 낮은 경우

                                    if(it.size > maxCount) {
                                        it.sortWith(kotlin.Comparator { o1, o2 -> -o1.dateText.compareTo(o2.dateText) })
                                        for (idx in 0 until  maxCount) temp.add(it[idx])    //maxCount 만큼만 temp에 추가
                                    } else {
                                        temp.addAll(it)
                                    }

                                    temp.sortWith(kotlin.Comparator { o1, o2 -> -o1.price.compareTo(o2.price) })
                                    result.addAll(temp)
                                }

                            }
                        }
                        break
                    }

                }
            }
        }


        return result
    }

    /** 최근 팔린 아이템들10개 중 비싼 아이템 5개의 평균 가격
     * 아이템에 공,마 추옵이 붙어있는 경우 싼 아이템 5개의 평균가격*/
    fun findBestPrice(info: ItemInfo, useLog:Boolean = false): Long? {

        val gradeKey = info.getGradeKey()

        val gradeMap = categoryMap[info.getCategory1()]
        gradeMap?.let {
            it[gradeKey]?.let {
                it.removeIf { it.isUpgraded ?: false }
                if (it.isEmpty()) return null
                val avgAll = it.sumByDouble { it.price.toDouble() }.div(it.size)
                val max = (avgAll * 2.5).toLong()  //평균의 2.5배가 넘는 가격은 비 정상적인 가격으로 판단
                val min = (avgAll / 2.5).toLong()  //평균의 반도 안되는 가격은 비 정상적인 가격으로 판단

                //최근 날짜로 정렬
                it.sortWith(kotlin.Comparator { o1, o2 -> -o1.dateText.compareTo(o2.dateText) })

                if(useLog) logI("전체 평균 대상")
                if(useLog) it.forEach { logI("${it.dateText} ${it.priceText}") }

                val targetList = arrayListOf<ItemInfo>()    // 10개를 저장할 변수
                for (item in it) {
                    if (item.price in (min)..max) {
                        targetList.add(item)
                        if (targetList.size > 9)
                            break
                    }
                }
                if(useLog) logI("전체 평균: ${avgAll.toLong()}")

                //가격순으로 정렬
                if (info.getAttackSpell() > 1) {
                    targetList.sortBy { it.price }
                } else targetList.sortByDescending { it.price }

                if(useLog) logI("10개 평균 대상")
                if(useLog) targetList.forEach { logI("${it.dateText} ${it.priceText}") }

                var sum = 0L
                var count = 0
                if(useLog) logI("최종 평균 대상")
                for (item in targetList) {
                    if(useLog) logI("${item.dateText} ${item.priceText}")
                    sum += item.price
                    count++
                    if (count > 4)
                        break
                }
                if(useLog) logI("최종 sum:$sum count:$count avg:${sum / count}")
                return sum / count
            }
        }
        return null
    }

    fun loadFromTxt(fileName: String): MutableCollection<ItemInfo> {
        val reader = BufferedReader(
            InputStreamReader(
                FileInputStream("$fileName DB"), "euc-kr"
            )
        )
        reader.forEachLine {
            val item = ItemInfo.fromJson(it)
            add(item)
        }


        return itemMap.values
    }

    fun saveToDB(fileName: String = "메이플시세목록", overwrite: Boolean) {
        if (itemMap.isEmpty()) return
        val file = File("$fileName DB")
        val bw = BufferedWriter(FileWriter(file, !overwrite))

        // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
        // 이미 파일이 존재할 경우 덮어쓰기로 저장
        try {
            itemMap.values.sortedBy { it.dateText }.forEach {
                bw.write(it.toDB())
                bw.newLine()
            }
        } catch (e: FileNotFoundException) {
            logI("FileNotFound: $fileName")
        }

        bw.flush()
        bw.close()

    }

    fun saveToTxt(fileName: String = "메이플시세목록", saveDB: Boolean = true) {
        val file = File("$fileName.txt")
        val bw = BufferedWriter(FileWriter(file, false))

        // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
        // 이미 파일이 존재할 경우 덮어쓰기로 저장
        try {
            var lastCategory = ""
            var lastGrade = ""
            sortedList.forEach {
                if (lastCategory != it.getCategory1()) {
                    bw.newLine()
                    bw.newLine()
                    lastCategory = it.getCategory1()
                    bw.write(lastCategory)
                    lastGrade = ""
                }

                if (lastGrade != it.getGradeKey()) {
                    bw.newLine()
                    lastGrade = it.getGradeKey()
                    bw.write("  [${it.getGrade().first} ${it.getGrade().second}]")
                    bw.write("  ")
                }

                bw.write("${it.getPriceAndDateAndOption()} > ")
            }
        } catch (e: FileNotFoundException) {
            logI("FileNotFound: $fileName")
        }

        bw.flush()
        bw.close()

    }
}