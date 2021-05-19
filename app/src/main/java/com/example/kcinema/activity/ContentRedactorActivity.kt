package com.example.kcinema.activity

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kcinema.MainActivity
import com.example.kcinema.R
import com.example.kcinema.fragment.contentTypeData
import com.example.kcinema.model.ContentEntity
import com.example.kcinema.model.ContentType
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.util.*


class ContentRedactorActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var database: DatabaseReference

    lateinit var name: EditText
    lateinit var genres: EditText
    private lateinit var contentType: Spinner

    lateinit var rating: SeekBar
    lateinit var ratingValue: TextView
    lateinit var country: EditText
    lateinit var watchTime: EditText
    lateinit var premiere: EditText
    lateinit var posterUrl: EditText
    lateinit var trailerUrl: EditText
    lateinit var description: MultiAutoCompleteTextView

    private lateinit var saveContent: Button

    lateinit var urlsList: MutableList<String>
    lateinit var recyclerView: RecyclerView

    lateinit var editingContent: ContentEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_redactor)
        database = Firebase.database.getReference("content")

        name = findViewById(R.id.name)
        contentType = findViewById(R.id.contentTypeSpinner)
        genres = findViewById(R.id.genres)
        country = findViewById(R.id.content_country)
        rating = findViewById(R.id.font_size)
        ratingValue = findViewById(R.id.font_size_value)
        watchTime = findViewById(R.id.content_watchTime)
        premiere = findViewById(R.id.realeseDate)
        posterUrl = findViewById(R.id.posterUrl)
        trailerUrl = findViewById(R.id.trailerUrl)
        description = findViewById(R.id.description)


        rating.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ratingValue.text = (rating.progress / 10.0).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        contentType.adapter =
            ArrayAdapter(
                applicationContext,
                android.R.layout.simple_list_item_1,
                contentTypeData.values.toTypedArray()
            )

        urlsList = mutableListOf()
        saveContent = findViewById(R.id.save)

        val content = intent.getSerializableExtra(CONTENT)
        if (content != null) {
            try {
                this.editingContent = content as ContentEntity
                initContentForEditing()

                saveContent.setOnClickListener {
                    updateContent()
                }
            } catch (e: NullPointerException) {
            }
        } else {
            saveContent.setOnClickListener {
                writeContent()
            }
        }

        recyclerView = findViewById(R.id.galleryUrls)

        recyclerView.apply {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = GalleryUrlsRecyclerAdapter(urlsList)
        }
    }

    private fun initContentForEditing() {

        editingContent.name?.let {
            name.setText(it)
        }
        editingContent.contentType?.let {
            contentType.setSelection(contentTypeData.keys.indexOf(editingContent.contentType))
        }
        editingContent.genres?.let {
            genres.setText(it)
        }
        editingContent.country?.let {
            country.setText(it)
        }
        editingContent.rating?.let {
            rating.progress = it.toInt() * 10
            ratingValue.text = it.toString()
        }
        editingContent.watchTime?.let {
            watchTime.setText(it.toString())
        }
        editingContent.releaseDate?.let {
            premiere.setText(it)
        }
        editingContent.posterUrl?.let {
            posterUrl.setText(it)
        }
        editingContent.trailerUrl?.let {
            trailerUrl.setText(it)
        }
        editingContent.description?.let {
            description.setText(it)
        }

        editingContent.gallery?.let {
            urlsList = editingContent.gallery!!
        }

        //contentType = findViewById(R.id.contentTypeSpinner)

    }

    private fun writeContent() {

        val data = mutableMapOf<String, Any>()
        data["name"] = name.text.toString()

        val type: Set<ContentType> = contentTypeData.filterKeys {
            contentTypeData[it].equals(contentType.selectedItem as String)
        }.keys

        data["contentType"] =
            type.elementAt(0).toString()

        if (genres.text.isNotEmpty()) {
            data["genres"] = genres.text.toString()
        }
        try {
            val rating = ratingValue.text.toString().toFloat()
            data["rating"] = rating
        } catch (e: NumberFormatException) {
        }

        if (country.text.isNotEmpty()) {
            data["country"] = country.text.toString()
        }
        try {
            val time = watchTime.text.toString().toInt()
            data["watchTime"] = time
        } catch (e: NumberFormatException) {
        }

        try {
            val dd = premiere.text.toString().subSequence(0, 2).toString().toInt()
            val mm = premiere.text.toString().subSequence(3, 5).toString().toInt()
            val yy =
                premiere.text.toString().subSequence(6, premiere.text.toString().length).toString()
                    .toInt()
            val date = LocalDate.of(yy, mm, dd)

            data["releaseDate"] = date.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (posterUrl.text.isNotEmpty()) {
            data["posterUrl"] = posterUrl.text.toString()
        }
        if (trailerUrl.text.isNotEmpty()) {
            data["trailerUrl"] = trailerUrl.text.toString()
        }
        if (description.text.isNotEmpty()) {
            data["description"] = description.text.toString()
        }

        if (urlsList.isNotEmpty()) {
            data["gallery"] = urlsList
        }
        database.child(database.push().key.toString()).setValue(data)
        MainActivity.content.clear()
        finish()
    }

    private fun updateContent() {

        val key = database.child("posts").push().key
        if (key == null) {
            Log.w("ContentRedactorActivity", "Couldn't get push key - content")
            return
        }

        val data = mutableMapOf<String, Any>()
        val newContent = editingContent

        if (name.text.toString() != editingContent.name) {
            data["name"] = name.text.toString()
            newContent.name = name.text.toString()
        }

        editingContent.contentType?.let {
            val type: Set<ContentType> = contentTypeData.filterKeys {
                contentTypeData[it].equals(contentType.selectedItem as String)
            }.keys
            if (editingContent.contentType != ContentType.valueOf(type.elementAt(0).toString())) {
                data["contentType"] = type.elementAt(0).toString()
                newContent.contentType = ContentType.valueOf(type.elementAt(0).toString())
            }
        }

        if (genres.text.toString() != editingContent.genres) {
            data["genres"] = genres.text.toString()
            newContent.genres = genres.text.toString()
        }

        try {
            val rating = ratingValue.text.toString().toFloat()
            if (rating != editingContent.rating) {
                data["rating"] = rating
                newContent.rating = rating
            }
        } catch (e: NumberFormatException) {
        }

        if (country.text.isNotEmpty()) {
            if (country.text.toString() != editingContent.country) {
                data["country"] = country.text.toString()
                newContent.country = country.text.toString()
            }
        }
        try {
            val time = watchTime.text.toString().toInt()
            if (time != editingContent.watchTime) {
                data["watchTime"] = time
                newContent.watchTime = time
            }
        } catch (e: NumberFormatException) {
        }

        try {
            val dd = premiere.text.toString().subSequence(0, 2).toString().toInt()
            val mm = premiere.text.toString().subSequence(3, 5).toString().toInt()
            val yy =
                premiere.text.toString().subSequence(6, premiere.text.toString().length).toString()
                    .toInt()
            val date = LocalDate.of(yy, mm, dd)

            if (date.toString() != editingContent.releaseDate) {
                data["releaseDate"] = date.toString()
                newContent.releaseDate = data.toString()
            }
        } catch (e: Exception) {
        }

        if (posterUrl.text.toString() != editingContent.posterUrl) {
            data["posterUrl"] = posterUrl.text.toString()
            newContent.posterUrl = posterUrl.text.toString()
        }

        if (trailerUrl.text.toString() != editingContent.trailerUrl) {
            data["trailerUrl"] = trailerUrl.text.toString()
            newContent.trailerUrl = trailerUrl.text.toString()
        }

        if (description.text.toString() != editingContent.description) {
            data["description"] = description.text.toString()
            newContent.description = description.text.toString()
        }


        data["gallery"] = urlsList
        newContent.gallery = urlsList

        database.child(editingContent.id).updateChildren(data)

        ContentActivity.callback.onContentChanged(newContent)
        MainActivity.content.clear()
        finish()
    }


    class GalleryUrlsRecyclerAdapter(private val data: MutableList<String>) :
        RecyclerView.Adapter<GalleryUrlsRecyclerAdapter.CatalogViewHolder>() {

        class CatalogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var url: EditText? = null
            var delete: ImageButton? = null

            init {
                url = itemView.findViewById(R.id.url)
                delete = itemView.findViewById(R.id.deleteUrl)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
            val itemView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.redactor_url_item, parent, false)
            return CatalogViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
            // init edit text
            val url = holder.url

            url?.setOnEditorActionListener(
                OnEditorActionListener { _, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action === KeyEvent.ACTION_DOWN && event.keyCode === KeyEvent.KEYCODE_ENTER) {
                        if (event == null || !event.isShiftPressed) {
                            // the user is done typing.
                            if (url.text.toString().isNotEmpty()) {
                                this.data.add(position, url.text.toString())
                            } else {
                                if (data.size != position) {
                                    this.data.removeAt(position)
                                    notifyItemChanged(position)
                                }
                            }
                            return@OnEditorActionListener true // consume.
                        }
                    }
                    false // pass on to oth,er listeners.
                }
            )

            if (data.size != position) {
                url?.setText(data[position])
            } else {
                url?.setText("")
            }

            // init delete button
            holder.delete?.setOnClickListener {
                if (position != data.size) {
                    data.removeAt(position)
                } else {
                    url?.setText("")
                }
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = data.size + 1
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Toast.makeText(this, "Select content type", Toast.LENGTH_LONG).show()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val items: String = p0?.getItemAtPosition(p2) as String
        Toast.makeText(this, items, Toast.LENGTH_LONG).show()
    }
}
