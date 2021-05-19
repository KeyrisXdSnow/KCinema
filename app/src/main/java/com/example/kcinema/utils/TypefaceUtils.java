package com.example.kcinema.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Field;

public class TypefaceUtils {

    /**
     * Using reflection to override default typeface
     * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
     * @param context to work with assets
     * @param defaultFontNameToOverride for example "monospace"
     * */
    public static void overrideFont(Context context, String defaultFontNameToOverride, int customFontId) {
        try {

            final Typeface customFontTypeface = ResourcesCompat.getFont(context, customFontId);
            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {
            Log.e("BLAT","Can not set custom font "+ " instead of " + defaultFontNameToOverride);
        }
    }
}
