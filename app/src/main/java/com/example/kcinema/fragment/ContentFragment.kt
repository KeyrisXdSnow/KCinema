package com.example.kcinema.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kcinema.R
import com.example.kcinema.activity.ContentActivity
import com.example.kcinema.activity.YouTubeHelper
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.squareup.picasso.Picasso
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class ContentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ContentActivity.fragment = this
    }


    fun initView() {

        val activity = (activity as ContentActivity)

        activity.name.text = ContentActivity.contentEntity.name

        try {
            Picasso.get()
                .load(ContentActivity.contentEntity.posterUrl)
                .into(activity.poster)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ContentActivity.contentEntity.country?.let {
            if (!ContentActivity.contentEntity.country.equals("")) {
                val title = getString(R.string.content_country)
                activity.country.text = "$title $it"
            }
        }
        ContentActivity.contentEntity.watchTime?.let {
            if (ContentActivity.contentEntity.watchTime != 0) {
                val title = getString(R.string.content_watch_time)
                activity.watchTime.text = "$title $it min."
            }
        }
        ContentActivity.contentEntity.releaseDate.let {
            try {
                val title = getString(R.string.content_premier)

                val formatter = DateTimeFormatter.ofPattern("yyyy MMMM dd")
                val date: LocalDate
                try {
                    date = LocalDate.parse(ContentActivity.contentEntity.releaseDate)
                    var resultDate = ""
                    formatter.format(date).split(" ").forEach {
                        resultDate = "$it $resultDate"
                    }

                    activity.releaseDate.text = "$title $resultDate"
                } catch (e: Exception) { }
            } catch (e: Exception) { }
        }

        ContentActivity.contentEntity.description?.let {
            activity.description.text = it
        }

        var player:YouTubePlayer? = null
        if (!ContentActivity.contentEntity.trailerUrl.isNullOrEmpty()) {
            player?.loadVideo(YouTubeHelper.extractVideoIdFromUrl(ContentActivity.contentEntity.trailerUrl!!) ?: "",
                0.0F
            )
            activity.youtubeView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.cueVideo(
                        YouTubeHelper.extractVideoIdFromUrl(ContentActivity.contentEntity.trailerUrl!!) ?: "",
                        0f
                    )
                    player = youTubePlayer
                    activity.youtubeView.visibility = View.VISIBLE
                }

            })
            activity.youtubeView.visibility = View.VISIBLE
        } else {
            activity.youtubeView.visibility = View.GONE
        }


        ContentActivity.contentEntity.gallery?.let {
            activity.recyclerView = activity?.findViewById(R.id.photo_gallery)!!
            activity.recyclerView.apply {
                activity.recyclerView.layoutManager = GridLayoutManager(context, 1)
                activity.recyclerView.adapter = ContentActivity.ImageGalleryRecyclerAdapter(it)
            }
        }

        ContentActivity.contentEntity.rating?.let {
            activity.rating.rating = it / 10.0F * 5
        }

    }


}