package core.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class PrefStorage @Inject constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor
        get() = sharedPreferences.edit()

    fun getVersion(): Int = sharedPreferences.getInt("version", 0)

    fun setVersion(newVersion: Int?) {
        newVersion?.let {
            editor.putInt("version", it).apply()
        }
    }

    fun resetVersion() {
        editor.putInt("version", 0).apply()
    }
}