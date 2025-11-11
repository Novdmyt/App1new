package com.example.easylearnlanguage;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.easylearnlanguage.settings.Prefs;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Prefs p = new Prefs(this);

        // Тема (нічний/світлий режим)
        AppCompatDelegate.setDefaultNightMode(p.getNightMode());

        // Мова інтерфейсу
        String tag = p.getLangTag();
        AppCompatDelegate.setApplicationLocales(
                "system".equals(tag)
                        ? LocaleListCompat.getEmptyLocaleList()
                        : LocaleListCompat.forLanguageTags(tag)
        );
    }
}
