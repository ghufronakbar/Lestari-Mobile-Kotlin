package com.example.lestari

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lestari.RecycleView.AnimalAdapter
import com.example.lestari.databinding.ActivityHomepageBinding
import com.example.lestari.Helper.FirebaseHelper

class HomepageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomepageBinding
    private lateinit var animalAdapter: AnimalAdapter
    private lateinit var dataHelper: FirebaseHelper
    private lateinit var user: List<User>
    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animalAdapter = AnimalAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        val layoutManager : RecyclerView.LayoutManager = GridLayoutManager(this, 2)
        binding.rvHistory.layoutManager = layoutManager
        binding.rvHistory.adapter = animalAdapter
        val ivProfPic=binding.ivProfPic
        val rvHistory=binding.rvHistory
        val fabAdd=binding.fabAdd
        val svHistory=binding.svHistory

        dataHelper = FirebaseHelper(this)
        loadData()
        svHistory.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle query submission if needed
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle text change in the SearchView
                animalAdapter.filter.filter(newText)

                if (newText=="") {
                    // Reset the adapter to the original list when the text is cleared
                    loadData()
                }

                return true
            }
        })


        rvHistory.setOnClickListener {
            val intent = Intent(this, EditDataActivity::class.java)
            startActivity(intent)
        }


        ivProfPic.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        fabAdd.setOnClickListener{
            val intent = Intent(this, AddDataActivity::class.java)
            startActivity(intent)
        }

    }
    private fun loadData(){
        dataHelper.getUserData(
            onSuccess = {userList->
                user = userList
                if (user.isNotEmpty()) {
                    val userProfilePictureUrl = user[0].picture

                    // Load profile picture using Glide
                    Glide.with(this)
                        .load(userProfilePictureUrl)
                        .circleCrop()
                        .into(binding.ivProfPic)
                }
            },
            onFailure = {
                Toast.makeText(this, "Cant connect to database", Toast.LENGTH_SHORT).show()
            }
        )

        dataHelper.getAnimalByUID(
            onSuccess = {animalList->
                animalAdapter.submitList(animalList)
            },
            onFailure = {
                animalAdapter.submitList(null)
            }
        )
    }
}