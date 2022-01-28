package jp.mirable.busller

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.mirable.busller.databinding.ActivityMainBinding
import jp.mirable.busller.generated.callback.OnClickListener
import jp.mirable.busller.model.ListData
import jp.mirable.busller.model.TimeData
import jp.mirable.busller.ui.MyDialogFragment
import jp.mirable.busller.ui.top.TopFragment
import jp.mirable.busller.viewmodel.TopViewModel
import java.util.*

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val topViewModel: TopViewModel by viewModels()
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            "jp.mirable.busller.TO_STATION" -> topViewModel.changeFS(false)
            "jp.mirable.busller.TO_SCHOOL" -> topViewModel.changeFS(true)
            else -> {}
        }
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, NavDestination, _ ->
            when (NavDestination.id) {
                R.id.top_page, R.id.list_dialog -> {
                    binding.changeFab.show()
                    binding.changeFab.setOnClickListener {
                        topViewModel.changeFS()
                    }
                }
                else -> {
                    binding.changeFab.setOnClickListener { }
                    binding.changeFab.hide()
                }
            }
        }
        binding.bottomNavigation.setupWithNavController(navController)

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "sangane", "beforeBus" -> topViewModel.load()
        }
    }

    fun setNotification(pendingIntent: PendingIntent, noticeDate: Calendar) {
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.setExact(AlarmManager.RTC, noticeDate.timeInMillis, pendingIntent)
    }


}