package com.food24.track

import android.content.Intent
import android.os.*
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.food24.track.ui.LoadingFragment
import okhttp3.*

class SplashActivity : AppCompatActivity() {

    private lateinit var bannerView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var clickBlocker: View

    private val handler = Handler(Looper.getMainLooper())
    private var isActive = false

    private val prefs by lazy {
        getSharedPreferences("banner_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_food24Events)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        bannerView = findViewById(R.id.bannerImageView)
        progressBar = findViewById(R.id.progressBar)
        clickBlocker = findViewById(R.id.clickBlocker)
        clickBlocker.visibility = View.VISIBLE

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        if (savedInstanceState == null && prefs.getBoolean("isPrivacyAccepted", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.splashRoot, LoadingFragment())
                .commitNow()
        }

        // Проверка privacy
        checkPrivacy()
    }

    private fun checkPrivacy() {
        handler.postDelayed({ goToMain() }, 1000)
    }

    private fun goToMain() {
        clickBlocker.visibility = View.GONE
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onStart() {
        super.onStart()
        isActive = true
    }

    override fun onStop() {
        super.onStop()
        isActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
    }
}
