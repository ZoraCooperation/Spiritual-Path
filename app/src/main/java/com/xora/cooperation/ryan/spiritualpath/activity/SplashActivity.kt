package com.xora.cooperation.ryan.spiritualpath.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xora.cooperation.ryan.spiritualpath.MainActivity
import com.xora.cooperation.ryan.spiritualpath.R
import com.xora.cooperation.ryan.spiritualpath.ads.SpiritualPath

/**
 * Number of seconds to count down before showing the app open ad. This simulates the time needed
 * to load the app.
 */
private const val COUNTER_TIME = 5000

private const val LOG_TAG = "SplashActivity"
@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var bottomAnimation: Animation
    private lateinit var appLogo: ImageView
    private lateinit var appTitle: TextView
    private lateinit var appDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash)

        val actionBar = supportActionBar
        actionBar!!.hide()

        bottomAnimation = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.bottom_animation)
        appLogo = findViewById(R.id.app_logo)
        appTitle = findViewById(R.id.app_title)
        appDescription = findViewById(R.id.app_description)

        appLogo.animation = bottomAnimation
        appTitle.animation = bottomAnimation
        appDescription.animation = bottomAnimation

        Handler().postDelayed({
            startMainActivity()
        }, COUNTER_TIME.toLong())
    }

    private fun startMainActivity() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            loadAd()
            finish()
        }
    }

    private fun loadAd(){
        val application = application as? SpiritualPath

        // If the application is not an instance of MyApplication, log an error message and
        // start the MainActivity without showing the app open ad.
        if (application == null) {
            Log.e(LOG_TAG, "Failed to cast application to Spiritual Path.")
            return
        }

        // Show the app open ad.
        application.showAdIfAvailable(
            this@SplashActivity,
            object : SpiritualPath.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                }
            })
    }
}