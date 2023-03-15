package com.example.autofillotp

import android.R.attr.label
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast


class BaseApp: Application() {
    override fun onCreate() {
        super.onCreate()
        var appSignature = AppSignatureHelper(this)
        for(item in appSignature.appSignatures) {
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label.toString(), item)
            clipboard.setPrimaryClip(clip)
//            Toast.makeText(this, "App signature is >> $item  <<<<", Toast.LENGTH_LONG).show()
        }
    }
}