package com.example.flashlightapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.example.flashlightapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    public final int FLASH_MODE_ON = 1;
    public final int FLASH_MODE_OFF = 0;
    public final String MODE_KEY = "mode";
    public int MODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences= getPreferences(MODE_PRIVATE);
        editor = preferences.edit();

        ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (!result)
                        finish();
                    }
                }
        );

        activityResultLauncher.launch(Manifest.permission.CAMERA);

        binding.mainBtnFlash.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
                    if (MODE == FLASH_MODE_OFF){
                        try {
                            flashTorch(FLASH_MODE_ON);
                            editor.putInt(MODE_KEY,FLASH_MODE_ON);
                            editor.apply();
                            flashButton(FLASH_MODE_ON);
                            MODE = FLASH_MODE_ON;
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            flashTorch(FLASH_MODE_OFF);
                            editor.putInt(MODE_KEY,FLASH_MODE_OFF);
                            editor.apply();
                            flashButton(FLASH_MODE_OFF);
                            MODE = FLASH_MODE_OFF;
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    flashDialog();
                }
            }
        });
    }

        public void flashDialog(){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

            alertDialog.setTitle(getText(R.string.main_dialog_title));
            alertDialog.setMessage(getText(R.string.main_dialog_message));
            alertDialog.setPositiveButton(getText(R.string.main_dialog_positive_button), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
             }
        });

            alertDialog.create();
            alertDialog.show();
        }

    public void flashTorch(int mode) throws CameraAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId;

            cameraId = cameraManager.getCameraIdList()[0];;

            if (mode == FLASH_MODE_ON)
                cameraManager.setTorchMode(cameraId, true);
            else
                cameraManager.setTorchMode(cameraId, false);
        }

    }

    public void flashButton(int mode){
        switch (mode){
            case FLASH_MODE_ON:
                binding.mainBtnFlash.setText(getText(R.string.main_button_flash_off));
                binding.mainBtnFlash.setBackgroundColor(getColor(R.color.line_color));
                binding.mainIvBulb.setImageTintList(ColorStateList.valueOf(getColor(R.color.green_500)));
                break;
                case FLASH_MODE_OFF:
                    binding.mainBtnFlash.setText(getText(R.string.main_button_flash_on));
                    binding.mainBtnFlash.setBackgroundColor(getColor(R.color.green_500));
                    binding.mainIvBulb.setImageTintList(ColorStateList.valueOf(getColor(R.color.line_color)));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            flashTorch(FLASH_MODE_OFF);
            editor.putInt(MODE_KEY,FLASH_MODE_OFF);
            editor.apply();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (MODE == FLASH_MODE_ON){
            editor.putInt(MODE_KEY,FLASH_MODE_ON);
            editor.apply();
        }else {
            editor.putInt(MODE_KEY,FLASH_MODE_OFF);
            editor.apply();
        }
        try {
            flashTorch(FLASH_MODE_OFF);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MODE = preferences.getInt(MODE_KEY,0);
        if (MODE == FLASH_MODE_ON){
            try {
                flashTorch(FLASH_MODE_ON);
                flashButton(FLASH_MODE_ON);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else {
            try {
                flashTorch(FLASH_MODE_OFF);
                flashButton(FLASH_MODE_OFF);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
}