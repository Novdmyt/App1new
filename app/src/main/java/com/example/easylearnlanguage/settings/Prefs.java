// app/src/main/java/com/example/easylearnlanguage/settings/Prefs.java
package com.example.easylearnlanguage.settings;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class Prefs {
    private static final String FILE = "prefs";

    private static final String KEY_UI_LANG = "ui_lang";   // "system" | "uk" | "de" | "en"
    private static final String KEY_TTS    = "tts_lang";   // "en" | "de" | "fr"
    private static final String KEY_NIGHT  = "night_mode"; // int: AppCompatDelegate.MODE_*

    private final SharedPreferences p;

    public Prefs(Context c) {
        p = c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    // ---------- UI language ----------
    public String getLangTag() {
        return p.getString(KEY_UI_LANG, "system");
    }
    public void setLangTag(String tag) {
        p.edit().putString(KEY_UI_LANG, tag).apply();
    }

    // ---------- TTS voice language ----------
    public String getTtsLang() {
        return p.getString(KEY_TTS, "en");
    }
    public void setTtsLang(String tag) {
        p.edit().putString(KEY_TTS, tag).apply();
    }

    // ---------- Night mode / Theme ----------
    // Повертає один з MODE_NIGHT_* із AppCompatDelegate
    public int getNightMode() {
        return p.getInt(KEY_NIGHT, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
    public void setNightMode(int mode) {
        p.edit().putInt(KEY_NIGHT, mode).apply();
    }
}
