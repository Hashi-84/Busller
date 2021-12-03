package jp.mirable.busller.model

data class ListData(
    val timeData: TimeData,
    var noticeFlag: Boolean = false,
    var highlightFlag: Boolean = false
)
