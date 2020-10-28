package maple_tasks

import changeBlackAndWhite
import logI
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.awt.Point

class Inventory {
    val list = arrayListOf<Item>()
    fun setItems(items: List<Item>) {
        list.clear()
        list.addAll(items)
    }

    fun getItemList(): ArrayList<Item> {
        val itemList = arrayListOf<Item>()
        list.forEach { if(!it.isEmpty) itemList.add(it)}
        return itemList
    }

    fun findIndexFromPoint(point:Point): Int {
        list.forEachIndexed { index, item ->
            if(item.point == point) {
                return index
            }
        }
        return -1
    }

    fun isEmpty():Boolean {
        for (i in list)
            if(!i.isEmpty) return false

        return true
    }

    fun isNotEmpty():Boolean {
        for (i in list)
            if(!i.isEmpty) return true

        return false
    }

    fun removeItem(item:Item){
        list.remove(item)
    }

    fun getFirstItem(): Item? {
        list.forEach {
            if (!it.isEmpty) return it
        }
        return null
    }

    fun getFirstEmpty(): Item? {
        list.forEach {
            if (it.isEmpty) return it
        }
        return null
    }

    fun removeFirstItem() {
        getFirstItem()?.let { removeItem(it) }
    }

    data class Item(val point: Point, var isEmpty: Boolean) {
        var mat: Mat? = null

        fun clear(){
            isEmpty = true
            mat = null
        }

        fun setMet(newMat: Mat) {
            mat = newMat
            isEmpty = false
        }

        fun setMetFrom(screenImg: Mat) {
//            Imgcodecs.imwrite("screenImg.png", screenImg)
//            logI("${point.x},${point.y} 저장")
            setMet(screenImg.colRange(point.x, point.x+20).rowRange(point.y, point.y+20))
            mat?.changeBlackAndWhite()
        }
    }
}