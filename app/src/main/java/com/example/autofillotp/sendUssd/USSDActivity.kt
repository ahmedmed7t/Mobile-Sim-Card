package com.example.autofillotp.sendUssd

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.autofillotp.R

class USSDActivity : AppCompatActivity() {

    private val CALL_PHONE_REQUEST_CODE = 201
    private val PHONE_STATE_REQUEST_CODE = 201

    private lateinit var line1: Button
    private lateinit var line2: Button
    private lateinit var progressBar: ProgressBar

    private val listOfMobileNumbers = arrayListOf<String>()
    private val simIds = ArrayDeque<Int>(listOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ussdactivity)

        line1 = findViewById(R.id.line_1)
        line2 = findViewById(R.id.line_2)
        progressBar = findViewById(R.id.progressBar)

        line1.setOnClickListener {
            if (listOfMobileNumbers.isNotEmpty())
                Toast.makeText(this@USSDActivity, listOfMobileNumbers[0], Toast.LENGTH_SHORT)
                    .show()
            else
                Toast.makeText(this@USSDActivity, "No Data Provided", Toast.LENGTH_SHORT)
                    .show()
        }

        line2.setOnClickListener {
            if (listOfMobileNumbers.size > 1)
                Toast.makeText(this@USSDActivity, listOfMobileNumbers[1], Toast.LENGTH_SHORT)
                    .show()
            else {
                Toast.makeText(this@USSDActivity, "No Data Provided", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE),
                PHONE_STATE_REQUEST_CODE
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                getSubscriptionData()
        }
    }

    private fun isDeviceSupportSecurityFeature(){

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSubscriptionData() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                PHONE_STATE_REQUEST_CODE
            )
            return
        }
        progressBar.visibility = View.VISIBLE
        val subscriptionManager = getSystemService(SubscriptionManager::class.java)
        val subsInfoList = subscriptionManager.activeSubscriptionInfoList
        for ((index, subscriptionInfo) in subsInfoList.withIndex()) {
            simIds.add(subscriptionInfo.subscriptionId)
        }
        simIds.reverse()
        getUssdFromNextSimCard()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getUssdFromNextSimCard(){
        if (simIds.isNotEmpty()) {
            callUssdCode(simIds.removeLast(), simIds.size)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @Synchronized
    private fun callUssdCode(id: Int, index: Int) {

        progressBar.visibility = View.VISIBLE
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val mSim0TelephonyManager = telephonyManager.createForSubscriptionId(id)

        val handler = Handler(Looper.getMainLooper())
        val responseCallback: TelephonyManager.UssdResponseCallback =
            object : TelephonyManager.UssdResponseCallback() {
                override fun onReceiveUssdResponse(
                    telephonyManager: TelephonyManager,
                    request: String,
                    response: CharSequence
                ) {
                    super.onReceiveUssdResponse(telephonyManager, request, response)
                    getMobileNumber(response.toString())
                    progressBar.visibility = View.GONE
                    Log.v("Medhat", "$index Success ${response.toString()}")
                    getUssdFromNextSimCard()
                }

                override fun onReceiveUssdResponseFailed(
                    telephonyManager: TelephonyManager,
                    request: String,
                    failureCode: Int
                ) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode)
//                    Toast.makeText(
//                        this@USSDActivity,
//                        failureCode.toString(),
//                        Toast.LENGTH_SHORT
//                    ).show()
                    Log.v("Medhat", "$index failed")
                    progressBar.visibility = View.GONE
                    getUssdFromNextSimCard()
                }
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
        mSim0TelephonyManager.sendUssdRequest(
            getProviderCode(mSim0TelephonyManager.networkOperatorName),
            responseCallback,
            handler
        )
    }

    private fun getProviderCode(provider: String): String {
        return with(provider.lowercase()) {
            when {
                contains("vodafone") -> "*878#"
                contains("etisalat") -> "*947#"
                contains("orange") -> "#119#1#"
                contains("we") -> "*688#"
                else -> {
                    ""
                }
            }
        }
    }

    private fun getMobileNumber(fullMessage: String) {
        val splitMessage = fullMessage.split("01")
        val mobileNumber = splitMessage[1].filter { it.isDigit() }
        listOfMobileNumbers.add("01$mobileNumber")
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
                    getSubscriptionData()
                }
            }
        }
    }
}