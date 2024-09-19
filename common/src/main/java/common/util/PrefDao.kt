package common.util

import android.content.Context
import android.content.SharedPreferences

class PrefDao(context: Context) {
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

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