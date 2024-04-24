package com.example.lestari.Helper

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.lestari.RecycleView.addAnimal
import com.example.lestari.RecycleView.animal
import com.example.lestari.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.UUID

class FirebaseHelper(private val activity: Activity) {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var database: DatabaseReference
    private var storageReference: StorageReference
    private var storageReferenceProf: StorageReference

    init {
        storageReference = FirebaseStorage.getInstance().reference.child("animal")
        storageReferenceProf = FirebaseStorage.getInstance().reference.child("user")
    }

    fun signIn(loginId: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        database = FirebaseDatabase.getInstance().reference.child("auth")
        database.child(loginId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val email = snapshot.child("email").value.toString()
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                onSuccess()
                            } else {
                                onFailure(task.exception?.message ?: "Authentication failed.")
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun getUserData(onSuccess: (List<User>) -> Unit, onFailure: (String) -> Unit) {
        database = FirebaseDatabase.getInstance().reference.child("user").child(auth.currentUser?.uid.toString())
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userList = mutableListOf<User>()
                    val email = dataSnapshot.child("email").getValue(String::class.java)
                    val name = dataSnapshot.child("name").getValue(String::class.java)
                    val phone = dataSnapshot.child("phone").getValue(String::class.java)
                    val picture = dataSnapshot.child("picture").getValue(String::class.java)
                    val userId = dataSnapshot.child("userid").getValue(String::class.java)

                    val user = User(
                        email ?: "",
                        name ?: "",
                        phone ?: "",
                        picture ?: "",
                        userId ?: ""

                    )

                    userList.add(user)
                    Log.d("AAAAAAAAAAAAAAAA", "onDataChange: "+userList)

                    onSuccess(userList)
                } else {
                    onFailure("User not found.")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                onFailure(databaseError.message)
            }
        })
    }

    fun updateUserData(
        user: Map<String, String>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = getUID()

        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().reference.child("user").child(uid)

            userRef.updateChildren(user)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure("Failed to update user data: ${e.message}")
                }
        } else {
            onFailure("User not authenticated.")
        }
    }

    fun getAnimalByUID(onSuccess: (List<animal>) -> Unit, onFailure: (String) -> Unit) {
        database = FirebaseDatabase.getInstance().reference.child("animal")
        val uid = auth.currentUser?.uid

        if (uid != null) {
            val query: Query = database.orderByChild("uid").equalTo(uid.toString())

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val animalList = mutableListOf<animal>()
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val animalId = snapshot.key
                            val animal_picture = snapshot.child("animal_picture").getValue(String::class.java)
                            val city = snapshot.child("city").getValue(String::class.java)
                            val date = snapshot.child("date").getValue(String::class.java)
                            val desc = snapshot.child("desc").getValue(String::class.java)
                            val latinName = snapshot.child("latin_name").getValue(String::class.java)
                            val latitude = snapshot.child("latitude").getValue(String::class.java)
                            val localName = snapshot.child("local_name").getValue(String::class.java)
                            val longitude = snapshot.child("longitude").getValue(String::class.java)
                            val uid = snapshot.child("uid").getValue(String::class.java)
                            val animal = animal(
                                animalId?:"",
                                animal_picture ?: "",
                                city ?: "",
                                date ?: "",
                                desc ?: "",
                                latinName ?: "",
                                localName ?: "",
                                latitude ?: "",
                                longitude ?: "",
                                uid ?: ""
                            )

                            animalList.add(animal)
                        }
                        onSuccess(animalList)
                    } else {
                        onFailure("Animal not found.")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure(databaseError.message)
                }
            })
        } else {
            onFailure("User not authenticated.")
        }
    }

    fun addAnimal(animal: addAnimal, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference.child("animal")
        val uid = auth.currentUser?.uid

        if (uid != null) {
            val animalRef = database.push()
            animalRef.setValue(animal)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener {
                    onFailure(it.message ?: "Failed to add animal.")
                }
        } else {
            onFailure("User not authenticated.")
        }
    }

    fun uploadImageToFirebase(imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val imageName = UUID.randomUUID().toString() // Generate a unique name for the image
        val imageRef = storageReference.child("\"$imageName")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Simpan link foto ke Firebase Realtime Database
                    val photoUrl = uri.toString()
                    onSuccess(photoUrl)
                }
            }
            .addOnFailureListener { e ->
                onFailure("Failed to upload image: ${e.message}")
            }
    }

    fun updateAnimal(
        updatedAnimal: animal,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance().reference.child("animal")
        val animalId = updatedAnimal.animalId
        val updatedAnimal = addAnimal(
            animal_picture = updatedAnimal.animal_picture,
            city = updatedAnimal.city,
            date = updatedAnimal.date,
            desc = updatedAnimal.desc,
            latin_name = updatedAnimal.latin_name,
            local_name = updatedAnimal.local_name,
            latitude = updatedAnimal.latitude,
            longitude = updatedAnimal.longitude,
            uid = updatedAnimal.uid,
        )

        if (animalId != null) {
            val animalRef = database.child(animalId)

            animalRef.setValue(updatedAnimal)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure("Failed to update animal data: ${e.message}")
                }
        } else {
            onFailure("Invalid animal ID.")
        }
    }

    fun updateProfilePicture(imageUri: Uri, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val uid = getUID()

        if (uid != null) {
            val imageRef = storageReferenceProf.child(uid)

            // Upload the image to Firebase Storage
            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    // Get the download URL
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        onSuccess(imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    onFailure("Failed to upload profile picture: ${e.message}")
                }
        } else {
            onFailure("User not authenticated.")
        }
    }

    fun updateAnimalPhoto(
        animalId: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val storageReference = FirebaseStorage.getInstance().reference.child("animal_photos")
        val imageName = UUID.randomUUID().toString() // Generate a unique name for the image
        val imageRef = storageReference.child("$animalId/$imageName")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Save the updated animal photo URL to Firebase Realtime Database
                    val imageUrl = uri.toString()
                    onSuccess(imageUrl)
                }
            }
            .addOnFailureListener { e ->
                onFailure("Failed to upload animal photo: ${e.message}")
            }
    }


    fun deleteAnimal(
        animalId: String,
        animalPictureUrl: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Delete the animal photo from Firebase Storage
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(animalPictureUrl)
        storageReference.delete()
            .addOnFailureListener { e ->
                Log.e("FirebaseHelper", "Failed to delete animal photo: ${e.message}")
            }

        // Delete the animal data from Firebase Realtime Database
        val database = FirebaseDatabase.getInstance().reference.child("animal").child(animalId)
        database.removeValue()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure("Failed to delete animal data: ${e.message}")
            }
    }



    fun getUID(): String? {
        return auth.currentUser?.uid
    }


    fun signOut() {
        auth.signOut()
    }

}