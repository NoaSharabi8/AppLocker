package dev.noash.applocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {
    public interface OnSMSReceivedListener {
        void onSMSReceived(String sender, String message);
    }

    private static OnSMSReceivedListener listener;

    public static void setOnSMSReceivedListener(OnSMSReceivedListener l) {
        listener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) return;

            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                String messageBody = sms.getMessageBody();
                String sender = sms.getOriginatingAddress();
                if (listener != null) {
                    listener.onSMSReceived(sender, messageBody);
                }
            }
        }
    }
}
