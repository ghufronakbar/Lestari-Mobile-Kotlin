package com.example.lestari

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lestari.databinding.ActivityAddDataBinding
import com.example.lestari.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var pickerMarker: Marker? = null
    private lateinit var pickLocationButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapBinding

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.pickLocationButton.setOnClickListener {
            val location = pickerMarker?.position
            if (location != null) {
                val intent = Intent()
                intent.putExtra("selected_location", location)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Cek dan minta izin lokasi jika belum diizinkan
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Mengatur lokasi awal ke lokasi pengguna saat ini
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    // Mengatur posisi awal dan tingkat zoom peta
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    pickerMarker?.position = currentLatLng
                }
            }
        } else {
            // Jika belum diizinkan, minta izin
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Menambahkan marker dengan gambar ikon picker di tengah layar
        val centerScreenLocation = LatLng(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude)
        pickerMarker = map.addMarker(
            MarkerOptions()
                .position(centerScreenLocation)
        )

        // Menambahkan OnCameraMoveListener untuk memperbarui posisi marker saat kamera bergerak
        map.setOnCameraMoveListener {
            // Perbarui posisi marker saat kamera bergerak
            pickerMarker?.position = map.cameraPosition.target
        }

        // Menambahkan OnCameraIdleListener untuk memperbarui posisi marker saat kamera berhenti bergerak
        map.setOnCameraIdleListener {
            // Perbarui posisi marker saat kamera berhenti bergerak
            pickerMarker?.position = map.cameraPosition.target
        }
    }


    // Override onRequestPermissionsResult untuk menangani hasil permintaan izin lokasi
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, coba lagi memperbarui lokasi
                onMapReady(map)
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
