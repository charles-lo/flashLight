package com.charleslab.flashlight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.vpadn.ads.VpadnAd;
import com.vpadn.ads.VpadnAdListener;
import com.vpadn.ads.VpadnAdRequest;
import com.vpadn.ads.VpadnAdSize;
import com.vpadn.ads.VpadnBanner;
import com.vpadn.ads.VpadnInterstitialAd;

public class MainActivity extends Activity {

    private CameraManager mCameraManager;
    private String mCameraId;
    private Boolean mIsTorchOn = true, mIsScreenOn = true;
    private Button mScreenLock;
    private int mBrightnessLevel, mBrightSetting;

    private VpadnBanner mVponBanner = null;
    private String bannerId = "8a808182586669e201587536217a0f1e";
    private String interstitialBannerId = "8a808182588f83d10158ce39d26e47ab";
    private VpadnInterstitialAd mInterstitialAd;
    boolean mIsCanWrite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mIsCanWrite = Settings.System.canWrite(MainActivity.this);
        if (!mIsCanWrite) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 1);
        }

        try {
            mBrightSetting = Settings.System.getInt(MainActivity.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
            mBrightnessLevel = Settings.System.getInt(MainActivity.this.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        final Button toggle = (Button) findViewById(R.id.toggle);
        mScreenLock = (Button) findViewById(R.id.screen_lock);
        Button advertisement  = (Button) findViewById(R.id.advertisement);

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (mIsTorchOn) {
                        torchOn();
                        toggle.setText(R.string.off);
                        mScreenLock.setVisibility(View.VISIBLE);
                        mIsTorchOn = false;
                    } else {
                        torchOff();
                        toggle.setText(R.string.on);
                        mScreenLock.setVisibility(View.GONE);
                        mIsTorchOn = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mScreenLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsScreenOn) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    mScreenLock.setText(R.string.unlock_screen);
                    mIsScreenOn = false;
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    mScreenLock.setText(R.string.lock_screen);
                    mIsScreenOn = true;
                }

            }
        });

        advertisement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mInterstitialAd.isReady()) {
                    mInterstitialAd.show();
                } else {
                }

            }
        });


        //create VpadnBanner instance
        RelativeLayout adBannerLayout = (RelativeLayout) findViewById(R.id.activity_main);
        mVponBanner = new VpadnBanner(this, bannerId, VpadnAdSize.SMART_BANNER, "TW");
        VpadnAdRequest adRequest = new VpadnAdRequest();
        //set auto refresh to get banner
        adRequest.setEnableAutoRefresh(true);
        //load vpon banner
        mVponBanner.loadAd(adRequest);
        //add vpon banner to your layout view
        adBannerLayout.addView(mVponBanner);

        mInterstitialAd = new VpadnInterstitialAd(MainActivity.this, interstitialBannerId, "TW");
        //Add listener
        mInterstitialAd.setAdListener(new VpadnAdListener() {
            @Override
            public void onVpadnReceiveAd(VpadnAd vpadnAd) {

            }

            @Override
            public void onVpadnFailedToReceiveAd(VpadnAd vpadnAd, VpadnAdRequest.VpadnErrorCode vpadnErrorCode) {

            }

            @Override
            public void onVpadnPresentScreen(VpadnAd vpadnAd) {

            }

            @Override
            public void onVpadnDismissScreen(VpadnAd vpadnAd) {

            }

            @Override
            public void onVpadnLeaveApplication(VpadnAd vpadnAd) {

            }
        });
        // Create ad request
        VpadnAdRequest request = new VpadnAdRequest();
        //Begin loading your interstitial
        mInterstitialAd.loadAd(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            torchOff();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVponBanner != null) {
            //remember to call destroy method
            mVponBanner.destroy();
            mVponBanner = null;
        }
        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
            mInterstitialAd = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mIsTorchOn) {
                torchOff();
            } else {
                torchOn();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mIsScreenOn) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void torchOn() throws CameraAccessException {
        mCameraManager.setTorchMode(mCameraId, true);
        if (!mIsCanWrite) {
            return;
        }
        Settings.System.putInt(MainActivity.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        Settings.System.putInt(MainActivity.this.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, 10);
    }

    private void torchOff() throws CameraAccessException {
        mCameraManager.setTorchMode(mCameraId, false);
        if (!mIsCanWrite) {
            return;
        }
        Settings.System.putInt(MainActivity.this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mBrightSetting);
        Settings.System.putInt(MainActivity.this.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, mBrightnessLevel);
    }
}
