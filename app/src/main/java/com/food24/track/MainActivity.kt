package com.food24.track

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import com.food24.track.ui.onboarding.LoadingFragment
import com.food24.track.ui.home.HomeDashboardFragment
import com.food24.track.ui.onboarding.WelcomeFragment
import com.food24.track.ui.progress.ProgressFragment
import com.food24.track.ui.settings.SettingsFragment
import com.food24.track.ui.shopping.ShoppingListFragment
import com.food24.track.ui.theme.food24EventsTheme

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: View
    private lateinit var navNewPlan: LinearLayout
    private lateinit var navShopList: LinearLayout
    private lateinit var navProgrees: LinearLayout
    private lateinit var navSet: LinearLayout

    private lateinit var newPlanIcon: ImageView
    private lateinit var shoppingListIcon: ImageView
    private lateinit var progressIcon: ImageView
    private lateinit var setIcon: ImageView

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_main)

        // Инициализация bottomNav один раз
        bottomNav = findViewById(R.id.bottomNavInclude)

        if (savedInstanceState == null) {
            val showWelcome = intent.getBooleanExtra("show_welcome", false)
            if (showWelcome) openWelcomeFragment() else openMainFragment()
        }

        hideSystemUI()

        // Инициализация элементов навигации
        navNewPlan = findViewById(R.id.navNewPlan)
        navShopList = findViewById(R.id.navShopList)
        navProgrees = findViewById(R.id.navProgrees)
        navSet = findViewById(R.id.navSet)

        // Инициализация иконок
        newPlanIcon = navNewPlan.findViewById(R.id.iconNewPlan)
        shoppingListIcon = navShopList.findViewById(R.id.iconShopList)
        progressIcon = navProgrees.findViewById(R.id.iconProgrees)
        setIcon = navSet.findViewById(R.id.iconSet)

        // Обработчики кликов для каждого элемента нижней панели
        navNewPlan.setOnClickListener {
            showBottomNav()
            openFragment(HomeDashboardFragment())
            updateNavIcons("new_plan")
        }

        navShopList.setOnClickListener {
            showBottomNav()
            openFragment(ShoppingListFragment())
            updateNavIcons("shopping_list")
        }

        navProgrees.setOnClickListener {
            showBottomNav()
            openFragment(ProgressFragment())
            updateNavIcons("progress")
        }

        navSet.setOnClickListener {
            showBottomNav()
            openFragment(SettingsFragment())
            updateNavIcons("set")
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateNavForCurrentFragment()
        }
    }

    private fun updateNavForCurrentFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.mainFragmentContainer)
        when (fragment) {
            is HomeDashboardFragment -> updateNavIcons("new_plan")
            is ShoppingListFragment  -> updateNavIcons("shopping_list")
            is ProgressFragment      -> updateNavIcons("progress")
            is SettingsFragment      -> updateNavIcons("set")
            else -> {
                // ни один из экранов нижней навигации — все иконки «неактивны»
                resetNavIcons()
            }
        }
    }

    fun updateNavIcons(activeFragment: String) {
        resetNavIcons()
        when (activeFragment) {
            "new_plan" -> newPlanIcon.setImageResource(R.drawable.new_plan_active)
            "shopping_list" -> shoppingListIcon.setImageResource(R.drawable.shop_list_active)
            "progress" -> progressIcon.setImageResource(R.drawable.progress_active)
            "set" -> setIcon.setImageResource(R.drawable.settings_active)
        }
    }

    private fun resetNavIcons() {
        newPlanIcon.setImageResource(R.drawable.new_plan)
        shoppingListIcon.setImageResource(R.drawable.shop_list)
        progressIcon.setImageResource(R.drawable.progress)
        setIcon.setImageResource(R.drawable.settings)
    }

    fun openFragment(fragment: Fragment) {
        // Здесь подумайте, действительно ли нужно очищать весь backstack?
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .addToBackStack(fragment::class.java.name)
            .commit()

        // обновить иконки сразу после commit
        window.decorView.post { updateNavForCurrentFragment() }
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.mainFragmentContainer, fragment)
            if (addToBackStack) addToBackStack(fragment::class.java.name)
            commit()
        }

        // обновить иконки сразу после commit
        window.decorView.post { updateNavForCurrentFragment() }
    }

    fun showBottomNav() {
        bottomNav.visibility = View.VISIBLE
    }

    fun hideBottomNav() {
        bottomNav.visibility = View.GONE
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val fm = supportFragmentManager
        if (fm.backStackEntryCount > 0) {
            fm.popBackStack()
            fm.executePendingTransactions()

            val current = fm.findFragmentById(R.id.mainFragmentContainer)
            if (current is WelcomeFragment) {
                hideBottomNav()
            } else {
                showBottomNav()
            }
        } else {
            moveTaskToBack(true)
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        hideBottomNav()
    }

    fun openDashboardFragment() {
        if (isFinishing || isDestroyed) return

        showBottomNav()

        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, HomeDashboardFragment())
            .commit()

        window.decorView.post {
            if (!isFinishing && !isDestroyed) {
                updateNavIcons("new_plan")
            }
        }
    }

    fun openMainFragment() {
        val fragment = WelcomeFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .commit()
    }

    fun openWelcomeFragment() {
        supportFragmentManager.commit {
            replace(R.id.mainFragmentContainer, WelcomeFragment())
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI() // Снова скрываем системные кнопки при возвращении
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    food24EventsTheme {
        Greeting("Android")
    }
}