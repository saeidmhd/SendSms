package ir.mahaksoft.sendsms;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class SMSInboxActivity extends AppCompatActivity {

    public final int REQUEST_CODE_SMS_PERMISSIONS = 1001;
    public final int REQUEST_CODE_READ_PHONE_STATE_PERMISSIONS = 1002;
    public final int REQUEST_CODE_RECEIVE_SMS_PERMISSIONS = 1003;
    public final int REQUEST_CODE_READ_SMS_PERMISSIONS = 1004;
    public final int REQUEST_CODE_READ_CONTACTS_PERMISSIONS = 1005;
    public final int REQUEST_CODE_WRITE_CONTACTS_PERMISSIONS = 1006;


    ListView lViewSMS;
    ProgressBar progressbar;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsinbox);

        askForPermission();

        lViewSMS = (ListView) findViewById(R.id.listViewSMS);
        progressbar = (ProgressBar)findViewById(R.id.progressbar);

        load("content://sms/sent");


    }


    private void askForPermission() {
        String[] permissions = new String[]{

                 Manifest.permission.SEND_SMS
                ,Manifest.permission.READ_PHONE_STATE
                ,Manifest.permission.RECEIVE_SMS
                ,Manifest.permission.READ_SMS
                ,Manifest.permission.READ_CONTACTS
                ,Manifest.permission.WRITE_CONTACTS

        };

        ActivityCompat.requestPermissions(SMSInboxActivity.this, permissions, REQUEST_CODE_SMS_PERMISSIONS);
        ActivityCompat.requestPermissions(SMSInboxActivity.this, permissions, REQUEST_CODE_READ_PHONE_STATE_PERMISSIONS);
        ActivityCompat.requestPermissions(SMSInboxActivity.this, permissions, REQUEST_CODE_RECEIVE_SMS_PERMISSIONS);
        ActivityCompat.requestPermissions(SMSInboxActivity.this, permissions, REQUEST_CODE_READ_SMS_PERMISSIONS);
        ActivityCompat.requestPermissions(SMSInboxActivity.this, permissions, REQUEST_CODE_READ_CONTACTS_PERMISSIONS);
        ActivityCompat.requestPermissions(SMSInboxActivity.this, permissions, REQUEST_CODE_WRITE_CONTACTS_PERMISSIONS);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case REQUEST_CODE_SMS_PERMISSIONS:
                //Code for handling SendSMS Permission results
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    if (permission.equals(Manifest.permission.SEND_SMS)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                           // load("content://sms/inbox");
                        } else {
                            ActivityCompat.requestPermissions(SMSInboxActivity.this,
                                    new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_SMS_PERMISSIONS);
                        }
                    }
                }
                break;

        }
    }

    public void load (String uri){

        if(ContextCompat.checkSelfPermission(getBaseContext(),
                "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {

            if(getAllSms(uri)!=null)
            {
                ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, getAllSms(uri));
                lViewSMS.setAdapter(adapter);
            }

        }
        else{

            ActivityCompat.requestPermissions(SMSInboxActivity.this,
                    new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_SMS_PERMISSIONS);
        }

    }


    public void send_message(View view) {

        Intent mIntent = new Intent(SMSInboxActivity.this,SendSMS.class);
        startActivity(mIntent);
    }


    public ArrayList getAllSms(String uri) {

        ArrayList sms = new ArrayList();
        Uri message = Uri.parse(uri);
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(message, new String[]{"_id", "address", "date", "body"}, null, null, null);
        c.moveToPrevious();
            while( c.moveToNext()){
                progressbar.setVisibility(View.VISIBLE);
                sms.add(
                        "شماره مخاطب : "
                                + c.getString(c
                                .getColumnIndexOrThrow("address"))
                                + "\n"
                                + "\n"
                                + c.getString(3)
                                + "\n"
                                + "\n"
                                /*+ "نام شخص : "
                                + getContactName(
                                getApplicationContext(),
                                c.getString(c
                                        .getColumnIndexOrThrow("address")))*/);

              //  c.moveToNext();
                //j++;

            }


        c.close();
        progressbar.setVisibility(View.GONE);
        return sms;

    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    @Override
    protected void onResume() {
        load("content://sms/sent");
        super.onResume();
    }

    public void InboxOnClick(View view) {

        load("content://sms/inbox");

    }

    public void SentOnClick(View view) {
        load("content://sms/sent");
    }
}
