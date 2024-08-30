package com.donkeyvpn.util.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.donkeyvpn.R

class VpnDisconnectDialog(context: Context, disconnectCallback: () -> Unit) : Dialog(context) {
    init {
        val params = window!!.attributes
        params.gravity = Gravity.CENTER
        window!!.attributes = params
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setTitle(null)
        setOnCancelListener(null)
        val view: View = LayoutInflater.from(context).inflate(R.layout.vpn_disconect_dialog, null)
        val okButton = view.findViewById<Button>(R.id.btOk)
        val kokButton = view.findViewById<Button>(R.id.btKok)
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