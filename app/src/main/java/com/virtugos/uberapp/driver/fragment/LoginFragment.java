package com.virtugos.uberapp.driver.fragment;

import java.util.HashMap;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.android.gms.plus.model.people.Person;
import com.virtugos.uberapp.driver.MapActivity;
import com.virtugos.uberapp.driver.R;
import com.virtugos.uberapp.driver.base.BaseRegisterFragment;
import com.virtugos.uberapp.driver.parse.AsyncTaskCompleteListener;
import com.virtugos.uberapp.driver.parse.HttpRequester;
import com.virtugos.uberapp.driver.parse.ParseContent;
import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AndyUtils;
import com.virtugos.uberapp.driver.utills.AppLog;
import com.virtugos.uberapp.driver.utills.PreferenceHelper;
import com.virtugos.uberapp.driver.widget.MyFontEdittextView;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.Permission.Type;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

public class LoginFragment extends BaseRegisterFragment implements
        OnClickListener, ConnectionCallbacks, OnConnectionFailedListener,
        AsyncTaskCompleteListener {
    private MyFontEdittextView etLoginEmail, etLoginPassword, etForgetEmail;
    private ImageButton btnFb, btnGplus;
    private ConnectionResult mConnectionResult;
    private GoogleApiClient mGoogleApiClient;
    private SimpleFacebook mSimpleFacebook;
    private SimpleFacebookConfiguration facebookConfiguration;
    private ParseContent parseContent;
    private boolean mSignInClicked, mIntentInProgress;
    private final String TAG = "LoginFragment";
    private static final int RC_SIGN_IN = 0;
    private Dialog forgotPasswordDialog;

    Permission[] facebookPermissions = new Permission[]{Permission.EMAIL};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View loginFragmentView = inflater.inflate(R.layout.fragment_login,
                container, false);
        etLoginEmail = (MyFontEdittextView) loginFragmentView
                .findViewById(R.id.etLoginEmail);
        etLoginPassword = (MyFontEdittextView) loginFragmentView
                .findViewById(R.id.etLoginPassword);
        loginFragmentView.findViewById(R.id.tvLoginForgetPassword)
                .setOnClickListener(this);
        loginFragmentView.findViewById(R.id.btnSignIn).setOnClickListener(
                this);
        loginFragmentView.findViewById(R.id.btnBackSignIn).setOnClickListener(
                this);
//		loginFragmentView.findViewById(R.id.btnLoginFb)
//				.setOnClickListener(this);
//		loginFragmentView.findViewById(R.id.btnLoginGplus).setOnClickListener(
//				this);

        return loginFragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerActivity.actionBar.hide();
        // registerActivity.setStatusBarColor(getResources().getColor(
        // R.color.color_action_bar_main));
        // registerActivity.setActionBarTitle(getResources().getString(
        // R.string.text_signin));
//		registerActivity.btnActionInfo.setVisibility(View.INVISIBLE);
        // registerActivity.setActionBarIcon(R.drawable.taxi);
        parseContent = new ParseContent(registerActivity);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // facebook api initialization
        facebookConfiguration = new SimpleFacebookConfiguration.Builder()
                .setAppId(registerActivity.getResources().getString(R.string.app_id))
                .setNamespace(registerActivity.getResources().getString(R.string.app_name))
                .setPermissions(facebookPermissions).build();
        SimpleFacebook.setConfiguration(facebookConfiguration);

        // Google plus api initialization
//        Scope scope = new Scope(AndyConstants.GOOGLE_API_SCOPE_URL);
//        mGoogleApiClient = new GoogleApiClient.Builder(registerActivity)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(Plus.API, PlusOptions.builder().build())
//                .addScope(scope).build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvLoginForgetPassword:
                showForgotPasswordDialog();
                // registerActivity.addFragment(new ForgetPasswordFragment(), true,
                // AndyConstants.FOREGETPASS_FRAGMENT_TAG, true);
                break;

            case R.id.btnSignIn:
                if (etLoginEmail.getText().length() == 0) {
                    AndyUtils.showToast(
                            registerActivity.getResources().getString(R.string.error_empty_email),
                            registerActivity);
                    return;
                } else if (!AndyUtils.eMailValidation(etLoginEmail.getText()
                        .toString())) {
                    AndyUtils.showToast(
                            registerActivity.getResources().getString(R.string.error_valid_email),
                            registerActivity);
                    return;
                } else if (etLoginPassword.getText().length() == 0) {
                    AndyUtils
                            .showToast(
                                    registerActivity.getResources().getString(
                                            R.string.error_empty_password),
                                    registerActivity);
                    return;
                } else {
                    login();
                }

                break;

            case R.id.btnBackSignIn:
                registerActivity.onBackPressed();
                break;

            case R.id.tvForgetSubmit:
                if (etForgetEmail.getText().length() == 0) {
                    AndyUtils.showToast(
                            registerActivity.getResources().getString(R.string.error_empty_email),
                            registerActivity);
                    return;
                } else if (!AndyUtils.eMailValidation(etForgetEmail.getText()
                        .toString())) {
                    AndyUtils.showToast(
                            registerActivity.getResources().getString(R.string.error_valid_email),
                            registerActivity);
                    return;
                } else {
                    if (!AndyUtils.isNetworkAvailable(registerActivity)) {
                        AndyUtils.showToast(
                                registerActivity.getResources()
                                        .getString(R.string.toast_no_internet),
                                registerActivity);
                        return;
                    }
                    forgetPassowrd();
                }
                break;

            default:
                break;
        }
    }

    public void checkPermissionForGoogleAccount() {
        if (ContextCompat.checkSelfPermission(registerActivity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            registerActivity.requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, AndyConstants.PERMISSION_GET_ACCOUNT_REQUEST_CODE);
        } else {
            mSignInClicked = true;
            Scope scope = new Scope(AndyConstants.GOOGLE_API_SCOPE_URL);
            mGoogleApiClient = new GoogleApiClient.Builder(registerActivity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API, PlusOptions.builder().build())
                    .addScope(scope).build();
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    private void login() {
        if (!AndyUtils.isNetworkAvailable(registerActivity)) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.toast_no_internet),
                    registerActivity);
            return;
        }
        AndyUtils.showCustomProgressDialog(registerActivity, registerActivity.getResources()
                .getString(R.string.progress_dialog_sign_in), false);
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.LOGIN);
        map.put(AndyConstants.Params.EMAIL, etLoginEmail.getText().toString());
        map.put(AndyConstants.Params.PASSWORD, etLoginPassword.getText()
                .toString());
        map.put(AndyConstants.Params.DEVICE_TYPE,
                AndyConstants.DEVICE_TYPE_ANDROID);
        map.put(AndyConstants.Params.DEVICE_TOKEN, new PreferenceHelper(
                registerActivity).getDeviceToken());
        map.put(AndyConstants.Params.LOGIN_BY, AndyConstants.MANUAL);

        new HttpRequester(registerActivity, map,
                AndyConstants.ServiceCode.LOGIN, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.LOGIN, this, this));
    }

    private void loginSocial(String email, String id, String loginType) {
        if (!AndyUtils.isNetworkAvailable(registerActivity)) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.toast_no_internet),
                    registerActivity);
            return;
        }

        AndyUtils.showCustomProgressDialog(registerActivity, registerActivity.getResources()
                .getString(R.string.progress_dialog_sign_in), false);

        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.LOGIN);
        map.put(AndyConstants.Params.EMAIL, email);
        map.put(AndyConstants.Params.SOCIAL_UNIQUE_ID, id);
        map.put(AndyConstants.Params.DEVICE_TYPE,
                AndyConstants.DEVICE_TYPE_ANDROID);
        map.put(AndyConstants.Params.DEVICE_TOKEN, new PreferenceHelper(
                registerActivity).getDeviceToken());
        map.put(AndyConstants.Params.LOGIN_BY, loginType);

        new HttpRequester(registerActivity, map,
                AndyConstants.ServiceCode.LOGIN, this);

        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.LOGIN, this, this));
    }

    private void getFbProfile() {
        AndyUtils.showCustomProgressDialog(registerActivity,
                getString(R.string.text_getting_info_facebook), true);
        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.ID).add(Profile.Properties.EMAIL).build();
        mSimpleFacebook.getProfile(properties, new OnProfileListener() {
            @Override
            public void onComplete(Profile profile) {
                // AndyUtils.removeSimpleProgressDialog();
                Log.i("Uber", "My profile id = " + profile.getId());
                Log.d("Facebook", "**********getEmail**********" + profile.getEmail());
                btnFb.setEnabled(true);
                btnGplus.setEnabled(true);
                AndyUtils.removeCustomProgressDialog();
                if (profile.getEmail() != null)
                    loginSocial(profile.getEmail(), profile.getId(), AndyConstants.SOCIAL_FACEBOOK);
            }
        });
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                registerActivity.startIntentSenderForResult(mConnectionResult
                                .getResolution().getIntentSender(), RC_SIGN_IN, null,
                        0, 0, 0, AndyConstants.LOGIN_FRAGMENT_TAG);
            } catch (SendIntentException e) {
                // The intent was canceled before it was sent. Return to the
                // default
                // state and attempt to connect to get an updated
                // ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (!mIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the
            // user clicks
            // 'sign-in'.
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {

            if (resultCode != Activity.RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            AppLog.Log("TAG", "on activity result facebook");
            mSimpleFacebook.onActivityResult(registerActivity, requestCode,
                    resultCode, data);
            if (mSimpleFacebook.isLogin()) {
                getFbProfile();
            } else {
                Toast.makeText(
                        registerActivity,
                        registerActivity.getResources().getString(
                                R.string.toast_facebook_login_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Person currentPerson = Plus.PeopleApi
                .getCurrentPerson(mGoogleApiClient);

        // String personName = currentPerson.getDisplayName();
        // String personPhoto = currentPerson.getImage().toString();
        // String personGooglePlusProfile = currentPerson.getUrl();
        // Toast.makeText(
        // registerActivity,
        // "email: " + email + "\nName:" + personName + "\n Profile URL:"
        // + personGooglePlusProfile + "\nPhoto:" + personPhoto
        // + "\nBirthday:" + currentPerson.getBirthday()
        // + "\n GENDER: " + currentPerson.getGender(),
        // Toast.LENGTH_LONG).show();
        btnGplus.setEnabled(true);
        btnFb.setEnabled(true);
        Log.d("google", "**********getEmail**********" + email);
        AndyUtils.removeCustomProgressDialog();
        if (email != null)
            loginSocial(email, currentPerson.getId(), AndyConstants.SOCIAL_GOOGLE);

    }

    @Override
    public void onConnectionSuspended(int arg0) {

    }

    @Override
    public void onStop() {
        super.onStop();
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerActivity.currentFragment = AndyConstants.LOGIN_FRAGMENT_TAG;
        mSimpleFacebook = SimpleFacebook.getInstance(registerActivity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AndyUtils.removeCustomProgressDialog();
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        AndyUtils.removeCustomProgressDialog();
        AppLog.Log(TAG, response);
        switch (serviceCode) {
            case AndyConstants.ServiceCode.LOGIN:
                if (!parseContent.isSuccess(response)) {
                    return;
                }
                if (parseContent.isSuccessWithId(response)) {
                    parseContent.parseUserAndStoreToDb(response);
                    new PreferenceHelper(getActivity()).putPassword(etLoginPassword
                            .getText().toString());
                    startActivity(new Intent(registerActivity, MapActivity.class));
                    registerActivity.finish();
                }
                break;
            case AndyConstants.ServiceCode.FORGET_PASSWORD:
                AppLog.Log("TAG", "forget res:" + response);
                if (new ParseContent(registerActivity).isSuccess(response)) {
                    AndyUtils.showToast(
                            registerActivity.getResources().getString(
                                    R.string.toast_forget_password_success),
                            registerActivity);
                }
                break;
            default:
                break;
        }

    }

    private void showForgotPasswordDialog() {
        forgotPasswordDialog = new Dialog(getActivity());
        forgotPasswordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        forgotPasswordDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        forgotPasswordDialog.setContentView(R.layout.fragment_forgetpassword);
        forgotPasswordDialog.setCancelable(true);
        etForgetEmail = (MyFontEdittextView) forgotPasswordDialog
                .findViewById(R.id.etForgetEmail);
        forgotPasswordDialog.findViewById(R.id.tvForgetSubmit)
                .setOnClickListener(this);
        etForgetEmail.requestFocus();
        showKeyboard(etForgetEmail);

        forgotPasswordDialog.show();

    }

    public void showKeyboard(View v) {
        InputMethodManager inputManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        // View view = activity.getCurrentFocus();
        // if (view != null) {
        inputManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
        // }
    }

    private void forgetPassowrd() {

        AndyUtils.showCustomProgressDialog(registerActivity,
                getString(R.string.progress_loading), false);
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.FORGET_PASSWORD);
        map.put(AndyConstants.Params.TYPE, 1 + "");
        map.put(AndyConstants.Params.EMAIL, etForgetEmail.getText().toString());
        new HttpRequester(registerActivity, map,
                AndyConstants.ServiceCode.FORGET_PASSWORD, this);

        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.FORGET_PASSWORD, this, this));

    }

}
