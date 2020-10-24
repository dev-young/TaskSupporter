package maple_tasks

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import logI
import java.io.*
import java.lang.Exception

class Settings {
    var delayOnCheckOptions = 30L   // 아이템 추옵 확인시 마우스 우클릭 후 대기 시간
    var saveErrorWhenCheckOption = true // 아이템 추옵 확인시 오류났을때 이미지 파일 저장 여부

    // 여러개의 추옵 확인시 이전의 아이템과 현재 측정한 아이템이 같은 아이템으로 판단될때 로그를 남길지 여부
    var logFindSameOptionWhenCheckOption = true

    var synthesizeMouseDelay = 100  //합성시 마우스 딜레이

    companion object {
        var instance = Settings()
        private var lastSaved = ""
        fun load(){
            GlobalScope.launch (Dispatchers.IO){
                val f = File("settings")
                if(!f.exists()) {
                    save()
                    return@launch
                }
                val reader = BufferedReader(
                    InputStreamReader(
                        FileInputStream("settings"), "euc-kr"
                    )
                )
                val json = reader.readText()
                try {
                    instance = Gson().fromJson(json, Settings::class.java)
                } catch (e:Exception) {
                    logI("settings 값 불러오기 오류 ${e.message}")
                }
                lastSaved = json

            }
        }

        fun save(){
            GlobalScope.launch(Dispatchers.IO) {
                val json = Gson().toJson(instance)
                if(json == lastSaved) return@launch
                lastSaved = json

                val file = File("settings")
                FileWriter(file, false).let {
                    it.write(json)
                    it.flush()
                    it.close()
                }
            }

        }
    }
}