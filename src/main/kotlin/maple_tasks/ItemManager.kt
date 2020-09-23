package maple_tasks

import logI
import java.io.*
import java.util.*

class ItemManager {
    companion object {
        private val comparator = kotlin.Comparator<String> { o1, o2 ->
            if(o1[0] == o2[0]){
                -o1.compareTo(o2)
            } else {
                if(o1[0] == 'H')
                    return@Comparator 1
                if(o2[0] == 'H')
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
                        put(gradeKey,arrayListOf())}
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
            info.name = "쿰의벨"
            cateList.add(info.getCategory1())
            info.name = "골든클"
            cateList.add(info.getCategory1())
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
            itemMap.values.forEach {
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