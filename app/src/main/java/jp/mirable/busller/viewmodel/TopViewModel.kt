package jp.mirable.busller.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.mirable.busller.MainActivity
import jp.mirable.busller.R
import jp.mirable.busller.classes.MyNotification
import jp.mirable.busller.classes.MyReceiver
import jp.mirable.busller.classes.SingletonContext
import jp.mirable.busller.model.ListData
import jp.mirable.busller.model.OmakeModel
import jp.mirable.busller.model.TimeData
import jp.mirable.busller.model.TimetableModel
import jp.mirable.busller.ui.MyDialogFragment
import java.util.*

class TopViewModel : ViewModel() {

    //ダイヤパターン格納用
    private var diaPat: Array<String>
    private var isError = false

    //大学行きか判定用のフラグ
    private val _forSchool = MutableLiveData<Boolean>(true)
    val forSchool: LiveData<Boolean> get() = _forSchool
    fun changeFS(fs: Boolean = !_forSchool.value!!) { //切り替えFAB用onClickメソッド
        _forSchool.value = fs
    }

    //該当パターンの全発車時刻を格納
    private var timeList: List<TimeData> = emptyList()
    private val _rvTimeList = MutableLiveData<MutableList<ListData>>(mutableListOf())
    val rvTimeList: LiveData<MutableList<ListData>> get() = _rvTimeList

    //表示用リストのデータ格納メソッド
    fun setRVTimeList() {
        if (timeList.isNotEmpty()) {
            _rvTimeList.value = model.formatList(
                timeList,
                forSchool.value,
                pref.getBoolean("sangane", false),
                pref.getBoolean("beforeBus", false)
            )
        } else _rvTimeList.value!!.clear()
    }

    //リストで選択された項目格納用
    private val _chooseLine = MutableLiveData<ListData>()
    val chooseLine: LiveData<ListData> get() = _chooseLine

    /*次発データ
        NULL許容です! <- 次発がない場面も想定されるため
    */
    private val _nextData = MutableLiveData<TimeData?>(null)
    val nextData: LiveData<TimeData?> get() = _nextData
    private fun setNextData() {
        if (!timeList.isNullOrEmpty()) { //発車時刻リストが空でないときのみ実行
            var count = 0
            var found = false //探索完了するとtrueになる
            while (count < timeList.size && !found) { //カウント上限か探索完了かでループ終了
                if (timeList[count].forSchool == forSchool.value) {
                    if (timeList[count].sangane && !pref.getBoolean("sangane", false)) {
                        count++
                        continue    //三ヶ根条件分岐(次のダイヤへスキップ)
                    } else {
                        model.getLeftSec(timeList[count]).let { //残り時間をもとに検索
                            if (it >= -10L) { // 見つかったら
                                _leftSec.value = it //残り秒数を代入
                                _nextData.value = timeList[count] //次発ダイヤを代入
                                found = true
                            }
                        }
                    }
                }
                count++
            }
            if (count == timeList.size && !found) _nextData.value = null
        } else {
            _nextData.value = null
        }
    }

    //次発発車までの残り時間(sec)
    private val _leftSec = MutableLiveData(-100L)
    val leftSec: LiveData<Long> get() = _leftSec
    fun setLeftSec() {
        if (_nextData.value != null) {
            val newSec = model.getLeftSec(_nextData.value!!) //新たな残り時間を取得
            if (newSec < -10L) load() //それが-10未満なら次発読み込み
            else if (newSec != leftSec.value) _leftSec.value = newSec //新値が異なる値なら代入
        } else _leftSec.value = -100L //次発nullなら初期値代入
    }

    private val context = SingletonContext.applicationContext()
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    //時刻表ファイルを読み込むモデル
    private var model: TimetableModel = TimetableModel()

    init {
        diaPat = try { //ダイヤパターン読み込み
            model.loadLocalDia()
        } catch (e: Exception) {
            emptyArray()
        }
        if (!diaPat.isNullOrEmpty()) timeList = try {
            model.loadLocalTimeline(diaPat[1])
        } catch (e: Exception) {
            when (e.toString()) {
                "EMPTYLIST" -> {
                    TODO("時刻表読み込んで空だった時")
                }
                "NOTEXIST" -> {
                    TODO("定義していないファイルを開こうとしたとき")
                }
                else -> {
                    isError = true
                }
            }
            emptyList()
        }
        if (!timeList.isNullOrEmpty()) {
            load()
        } //メソッドは使い回し
    }

    //残り時間をString型で出力
    fun formatTime(second: Long): String {
        if (second > 3599) {//60分以上ならば
            return String.format(
                "%d時間 %02d分",
                (second.toDouble() / 3600).toInt(),
                (second.toDouble() % 3600 / 60).toInt()
            )
        } else if (second > 0) return String.format(
            "%02d:%02d", (second / 60).toInt(), (second % 60).toInt()
        ) else if (second > -100) return "発車しました"
        else return "本日は終了しました"
    }

    //RecyclerView用
    //時刻を整形してString型で出力
    fun formatTime(hour: Int, min: Int): String {
        if (!(hour == 0 && min == 0)) return String.format("%d:%02d", hour, min)
        else return "--:--"
    }

    //行き先整形して出力
    fun formatDest(bool: Boolean, fs: Boolean, san: Boolean = false): String {
        return when (bool) { //上段(true)か下段(false)か
            true -> when (fs) {
                true -> {
                    if (san) "三ヶ根駅 発"
                    else "蒲郡駅 発"
                }
                false -> "愛知工科大学 発"
            }
            false -> when (fs) {
                true -> "愛知工科大学"
                false -> "蒲郡駅南口"
            }
        }
    }

    fun load(sw: Int = 0) {
        if (timeList.isNotEmpty()) {
            setNextData()
            setLeftSec()
            when (sw) {
                0 -> setRVTimeList()
                1 -> _rvTimeList.value!!.removeAt(0)
            }
        }
    }

    fun testOnClick() {
        nextData.value?.let { next ->
            MyNotification.sendDepNotification(
                context,
                formatDest(false, next.forSchool, pref.getBoolean("sangane", false)),
                next.hour,
                next.minute
            )
        }
    }

    fun addDepNotification(timeData: TimeData) {
        val calendar = Calendar.getInstance()
        val intent = Intent(context, MyReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(context, )
    }

    val media1 = OmakeModel().loadSoundFile("Melody.mp3", true)
    val media2 = OmakeModel().loadSoundFile("Voice.mp3", false)
    private val _omake1 = MutableLiveData<Boolean>(false)
    val omake1: LiveData<Boolean> get() = _omake1
    fun turnOmake1(b: Boolean?) {
        _omake1.value = b!!
    }

    fun soundC() {
        if (media1.isPlaying) {
            media1.stop()
            media2.start()
        } else if (media2.isPlaying) {
            media2.stop()
            media1.start()
        } else {
            media1.start()
        }
    }
}