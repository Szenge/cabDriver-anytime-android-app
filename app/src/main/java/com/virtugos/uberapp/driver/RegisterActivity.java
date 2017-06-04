package com.virtugos.uberapp.driver;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.virtugos.uberapp.driver.base.ActionBarBaseActivitiy;
import com.virtugos.uberapp.driver.fragment.LoginFragment;
import com.virtugos.uberapp.driver.fragment.RegisterFragment;
import com.virtugos.uberapp.driver.utills.AndyConstants;

/**
 * @author Elluminati elluminati.in
 */
public class RegisterActivity extends ActionBarBaseActivitiy {
    public ActionBar actionBar;
    public RegisterFragment registerFragment = new RegisterFragment();
    public LoginFragment loginFragment = new LoginFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();

        if (getIntent().getBooleanExtra("isSignin", false)) {

            addFragment(loginFragment, true,
                    AndyConstants.LOGIN_FRAGMENT_TAG, false);
        } else {
            addFragment(registerFragment, true,
                    AndyConstants.REGISTER_FRAGMENT_TAG, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionNotification:
                onBackPressed();
                break;

            default:
                break;
        }

    }

//    public void registerGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
//        if (mHandleMessageReceiver != null) {
//            AndyUtils.showCustomProgressDialog(this,
//                    getResources().getString(R.string.progress_loading), false);
//            new GCMRegisterHendler(RegisterActivity.this,
//                    mHandleMessageReceiver);
//
//        }
//    }

//    public void unregisterGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
//        if (mHandleMessageReceiver != null) {
//
//            if (mHandleMessageReceiver != null) {
//                unregisterReceiver(mHandleMessageReceiver);
//            }
//
//        }
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {

        Fragment signinFragment = getSupportFragmentManager()
                .findFragmentByTag(AndyConstants.LOGIN_FRAGMENT_TAG);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(
                AndyConstants.REGISTER_FRAGMENT_TAG);
        if (fragment != null && fragment.isVisible()) {

            goToMainActivity();
        } else if (signinFragment != null && signinFragment.isVisible()) {
            goToMainActivity();
        } else {
            super.onBackPressed();

        }

    }

    private void requestRequiredPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission
                    .WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE}, AndyConstants.PERMISSION_STORAGE_REQUEST_CODE);

            return;
        } else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA
            }, AndyConstants.PERMISSION_CAMERA_REQUEST_CODE);

            return;
        } else {
            if (getIntent().getBooleanExtra("isSignin", false)) {

                addFragment(loginFragment, true,
                        AndyConstants.LOGIN_FRAGMENT_TAG, false);
            } else {
                addFragment(registerFragment, true,
                        AndyConstants.REGISTER_FRAGMENT_TAG, false);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d("RequestActivity","request permission result");
        switch (requestCode) {
            case AndyConstants.PERMISSION_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerFragment.choosePhotoFromGallary();
                }

                break;
            case AndyConstants.PERMISSION_CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerFragment.takePhotoFromCamera();
                }
                break;

            case AndyConstants.PERMISSION_GET_ACCOUNT_REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if(currentFragment.equals(AndyConstants.REGISTER_FRAGMENT_TAG)) {
                            registerFragment.checkPermissionForGoogleAccount();
                        }else{
                            loginFragment.checkPermissionForGoogleAccount();
                        }
                    }
                }
                break;
        }
    }
}
