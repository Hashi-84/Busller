package jp.mirable.busller

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import jp.mirable.busller.databinding.ActivityMainBinding
import jp.mirable.busller.viewmodel.TopViewModel

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val topViewModel: TopViewModel by viewModels()
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener {_, NavDestination, _ ->
            when(NavDestination.id) {
                R.id.top_page -> {
                    binding.changeFab.show()
                    binding.changeFab.setOnClickListener {
                        topViewModel.changeFS()
                    }
                }
                else -> {
                    binding.changeFab.setOnClickListener {  }
                    binding.changeFab.hide()
                }
            }
        }

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "sangane","beforeBus" -> topViewModel.load()
        }
    }
}