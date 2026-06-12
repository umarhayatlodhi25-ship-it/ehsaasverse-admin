package com.lodhidevelop.ehsaasverse.data.local

import android.content.Context
import android.content.SharedPreferences

class FavoriteManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ehsaas_prefs", Context.MODE_PRIVATE)

    fun toggleFavorite(shayariId: Int) {
        val favorites = getFavoriteIds().toMutableSet()
        val idString = shayariId.toString()
        if (favorites.contains(idString)) {
            favorites.remove(idString)
        } else {
            favorites.add(idString)
        }
        prefs.edit().putStringSet("favorites", favorites).apply()
    }

    fun isFavorite(shayariId: Int): Boolean {
        return getFavoriteIds().contains(shayariId.toString())
    }

    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }
}
