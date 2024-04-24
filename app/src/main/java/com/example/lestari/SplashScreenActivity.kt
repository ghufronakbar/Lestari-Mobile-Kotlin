package com.example.lestari

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreenActivity : AppCompatActivity() {
    private val SPLASH_TIME: Long = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val sharedPreferences = getSharedPreferences("login_status", MODE_PRIVATE)
        val loginStatus = sharedPreferences.getString("status", "logout")

        supportActionBar?.hide()
        val handler = Handler()
        if (loginStatus == "login") {
            handler.postDelayed({
                val intent = Intent(this, HomepageActivity::class.java)
                startActivity(intent)
                finish()
            }, SPLASH_TIME)
        } else {
            handler.postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, SPLASH_TIME)
        }

    }
}