package com.example.lestari

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.lestari.Helper.FirebaseHelper
import com.example.lestari.RecycleView.animal
import com.example.lestari.databinding.ActivityDetailDataBinding


class DetailDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailDataBinding
    private val EDIT_ACTIVITY_REQUEST_CODE = 1

    private val dataHelper: FirebaseHelper by lazy {
        FirebaseHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val animalData: animal = intent.getParcelableExtra("animalData")!!
        binding.etLatin.setText(animalData.latin_name)
        binding.etLocal.setText(animalData.local_name)
        binding.tvDate.setText(animalData.date)
        binding.etDesc.setText(animalData.desc)
        binding.tvCity.setText(animalData.city)
        binding.tvLatitude.setText(animalData.latitude)
        binding.tvLongitude.setText(animalData.longitude)
        Glide.with(this)
            .load(animalData.animal_picture)
            .into(binding.ivPreviewData)

        binding.ivButtonEdit.setOnClickListener {
            val intent = Intent(this, EditDataActivity::class.java)
            intent.putExtra("animalData", animalData)
            startActivityForResult(intent, EDIT_ACTIVITY_REQUEST_CODE)
        }
        binding.ivButtonBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnDeleteData.setOnClickListener {
            dataHelper.deleteAnimal(
                animalData.animalId.toString(),
                animalData.animal_picture.toString(),
                onSuccess = {
                    Toast.makeText(this, "Animal data deleted successfully", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                    finish()
                },
                onFailure = { errorMessage ->
                    Toast.makeText(this, "Failed to delete animal data: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Called when the EditDataActivity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Update the UI with the edited data
            val updatedAnimalData = data?.getParcelableExtra<animal>("updatedAnimalData")
            if (updatedAnimalData != null) {
                updateUI(updatedAnimalData)
                Log.d("AAAAAAAAAAAA", "onActivityResult: "+updatedAnimalData)
            }
        }
    }

    // Update UI with the edited data
    private fun updateUI(updatedAnimalData: animal) {
        binding.etLatin.setText(updatedAnimalData.latin_name)
        binding.etLocal.setText(updatedAnimalData.local_name)
        binding.tvDate.setText(updatedAnimalData.date)
        binding.etDesc.setText(updatedAnimalData.desc)
        binding.tvCity.setText(updatedAnimalData.city)
        binding.tvLatitude.setText(updatedAnimalData.latitude)
        binding.tvLongitude.setText(updatedAnimalData.longitude)

        Glide.with(this)
            .load(updatedAnimalData.animal_picture)
            .into(binding.ivPreviewData)
    }

}