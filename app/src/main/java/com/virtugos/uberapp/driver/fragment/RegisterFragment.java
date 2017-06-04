package com.virtugos.uberapp.driver.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.Plus.PlusOptions;
import com.google.android.gms.plus.model.people.Person;
import com.virtugos.uberapp.driver.R;
import com.virtugos.uberapp.driver.adapter.VehicalTypeListAdapter;
import com.virtugos.uberapp.driver.base.BaseRegisterFragment;
import com.virtugos.uberapp.driver.model.VehicalType;
import com.virtugos.uberapp.driver.parse.AsyncTaskCompleteListener;
import com.virtugos.uberapp.driver.parse.HttpRequester;
import com.virtugos.uberapp.driver.parse.MultiPartRequester;
import com.virtugos.uberapp.driver.parse.ParseContent;
import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AndyUtils;
import com.virtugos.uberapp.driver.utills.AppLog;
import com.virtugos.uberapp.driver.utills.PreferenceHelper;
import com.virtugos.uberapp.driver.widget.MyFontButton;
import com.virtugos.uberapp.driver.widget.MyFontEdittextView;
import com.virtugos.uberapp.driver.widget.MyFontTextView;
import com.virtugos.uberapp.driver.widget.MyFontTextViewMedium;
import com.soundcloud.android.crop.Crop;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.Permission.Type;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.entities.Profile.Properties;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class RegisterFragment extends BaseRegisterFragment implements
        OnClickListener, ConnectionCallbacks, OnConnectionFailedListener,
        AsyncTaskCompleteListener {
    private MyFontEdittextView etRegisterFname, etRegisterLName,
            etRegisterPassword, etRegisterEmail, etRegisterNumber,
            etRegisterAddress, etRegisterZipcode, etRegisterModel,
            etRegisterTaxiNo;

    private MyFontTextView tvPopupMsg;
    private MyFontTextViewMedium tvCountryCode;
    private ImageButton btnFb, btnGplus, btnClickPhoto;
    private GridView gvTypes;
    private SlidingDrawer drawer;
    private TextView tvRegisterPassword;
    private ImageView ivProfile;
    private View ivRegisterPassword;
    private boolean mSignInClicked, mIntentInProgress;
    private ConnectionResult mConnectionResult;
    private GoogleApiClient mGoogleApiClient;
    private AQuery aQuery;
    private SimpleFacebook mSimpleFacebook;
    private ParseContent parseContent;
    private ArrayList<String> countryList;
    private String country;
    private Uri uri = null;
    private String loginType = AndyConstants.MANUAL, socialId,
            profileImageData = null, socialProPicUrl, profileImageFilePath,
            filePath = "";
    // private Bitmap profilePicBitmap;
    private ImageOptions profileImageOptions;
    private SimpleFacebookConfiguration facebookConfiguration;
    private ArrayList<VehicalType> listType;
    private VehicalTypeListAdapter adapter;
    private PopupWindow registerInfoPopup;

    private final String TAG = "RegisterFragment";
    private static final int RC_SIGN_IN = 0;
    private int selectedTypePostion = -1, rotationAngle;
    private ArrayAdapter<String> arrayAdapter;
    private Bitmap photoBitmap;
    private Scope scope;

    private int isSelectedImage = 0;

    Permission[] facebookPermission = new Permission[]{Permission.EMAIL};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        country = Locale.getDefault().getDisplayCountry();

        // facebook api initialization
        facebookConfiguration = new SimpleFacebookConfiguration.Builder()
                .setAppId(registerActivity.getResources().getString(R.string.app_id))
                .setNamespace(registerActivity.getResources().getString(R.string.app_name))
                .setPermissions(facebookPermission).build();
        SimpleFacebook.setConfiguration(facebookConfiguration);

        // Google plus api initialization
        scope = new Scope(AndyConstants.GOOGLE_API_SCOPE_URL);
        mGoogleApiClient = new GoogleApiClient.Builder(registerActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, PlusOptions.builder().build())
                .addScope(scope).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View registerFragmentView = inflater.inflate(
                R.layout.fragment_register, container, false);

        ivProfile = (ImageView) registerFragmentView
                .findViewById(R.id.ivRegisterProfile);
        etRegisterAddress = (MyFontEdittextView) registerFragmentView
                .findViewById(R.id.etRegisterAddress);
        // etRegisterBio = (MyFontEdittextView) registerFragmentView
        // .findViewById(R.id.etRegisterBio);
        etRegisterEmail = (MyFontEdittextView) registerFragmentView
                .findViewById(R.id.etRegisterEmail);
        etRegisterFname = (MyFontEdittextView) registerFragmentView
                .findViewById(R.id.etRegisterFName);
        etRegisterLName = (MyFontEdittextView) registerFragmentView
                .findViewById(R.id.etRegisterLName);
        etRegisterNumber = (MyFontEdittextView) registerFragmentView
                .findViewById(R.id.etRegisterNumber);
        etRegisterPassword = (MyFontEdittextView) registerFragmentView
                .findViewById(R.id.etRegisterPassword);
        etRegisterZipcode = (MyFontEdittextView) registerFragmentView
                .findViewById(R.id.etRegisterZipCode);
        etRegisterModel = (MyFontEdittextView) registerFragmentView
                .findViewById(R.id.etRegisterModel);
        etRegisterTaxiNo = (MyFontEdittextView) registerFragmentView
                .findViewById(R.id.etRegisterTaxiNo);

        tvRegisterPassword = (TextView) registerFragmentView
                .findViewById(R.id.tvRegisterPassword);
        ivRegisterPassword = (View) registerFragmentView
                .findViewById(R.id.ivRegisterPassword);
        tvCountryCode = (MyFontTextViewMedium) registerFragmentView
                .findViewById(R.id.tvRegisterCountryCode);
//        btnRegisterEmailInfo = (MyFontButton) registerFragmentView
//                .findViewById(R.id.btnRegisterEmailInfo);
//        btnRegisterModelInfo = (MyFontButton) registerFragmentView
//                .findViewById(R.id.btnRegisterModelInfo);
//        btnRegisterTaxiNoInfo = (MyFontButton) registerFragmentView
//                .findViewById(R.id.btnRegisterTaxiNoInfo);
        btnClickPhoto = (ImageButton) registerFragmentView
                .findViewById(R.id.btnClickPhoto);
        drawer = (SlidingDrawer) registerFragmentView.findViewById(R.id.drawer);

        gvTypes = (GridView) registerFragmentView.findViewById(R.id.gvTypes);
        //tvCountryCode.setOnClickListener(this);
        ivProfile.setOnClickListener(this);
        btnClickPhoto.setOnClickListener(this);
//        btnRegisterEmailInfo.setOnClickListener(this);
//        btnRegisterModelInfo.setOnClickListener(this);
//        btnRegisterTaxiNoInfo.setOnClickListener(this);
        registerFragmentView.findViewById(R.id.tvRegisterSubmit)
                .setOnClickListener(this);

        facebookConfiguration = new SimpleFacebookConfiguration.Builder()
                .setAppId(registerActivity.getResources().getString(R.string.app_id))
                .setNamespace(registerActivity.getResources().getString(R.string.app_name))
                .setPermissions(facebookPermission).build();
        SimpleFacebook.setConfiguration(facebookConfiguration);

        return registerFragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerActivity.actionBar.show();

        registerActivity.setActionBarTitle(registerActivity.getResources().getString(
                R.string.text_register));
        registerActivity.setActionBarIcon(R.drawable.close_payment);
        registerActivity.btnActionMenu.setOnClickListener(this);
//        registerActivity.btnActionInfo.setVisibility(View.VISIBLE);
//        registerActivity.btnActionInfo.setOnClickListener(this);
        registerActivity.btnNotification.setVisibility(View.GONE);
        parseContent = new ParseContent(registerActivity);

        // popup
        LayoutInflater inflate = LayoutInflater.from(registerActivity);
        RelativeLayout layout = (RelativeLayout) inflate.inflate(
                R.layout.popup_notification_window, null);
        tvPopupMsg = (MyFontTextView) layout.findViewById(R.id.tvPopupMsg);
        registerInfoPopup = new PopupWindow(layout, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        layout.setOnClickListener(this);
        registerInfoPopup.setBackgroundDrawable(new BitmapDrawable());
        registerInfoPopup.setOutsideTouchable(true);

        aQuery = new AQuery(registerActivity);
        profileImageOptions = new ImageOptions();
        profileImageOptions.fileCache = true;
        profileImageOptions.memCache = true;
        profileImageOptions.targetWidth = 200;
        profileImageOptions.fallback = R.drawable.user;

        listType = new ArrayList<>();
        adapter = new VehicalTypeListAdapter(registerActivity, listType, this);
        gvTypes.setAdapter(adapter);
        gvTypes.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                for (int i = 0; i < listType.size(); i++)
                    listType.get(i).isSelected = false;
                listType.get(position).isSelected = true;
                // onItemClick(position);
                selectedTypePostion = position;
                adapter.notifyDataSetChanged();
            }
        });

        countryList = parseContent.parseCountryCodes();
        for (int i = 0; i < countryList.size(); i++) {
            if (countryList.get(i).contains(country)) {
                tvCountryCode.setText((countryList.get(i).substring(0,
                        countryList.get(i).indexOf(" "))));
            }
        }
        if (TextUtils.isEmpty(tvCountryCode.getText())) {
            tvCountryCode.setText((countryList.get(0).substring(0, countryList
                    .get(0).indexOf(" "))));
        }
        getVehicalTypes();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnClickPhoto:
                takePhotoFromCamera();
                break;

            case R.id.tvRegisterSubmit:
                onRegisterButtonClick();
                break;

            case R.id.tvRegisterCountryCode:
                // AlertDialog.Builder countryBuilder = new
                // Builder(registerActivity);
                // countryBuilder.setView(R.layout.countrycode_layout);
                // countryBuilder.setTitle(getResources().getString(
                // R.string.dialog_title_country_codes));
                //
                final String[] countryListArray = new String[countryList.size()];
                countryList.toArray(countryListArray);

                // countryBuilder.setItems(countryListArray,
                // new DialogInterface.OnClickListener() {
                //
                // @Override
                // public void onClick(DialogInterface dialog, int which) {
                // tvCountryCode.setText(countryListArray[which]
                // .substring(0, countryListArray[which]
                // .indexOf(" ")));
                // }
                // }).show();

                final Dialog dialog = new Dialog(registerActivity);
                dialog.setContentView(R.layout.countrycode_layout);
                final EditText countryName = (EditText) dialog
                        .findViewById(R.id.country_name);
                final ListView countryList1 = (ListView) dialog
                        .findViewById(R.id.country_list);
                dialog.setTitle(registerActivity.getResources().getString(
                        R.string.dialog_title_country_codes));
                arrayAdapter = new ArrayAdapter<>(registerActivity,
                        android.R.layout.simple_list_item_1, countryList);
                countryList1.setAdapter(arrayAdapter);

                countryList1.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        String country = arrayAdapter.getItem(position).toString();
                        tvCountryCode.setText(country.substring(0,
                                country.indexOf(" ")));
                        countryName.setText(country);
                        Log.d("country code", country);
                        dialog.dismiss();
                    }
                });

                countryName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        String text = countryName.getText().toString();
                        arrayAdapter.getFilter().filter(text);

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                });
                dialog.show();

                break;
