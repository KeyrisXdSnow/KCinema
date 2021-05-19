package com.example.kcinema.fragment

import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.kcinema.MainActivity
import com.example.kcinema.R
import com.example.kcinema.utils.LocaleHelper
import com.example.kcinema.utils.TypefaceUtils
import com.google.firebase.auth.FirebaseAuth
import java.util.*


object ApplicationSettings {
    var fontName: String = "roboto"
    var fontSize: Int = 17
}

class SettingsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val fontFamilies = mapOf(
        "roboto" to R.font.roboto_medium,
        "advent" to R.font.advent_pro_medium,
        "serif" to R.font.arbutus_slab,
        "atomic age" to R.font.atomic_age,
        "merienda" to R.font.merienda,
        "sarala" to R.font.sarala
    )

    lateinit var fontType: Spinner
    lateinit var fontSize: SeekBar
    lateinit var fontSizeValue: TextView
    lateinit var logOut: Button

    private lateinit var ligthMode: Switch
    private lateinit var langMode: Switch

    lateinit var okButton:Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewData()
        initFragmentActions()
    }

    private fun initView() {
        view?.let {
            fontType = it.findViewById(R.id.fontType)
            fontSize = it.findViewById(R.id.font_size)
            fontSizeValue = it.findViewById(R.id.font_size_value)
            logOut = it.findViewById(R.id.log_out)
            ligthMode = it.findViewById(R.id.themeMode)
            langMode = it.findViewById(R.id.langMode)
            okButton = it.findViewById(R.id.OK)
        }
    }

    private fun initViewData() {
        //ligthMode.isChecked = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO
        langMode.isChecked = Locale.getDefault().language != Locale.ENGLISH.language

        fontType.adapter =
            this.context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_list_item_1,
                    fontFamilies.keys.toTypedArray()
                )
            }
        fontSize.progress = ApplicationSettings.fontSize
        fontSizeValue.text = ApplicationSettings.fontSize.toString()

    }

    private fun initFragmentActions() {

        fontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                fontSizeValue.text = fontSize.progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                val fontName = fontFamilies[fontType.selectedItem as String]
//                if (fontName != null) { //overrideFont(fontName) }
            }
        })

        if (fontFamilies.containsKey( ApplicationSettings.fontName)) {
            val index = fontFamilies.entries.indexOfFirst { it.key == ApplicationSettings.fontName  }
            fontType.setSelection(index)
        }

        logOut.setOnClickListener {
            activity?.let {
                FirebaseAuth.getInstance().signOut()
                it.startActivity(Intent(it, MainActivity::class.java))
                activity?.finish()
            }

        }

        okButton.setOnClickListener {
            if (ligthMode.isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            if (langMode.isChecked) {
                LocaleHelper.setLocale(requireContext(), "ru")
                resources.configuration.locale = Locale("ru")
            } else {
                LocaleHelper.setLocale(requireContext(), "en")
                resources.configuration.locale = Locale.ENGLISH
            }


            fontFamilies[fontType.selectedItem as String]?.let { it1 ->
                TypefaceUtils.overrideFont(
                    activity?.applicationContext, "SERIF",
                    it1
                )
            }

            resources.configuration.fontScale = fontSize.progress.toFloat() / ApplicationSettings.fontSize.toFloat()
            val metrics = resources.displayMetrics
            val wm = activity?.baseContext?.getSystemService(WINDOW_SERVICE) as WindowManager?
            wm!!.defaultDisplay.getMetrics(metrics)
            metrics.scaledDensity = resources.configuration.fontScale * metrics.density

            ApplicationSettings.fontName = fontType.selectedItem as String
            ApplicationSettings.fontSize = fontSize.progress


            activity?.baseContext?.resources?.updateConfiguration(resources.configuration, metrics)

            activity?.recreate()
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//        val items: String = p0?.getItemAtPosition(p2) as String
//        Toast.makeText(this.context, items, Toast.LENGTH_LONG).show()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        //Toast.makeText(this.context, "Select content type", Toast.LENGTH_LONG).show()
    }

//
//    fun overrideFont(customFontId: Int) {
//        try {
//            (activity as MainActivity).let { activity ->
//
//                val font = ResourcesCompat.getFont(activity.baseContext, customFontId)
//
//                val fontChanger = FontChangeCrawler(font!!, fontSize.progress.toFloat())
//                fontChanger.replaceFonts(activity.findViewById(android.R.id.content)!!)
//
//
//            }
//
//        } catch (e: Exception) {
//            Log.e("AN|BO", e.message)
//        }
//    }
}


//class FontChangeCrawler {
//    private var typeface: Typeface
//    private var size: Float = 16.0F
//
//    constructor(typeface: Typeface, size: Float) {
//        this.typeface = typeface
//        this.size = size
//    }
//
//    constructor(assets: AssetManager?, assetsFontFileName: String?) {
//        typeface = Typeface.createFromAsset(assets, assetsFontFileName)
//    }
//
//    fun replaceFonts(viewTree: ViewGroup) {
//        var child: View?
//        for (i in 0 until viewTree.childCount) {
//            child = viewTree.getChildAt(i)
//            if (child is ViewGroup) {
//                // recursive call
//                replaceFonts(child)
//            } else if (child is TextView) {
//                // base case
//                child.typeface = typeface
//                child.textSize = size
//            }
//        }
//    }
//}

