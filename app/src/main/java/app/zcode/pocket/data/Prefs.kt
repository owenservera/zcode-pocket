package app.zcode.pocket.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val sp: SharedPreferences = context.getSharedPreferences("zcode_pocket", Context.MODE_PRIVATE)

    var hubUrl: String
        get() = sp.getString("hub_url", DEFAULT_HUB_URL)?.takeIf { it.isNotBlank() } ?: DEFAULT_HUB_URL
        set(v) = sp.edit().putString("hub_url", v.trim()).apply()

    var token: String
        get() = sp.getString("token", "") ?: ""
        set(v) = sp.edit().putString("token", v.trim()).apply()

    companion object {
        /** This machine's tailnet address — the owner's phone reaches the hub here. */
        const val DEFAULT_HUB_URL = "http://100.118.170.70:8377"
    }
}
