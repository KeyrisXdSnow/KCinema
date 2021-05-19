package com.example.kcinema

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.kcinema.activity.ContentRedactorActivity
import com.example.kcinema.activity.LoginActivity
import com.example.kcinema.fragment.CatalogFragment
import com.example.kcinema.fragment.MapsFragment
import com.example.kcinema.fragment.SettingsFragment
import com.example.kcinema.fragment.contentTypeData
import com.example.kcinema.model.ContentEntity
import com.example.kcinema.model.ContentType
import com.example.kcinema.utils.LocaleHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var content: MutableList<ContentEntity>
        lateinit var allContent: MutableList<ContentEntity>

        lateinit var database: DatabaseReference
        var typeIndex: Int = 0;
    }

    lateinit var catalogFragment: CatalogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = Firebase.database.getReference("content")

        content = mutableListOf()

        catalogFragment = CatalogFragment()
        val mapFragment = MapsFragment()
        val settingsFragment = SettingsFragment()

        makeCurrentFragment(catalogFragment)

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val addContent = findViewById<Button>(R.id.add_content)

        addContent.setOnClickListener {
            val intent = Intent(addContent.context, ContentRedactorActivity::class.java).apply {}
            addContent.context.startActivity(intent)
        }

        navView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_catalog -> {
                    addContent.visibility = View.VISIBLE
                    makeCurrentFragment(catalogFragment)
                }
                R.id.navigation_map -> {
                    addContent.visibility = View.VISIBLE
                    makeCurrentFragment(mapFragment)
                }
                R.id.navigation_settings -> {
                    addContent.visibility = View.INVISIBLE
                    makeCurrentFragment(settingsFragment)
                }
            }
            true
        }

        val contentType = mapOf(
            ContentType.Film to getString(R.string.content_type_film),
            ContentType.Series to getString(R.string.content_type_series),
            ContentType.Cartoon to getString(R.string.content_type_cartoon),
            ContentType.AnimatedSeries to getString(R.string.content_type_animatedSeries),
            ContentType.Anime to getString(R.string.content_type_anime)
        )
        contentTypeData = contentType
    }


    override fun onResume() {
        super.onResume()

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            this.startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }

    public override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase, Locale.getDefault().getDisplayLanguage()))
    }
}