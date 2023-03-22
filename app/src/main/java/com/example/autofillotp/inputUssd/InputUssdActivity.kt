package com.example.autofillotp.inputUssd

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.autofillotp.R
import com.example.autofillotp.SimCardManager
import com.example.autofillotp.SimCardsData
import com.example.autofillotp.sendUssd.SimCardModel

class InputUssdActivity : AppCompatActivity() {
    private val CALL_PHONE_REQUEST_CODE = 201
    private val PHONE_STATE_REQUEST_CODE = 201

    private lateinit var line1: Button
    private lateinit var line2: Button
    private lateinit var line1Ussd: EditText
    private lateinit var line2Ussd: EditText
    private lateinit var progressBar: ProgressBar

    private val simCardManager = SimCardManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_ussd)

        line1 = findViewById(R.id.line_1)
        line2 = findViewById(R.id.line_2)
        line1Ussd = findViewById(R.id.line1Ussd)
        line2Ussd = findViewById(R.id.line2Ussd)
        progressBar = findViewById(R.id.progressBar)


        line1.setOnClickListener {
            if(SimCardsData.simCards.size > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && line1Ussd.text.toString().isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                callUssdCode(SimCardsData.getKeyAt(0), line1Ussd.text.toString())
            }
        }

        line2.setOnClickListener {
            if(SimCardsData.simCards.size > 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && line2Ussd.text.toString().isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                callUssdCode(SimCardsData.getKeyAt(1), line2Ussd.text.toString())
            }
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
            getSubscriptionData(this)
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
                   getSubscriptionData(this@InputUssdActivity)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSubscriptionData(context: Context) {
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
        val subsInfoList = subscriptionManager.activeSubscriptionInfoList
        for (subscriptionInfo in subsInfoList) {
            SimCardsData.addCardModel(subscriptionInfo.subscriptionId, null)
        }
        progressBar.visibility = View.GONE
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun callUssdCode(id: Int, ussd: String) {
        //SimCardsData.getKeyAt(simCardsCounter), context
        val telephonyManager =
              getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
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
                    val mobileNumber = getMobileNumber(response.toString())
                    Toast.makeText(this@InputUssdActivity, mobileNumber, Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }

                override fun onReceiveUssdResponseFailed(
                    telephonyManager: TelephonyManager,
                    request: String,
                    failureCode: Int
                ) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode)
                    Toast.makeText(this@InputUssdActivity, "Fail to call ussd", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }

        mSim0TelephonyManager.sendUssdRequest(
            ussd,
            responseCallback,
            handler
        )
    }

    private fun getMobileNumber(fullMessage: String): String {
        val splitMessage = fullMessage.split("01".toRegex(), 2)
        var mobileNumber = ""
        if (splitMessage.size > 1) {
            mobileNumber = "01"+splitMessage[1].filter { it.isDigit() }
        }
        return mobileNumber
    }
}