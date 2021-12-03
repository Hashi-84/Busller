package jp.mirable.busller.ui.preferences

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.snackbar.Snackbar
import jp.mirable.busller.BuildConfig
import jp.mirable.busller.R
import jp.mirable.busller.classes.SingletonContext
import jp.mirable.busller.viewmodel.TopViewModel

class PreferencesFragment : PreferenceFragmentCompat() {

    private val topVM: TopViewModel by activityViewModels()
    private var count = 10
    private var toast = Toast.makeText(SingletonContext.applicationContext(),
        "開拓者になるまで、あと${count}回です", Toast.LENGTH_SHORT)

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
        findPreference<Preference>("diaVer")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                if (count > -1) count--
                when (count) {
                    in -1..0 -> {
                        toast.cancel()
                        toast = Toast.makeText(
                            SingletonContext.applicationContext()
                            , "これで開拓者になりました！"
                            , Toast.LENGTH_LONG
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
                        false -> this.isVisible = false
                    }
                }
                true
            }
    }
}