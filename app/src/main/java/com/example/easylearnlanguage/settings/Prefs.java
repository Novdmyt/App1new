package com.example.easylearnlanguage.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class Prefs {
    private final SharedPreferences sp;

    public Prefs(Context ctx) {
        Context app = ctx.getApplicationContext();
        sp = app.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    // --- Language (BCP-47 tag) ---
    public void setLangTag(String tag){ sp.edit().putString("lang_tag", tag).apply(); }
    public String getLangTag(){ return sp.getString("lang_tag", "system"); }

    // --- Night mode ---
    public void setNightMode(int mode){ sp.edit().putInt("night_mode", mode).apply(); }
    public int getNightMode(){ return sp.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); }

    // --- TTS voice lang (BCP-47, en/de/fr) ---
    public void setTtsLang(String tag){ sp.edit().putString("tts_lang", tag).apply(); }
    public String getTtsLang(){ return sp.getString("tts_lang", "en"); }

    // --- Play choices count (3..6) ---
    public void setPlayChoices(int n){ sp.edit().putInt("play_choices", n).apply(); }
    public int getPlayChoices(){ return sp.getInt("play_choices", 4); }
}
