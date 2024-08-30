package com.vpnduck.util.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.vpnduck.R

class ResourceUseAlert(context: Context) : Dialog(context) {
    init {
        val params = window!!.attributes
        params.gravity = Gravity.CENTER
        window!!.attributes = params
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setTitle(null)
        setOnCancelListener(null)
        val view: View = LayoutInflater.from(context).inflate(R.layout.resource_use_dialog, null)
        val okButton = view.findViewById<Button>(R.id.btOk)
        okButton.setOnClickListener {
            this.dismiss()
        }
        setContentView(view)
    }
}