package jp.mirable.busller.ui.preferences

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.preference.EditTextPreference
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.snackbar.Snackbar
import jp.mirable.busller.BuildConfig
import jp.mirable.busller.R
import jp.mirable.busller.classes.SingletonContext
import jp.mirable.busller.viewmodel.TopViewModel

class PreferencesFragment : PreferenceFragmentCompat() {

    private val topVM: TopViewModel by activityViewModels()
    private var count = 10
    private var toast = Toast.makeText(
        SingletonContext.applicationContext(),
        "開拓者になるまで、あと${count}回です", Toast.LENGTH_SHORT
    )

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<Preference>("appVer")?.summary = BuildConfig.VERSION_NAME
        findPreference<Preference>("diaVer")?.summary =
            String.format(
                "%s年%02d月 第%d版",
                BuildConfig.DIA_VERSION.take(4),
                BuildConfig.DIA_VERSION.substring(4, 6).toInt(),
                BuildConfig.DIA_VERSION.takeLast(1).toInt()
            )
        findPreference<EditTextPreference>("nearStation")?.let { et ->
            if (et.text != "") {
                et.title = getString(R.string.nearStationSet)
                et.summary = String.format("%s駅と蒲郡駅間で検索します", et.text)
            } else {
                et.title = getString(R.string.nearStationSet) + " (未設定)"
                et.summary = getString(R.string.transfer_before_summary)
            }
            et.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, value ->
                if (value != "") {
                    et.title = getString(R.string.nearStationSet)
                    et.summary = String.format("%s駅と蒲郡駅間で検索します", value)
                }
                else {
                    et.title = getString(R.string.nearStationSet) + " (未設定)"
                    et.summary = getString(R.string.transfer_before_summary)
                }
                true
            }
        }
        findPreference<Preference>("diaVer")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                if (count > -1) count--
                when (count) {
                    -1 -> {
                        toast.cancel()
                        toast = Toast.makeText(
                            SingletonContext.applicationContext(),
                            "既に開拓者向けオプションが有効です。",
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                    }
                    0 -> {
                        toast.cancel()
                        toast = Toast.makeText(
                            SingletonContext.applicationContext(),
                            "これで開拓者になりました！",
                            Toast.LENGTH_LONG
                        )
                        toast.show()
                        findPreference<PreferenceCategory>("hoge")?.isVisible = true
                        findPreference<SwitchPreferenceCompat>("hide")?.isChecked = true
                    }
                    in 1..4 -> {
                        toast.cancel()
                        toast = Toast.makeText(
                            SingletonContext.applicationContext(),
                            "開拓者になるまで、あと${count}回です",
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                    }
                }
                true
            }
        findPreference<SwitchPreferenceCompat>("hide")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, value ->
                findPreference<PreferenceCategory>("hoge")?.apply {
                    when (value) {
                        true -> this.isVisible = true
                        false -> {
                            this.isVisible = false
                            count = 10
                        }
                    }
                }
                true
            }
    }
}