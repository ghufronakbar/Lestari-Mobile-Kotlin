package com.example.lestari

import android.app.DatePickerDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.lestari.Helper.FirebaseHelper
import com.example.lestari.RecycleView.addAnimal
import com.example.lestari.RecycleView.animal
import com.example.lestari.databinding.ActivityAddDataBinding
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


@Suppress("DEPRECATION")
class AddDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddDataBinding
    private var selectedImageUri: Uri? = null
    private lateinit var dataHelper: FirebaseHelper

    private val pickImage: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                // Get the selected image URI
                selectedImageUri = result.data?.data

                // Set the ImageView with the selected image
                binding.ivPreviewData.setImageURI(selectedImageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDataBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.ivButtonBack.setOnClickListener{
            onBackPressed()
        }

        binding.ivAddData.setOnClickListener{
            openGallery()

        }

        binding.buttonGetLoc.setOnClickListener{
            startActivityForResult(Intent(this, MapActivity::class.java), 1)
        }

        binding.ivDate.setOnClickListener {
            showDatePickerDialog()
        }


        dataHelper = FirebaseHelper(this)

        binding.ivButtonAdd.setOnClickListener{
            if (selectedImageUri != null) {
                dataHelper.uploadImageToFirebase(
                    selectedImageUri!!,
                    onSuccess = { imageUrl ->
                        val newAnimal = addAnimal(
                            animal_picture = imageUrl,
                            city = binding.tvCity.text.toString(),
                            date = binding.tvDate.text.toString(),
                            desc = binding.etDesc.text.toString(),
                            latin_name = binding.etLatin.text.toString(),
                            latitude = binding.tvLatitude.text.toString(),
                            local_name = binding.etLocal.text.toString(),
                            longitude = binding.tvLongitude.text.toString(),
                            uid = dataHelper.getUID()
                        )
                        Log.d("AAAAAAAAAAAAAAAAAAAAAAAAAA", "onCreate: "+newAnimal)
                        dataHelper.addAnimal(
                            newAnimal,
                            onSuccess = {
                                val intent = Intent(this, HomepageActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                        ) { errorMessage ->
                            // Handle failure
                            Toast.makeText(this, "Animal added Failed.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { errorMessage ->
                    }
                )
            } else {

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
