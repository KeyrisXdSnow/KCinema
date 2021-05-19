package com.example.kcinema.model

import java.io.Serializable

class ContentEntity() : Serializable {

    lateinit var id: String

    var name: String? = null
    var contentType: ContentType? = null
    var genres: String? = null
    var rating: Float? = null

    var country: String? = null
    var watchTime: Int? = 0
    var releaseDate: String? = null

    var posterUrl: String? = null
    var trailerUrl: String? = null

    var description: String? = null

    var gallery: MutableList<String>? = null


    constructor(
        id: String,
        name: String,
        contentType: String,
        rating:Float,
        genres: String,
        country: String,
        watchTime: Int,
        releaseDate: String,
        posterUrl: String,
        trailerUrl: String,
        description: String,
        gallery: MutableList<String>

    ) : this() {
        this.id = id
        this.name = name
        this.rating = rating
        this.genres = genres
        this.country = country
        this.watchTime = watchTime
        this.releaseDate = releaseDate
        this.posterUrl = posterUrl
        this.trailerUrl = trailerUrl
        this.description = description
        this.gallery = gallery

        this.contentType = ContentType.valueOf(contentType)
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "contentType" to contentType,
            "rating" to rating,
            "genres" to genres,
            "country" to country,
            "watchTime" to watchTime,
            "releaseDate" to releaseDate,
            "posterUrl" to posterUrl,
            "trailerUrl" to trailerUrl,
            "description" to description,
            "gallery" to gallery

        )
    }

}

enum class ContentType {
    Film,
    Series,
    Cartoon,
    AnimatedSeries,
    Anime,
}