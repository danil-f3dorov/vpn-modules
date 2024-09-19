package common.util.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button

class VpnDisconnectDialog(
    context: Context,
    layoutId: Int,
    cancelButtonId: Int,
    disconnectButtonId: Int, disconnectCallback: () -> Unit
) : Dialog(context) {
    init {
        val params = window!!.attributes
        params.gravity = Gravity.CENTER
        window!!.attributes = params
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setTitle(null)
        setOnCancelListener(null)
        val view: View = LayoutInflater.from(context).inflate(layoutId, null)
        val okButton = view.findViewById<Button>(cancelButtonId)
        val kokButton = view.findViewById<Button>(disconnectButtonId)
        okButton.setOnClickListener {
            this.dismiss()
        }
        kokButton.setOnClickListener {
            disconnectCallback()
            this.dismiss()
        }
        setContentView(view)
    }
}