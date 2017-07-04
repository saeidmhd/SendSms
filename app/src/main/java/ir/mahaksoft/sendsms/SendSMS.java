package ir.mahaksoft.sendsms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.util.ArrayList;

public class SendSMS extends AppCompatActivity {
    Button btnSendSMS;
    EditText txtPhoneNo;
    EditText txtMessage;
    ProgressBar progressBar;
    BroadcastReceiver mBroadcastReceiverDelivery;
    BroadcastReceiver mBroadcastReceiver;

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);


         mBroadcastReceiverDelivery = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "پیامک تحویل داده شد",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "پیامک تحویل داده نشده است",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
         mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "پیامک ارسال شد",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        finish();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "خطای عمومی",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "خارج از سرویس",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "PDU خالی",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "ارتباط رادیویی برقرار نیست",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        break;
                }

            }
        };

        registerReceiver(mBroadcastReceiver,new IntentFilter(SENT));
        registerReceiver(mBroadcastReceiverDelivery,new IntentFilter(DELIVERED));


        btnSendSMS = (Button) findViewById(R.id.btnSendSMS);
        txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
        txtMessage = (EditText) findViewById(R.id.txtMessage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        txtPhoneNo.requestFocus();

        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNo = txtPhoneNo.getText().toString();
                String message = txtMessage.getText().toString();
                if (phoneNo.length()>0 && message.length()>0 )
                {
                    //sendSMS(phoneNo, message);
                    sendMultipartSMS(phoneNo,message);
                    progressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(getBaseContext(),
                            "لطفا منتظر بمانید ...",
                            Toast.LENGTH_SHORT).show();}

                else
                    Toast.makeText(getBaseContext(),
                            "لطفا شماره تلفن و متن را وارد نمایید!",
                            Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mBroadcastReceiverDelivery);
    }

    private void sendMultipartSMS(String sendTo, String msg) {

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        Context context = this.getBaseContext();

        //Toast.makeText(getApplicationContext(), getString(R.string.portal_1_sending_to) + sendTo, Toast.LENGTH_LONG).show();
        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();


        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(msg);
        for (int i = 0; i < parts.size(); i++) {
            PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(SENT), 0);
            PendingIntent deliveryIntent = PendingIntent.getBroadcast(context,
                    0, new Intent(DELIVERED), 0);
            sentIntents.add(sentIntent);
            deliveryIntents.add(deliveryIntent);
        }
        if (PhoneNumberUtils.isGlobalPhoneNumber(sendTo)) {
            sms.sendMultipartTextMessage(sendTo, null, parts, sentIntents,
                    deliveryIntents);
        }
    }
}
