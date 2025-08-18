package com.food24.track

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.food24.track.ui.onboarding.LoadingFragment
import com.food24.track.ui.onboarding.SplashFragment

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val LOADER_DELAY = 1200L   // 1.2s
        private const val SPLASH_DELAY = 1400L   // 1.4s
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_food24Events)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (savedInstanceState == null) {
            showLoader()
        }
    }

    private fun showLoader() {
        supportFragmentManager.commit {
            replace(R.id.splashRoot, LoadingFragment())
        }
        // через 1.2s – перейти на Splash с fade
        window.decorView.postDelayed({ showSplash() }, LOADER_DELAY)
    }

    private fun showSplash() {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out,  // enter/exit
                R.anim.fade_in, R.anim.fade_out   // popEnter/popExit (на всякий)
            )
            replace(R.id.splashRoot, SplashFragment())
        }
        // ещё 1.4s – перейти в MainActivity → Welcome
        window.decorView.postDelayed({ goToWelcome() }, SPLASH_DELAY)
    }

    private fun goToWelcome() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra("show_welcome", true)
        )
        finish()
    }
}
