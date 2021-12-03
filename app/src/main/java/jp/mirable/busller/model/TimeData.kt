package jp.mirable.busller.model
//時刻表データの形式を定義するデータクラス
data class TimeData(
    val id: String,
    val hour: Int,
    val minute: Int,
    val forSchool: Boolean,
    val sangane: Boolean,
    val option: String
)
