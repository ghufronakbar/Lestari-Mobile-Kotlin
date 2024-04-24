

package com.example.lestari

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.lestari.Helper.FirebaseHelper
import com.example.lestari.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var authHelper: FirebaseHelper
    private lateinit var binding: ActivityLoginBinding
    private var loginIdRegex = Regex("^\\d{10,}\$")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("login_status", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        authHelper = FirebaseHelper(this)

        var cvButtonLogin= binding.cvButtonLogin
        var etLoginId= binding.etLoginId
        var etLoginPassword= binding.etLoginPassword
        var tvRegister= binding.tvRegister
        var tvForgotPassword= binding.tvForgotPassword


        cvButtonLogin.setOnClickListener {
            val loginId = etLoginId.text.toString()
            val password = etLoginPassword.text.toString()
            if(loginId.isEmpty() || password.isEmpty()){
                if(loginId.isEmpty()){
                    etLoginId.error = "Enter Your User Id"
                }
                if(password.isEmpty()){
                    etLoginPassword .error = "Enter Your Password"
                }
            }else if(!loginId.matches(loginIdRegex)){
                etLoginId.error = "Enter Valid User Id"
            }else{
                authHelper.signIn(loginId, password,
                    onSuccess = {
                        editor.putString("status", "login")
                        editor.apply()
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomepageActivity::class.java)
                        startActivity(intent)
                    },
                    onFailure = {
                        Toast.makeText(this, "Something went wrong, Try Again!",Toast.LENGTH_SHORT).show()
                    })
            }
        }

        tvRegister.setOnClickListener {

            val url = "https://www.example.com"

            val uri = Uri.parse(url)

            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(packageManager) != null) {

                startActivity(intent)
            } else {
                Toast.makeText(this, "Something went wrong, Try Again!",Toast.LENGTH_SHORT).show()
            }
        }
    }
}