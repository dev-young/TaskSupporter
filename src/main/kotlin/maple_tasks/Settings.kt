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
    var enableF4OnlyForeground = false   //f4를 통해 종료시 Foreground 상태에 있을때만 활성화하기
    var delayOnCheckOptions = 30L   // 아이템 추옵 확인시 마우스 우클릭 후 대기 시간
    var saveErrorWhenCheckOption = true // 아이템 추옵 확인시 오류났을때 이미지 파일 저장 여부

    // 여러개의 추옵 확인시 이전의 아이템과 현재 측정한 아이템이 같은 아이템으로 판단될때 로그를 남길지 여부
    var logFindSameOptionWhenCheckOption = true

    // 여러개의 추옵 확인시 마우스 기준으로 캡쳐할때마다 스샷 남기기
    var saveCapturedMatWhenCheckOption = false

    var synthesizeMouseDelay = 100  //합성시 마우스 딜레이

    var buyItemMaxTimeMinute = 360  //경매장 구매시 최대 동작 시간 (단위: 분)

    var logStepWhenSynthesizeUtilEndFast = true //무한합성시 단계별 로그 남기기
    var logStepWhenBuyItemListUntilEnd = false //구매 및 제작시 단계별 로그 남기기

    var beepLongTimeWhenTaskFinished = false //작업 끝날때 비프 오랫동안 울리기

    companion object {
        var instance = Settings()
        private var lastSaved = ""
        fun load() {
            val f = File("settings")
            if (!f.exists()) {
                save()
                return
            }
            val reader = BufferedReader(
                InputStreamReader(
                    FileInputStream("settings"), "euc-kr"
                )
            )
            val json = reader.readText()
            try {
                instance = Gson().fromJson(json, Settings::class.java)
            } catch (e: Exception) {
                logI("settings 값 불러오기 오류 ${e.message}")
            }
            lastSaved = json
        }

        fun save() {
            GlobalScope.launch(Dispatchers.IO) {
                val json = Gson().toJson(instance)
                if (json == lastSaved) return@launch
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