package com.example.kcinema.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kcinema.MainActivity
import com.example.kcinema.MainActivity.Companion.allContent
import com.example.kcinema.MainActivity.Companion.content
import com.example.kcinema.MainActivity.Companion.database
import com.example.kcinema.R
import com.example.kcinema.activity.CONTENT
import com.example.kcinema.activity.ContentActivity
import com.example.kcinema.model.ContentEntity
import com.example.kcinema.model.ContentType
import com.google.android.gms.maps.MapFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


var contentTypeData: Map<ContentType, String> = emptyMap()

class CatalogFragment : Fragment(), OnItemSelectedListener{

    lateinit var navController: NavController
    lateinit var recyclerView: RecyclerView
    lateinit var contentTypeSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_catalog, container, false)
    }

    override fun onStart() {
        super.onStart()

        if (MapsFragment.isChange) {
            content.clear()
            recyclerView.adapter?.notifyDataSetChanged()
            readContent()
            MapsFragment.isChange = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.catalog)
        recyclerView.apply {
            recyclerView.layoutManager = GridLayoutManager(view.context, 2)
            if (content.isEmpty()) {
                readContent()
            } else {
                recyclerView.adapter = CatalogRecyclerAdapter(content)
            }
        }
        val types = mutableSetOf("All")
        types.addAll(contentTypeData.values)

        contentTypeSpinner = (activity as MainActivity).findViewById(R.id.main_content_type)
        contentTypeSpinner.adapter = ArrayAdapter(
            (activity as MainActivity).applicationContext,
            android.R.layout.simple_list_item_1,
            types.toTypedArray()
        )

       contentTypeSpinner.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val items: String = parent?.selectedItem as String

                if (items != "All") {

                    val type:Set<ContentType> = contentTypeData.filterKeys {
                        contentTypeData[it].equals(items)
                    }.keys

                    val content = allContent.filter {
                        it.contentType == ContentType.valueOf(type.elementAt(0).toString())
                    }
                    MainActivity.typeIndex = parent.selectedItemId.toInt()
                    MainActivity.content = content as MutableList<ContentEntity>
                    recyclerView.adapter = CatalogRecyclerAdapter(content)
                 } else {
                    recyclerView.adapter = CatalogRecyclerAdapter(allContent.toList())
                }
            }

        }
        contentTypeSpinner.setSelection(MainActivity.typeIndex)
    }

    private fun readContent() {

        val data: MutableList<ContentEntity> = mutableListOf()

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (postSnapshot in dataSnapshot.children) {
                    val value = postSnapshot.getValue(ContentEntity::class.java) as ContentEntity
                    value.id = postSnapshot.key!!
                    data.add(value)

                }
                content = data

                val type = contentTypeSpinner.selectedItem as String
                if (type != "All") {

                    val type:Set<ContentType> = contentTypeData.filterKeys {
                        contentTypeData[it].equals(type)
                    }.keys

                    val content = allContent.filter {
                        it.contentType == ContentType.valueOf(type.elementAt(0).toString())
                    }
                    MainActivity.typeIndex = contentTypeSpinner.selectedItemId.toInt()
                    MainActivity.content = content as MutableList<ContentEntity>
                    recyclerView.adapter = CatalogRecyclerAdapter(content)
                } else {
                    recyclerView.adapter = CatalogRecyclerAdapter(content.toList())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        allContent = data

    }

    class CatalogRecyclerAdapter(private val contentList: List<ContentEntity>) :
        RecyclerView.Adapter<CatalogRecyclerAdapter.CatalogViewHolder>() {

        class CatalogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var contentName: TextView? = null
            var poster: ImageView? = null
            var genres: TextView? = null
            var contentItem: ConstraintLayout? = null
            var contentType : Spinner? = null


            init {
                contentName = itemView.findViewById(R.id.content_name)
                poster = itemView.findViewById(R.id.poster)
                contentItem = itemView.findViewById(R.id.content_item)
                genres = itemView.findViewById(R.id.genres)
                contentType = itemView.findViewById(R.id.main_content_type)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
            val itemView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.catalog_item, parent, false)
            return CatalogViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {

                holder.contentName?.text = contentList[position].name

            contentList[position].genres?.apply {
                holder.genres?.text = contentList[position].genres
            }

            holder.contentItem?.setOnClickListener {
                val intent =
                    Intent(holder.contentItem!!.context, ContentActivity::class.java).apply {
                        putExtra(CONTENT, contentList[position])
                    }
                holder.contentItem!!.context.startActivity(intent)
            }
            try {
                holder.poster?.let {
                    Picasso.get()
                        .load(contentList[position].posterUrl)
                        .into(holder.poster)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount() = contentList.size

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        TODO("Not yet implemented")
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}
