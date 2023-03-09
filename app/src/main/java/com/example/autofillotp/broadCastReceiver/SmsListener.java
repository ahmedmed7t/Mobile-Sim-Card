package com.example.autofillotp.broadCastReceiver;

import android.content.Intent;

public interface SmsListener {
    void messageReceived(String messageText);
}
