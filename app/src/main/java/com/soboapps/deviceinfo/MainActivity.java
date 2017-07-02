package com.soboapps.deviceinfo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Bitmap bitmap ;
    private TextView tv;
    private ImageView iv;

    String getDate;
    String getTime;
    String getPhoneNumber;
    String getSimSn;
    String getGmail = null;
    String deviceOwner = null;
    public String allInfo;
    public String qrCodeInfo;
    public String csvCodeInfo;

    private int resultGet_Accounts;
    private int resultRead_Phone_State;
    private int resultRead_Contacts;
    private int resultWrite_External_Storage;
    private int resultRead_External_Storage;

    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) this.findViewById(R.id.tvInfo);
        iv = (ImageView) this.findViewById(R.id.iv);

        getDate = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());
        getTime = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());

        resultGet_Accounts = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        resultRead_Phone_State = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        resultRead_Contacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        resultWrite_External_Storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        resultRead_External_Storage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

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
                //dialog.setCancelable(false); //Cancel Button Not needed, but left code here just in case.
                dialog.setTitle(deviceOwner);
                dialog.setMessage(allInfo);
                dialog.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Action if you want it to do something
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
                resultRead_Contacts == PackageManager.PERMISSION_GRANTED &&
                resultRead_External_Storage == PackageManager.PERMISSION_GRANTED &&
                resultWrite_External_Storage == PackageManager.PERMISSION_GRANTED) {

            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        getDeviceInfo();
    }

    private void getDeviceInfo() {
        Log.i(TAG, "getDeviceInfo");

        res = getResources();

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

            // Get the Devices First Registered Owner
            Cursor c = getApplication().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
            c.moveToFirst();
            String userName = (c.getString(c.getColumnIndex("display_name")));
            deviceOwner = userName;
            c.close();

            String s = "";
            s += "\n" + " " + res.getString(R.string.device_manufacture) + " " +  android.os.Build.MANUFACTURER;
            s += "\n" + " " + res.getString(R.string.device_model) + " " +  android.os.Build.MODEL;
            s += "\n" + " " + res.getString(R.string.device_serial) + " " +  android.os.Build.SERIAL;

            // MORE STUFF YOU CAN GET
            //s += "\n" + " " + res.getString(R.string.device_release) + " " +  android.os.Build.VERSION.RELEASE;
            //s += "\n" + " " + res.getString(R.string.device_os_version) + " " +  System.getProperty("os.version")      + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
            //s += "\n" + " " + res.getString(R.string.device_api_level) + " " +  android.os.Build.VERSION.SDK_INT;
            //s += "\n" + " " + res.getString(R.string.device_brand) + " " +  android.os.Build.BRAND;
            //s += "\n" + " " + res.getString(R.string.device_device) + " " +  android.os.Build.DEVICE;
            ////s += "\n" + " " + res.getString(R.string.device_product) + " " +  android.os.Build.PRODUCT;
            //s += "\n" + " " + res.getString(R.string.device_display) + " " +  android.os.Build.DISPLAY;
            //s += "\n" + " " + res.getString(R.string.device_cpu_abi) + " " +  android.os.Build.CPU_ABI;
            //s += "\n" + " " + res.getString(R.string.device_cpu_abi2) + " " +  android.os.Build.CPU_ABI2;
            //s += "\n" + " " + res.getString(R.string.device_unknown) + " " +  android.os.Build.UNKNOWN;
            //s += "\n" + " " + res.getString(R.string.device_hardware) + " " +  android.os.Build.HARDWARE;
            //s += "\n" + " " + res.getString(R.string.device_build_id) + " " +  android.os.Build.ID;
            //s += "\n" + " " + res.getString(R.string.device_user) + " " +  android.os.Build.USER;
            //s += "\n" + " " + res.getString(R.string.device_host) + " " +  android.os.Build.HOST;

            Log.i(TAG + " | Device Info > ", s);
            allInfo =
                    "\n" + res.getString(R.string.device_date) + " " + getDate +
                    "\n" + res.getString(R.string.device_time) + " " +  getTime +
                    //"\n\n" + " " + res.getString(R.string.device_owner) + " " +  deviceOwner +
                    "\n" + " " + res.getString(R.string.device_email) + " " +  getGmail +
                    "\n" + " " + res.getString(R.string.device_phone) + " " +  getPhoneNumber +
                    "\n" + s.toString() +
                    "\n" + " " + res.getString(R.string.device_sim) + " " +  getSimSn;

            tv.setText(allInfo);

            csvCodeInfo = deviceOwner + "," + getGmail + "," + getPhoneNumber + "," +
                    android.os.Build.MANUFACTURER + "," + android.os.Build.MODEL + "," +
                    android.os.Build.SERIAL + "," + getSimSn + "," + getDate + "," +  getTime;

            //Intent intent=new Intent(this,DeviceInfoTable.class);
            //intent.putExtra("csvCodeInfo",csvCodeInfo);
            //startActivity(intent);


            /*
            private static final String INSERT_QRCODE_INFO = "insert into "
                    + TABLE_DEVICE_INFO + "(_ID,OWNER,EMAIL,PHONE,MANUFACTURER,MODEL,SERIAL,DATE,TIME)"+
                    " values ('1','deviceOwner','getGmail','getPhoneNumber','android.os.Build.MANUFACTURER', + " +
                    "'android.os.Build.MODEL','android.os.Build.SERIAL','getSimSn','getDate','getTime')";

            public static void onCreate(SQLiteDatabase database) {
                database.execSQL(INSERT_QRCODE_INFO);
            }
            */

            //Find screen size
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3/4;

            qrCodeInfo = res.getString(R.string.device_owner) + " " +  deviceOwner + allInfo;

            //Encode with a QR Code image csvCodeInfo
            //QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrCodeInfo,
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(csvCodeInfo,
                    null,
                    Contents.Type.TEXT,
                    BarcodeFormat.QR_CODE.toString(),
                    smallerDimension);

                bitmap = qrCodeEncoder.encodeAsBitmap();
                ImageView myImage = (ImageView) findViewById(R.id.iv);
                myImage.setImageBitmap(bitmap);

            //Toast.makeText(getApplicationContext(),csvCodeInfo, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Error getting Device INFO");
        }
    }
}
