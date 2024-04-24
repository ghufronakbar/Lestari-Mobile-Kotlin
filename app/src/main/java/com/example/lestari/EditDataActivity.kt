package com.example.lestari

import android.app.DatePickerDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.lestari.Helper.FirebaseHelper
import com.example.lestari.RecycleView.animal
import com.example.lestari.databinding.ActivityEditDataBinding
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditDataBinding
    private var selectedImageUri: Uri? = null

    private val dataHelper: FirebaseHelper by lazy {
        FirebaseHelper(this)
    }

    private val pickImage: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                // Get the selected image URI
                selectedImageUri = result.data?.data

                // Set the ImageView with the selected image
                Glide.with(this)
                    .load(selectedImageUri)
                    .into(binding.ivPreviewData)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDataBinding.inflate(layoutInflater)
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

        binding.ivAddData.setOnClickListener {
            openGallery()
        }

        binding.ivButtonBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.buttonGetLoc.setOnClickListener{
            startActivityForResult(Intent(this, MapActivity::class.java), 1)
        }




        binding.cvSubmit.setOnClickListener {
            val updatedAnimal = animal(
                animalId = animalData.animalId,
                animal_picture = animalData.animal_picture,
                city = binding.tvCity.text.toString(),
                date = binding.tvDate.text.toString(),
                desc = binding.etDesc.text.toString(),
                latin_name = binding.etLatin.text.toString(),
                local_name = binding.etLocal.text.toString(),
                latitude = binding.tvLatitude.text.toString(),
                longitude = binding.tvLongitude.text.toString(),
                uid = dataHelper.getUID() ?: ""
            )
            if (selectedImageUri!=null){
                dataHelper.updateAnimalPhoto(
                    animalData.animalId.toString(),
                    selectedImageUri!!,
                    onSuccess = { imageUrl ->
                        updatedAnimal.animal_picture = imageUrl
                        dataHelper.updateAnimal(updatedAnimal,
                            onSuccess = {
                                Toast.makeText(this, "Animal data updated successfully", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK, Intent().apply {
                                    putExtra("updatedAnimalData", updatedAnimal)
                                })
                                finish()
                            },
                            onFailure = { errorMessage ->
                                Toast.makeText(this, "Failed to update animal data: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onFailure = {errorMessage ->
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                    )


            }else{
                dataHelper.updateAnimal(updatedAnimal,
                    onSuccess = {
                        Toast.makeText(this, "Animal data updated successfully", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK, Intent().apply {
                            putExtra("updatedAnimalData", updatedAnimal)
                        })
                        finish()
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(this, "Failed to update animal data: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            }



        }
    }
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(galleryIntent)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Tangani tanggal yang dipilih
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)

                // Update TextView dengan tanggal yang dipilih
                updateDateTextView(selectedDate.time)
            },
            year, month, day
        )

        // Tampilkan dialog
        datePickerDialog.show()
    }

    private fun updateDateTextView(selectedDate: Date) {
        // Format tanggal sesuai kebutuhan Anda
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = sdf.format(selectedDate)

        // Setel nilai TextView dengan tanggal yang dipilih
        binding.tvDate.text = formattedDate
    }
    

    // Metode ini dipanggil ketika aktivitas yang dimulai dengan startActivityForResult selesai
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Mendapatkan lokasi dari hasil aktivitas peta
            val location = data?.getParcelableExtra<LatLng>("selected_location")

            // Menampilkan latitude dan longitude
            binding.tvLatitude.text = location?.latitude.toString()
            binding.tvLongitude.text = location?.longitude.toString()

            // Mengonversi koordinat ke alamat kota
            val geocoder = Geocoder(this)
            try {
                val addresses: List<Address>? = location?.let {
                    geocoder.getFromLocation(
                        it.latitude,
                        it.longitude,
                        1
                    )
                }
                val cityName = addresses?.get(0)?.locality ?: "Unknown City"
                binding.tvCity.text = cityName
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
