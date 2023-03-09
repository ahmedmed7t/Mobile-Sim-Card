package com.example.autofillotp.broadCastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.Objects;

public class OtpBroadCastReceiver extends BroadcastReceiver {
    private SmsListener smsVerificationCallback;

    public void setSmsVerificationCallback(SmsListener smsVerificationCallback) {
        this.smsVerificationCallback = smsVerificationCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status smsRetrieverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            switch (Objects.requireNonNull(smsRetrieverStatus).getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String otp  = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE);
                    Log.d("OTP_Message", otp);
                    // Extract one-time code from the message and complete verification
                    // by sending the code back to your server for SMS authenticity.
                    // But here we are just passing it to MainActivity
                    if (smsVerificationCallback != null) {
                        otp = otp.replace("<#> Your ExampleApp code is: ", "");
                        smsVerificationCallback.messageReceived(otp);
                    }

//                    Intent consentIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
//                    smsVerificationCallback.messageReceived(consentIntent);
                    break;
                case CommonStatusCodes.TIMEOUT:
                    if (smsVerificationCallback != null) {
                        smsVerificationCallback.messageReceived("TIMEOUT");
                    }
                    break;
            }
        }
    }
}

//            try{
//                Object[] pdus = (Object[]) extras.get("pdus");
//                SmsMessage[] msgs = new SmsMessage[pdus.length];
//                for(int i=0; i<msgs.length; i++){
//                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
//                    String msg_from = msgs[i].getOriginatingAddress();
//                    String msgBody = msgs[i].getMessageBody();
//                }
//            }catch(Exception e){
////                            Log.d("Exception caught",e.getMessage());
//            }