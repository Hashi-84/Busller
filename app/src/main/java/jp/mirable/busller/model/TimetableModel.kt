package jp.mirable.busller.model

import android.util.Log
import jp.mirable.busller.BuildConfig
import jp.mirable.busller.classes.SingletonContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

//時刻表ファイルからデータを読みだすモデル
class TimetableModel {

    //引数のファイルからダイアパターン列を出力
    fun loadLocalDia(
        file: InputStream = SingletonContext.applicationContext().assets
            .open("${BuildConfig.DIA_VERSION}/${BuildConfig.DIA_VERSION}pat.csv"),
        dateFormat: String = "yyyy-MM-dd",
        day: String = SimpleDateFormat(dateFormat, Locale.JAPAN).format(Date()).toString()
    ): Array<String> {
        var count = 0

        var dia: Array<String> = emptyArray()
        val reader = BufferedReader(InputStreamReader(file))
        reader.forEachLine {
            if (dia.isEmpty()) { // ダイア配列が埋まるまで繰り返し
                if (count == 0) it.split(",") // CSVの1段目の処理
                else {
                    val line = it.split(",").toTypedArray()
//                    Log.d("Date: ", line[0])
                    if (line[0] == day) {
                        dia = line
                        return@forEachLine
                    }
                }
            }
            count++
        }
        reader.close()
        file.close()
        if (dia.isEmpty()) {
            Log.d("LoadError: ", "パターンデータが読み込めません。")
            throw IOException("ダイヤデータが更新されていないか、ダイヤデータが不正な書式のため読み込めません。")
        } else return dia
    }

    //引数に応じてローカルの時刻表ファイルから時刻表をリストで出力
    fun loadLocalTimeline(
        diaName: String
    ): MutableList<TimeData> {
        var count = 0

        val timeData: MutableList<TimeData> = mutableListOf()
        if (diaName == "0") throw IOException("NOTEXIST")
        val file = SingletonContext.applicationContext().assets
            .open("${BuildConfig.DIA_VERSION}/${BuildConfig.DIA_VERSION}${diaName}.csv")
        val reader = BufferedReader(InputStreamReader(file))
        reader.forEachLine {
            if (count == 0) it.split(",")
            else {
                val line = it.split(",").toTypedArray()
                timeData += TimeData(
                    id = diaName + String.format("%02d", count), //ex: nor01
                    hour = line[0].toInt(),
                    minute = line[1].toInt(),
                    forSchool = line[2].toBoolean(),
                    sangane = line[3].toBoolean(),
                    option = line[4]
                )
            }
            count++
        }
        reader.close()
        file.close()
        if (timeData.isEmpty()) {
            Log.d("LoadError: ", "時刻表データが読み込めません。")
            throw IOException("EMPTYLIST")
        } else return timeData
    }
    /* 返り値"MutableList<TimeData>"バージョン
    fun formatList(
        list: List<TimeData>,
        forSchool: Boolean?,
        sangane: Boolean = false,
        beforeBus: Boolean
    ): MutableList<TimeData> {
        var newList: MutableList<TimeData> = mutableListOf()
        list.forEach {
            if (!(!beforeBus && (getLeftSec(it) < 10L))) {
                //「前のバスを無表示かつ-10秒未満のとき」以外ではリストに追加
                if (it.forSchool == forSchool) when (forSchool) {
                    true -> when (sangane) {
                        true -> newList += it
                        false -> if (!it.sangane) newList += it
                    }
                    false -> newList += it
                }
            }
        }
        return newList
    }
    */
    fun formatList(
        list: List<TimeData>,
        forSchool: Boolean?,
        sangane: Boolean = false,
        beforeBus: Boolean
    ): MutableList<ListData> {
        val newList: MutableList<ListData> = mutableListOf()
        list.forEach {
            if (!(!beforeBus && (getLeftSec(it) <= 10L))) {
                //「前のバスを無表示かつ-10秒以下のとき」以外ではリストに追加
                if (it.forSchool == forSchool) when (forSchool) {
                    true -> when (sangane) { //以下、TimeData以外のフィールドは初期値falseがあるため省略
                        true -> {
                            newList += ListData(it)
                        }
                        false -> if (!it.sangane) newList += ListData(it)
                    }
                    false -> newList += ListData(it)
                }
            }
        }
        return newList
    }

    //引数で得たTimeDataから残り時間を秒で出力
    fun getLeftSec(data: TimeData): Long {
        val time = getTime()
        val nowSec = ((time[0] * 60) + time[1]) * 60 + time[2]
        val nextTime = ((data.hour * 60) + data.minute) * 60
        return (nextTime - nowSec).toLong()
    }

    //現在時刻を配列で取得
    fun getTime(): Array<Int> {
        val date = Date()
        val format = SimpleDateFormat("HHmmss").format(date)
        val hour = format.toInt() / 10000
        val min = format.toInt() % 10000 / 100
        val sec = format.toInt() % 100
        return arrayOf(hour, min, sec)
    }
}