package com.example.lestari

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.lestari.Helper.FirebaseHelper
import com.example.lestari.databinding.ActivityHomepageBinding
import com.example.lestari.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var dataHelper: FirebaseHelper
    private lateinit var user: List<User>
    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataHelper = FirebaseHelper(this)

        loadData()


        binding.ivButtonBack.setOnClickListener{
            onBackPressed()
        }
        binding.cvButtonEdit.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.cvButtonLogout.setOnClickListener {
            val sharedPreferences = getSharedPreferences("login_status", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("status", "logout")
            editor.apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadData(){
        dataHelper.getUserData(
            onSuccess = {userList->
                user = userList
                if (user.isNotEmpty()) {
                    // Load profile picture using Glide
                    Glide.with(this)
                        .load(user[0].picture)
                        .circleCrop()
                        .into(binding.ivProfPic)
                    binding.tvEmail.setText(user[0].email)
                    binding.tvName.setText(user[0].name)
                    binding.tvPhone.setText(user[0].phone)
                    binding.tvUserId.setText(user[0].userId)
                }
            },
            onFailure = {
                Toast.makeText(this, "Cant connect to database", Toast.LENGTH_SHORT).show()
            }
        )
    }
}