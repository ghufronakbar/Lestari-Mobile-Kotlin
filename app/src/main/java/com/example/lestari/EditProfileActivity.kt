package com.example.lestari

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.lestari.Helper.FirebaseHelper
import com.example.lestari.databinding.ActivityEditProfileBinding
import com.example.lestari.databinding.ActivityHomepageBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var dataHelper: FirebaseHelper
    private lateinit var user: List<User>
    private var selectedImageUri: Uri? = null

    private val pickImage: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                // Get the selected image URI
                selectedImageUri = result.data?.data

                // Set the ImageView with the selected image
                Glide.with(this)
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(binding.ivPrevProfpic)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataHelper = FirebaseHelper(this)
        val email = binding.etEmail
        val name = binding.etName
        val phone = binding.etPhone

        dataHelper.getUserData(
            onSuccess = {userList->
                user = userList
                if (user.isNotEmpty()) {
                    // Load profile picture using Glide
                    Glide.with(this)
                        .load(user[0].picture)
                        .circleCrop()
                        .into(binding.ivPrevProfpic)
                    email.setText(user[0].email)
                    name.setText(user[0].name)
                    phone.setText(user[0].phone)
                    binding.tvUserId.setText(user[0].userId)
                }
            },
            onFailure = {
                Toast.makeText(this, "Cant connect to database", Toast.LENGTH_SHORT).show()
            }
        )
        binding.ivEditProfpic.setOnClickListener{
            openGallery()
        }
        binding.ivButtonBack.setOnClickListener {
            onBackPressed()
        }

        binding.cvButtonConfirmProf.setOnClickListener {
            val upUser = mutableMapOf(
                "email" to email.text.toString(),
                "name" to name.text.toString(),
                "phone" to phone.text.toString(),
                "picture" to user[0].picture
            )
            if (selectedImageUri != null) {
                dataHelper.updateProfilePicture(
                    selectedImageUri!!,
                    onSuccess = { imageUrl ->
                        upUser["picture"] = imageUrl // Add the updated picture URL to the user data

                        dataHelper.updateUserData(
                            user = upUser,
                            onSuccess = {
                                Toast.makeText(this, "User data updated successfully", Toast.LENGTH_SHORT).show()
                                onBackPressed()
                                finish()
                            },
                            onFailure = { errorMessage ->
                                Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // If no image is selected, directly update user data without calling updateProfilePicture
                dataHelper.updateUserData(
                    user = upUser,
                    onSuccess = {
                        Toast.makeText(this, "User data updated successfully", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            }


        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(galleryIntent)
    }
}