//            case R.id.btnRegisterEmailInfo:
//                openPopup(btnRegisterEmailInfo,
//                        getString(R.string.text_regemail_popup));
//                break;

//            case R.id.btnRegisterModelInfo:
//                openPopup(btnRegisterModelInfo,
//                        getString(R.string.text_regmodelno_popup));
//                break;

//            case R.id.btnRegisterTaxiNoInfo:
//                openPopup(btnRegisterTaxiNoInfo,
//                        getString(R.string.text_regtaxino_popup));
//                break;

//            case R.id.btnActionInfo:
//                openPopup(registerActivity.btnActionInfo,
//                        getString(R.string.text_regaction_popup));
//                break;

            case R.id.btnActionMenu:
                registerActivity.onBackPressed();
                break;

            default:
                break;
        }
    }

    public void checkPermissionForGoogleAccount(){
        if(ContextCompat.checkSelfPermission(registerActivity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED){
            registerActivity.requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, AndyConstants.PERMISSION_GET_ACCOUNT_REQUEST_CODE);
        }else {
            mSignInClicked = true;
            if (!mGoogleApiClient.isConnecting()) {
                AndyUtils.showCustomProgressDialog(registerActivity,
                        getString(R.string.progress_getting_info), false);
                mGoogleApiClient.connect();
            }
        }
    }

    public void openPopup(View view, String msg) {
        if (registerInfoPopup.isShowing())
            registerInfoPopup.dismiss();
        else {
            registerInfoPopup.showAsDropDown(view);
            tvPopupMsg.setText(msg);
        }

    }

    private void onRegisterButtonClick() {

        if (etRegisterFname.getText().length() == 0) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.error_empty_fname),
                    registerActivity);
            return;
        } else if (etRegisterLName.getText().length() == 0) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.error_empty_lname),
                    registerActivity);
            return;
        } else if (etRegisterEmail.getText().length() == 0) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.error_empty_email),
                    registerActivity);
            return;
        } else if (!AndyUtils.eMailValidation(etRegisterEmail.getText()
                .toString())) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.error_valid_email),
                    registerActivity);
            return;
        } else if (etRegisterModel.getText().length() == 0) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.error_empty_model),
                    registerActivity);
            return;
        } else if (etRegisterTaxiNo.getText().length() == 0) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.error_empty_taxi_no),
                    registerActivity);
            return;

        } else if (etRegisterPassword.getVisibility() == View.VISIBLE) {
            if (etRegisterPassword.getText().length() == 0) {
                AndyUtils.showToast(
                        registerActivity.getResources().getString(
                                R.string.error_empty_reg_password),
                        registerActivity);
                return;
            } else if (etRegisterPassword.getText().length() < 6) {
                AndyUtils
                        .showToast(
                                registerActivity.getResources().getString(
                                        R.string.error_valid_password),
                                registerActivity);
                return;
            }
        }

        if (etRegisterPassword.getVisibility() == View.GONE) {
            if (!TextUtils.isEmpty(socialProPicUrl)) {
                profileImageData = null;
                profileImageData = aQuery.getCachedFile(socialProPicUrl)
                        .getPath();
            }
        }

        if (etRegisterNumber.getText().length() == 0) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.error_empty_number),
                    registerActivity);
            return;
            // } else if (profileImageData == null ||
            // profileImageData.equals("")) {
            // AndyUtils.showToast(
            // getResources().getString(R.string.error_empty_image),
            // registerActivity);
            // return;
        } else if (selectedTypePostion == -1) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.error_empty_type),
                    registerActivity);
            drawer.open();
            return;
        } else if(listType.get(selectedTypePostion).getName().equals("Táxi")){
            AndyUtils.showToast(
                    "Para cadastro de Táxi, entre em contato por email ou telefone(contato@umovel.com.br ou (49) 99197-9478)",
                    registerActivity);
            drawer.open();
        }else if(isSelectedImage==0){
            AndyUtils.showToast(
                    "Por favor, selecione uma imagem de perfil",
                    registerActivity);
            drawer.open();
        } else {
            register(loginType, socialId);
        }
    }

    // private void showPictureDialog() {
    // AlertDialog.Builder pictureDialog = new AlertDialog.Builder(
    // registerActivity);
    // pictureDialog.setTitle(getResources().getString(
    // R.string.dialog_chhose_photo));
    // String[] pictureDialogItems = {
    // getResources().getString(R.string.dialog_from_gallery),
    // getResources().getString(R.string.dialog_from_camera) };
    //
    // pictureDialog.setItems(pictureDialogItems,
    // new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // switch (which) {
    //
    // case 0:
    // choosePhotoFromGallary();
    // break;
    //
    // case 1:
    // takePhotoFromCamera();
    // break;
    //
    // }
    // }
    // });
    // pictureDialog.show();
    // }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                registerActivity.startIntentSenderForResult(mConnectionResult
                                .getResolution().getIntentSender(), RC_SIGN_IN, null,
                        0, 0, 0, AndyConstants.REGISTER_FRAGMENT_TAG);
            } catch (SendIntentException e) {
                /*
                 * The intent was canceled before it was sent. Return to the
				 * default state and attempt to connect to get an updated
				 * ConnectionResult.
				 */
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (!mIntentInProgress) {
            mConnectionResult = result;
            if (mSignInClicked) {
                resolveSignInError();
            }
        }

    }

    private void getFbProfile() {
        AndyUtils.showCustomProgressDialog(registerActivity,
                getString(R.string.text_getting_info_facebook), true);
        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Properties.ID).add(Properties.FIRST_NAME)
                .add(Properties.GENDER).add(Properties.EMAIL)
                .add(Properties.LAST_NAME).add(Properties.BIRTHDAY)
                .add(Properties.EDUCATION).add(Properties.PICTURE).build();
        mSimpleFacebook.getProfile(properties, new OnProfileListener() {
            @Override
            public void onComplete(Profile profile) {
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log("Uber", "My profile id = " + profile.getId());
                btnFb.setEnabled(false);
                btnGplus.setEnabled(false);
                etRegisterEmail.setText(profile.getEmail());
                etRegisterFname.setText(profile.getFirstName());
                etRegisterLName.setText(profile.getLastName());
                socialId = profile.getId();
                loginType = AndyConstants.SOCIAL_FACEBOOK;
                // etRegisterPassword.setEnabled(false);
                tvRegisterPassword.setVisibility(View.GONE);
                ivRegisterPassword.setVisibility(View.GONE);
                etRegisterPassword.setVisibility(View.GONE);

                if (!TextUtils.isEmpty(profile.getPicture())
                        || !profile.getPicture().equalsIgnoreCase("null")) {
                    socialProPicUrl = profile.getPicture();
                    aQuery.id(ivProfile).image(profile.getPicture(),
                            profileImageOptions);
                } else {
                    socialProPicUrl = null;
                }

            }
        });
    }

    public void onItemClick(int pos) {
        selectedTypePostion = pos;

    }

    @SuppressWarnings("static-access")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {

            if (resultCode != registerActivity.RESULT_OK) {
                mSignInClicked = false;
                AndyUtils.removeCustomProgressDialog();
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else if (requestCode == AndyConstants.CHOOSE_PHOTO) {
            if (data != null) {
                Uri uri = data.getData();

                AppLog.Log(TAG, "Choose photo on activity result");

                profileImageFilePath = getRealPathFromURI(uri);
                filePath = profileImageFilePath;
                try {
                    int mobile_width = 480;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, options);
                    int outWidth = options.outWidth;
                    int ratio = (int) ((((float) outWidth) / mobile_width) + 0.5f);

                    if (ratio == 0) {
                        ratio = 1;
                    }
                    ExifInterface exif = new ExifInterface(filePath);

                    String orientString = exif
                            .getAttribute(ExifInterface.TAG_ORIENTATION);
                    int orientation = orientString != null ? Integer
                            .parseInt(orientString)
                            : ExifInterface.ORIENTATION_NORMAL;


                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        rotationAngle = 90;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        rotationAngle = 180;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        rotationAngle = 270;
                    } else {
                        rotationAngle = 0;
                    }

                    System.out.println("Rotation : " + rotationAngle);

                    options.inJustDecodeBounds = false;
                    options.inSampleSize = ratio;

                    photoBitmap = BitmapFactory.decodeFile(filePath, options);
                    if (photoBitmap != null) {
                        Matrix matrix = new Matrix();
                        matrix.setRotate(rotationAngle,
                                (float) photoBitmap.getWidth() / 2,
                                (float) photoBitmap.getHeight() / 2);
                        photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0,
                                photoBitmap.getWidth(),
                                photoBitmap.getHeight(), matrix, true);

                        AppLog.Log(TAG, "Take photo on activity result");
                        String path = Images.Media.insertImage(
                                registerActivity.getContentResolver(),
                                photoBitmap, Calendar.getInstance()
                                        .getTimeInMillis() + ".jpg", null);

                        beginCrop(Uri.parse(path));

                    }
                } catch (OutOfMemoryError e) {
                    System.out.println("out of bound");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(
                        registerActivity,
                        registerActivity.getResources().getString(
                                R.string.toast_unable_to_selct_image),
                        Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == AndyConstants.TAKE_PHOTO) {

            String imageFilePath = uri.getPath();
            if (imageFilePath != null && imageFilePath.length() > 0) {
                // File myFile = new File(imageFilePath);
                try {
                    // if (bmp != null)
                    // bmp.recycle();
                    int mobile_width = 480;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    // options.inJustDecodeBounds = true;
                    // BitmapFactory.decodeFile(imageFilePath, options);
                    int outWidth = options.outWidth;
                    int outHeight = options.outHeight;
                    int ratio = (int) ((((float) outWidth) / mobile_width) + 0.5f);

                    if (ratio == 0) {
                        ratio = 1;
                    }
                    ExifInterface exif = new ExifInterface(imageFilePath);

                    String orientString = exif
                            .getAttribute(ExifInterface.TAG_ORIENTATION);
                    int orientation = orientString != null ? Integer
                            .parseInt(orientString)
                            : ExifInterface.ORIENTATION_NORMAL;
                    System.out.println("Orientation : " + orientation);

                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        rotationAngle = 90;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        rotationAngle = 180;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        rotationAngle = 270;
                    } else {
                        rotationAngle = 0;
                    }

                    System.out.println("Rotation : " + rotationAngle);

                    options.inJustDecodeBounds = false;
                    options.inSampleSize = ratio;

                    Bitmap bmp = BitmapFactory.decodeFile(imageFilePath,
                            options);
                    File myFile = new File(imageFilePath);
                    // bmp = new ImageHelper().decodeFile(myFile);
                    FileOutputStream outStream = new FileOutputStream(myFile);
                    if (bmp != null) {
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();

                        Matrix matrix = new Matrix();
                        matrix.setRotate(rotationAngle,
                                (float) bmp.getWidth() / 2,
                                (float) bmp.getHeight() / 2);

                        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                                bmp.getHeight(), matrix, true);

                        // ivStuffPicture.setImageBitmap(bmp);

                        String path = Images.Media.insertImage(
                                registerActivity.getContentResolver(), bmp,
                                Calendar.getInstance().getTimeInMillis()
                                        + ".jpg", null);
                        beginCrop(Uri.parse(path));

                        // AQuery aQuery = new AQuery(this);
                        // aQuery.id(ivProfile).image(bmp);
                        //
                        profileImageData = imageFilePath;
                        // rlDescription.setVisibility(View.VISIBLE);
                        // llPicture.setVisibility(View.GONE);
                    }
                } catch (OutOfMemoryError e) {
                    System.out.println("out of bound");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == Crop.REQUEST_CROP) {
            AppLog.Log(TAG, "Crop photo on activity result");
            handleCrop(resultCode, data);
        } else {
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

    public void choosePhotoFromGallary() {
        if (ContextCompat.checkSelfPermission(registerActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(registerActivity, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, AndyConstants.PERMISSION_STORAGE_REQUEST_CODE);
        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            registerActivity
                    .startActivityForResult(galleryIntent,
                            AndyConstants.CHOOSE_PHOTO,
                            AndyConstants.REGISTER_FRAGMENT_TAG);
        }

    }

    public void takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(registerActivity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(registerActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(registerActivity, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, AndyConstants.PERMISSION_CAMERA_REQUEST_CODE);
        } else {
            Calendar cal = Calendar.getInstance();
            File file = new File(Environment.getExternalStorageDirectory(),
                    (cal.getTimeInMillis() + ".jpg"));

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                file.delete();
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            uri = Uri.fromFile(file);
            Intent cameraIntent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            registerActivity.startActivityForResult(cameraIntent,
                    AndyConstants.TAKE_PHOTO, AndyConstants.REGISTER_FRAGMENT_TAG);

        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        AndyUtils.removeCustomProgressDialog();
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Person currentPerson = Plus.PeopleApi
                .getCurrentPerson(mGoogleApiClient);

        String personName = currentPerson.getDisplayName();
        String personPhoto = currentPerson.getImage().toString();

        btnGplus.setEnabled(false);
        btnFb.setEnabled(false);
        etRegisterEmail.setText(email);
        if (personName.contains(" ")) {
            String[] split = personName.split(" ");
            etRegisterFname.setText(split[0]);
            etRegisterLName.setText(split[1]);
        } else {
            etRegisterFname.setText(personName);
        }

        // etRegisterPassword.setEnabled(false);
        etRegisterPassword.setVisibility(View.GONE);
        tvRegisterPassword.setVisibility(View.GONE);
        ivRegisterPassword.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(personPhoto)
                || !personPhoto.equalsIgnoreCase("null")) {
            try {
                socialProPicUrl = new JSONObject(personPhoto).getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            aQuery.id(ivProfile).image(socialProPicUrl, profileImageOptions);
        } else {
            socialProPicUrl = null;
        }
        socialId = currentPerson.getId();
        loginType = AndyConstants.SOCIAL_GOOGLE;
        // etRegisterPassword.setEnabled(false);
        // etRegisterPassword.setVisibility(View.GONE);
    }

    private void register(String type, String id) {

        if (!AndyUtils.isNetworkAvailable(registerActivity)) {
            AndyUtils.showToast(
                    registerActivity.getResources().getString(R.string.toast_no_internet),
                    registerActivity);
            return;
        }

        AndyUtils.showCustomProgressDialog(registerActivity, registerActivity.getResources()
                .getString(R.string.progress_dialog_register), false);

        if (type.equals(AndyConstants.MANUAL)) {
            AppLog.Log(TAG, "Simple Register method");
            HashMap<String, String> map = new HashMap<>();
            map.put(AndyConstants.URL, AndyConstants.ServiceType.REGISTER);
            map.put(AndyConstants.Params.FIRSTNAME, etRegisterFname.getText()
                    .toString());
            map.put(AndyConstants.Params.LAST_NAME, etRegisterLName.getText()
                    .toString());
            map.put(AndyConstants.Params.EMAIL, etRegisterEmail.getText()
                    .toString());
            map.put(AndyConstants.Params.PASSWORD, etRegisterPassword.getText()
                    .toString());

            if (!TextUtils.isEmpty(profileImageData)) {
                map.put(AndyConstants.Params.PICTURE, profileImageData);
            }
            map.put(AndyConstants.Params.PHONE, tvCountryCode.getText()
                    .toString().trim()
                    + etRegisterNumber.getText().toString());
            // map.put(AndyConstants.Params.BIO, etRegisterBio.getText()
            // .toString());
            map.put(AndyConstants.Params.ADDRESS, etRegisterAddress.getText()
                    .toString());
            map.put(AndyConstants.Params.STATE, "");
            map.put(AndyConstants.Params.COUNTRY, "");
            map.put(AndyConstants.Params.ZIPCODE, etRegisterZipcode.getText()
                    .toString().trim());
            map.put(AndyConstants.Params.TYPE,
                    String.valueOf(listType.get(selectedTypePostion).getId()));
            map.put(AndyConstants.Params.DEVICE_TYPE,
                    AndyConstants.DEVICE_TYPE_ANDROID);
            map.put(AndyConstants.Params.DEVICE_TOKEN, new PreferenceHelper(
                    registerActivity).getDeviceToken());
            map.put(AndyConstants.Params.TAXI_MODEL, etRegisterModel.getText()
                    .toString().trim());
            map.put(AndyConstants.Params.TAXI_NUMBER, etRegisterTaxiNo
                    .getText().toString().trim());
            map.put(AndyConstants.Params.LOGIN_BY, AndyConstants.MANUAL);
            map.put(AndyConstants.Params.LANGUAGE,AndyConstants.PUSH_LANGUAGE+"");
            new MultiPartRequester(registerActivity, map,
                    AndyConstants.ServiceCode.REGISTER, this);
        } else {
            registerSoicial(id, type);
        }
    }

    private void registerSoicial(String id, String loginType) {
        AppLog.Log(TAG, "Register social method");
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.REGISTER);
        map.put(AndyConstants.Params.FIRSTNAME, etRegisterFname.getText()
                .toString());
        map.put(AndyConstants.Params.LAST_NAME, etRegisterLName.getText()
                .toString());
        map.put(AndyConstants.Params.ADDRESS, etRegisterAddress.getText()
                .toString());
        map.put(AndyConstants.Params.EMAIL, etRegisterEmail.getText()
                .toString());
        map.put(AndyConstants.Params.PHONE, tvCountryCode.getText().toString()
                .trim()
                + etRegisterNumber.getText().toString());
        if (!TextUtils.isEmpty(profileImageData)) {
            map.put(AndyConstants.Params.PICTURE, profileImageData);
        }
        map.put(AndyConstants.Params.STATE, "");
        map.put(AndyConstants.Params.TYPE,
                String.valueOf(listType.get(selectedTypePostion).getId()));
        map.put(AndyConstants.Params.COUNTRY, "");
        // map.put(AndyConstants.Params.BIO,
        // etRegisterBio.getText().toString());
        map.put(AndyConstants.Params.DEVICE_TYPE,
                AndyConstants.DEVICE_TYPE_ANDROID);
        map.put(AndyConstants.Params.DEVICE_TOKEN, new PreferenceHelper(
                registerActivity).getDeviceToken());
        map.put(AndyConstants.Params.ZIPCODE, etRegisterZipcode.getText()
                .toString().trim());
        map.put(AndyConstants.Params.TAXI_MODEL, etRegisterModel.getText()
                .toString().trim());
        map.put(AndyConstants.Params.TAXI_NUMBER, etRegisterTaxiNo.getText()
                .toString().trim());
        map.put(AndyConstants.Params.SOCIAL_UNIQUE_ID, id);
        map.put(AndyConstants.Params.LOGIN_BY, loginType);
        map.put(AndyConstants.Params.LANGUAGE,AndyConstants.PUSH_LANGUAGE+"");
        new MultiPartRequester(registerActivity, map,
                AndyConstants.ServiceCode.REGISTER, this);

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {
            case AndyConstants.ServiceCode.GET_VEHICAL_TYPES:
                AppLog.Log(TAG, "Vehicle types  " + response);
                if (parseContent.isSuccess(response)) {
                    parseContent.parseTypes(response, listType);
                    adapter.notifyDataSetChanged();
                }
                AndyUtils.removeCustomProgressDialog();
                break;

            case AndyConstants.ServiceCode.REGISTER:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log(TAG, "Register response :" + response);

                if (parseContent.isSuccess(response)) {
                    // AndyUtils.showToast(
                    // registerActivity.getResources().getString(
                    // R.string.toast_register_success),
                    // registerActivity);
                    parseContent.parseUserAndStoreToDb(response);
                    new PreferenceHelper(getActivity())
                            .putPassword(etRegisterPassword.getText().toString());
                    showRegistrationConfirmationDialog();
                    // Intent intent = new Intent(registerActivity,
                    // MapActivity.class);
                    // startActivity(intent);
                    registerActivity.addFragment(new LoginFragment(), false,
                            AndyConstants.LOGIN_FRAGMENT_TAG, false);
                    // registerActivity.finish();
                }
                break;

            default:
                break;
        }

    }

    private void showRegistrationConfirmationDialog() {
        final Dialog dialog = new Dialog(registerActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout);
        Button okBtn = (Button) dialog.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = registerActivity.getContentResolver().query(contentURI,
                null, null, null, null);

        if (cursor == null) { // Source is Dropbox or other similar local file
            // path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            try {
                int idx = cursor
                        .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
            } catch (Exception e) {
                AndyUtils
                        .showToast(
                                registerActivity.getResources().getString(
                                        R.string.text_error_get_image),
                                registerActivity);
                result = "";
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AndyUtils.removeCustomProgressDialog();
    }

    private void getVehicalTypes() {
        AndyUtils.showCustomProgressDialog(registerActivity, registerActivity.getResources()
                .getString(R.string.progress_getting_types), false);
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.GET_VEHICAL_TYPES);
        AppLog.Log(TAG, AndyConstants.URL);
        new HttpRequester(registerActivity, map,
                AndyConstants.ServiceCode.GET_VEHICAL_TYPES, true, this);

        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // AndyConstants.ServiceCode.GET_VEHICAL_TYPES, this, this));
    }

    @Override
    public void onResume() {
        super.onResume();
        registerActivity.currentFragment = AndyConstants.REGISTER_FRAGMENT_TAG;
        mSimpleFacebook = SimpleFacebook.getInstance(registerActivity);
    }

    private void beginCrop(Uri source) {
        // Uri outputUri = Uri.fromFile(new File(registerActivity.getCacheDir(),
        // "cropped"));
        Uri outputUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), (Calendar.getInstance()
                .getTimeInMillis() + ".jpg")));
        new Crop(source).output(outputUri).asSquare().start(registerActivity);
    }

    @SuppressWarnings("static-access")
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == registerActivity.RESULT_OK) {
            AppLog.Log(TAG, "Handle crop");
            profileImageData = getRealPathFromURI(Crop.getOutput(result));
            ivProfile.setImageURI(Crop.getOutput(result));
            isSelectedImage = 1;
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(registerActivity,
                    Crop.getError(result).getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

}
