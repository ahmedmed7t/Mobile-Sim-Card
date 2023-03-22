package com.example.autofillotp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.autofillotp.sendUssd.SimCardModel

class SimCardManager {

    private var simCardsCounter: Int

    init {
        simCardsCounter = -1
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSubscriptionData(context: Context) {
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
        val subsInfoList = subscriptionManager.activeSubscriptionInfoList
        for (subscriptionInfo in subsInfoList) {
            SimCardsData.addCardModel(subscriptionInfo.subscriptionId, null)
        }
        getUssdFromNextSimCard(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getUssdFromNextSimCard(context: Context) {
        if (simCardsCounter < SimCardsData.simCards.keys.size - 1) {
            simCardsCounter++
            callUssdCode(SimCardsData.getKeyAt(simCardsCounter), context)
        } else {
            simCardsCounter = -1
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    @Synchronized
    private fun callUssdCode(id: Int, context: Context) {

        if(SimCardsData.simCards[id]?.mobileNumber?.isNotEmpty() == true) {
            // in case mobile number already extracted before
            getUssdFromNextSimCard(context)
            return
        }

        val telephonyManager =
            context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
        val mSim0TelephonyManager = telephonyManager.createForSubscriptionId(id)

//        if (mSim0TelephonyManager.networkOperatorName.lowercase().contains("orange"))
//            return

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
                    SimCardsData.addCardModel(
                        id, SimCardModel(
                            provider = mSim0TelephonyManager.networkOperatorName,
                            mobileNumber = mobileNumber
                        )
                    )
                    getUssdFromNextSimCard(context)
                }

                override fun onReceiveUssdResponseFailed(
                    telephonyManager: TelephonyManager,
                    request: String,
                    failureCode: Int
                ) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode)
                    SimCardsData.addCardModel(
                        id, SimCardModel(
                            provider = mSim0TelephonyManager.networkOperatorName,
                        )
                    )
                    getUssdFromNextSimCard(context)
                }
            }

        mSim0TelephonyManager.sendUssdRequest(
            getProviderCode(mSim0TelephonyManager.networkOperatorName),
            responseCallback,
            handler
        )
    }

    private fun getProviderCode(provider: String): String {
        // we will exclude orange
        return with(provider.lowercase()) {
            when {
                contains("vodafone") -> "*878#"
                contains("etisalat") -> "*947#"
                contains("orange") -> "#119*1#"
                contains("we") -> "*688#"
                else -> {
                    ""
                }
            }
        }
    }

    private fun getMobileNumber(fullMessage: String): String {
        val splitMessage = fullMessage.split("01".toRegex(), 2)
        var mobileNumber = ""
        if (splitMessage.size > 1) {
            mobileNumber = "01"+splitMessage[1].filter { it.isDigit() }
        }
        return mobileNumber
    }

    fun getMobileNumberAt(index: Int): String {
        return SimCardsData.getMobileNumberAt(index)
    }
}
