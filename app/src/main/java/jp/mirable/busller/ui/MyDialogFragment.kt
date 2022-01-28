package jp.mirable.busller.ui

import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.get
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.mirable.busller.MainActivity
import jp.mirable.busller.R
import jp.mirable.busller.viewmodel.TopViewModel
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MyDialogFragment : DialogFragment() {
    val topVM: TopViewModel by activityViewModels()
    private val args: MyDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(activity!!)

        val position = args.position
        val station =
            PreferenceManager.getDefaultSharedPreferences(context).getString("nearStation", "")
        if (position != 999) topVM.rvTimeList.value?.get(position).let {
            it?.timeData?.let { data ->
                builder.setTitle(
                    String.format(
                        "%d:%02d発 %s行き",
                        data.hour,
                        data.minute,
                        when (data.forSchool) {
                            true -> "大学"
                            false -> "蒲郡駅"
                        }
                    )
                )
                builder.setMessage(
                    when (data.forSchool) {
                        true -> String.format(
                            "%s\n → バスの発車5分前に通知を送信します。\n\n%s\n → ジョルダンで、このバスに間に合う電車の時間を検索します。\n(設定画面で、最寄り駅を設定すると利用できます)",
                            getString(R.string.btn_noti),
                            getString(R.string.btn_tran)
                        )
                        false -> String.format(
                            "%s\n → バスの発車5分前に通知を送信します。\n\n%s\n → ジョルダンで、このバスと接続するかもしれない(12分後の)電車の時間を検索します。\n(設定画面で、最寄り駅を設定すると利用できます)",
                            getString(R.string.btn_noti),
                            getString(R.string.btn_conn)
                        )
                    }
                )
                builder.setNeutralButton(getString(R.string.cancel)) { dialog: DialogInterface, i: Int ->
                    dialog.cancel()
                }
                if (!station.isNullOrBlank()) {
                    val today = LocalDate.now()
                    lateinit var url: String
                    when (data.forSchool) {
                        true -> builder.setNegativeButton(getString(R.string.btn_tran)) { dialog, i ->
                            url = "https://www.jorudan.co.jp/norikae/cgi/nori.cgi?eki1=" +
                                    URLEncoder.encode(station, "utf-8") +//エンコード済み駅名
                                    "&eki2="+ (if (data.sangane) "%E4%B8%89%E3%83%B6%E6%A0%B9" else "%E8%92%B2%E9%83%A1") +"&eki3=&via_on=1&Dym=" +
                                    today.format(DateTimeFormatter.ofPattern("yyyyMM")) +//年//月
                                    "&Ddd=" + today.format(DateTimeFormatter.ofPattern("dd"))
                                .toInt().toString() +//日
                                    "&Dhh=" + data.hour.toString() +//時
                                    "&Dmn1=" + (data.minute / 10).toString() +//分(十の位)
                                    "&Dmn2=" + (data.minute % 10).toString() +//分(一の位)
                                    "&Cway=1&Cfp=1&Czu=2&C7=1&C2=0&C3=0&C1=0&sort=rec&C4=5&C5=0&C6=2&S=%E6%A4%9C%E7%B4%A2&Cmap1=&rf=nr&pg=0&eok1=R-&eok2=&eok3="
                            val tabsIntent = CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .build()
                            Log.d("URL", url)
                            dialog.dismiss()
                            tabsIntent.launchUrl(
                                activity!!,
                                Uri.parse(url)
                            )
                        }
                        false -> {
                            val time = when (data.minute) {
                                in 0..47 -> arrayOf(data.hour, data.minute+12)
                                in 48..59 -> arrayOf(data.hour+1, data.minute-48)
                                else -> emptyArray()
                            }
                            builder.setNegativeButton(getString(R.string.btn_conn)) { dialog, i ->
                                url =
                                    "https://www.jorudan.co.jp/norikae/cgi/nori.cgi?eki1=%E8%92%B2%E9%83%A1&eki2=" +
                                            URLEncoder.encode(station, "utf-8") +//エンコード済み駅名
                                            "&eki3=&via_on=1&Dym=" +
                                            today.format(DateTimeFormatter.ofPattern("yyyyMM")) +//年//月
                                            "&Ddd=" + today.format(DateTimeFormatter.ofPattern("dd"))
                                        .toInt().toString() +//日
                                            "&Dhh=" + time[0].toString() +//時
                                            "&Dmn1=" + (time[1] / 10).toString() +//分(十の位)
                                            "&Dmn2=" + (time[1] % 10).toString() +//分(一の位)
                                            "&Cway=0&Cfp=1&Czu=2&C7=1&C2=0&C3=0&C1=0&sort=rec&C4=5&C5=0&C6=2&S=%E6%A4%9C%E7%B4%A2&Cmap1=&rf=nr&pg=0&eok1=R-&eok2=&eok3="
                                val tabsIntent = CustomTabsIntent.Builder()
                                    .setShowTitle(true)
                                    .build()
                                Log.d("URL", url)
                                dialog.dismiss()
                                tabsIntent.launchUrl(
                                    activity!!,
                                    Uri.parse(url)
                                )
                            }
                        }
                    }
                }
                builder.setPositiveButton(getString(R.string.btn_noti)) { dialog, i ->

                }

            }
                ?: throw Exception("No Data!")
        }
        else topVM.nextData.value?.let { data ->
            builder.setTitle(
                String.format(
                    "%d:%02d発 %s行き",
                    data.hour,
                    data.minute,
                    when (data.forSchool) {
                        true -> "大学"
                        false -> "蒲郡駅"
                    }
                )
            )
            builder.setMessage(
                when (data.forSchool) {
                    true -> String.format(
                        "%s\n → バスの発車5分前に通知を送信します。\n\n%s\n → ジョルダンで、このバスに間に合う電車の時間を検索します。\n(設定画面で、最寄り駅を設定すると利用できます)",
                        getString(R.string.btn_noti),
                        getString(R.string.btn_tran)
                    )
                    false -> String.format(
                        "%s\n → バスの発車5分前に通知を送信します。\n\n%s\n → ジョルダンで、このバスと接続する電車の時間を検索します。\n(設定画面で、最寄り駅を設定すると利用できます)",
                        getString(R.string.btn_noti),
                        getString(R.string.btn_conn)
                    )
                }
            )
            builder.setNeutralButton(getString(R.string.cancel)) { dialog: DialogInterface, i: Int ->
                dialog.cancel()
            }
            if (!station.isNullOrBlank()) {
                val today = LocalDate.now()
                lateinit var url: String
                when (data.forSchool) {
                    true -> builder.setNegativeButton(getString(R.string.btn_tran)) { dialog, i ->
                        url = "https://www.jorudan.co.jp/norikae/cgi/nori.cgi?eki1=" +
                                URLEncoder.encode(station, "utf-8") +//エンコード済み駅名
                                "&eki2=%E8%92%B2%E9%83%A1&eki3=&via_on=1&Dym=" +
                                today.format(DateTimeFormatter.ofPattern("yyyyMM")) +//年//月
                                "&Ddd=" + today.format(DateTimeFormatter.ofPattern("dd")).toInt()
                            .toString() +//日
                                "&Dhh=" + data.hour.toString() +//時
                                "&Dmn1=" + (data.minute / 10).toString() +//分(十の位)
                                "&Dmn2=" + (data.minute % 10).toString() +//分(一の位)
                                "&Cway=1&Cfp=1&Czu=2&C7=1&C2=0&C3=0&C1=0&sort=rec&C4=5&C5=0&C6=2&S=%E6%A4%9C%E7%B4%A2&Cmap1=&rf=nr&pg=0&eok1=R-&eok2=&eok3="
                        val tabsIntent = CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build()
                        Log.d("URL", url)
                        dialog.dismiss()
                        tabsIntent.launchUrl(
                            activity!!,
                            Uri.parse(url)
                        )
                    }
                    false -> builder.setNegativeButton(getString(R.string.btn_conn)) { dialog, i ->
                        url =
                            "https://www.jorudan.co.jp/norikae/cgi/nori.cgi?eki1=%E8%92%B2%E9%83%A1&eki2=" +
                                    URLEncoder.encode(station, "utf-8") +//エンコード済み駅名
                                    "&eki3=&via_on=1&Dym=" +
                                    today.format(DateTimeFormatter.ofPattern("yyyyMM")) +//年//月
                                    "&Ddd=" + today.format(DateTimeFormatter.ofPattern("dd"))
                                .toInt().toString() +//日
                                    "&Dhh=" + data.hour.toString() +//時
                                    "&Dmn1=" + (data.minute / 10).toString() +//分(十の位)
                                    "&Dmn2=" + (data.minute % 10).toString() +//分(一の位)
                                    "&Cway=0&Cfp=1&Czu=2&C7=1&C2=0&C3=0&C1=0&sort=rec&C4=5&C5=0&C6=2&S=%E6%A4%9C%E7%B4%A2&Cmap1=&rf=nr&pg=0&eok1=R-&eok2=&eok3="
                        val tabsIntent = CustomTabsIntent.Builder()
                            .setShowTitle(true)
                            .build()
                        Log.d("URL", url)
                        dialog.dismiss()
                        tabsIntent.launchUrl(
                            activity!!,
                            Uri.parse(url)
                        )
                    }
                }
            }
            builder.setPositiveButton(getString(R.string.btn_noti)) { dialog, i ->

            }
                ?: throw Exception("No Data!")
        }
        return builder.create()
    }
}