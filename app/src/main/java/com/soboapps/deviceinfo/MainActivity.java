package com.soboapps.deviceinfo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Bitmap bitmap ;
    private TextView tv;
    private ImageView iv;

    String getPhoneNumber;
    String getSimSn;
    String getGmail = null;
    String deviceOwner = null;
    public String allInfo;

    private int resultGet_Accounts;
    private int resultRead_Phone_State;
    private int resultRead_Contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) this.findViewById(R.id.tvInfo);
        iv = (ImageView) this.findViewById(R.id.iv);

        resultGet_Accounts = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        resultRead_Phone_State = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        resultRead_Contacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            } else {
                getDeviceInfo();
            }
        }

        iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                //dialog.setCancelable(false);
                dialog.setTitle(R.string.app_name);
                dialog.setMessage(allInfo);
                dialog.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Action if you want it to do something else.
                    }
                    //Cancel Button Not needed, but left code here just in case.
                //})
                        //.setNegativeButton("Cancel ", new DialogInterface.OnClickListener() {
                        //    @Override
                        //    public void onClick(DialogInterface dialog, int which) {
                        //        //Action for "Cancel".
                        //    }
                        });

                final AlertDialog alert = dialog.create();
                alert.show();

                return true;
            }
        });
    }

    private boolean checkIfAlreadyhavePermission() {
        if (resultGet_Accounts == PackageManager.PERMISSION_GRANTED &&
                resultRead_Phone_State == PackageManager.PERMISSION_GRANTED &&
                resultRead_Contacts == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        getDeviceInfo();
    }

    private void getDeviceInfo() {
        Log.i(TAG, "getDeviceInfo");

        try {
            TelephonyManager telemamanger = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            getPhoneNumber = telemamanger.getLine1Number();
            getSimSn = telemamanger.getSimSerialNumber();

            Account[] accounts = AccountManager.get(this).getAccounts();
            for (Account account : accounts) {

                String possibleEmail = account.name;
                String type = account.type;

                if (type.equals("com.google")) {
                    getGmail = possibleEmail;
                    Log.e("", "Emails: " + getGmail);
                    break;
                }
            }

            Cursor c = getApplication().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
            c.moveToFirst();
            String userName = (c.getString(c.getColumnIndex("display_name")));
            //textView.setText(c.getString(c.getColumnIndex("display_name")));
            deviceOwner = userName;
            c.close();

            String s = "";
            s += "\n MANUFACTURER: "    + android.os.Build.MANUFACTURER;
            s += "\n Model: "           + android.os.Build.MODEL;
            s += "\n SERIAL: "          + android.os.Build.SERIAL;

            // MORE STUFF YOU CAN GET
            //s += "\n RELEASE: "         + android.os.Build.VERSION.RELEASE;
            //s += "\n OS Version: "      + System.getProperty("os.version")      + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
            //s += "\n OS API Level: "    + android.os.Build.VERSION.SDK_INT;
            //s += "\n BRAND: "           + android.os.Build.BRAND;
            //s += "\n Device: "          + android.os.Build.DEVICE;
            ////s += "\n Model (and Product): " + android.os.Build.MODEL            + " ("+ android.os.Build.PRODUCT + ")";
            //s += "\n DISPLAY: "         + android.os.Build.DISPLAY;
            //s += "\n CPU_ABI: "         + android.os.Build.CPU_ABI;
            //s += "\n CPU_ABI2: "        + android.os.Build.CPU_ABI2;
            //s += "\n UNKNOWN: "         + android.os.Build.UNKNOWN;
            //s += "\n HARDWARE: "        + android.os.Build.HARDWARE;
            //s += "\n Build ID: "        + android.os.Build.ID;
            //s += "\n USER: "            + android.os.Build.USER;
            //s += "\n HOST: "            + android.os.Build.HOST;

            Log.i(TAG + " | Device Info > ", s);
            allInfo = "Owner: " + deviceOwner + "\nEmail: " + getGmail + "\nPhone Number: " + getPhoneNumber + "\nSIM : " + getSimSn + "\n" + s.toString();
            tv.setText(allInfo);

            //Find screen size
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3/4;

            //Encode with a QR Code image
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(allInfo,
                    null,
                    Contents.Type.TEXT,
                    BarcodeFormat.QR_CODE.toString(),
                    smallerDimension);

                bitmap = qrCodeEncoder.encodeAsBitmap();
                ImageView myImage = (ImageView) findViewById(R.id.iv);
                myImage.setImageBitmap(bitmap);

        } catch (Exception e) {
            Log.e(TAG, "Error getting Device INFO");
        }
    }
}
