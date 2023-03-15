package com.example.autofillotp

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.autofillotp.broadCastReceiver.OtpBroadCastReceiver
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import java.util.regex.Pattern

class AutoFillOtpActivity : AppCompatActivity() {
    private lateinit var otp_txt: TextView
    private val smsVerificationReceiver: OtpBroadCastReceiver = OtpBroadCastReceiver()
    private var startActivityForResult: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_fill_otp)
        otp_txt = findViewById(R.id.otpText)

        println("Medhat start app ")
        startListeningToSmsService()
        handleActivityResult()
    }

    private fun startListeningToSmsService() {
        SmsRetriever.getClient(this).startSmsUserConsent(null)
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        smsVerificationReceiver.setSmsVerificationCallback{ consentIntent ->
            println("Medhat Launch intent")
            startActivityForResult?.launch(consentIntent)
        }
        registerReceiver(
            smsVerificationReceiver, intentFilter,
            SmsRetriever.SEND_PERMISSION, null
        )
    }

//    private fun initSmsManager() {
//
//        val task = ReadSmsManager.start(this@AutoFillOtpActivity)
//
//        task.addOnCompleteListener {
//
//            if (task.isSuccessful) {
//                // The service is enabled successfully. Continue with the process.
//                Toast.makeText(this, "ReadSms service has been enabled.", Toast.LENGTH_LONG).show()
//            } else
//                Toast.makeText(this, "The service failed to be enabled.", Toast.LENGTH_LONG).show()
//        }
//        task.addOnSuccessListener(this, OnSuccessListener {
//            if(task.isSuccessful){
//                Toast.makeText(this, "ReadSms service has been enabled.", Toast.LENGTH_LONG).show()
//                val myReceiver = OtpBroadCastReceiver()
//                val intentFilter = IntentFilter(READ_SMS_BROADCAST_ACTION)
//                registerReceiver(myReceiver, intentFilter)
//            }
//        })
//        task.addOnFailureListener(this, OnFailureListener {
//            Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show();
//        })
//    }

    private fun handleActivityResult() {
        startActivityForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                println("Medhat inside intent")
                val message: String? =
                    result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                println("Medhat inside intent message $message")
                val pattern = Pattern.compile("-?\\d+")
                val matcher = pattern.matcher(message)
                if (matcher.find()) {
                    val code = matcher.group()
                    if (code.length == 6)
                        otp_txt.text = (code)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsVerificationReceiver)
    }
}