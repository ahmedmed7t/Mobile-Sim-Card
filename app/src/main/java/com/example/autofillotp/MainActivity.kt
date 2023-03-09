package com.example.autofillotp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.autofillotp.broadCastReceiver.OtpBroadCastReceiver
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient


class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    private var mCredentialsApiClient: GoogleApiClient? = null
    private val smsVerificationReceiver: OtpBroadCastReceiver = OtpBroadCastReceiver()
    private var startActivityForResult: ActivityResultLauncher<Intent>? = null

    private val CALL_PHONE_REQUEST_CODE = 201
    lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.text_text)

//        requestHint()
        startListen()
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.O)
            getSubscriptionData()
    }

    private val RC_HINT = 2

    @SuppressLint("LongLogTag")
    private fun requestHint() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()

        mCredentialsApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .enableAutoManage(this, this)
            .addApi(Auth.CREDENTIALS_API)
            .build()

        val intent = Auth.CredentialsApi.getHintPickerIntent(
            mCredentialsApiClient, hintRequest
        )

        try {
            startIntentSenderForResult(
                intent.intentSender,
                RC_HINT, null, 0, 0, 0
            )
        } catch (e: Exception) {
            Log.e("Error In getting Message", e.message ?: "")
        }
    }

    private fun startListen() {
        val client = SmsRetriever.getClient(this /* context */)
        val task = client.startSmsRetriever()
        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener {
            // Successfully started retriever, expect broadcast intent
            // ...
            textView.text = "Waiting for the OTP"
        }

        task.addOnFailureListener {
            // Failed to start retriever, inspect Exception for more details
            // ...
            textView.text = "Cannot Start SMS Retriever"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_HINT && resultCode == Activity.RESULT_OK) {

            /*You will receive user selected phone number here if selected and send it to the server for request the otp*/
            var credential: Credential? = data!!.getParcelableExtra(Credential.EXTRA_KEY)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsVerificationReceiver)
    }

    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callUssdCode(id: Int) {

        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val mSim0TelephonyManager = telephonyManager.createForSubscriptionId(id)

        val handler = Handler()
        val responseCallback: TelephonyManager.UssdResponseCallback =
            object : TelephonyManager.UssdResponseCallback() {
                override fun onReceiveUssdResponse(
                    telephonyManager: TelephonyManager,
                    request: String,
                    response: CharSequence
                ) {
                    super.onReceiveUssdResponse(telephonyManager, request, response)
                    Toast.makeText(this@MainActivity, response.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onReceiveUssdResponseFailed(
                    telephonyManager: TelephonyManager,
                    request: String,
                    failureCode: Int
                ) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode)
                    Toast.makeText(
                        this@MainActivity,
                        failureCode.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CALL_PHONE),
                CALL_PHONE_REQUEST_CODE)
            return
        }
        mSim0TelephonyManager.sendUssdRequest("*878#", responseCallback, handler)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        @RequiresApi(Build.VERSION_CODES.O)
        if (requestCode == CALL_PHONE_REQUEST_CODE) {
            if(permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                callUssdCode(0)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSubscriptionData() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                101
            )
            return
        }

        val subscriptionManager = SubscriptionManager.from(applicationContext)
        val subsInfoList =
            subscriptionManager.activeSubscriptionInfoList
//        Log.d("Test", "Current list = $subsInfoList")
        for ((index, subscriptionInfo) in subsInfoList.withIndex()) {
            val number = subscriptionInfo.number
            Log.d("Test Number $index", "  subscriptionId is  ${subscriptionInfo.subscriptionId}")
            callUssdCode(subscriptionInfo.subscriptionId)
        }
    }
}

//        startListeningToSmsService()
//        handleActivityResult()


//private fun startListeningToSmsService() {
//    SmsRetriever.getClient(this).startSmsUserConsent(null)
//    val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
//    smsVerificationReceiver.setSmsVerificationCallback { consentIntent ->
//        startActivityForResult?.launch(
//            consentIntent
//        )
//    }
//    registerReceiver(
//        smsVerificationReceiver, intentFilter,
//        SmsRetriever.SEND_PERMISSION, null
//    )
//}
//
//private fun handleActivityResult() {
//    startActivityForResult = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == AppCompatActivity.RESULT_OK) {
//            Log.v("Medhat","arrived")
//            val message: String =
//                result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE) ?: ""
//            Log.v("Medhat","$message")
//            val pattern = Pattern.compile("-?\\d+")
//            val matcher = pattern.matcher(message)
//            if (matcher.find()) {
//                val code = matcher.group()
//                if (code.length == 6)
//                    textView.text = code
//            }
//        }
//    }
//}