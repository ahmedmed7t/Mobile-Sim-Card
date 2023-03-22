package com.example.autofillotp.sendUssd

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.autofillotp.R
import com.example.autofillotp.SimCardManager


class USSDActivity : AppCompatActivity() {

    private val CALL_PHONE_REQUEST_CODE = 201
    private val PHONE_STATE_REQUEST_CODE = 201

    private lateinit var line1: Button
    private lateinit var line2: Button
    private lateinit var progressBar: ProgressBar

    private val simCardManager = SimCardManager()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ussdactivity)

        line1 = findViewById(R.id.line_1)
        line2 = findViewById(R.id.line_2)
        progressBar = findViewById(R.id.progressBar)


//        val telephonyManager =
//            this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        val phoneNumber = telephonyManager.line1Number
//        Log.v("Medhat", "${telephonyManager.line1Number}")

//        val subscription = SubscriptionManager.from(
//            applicationContext
//        ).activeSubscriptionInfoList
//        for (i in subscription.indices) {
//            val info = subscription[i]
//            Log.v("Medhat", "number " + info.number)
//            Log.v("Medhat", "network name : " + info.carrierName)
//            Log.v("Medhat", "country iso " + info.countryIso)
//        }
        line1.setOnClickListener {
            Toast.makeText(
                this@USSDActivity,
                simCardManager.getMobileNumberAt(0),
                Toast.LENGTH_SHORT
            )
                .show()
        }

        line2.setOnClickListener {
            Toast.makeText(
                this@USSDActivity,
                simCardManager.getMobileNumberAt(1),
                Toast.LENGTH_SHORT
            )
                .show()


//            Toast.makeText(
//                this@USSDActivity,
//                simCardManager.getMobileNumberAt(1),
//                Toast.LENGTH_SHORT
//            )
//                .show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_NUMBERS),
                    PHONE_STATE_REQUEST_CODE
                )
                return
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    CALL_PHONE_REQUEST_CODE
                )
                return
            }
            simCardManager.getSubscriptionData(this)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (requestCode == PHONE_STATE_REQUEST_CODE) {
                if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    simCardManager.getSubscriptionData(this@USSDActivity)
                }
            }
        }
    }
}