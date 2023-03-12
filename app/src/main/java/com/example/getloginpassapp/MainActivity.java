package com.example.getloginpassapp;

import androidx.annotation.LongDef;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getloginpassapp.utils.PhotosUtils;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvFullUserName;
    private SharedPreferences sPref;
    public static String MY_PREF = "GETLOGGINPASSAPP_PREFERENCE_FILE";
    public final static String FULLUSERNAME = "fullusername";
    private int code = 0;
    private Toast toast;
    private Button buttonSendInfoToContact;
    private Button buttonGetContact;
    private ImageButton buttonSelfPhoto;
    private ImageView ivSelfPhoto;
    private Intent intentGetContact;
    private Intent intentGetPhoto;
    private final static int REQUEST_CONTACT = 1;
    private final static int REQUEST_PHOTO = 2;
    private final static String TAG = "MainActivity";
    private File selfPhotoFile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        tvFullUserName = findViewById(R.id.tvViewFullName);
        buttonSendInfoToContact = findViewById(R.id.buttonSendInfoToContact);

        String fullUserName;
        if((fullUserName = loadUserFullNameFromMyPref()) == null){
            getUserFullNameFromLoginActivity();
        } else {
            tvFullUserName.setText("Hi " + fullUserName);

            buttonSendInfoToContact.setOnClickListener(this);

            intentGetContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

            PackageManager packageManager = this.getPackageManager();
            if((packageManager.resolveActivity(intentGetContact, packageManager.MATCH_DEFAULT_ONLY)) == null){
                buttonGetContact.setEnabled(false);
            } else {
                buttonGetContact = findViewById(R.id.buttonGetContact);
                buttonGetContact.setOnClickListener(this);
            }

            ivSelfPhoto = findViewById(R.id.ivSelfPhoto);
            buttonSelfPhoto = findViewById(R.id.buttonSelfPhoto);
            intentGetPhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String photoFileName = getPhotoFileName();
            selfPhotoFile = getPhotoFile(photoFileName);

            boolean canTakePhoto = false;
            canTakePhoto = selfPhotoFile != null && intentGetPhoto.resolveActivity(packageManager) != null;

            buttonSelfPhoto.setEnabled(canTakePhoto);

            if(canTakePhoto){
                Uri uri = FileProvider.getUriForFile(this,"com.example.getloginpassapp.fileprovider", selfPhotoFile);
                intentGetPhoto.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }
            buttonSelfPhoto.setOnClickListener(this);
            
        }
    }

    private String getPhotoFileName(){
        Date date = new Date();
        return "IMG+" + "20230302" +".jpg";
    }

    private File getPhotoFile(String fileName){
        File filesDir = this.getFilesDir();
        if(filesDir == null){
            return null;
        }
        //TODO: insert checl that file exist, in this case dont create new file
        return new File(filesDir, fileName);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == buttonSendInfoToContact.getId()) {
            Intent intent = new Intent(Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "I have get up at 5:40");
            intent.putExtra(Intent.EXTRA_SUBJECT, "early report");

            Intent intentChooser = Intent.createChooser(intent, "SendReport");
            startActivity(intentChooser);
        } else if (view.getId() == buttonGetContact.getId()) {
            startActivityForResult(intentGetContact, REQUEST_CONTACT);
        } else if (view.getId() == buttonSelfPhoto.getId()) {
            startActivityForResult(intentGetPhoto, REQUEST_PHOTO);
        }
    }

    private String loadUserFullNameFromMyPref(){
        sPref = getApplicationContext().getSharedPreferences(MY_PREF, MODE_PRIVATE);

        String fullUserName = sPref.getString(FULLUSERNAME, "");

        if (fullUserName.isEmpty()){
            return null;
        } else{
            return fullUserName;
        }
    }

    private void getUserFullNameFromLoginActivity(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(intent, code);
    }

    private void updateSelfPhotoImageView(File photoFile){
        if(photoFile == null || !photoFile.exists()){
            ivSelfPhoto.setImageDrawable(null);
            Log.d(TAG, "updateSelfPhotoImageView: ERROR with file");
        } else {
            Point size =  new Point();
            this.getWindowManager().getDefaultDisplay().getSize(size);
            Bitmap bitmap = PhotosUtils.getScaleBitmap(photoFile.getPath(), size.x, size.y);
            ivSelfPhoto.setImageBitmap(bitmap);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == code){
            if(resultCode == RESULT_OK){
                String fullUserName = data.getStringExtra(FULLUSERNAME);
                saveFullUserNameInMyPref(fullUserName);
                tvFullUserName.setText("Hi " + fullUserName);
            } else{
                int duration = Toast.LENGTH_SHORT;
                if(toast != null){
                    toast.cancel();
                }
                toast = Toast.makeText(this,"This User not found", duration);
                toast.show();

                getUserFullNameFromLoginActivity();
            }
        } else if (requestCode == REQUEST_CONTACT) {
            if(resultCode == RESULT_OK){
                if(data != null){
                    Uri contactUri = data.getData();
                    String[] queruFiled = new String[]{
                            ContactsContract.Contacts.DISPLAY_NAME
                    };

                    Cursor cursor = this.getContentResolver().query(contactUri, queruFiled, null, null, null);
                    try{
                        if(cursor.getCount() == 0){
                            return;
                        }
                        cursor.moveToFirst();
                        String name = cursor.getString(0);
                        Log.d(TAG, "onActivityResult: name of contact " + name);

                    } finally {
                        cursor.close();
                    }
                }
            }
            
        } else if (requestCode == REQUEST_PHOTO) {
            if(resultCode == RESULT_OK){
                updateSelfPhotoImageView(selfPhotoFile);
            } else {
                Log.d(TAG, "onActivityResult: ERROR with photo");
            }

        }
    }

    private void saveFullUserNameInMyPref(String data){
        sPref = getApplicationContext().getSharedPreferences(MY_PREF, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(FULLUSERNAME, data);
        ed.commit();
    }
}