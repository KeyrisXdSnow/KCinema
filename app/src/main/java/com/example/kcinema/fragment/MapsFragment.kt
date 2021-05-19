package com.example.kcinema.fragment

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kcinema.MainActivity
import com.example.kcinema.R
import com.example.kcinema.activity.CONTENT
import com.example.kcinema.activity.ContentActivity
import com.example.kcinema.model.ContentEntity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.lang.reflect.InvocationTargetException

class MapsFragment : Fragment(), GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap

    val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        googleMap.setOnMarkerClickListener(this)
        initMarkers()
    }

    companion object {
        var isChange = false
    }

    override fun onStart() {
        super.onStart()
        if (isChange) {
            mMap.clear()
            initMarkers()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    private fun readContent() {
        val data: MutableList<ContentEntity> = mutableListOf()

        MainActivity.database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (postSnapshot in dataSnapshot.children) {
                    val value = postSnapshot.getValue(ContentEntity::class.java) as ContentEntity
                    data.add(value)
                }
                try {
                    mMap.let {
                        callback.onMapReady(mMap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        MainActivity.content = data
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        if (MainActivity.content.isEmpty()) {
            readContent()
        } else {
        }
    }

    private fun initMarkers() {

        var index = 0
        MainActivity.content.forEach { content ->
            content.country?.let { country ->

                if (country.isNotEmpty()) {
                    getLocationFromAddress(country)?.let {
                        val location = LatLng(it["latitude"]!!, it["longitude"]!!)

                        val marker =
                            mMap.addMarker(MarkerOptions().position(location).title(content.name))
                        marker.tag = index
                    }
                }
                index++
            }
        }
    }

    private fun getLocationFromAddress(address: String): Map<String, Double?>? {

        try {
            val coder = Geocoder(view?.context)
            val coord = coder.getFromLocationName(address, 1)

            if (coord.isEmpty()) return null

            coord[0]?.let {
                return mapOf<String, Double?>(
                    "latitude" to it.latitude,
                    "longitude" to it.longitude
                )
            } ?: run {}
        } catch (e: Exception) {
        }
        return null
    }

    override fun onMarkerClick(p0: Marker?): Boolean {

        val intent = Intent(context, ContentActivity::class.java).apply {
            putExtra(CONTENT, MainActivity.content[p0!!.tag as Int])
            // activity?.finish()
        }
        startActivity(intent)
        return true


    }
}