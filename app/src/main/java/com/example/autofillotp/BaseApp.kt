package com.example.autofillotp

import android.app.Application
import android.widget.Toast

class BaseApp: Application() {
    override fun onCreate() {
        super.onCreate()
        var appSignature = AppSignatureHelper(this)
        for(item in appSignature.appSignatures) {
//            Toast.makeText(this, "App signature is >> $item  <<<<", Toast.LENGTH_LONG).show()
        }
    }
}