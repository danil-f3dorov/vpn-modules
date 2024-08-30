package data.sharedprefs

import android.content.Context
import android.content.SharedPreferences
import common.App

object PrefDao {
    private var sharedPreferences: SharedPreferences =
        App.instance.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun getVersion(): Int {
        return sharedPreferences.getInt("version", 0)
    }

    fun updateVersion(newVersion: Int?) {
        if(newVersion == null) return
        editor.putInt("version", newVersion)
        editor.apply()
    }

    fun resetVersion() {
        editor.putInt("version", 0).apply()
    }
}