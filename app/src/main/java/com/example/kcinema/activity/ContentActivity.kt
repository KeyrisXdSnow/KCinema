package com.example.kcinema.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.example.kcinema.MainActivity
import com.example.kcinema.MainActivity.Companion.database
import com.example.kcinema.R
import com.example.kcinema.fragment.ContentFragment
import com.example.kcinema.fragment.MapsFragment
import com.example.kcinema.model.ContentEntity
import com.example.kcinema.model.OnContentEntityChangedCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.squareup.picasso.Picasso
import java.util.regex.Pattern

const val CONTENT = "content"

class ContentActivity : AppCompatActivity(), LifecycleObserver {

    private var changedId: String? = null

    companion object {

        lateinit var fragment: ContentFragment
        lateinit var contentEntity: ContentEntity


        val callback = OnContentEntityChangedCallback { newContentEntity ->

            val id = MainActivity.allContent.indexOfFirst {
                it.id == contentEntity.id
            }

            MainActivity.allContent[id] =
                newContentEntity
            contentEntity = newContentEntity


            fragment.initView()

            MainActivity.content = emptyList<ContentEntity>().toMutableList()
            MapsFragment.isChange = true


        }
    }

    lateinit var poster: ImageView
    lateinit var name: TextView
    lateinit var rating: RatingBar
    lateinit var description: TextView
    lateinit var country: TextView
    lateinit var watchTime: TextView
    lateinit var releaseDate: TextView
    lateinit var youtubeView: YouTubePlayerView
    internal lateinit var deleteButton: Button
    lateinit var editButton: Button

    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        deleteButton = findViewById(R.id.delete_button)
        editButton = findViewById(R.id.edit_button)
        deleteButton.setOnClickListener {
            deleteContent()
            val intent = Intent(deleteButton.context, MainActivity::class.java)
            startActivity(intent)
        }
        editButton.setOnClickListener {

            val intent =
                Intent(
                    editButton.context,
                    ContentRedactorActivity::class.java
                ).apply {
                    putExtra(CONTENT, contentEntity)
                }
            startActivity(intent)
        }

        name = findViewById(R.id.content_name)
        poster = findViewById(R.id.content_poster)
        description = findViewById(R.id.content_description)
        rating = findViewById(R.id.font_size)
        country = findViewById(R.id.content_country)
        watchTime = findViewById(R.id.content_watchTime)
        releaseDate = findViewById(R.id.content_releaseDate)
        youtubeView = findViewById(R.id.video)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (changedId != null) {
                        var i = 0
                        MainActivity.allContent.forEach {

                            if (it.id.equals(changedId)) {
                                MainActivity.allContent.set(i, contentEntity)
                            }
                            i++
                        }
                    }
                    finish()
                }
            }
        this.onBackPressedDispatcher.addCallback(this, callback)

        val fragment = (supportFragmentManager.fragments[0] as ContentFragment)

        var content = intent.getSerializableExtra(CONTENT)
        if (content != null) {
            contentEntity = content as ContentEntity
            runOnUiThread {
                fragment.initView()
            }
            intent.removeExtra(CONTENT)
        }
    }


    private fun deleteContent() {
        database.child(contentEntity.id).removeValue()
        MainActivity.content.remove(contentEntity)
    }

    class ImageGalleryRecyclerAdapter(private val photos: MutableList<String>) :
        RecyclerView.Adapter<ImageGalleryRecyclerAdapter.GalleryViewHolder>() {

        class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var image: ImageView? = null

            init {
                image = itemView.findViewById(R.id.iv_photo)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
            val itemView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_image, parent, false)
            return GalleryViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {

            try {
                holder.image?.let {
                    Picasso.get()
                        .load(photos[position])
                        .into(holder.image)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount() = photos.size
    }


}


object YouTubeHelper {

    private const val youTubeUrlRegEx = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/"
    private val videoIdRegex = arrayOf(
        "\\?vi?=([^&]*)",
        "watch\\?.*v=([^&]*)",
        "(?:embed|vi?)/([^/?]*)",
        "^([A-Za-z0-9\\-]*)"
    )

    fun extractVideoIdFromUrl(url: String): String? {
        val youTubeLinkWithoutProtocolAndDomain = youTubeLinkWithoutProtocolAndDomain(url)

        for (regex in videoIdRegex) {
            val compiledPattern = Pattern.compile(regex)
            val matcher = compiledPattern.matcher(youTubeLinkWithoutProtocolAndDomain)

            if (matcher.find()) {
                return matcher.group(1)
            }
        }

        return null
    }

    private fun youTubeLinkWithoutProtocolAndDomain(url: String): String {
        val compiledPattern = Pattern.compile(youTubeUrlRegEx)
        val matcher = compiledPattern.matcher(url)

        return if (matcher.find()) {
            url.replace(matcher.group(), "")
        } else url
    }
}