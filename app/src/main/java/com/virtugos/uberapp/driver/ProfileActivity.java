package com.virtugos.uberapp.driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.callback.ImageOptions;
import com.virtugos.uberapp.driver.base.ActionBarBaseActivitiy;
import com.virtugos.uberapp.driver.db.DBHelper;
import com.virtugos.uberapp.driver.model.User;
import com.virtugos.uberapp.driver.parse.AsyncTaskCompleteListener;
import com.virtugos.uberapp.driver.parse.MultiPartRequester;
import com.virtugos.uberapp.driver.parse.ParseContent;
import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AndyUtils;
import com.virtugos.uberapp.driver.utills.AppLog;
import com.virtugos.uberapp.driver.utills.PreferenceHelper;
import com.virtugos.uberapp.driver.widget.MyFontEdittextView;
import com.virtugos.uberapp.driver.widget.MyFontTextView;
import com.soundcloud.android.crop.Crop;

public class ProfileActivity extends ActionBarBaseActivitiy implements
        OnClickListener, AsyncTaskCompleteListener {
    private MyFontEdittextView etProfileFname, etProfileLName, etProfileEmail,
            etProfileNumber, etProfileAddress, etProfileZipcode,
            etProfileCurrentPassword, etProfileNewPassword,
            etProfileRetypePassword, etProfileModel, etProfileVehicleNo;
    private ImageView ivProfile;
    // private MyFontButton tvProfileSubmit, btnProfileModelInfo,
    // , btnProfileVehicleNoInfo;
    private MyFontTextView tvPopupMsg, tvProfileFName, tvProfileLName;
    private DBHelper dbHelper;
    private Uri uri = null;
    private AQuery aQuery;
    private String profileImageData, loginType, filePath = "",
            profileImageFilePath;
    private Bitmap photoBitmap;
    private int rotationAngle;
    private PreferenceHelper preferenceHelper;
    private ImageOptions imageOptions;
    private final String TAG = "profileActivity";
    private ParseContent parseContent;
    private PopupWindow registerInfoPopup;
    private LinearLayout llCurrentPassword, llNewPassword, llConfirmPassword;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // getSupportActionBar().setTitle(getString(R.string.text_profile));
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeButtonEnabled(true);
        // findViewById(R.id.llProfileSocial).setVisibility(View.GONE);
        // findViewById(R.id.etProfilePassword).setVisibility(View.GONE);
        // findViewById(R.id.tvProfileSubmit).setVisibility(View.GONE);
        findViewById(R.id.tvProfileCountryCode).setVisibility(View.GONE);
        // findViewById(R.id.llseprateView).setVisibility(View.GONE);
        etProfileFname = (MyFontEdittextView) findViewById(R.id.etProfileFName);
        etProfileLName = (MyFontEdittextView) findViewById(R.id.etProfileLName);
        etProfileEmail = (MyFontEdittextView) findViewById(R.id.etProfileEmail);
        etProfileCurrentPassword = (MyFontEdittextView) findViewById(R.id.etProfileCurrentPassword);
        etProfileNewPassword = (MyFontEdittextView) findViewById(R.id.etProfileNewPassword);
        etProfileRetypePassword = (MyFontEdittextView) findViewById(R.id.etProfileRetypePassword);
        etProfileNumber = (MyFontEdittextView) findViewById(R.id.etProfileNumber);
        // etProfileBio = (MyFontEdittextView) findViewById(R.id.etProfileBio);
        etProfileAddress = (MyFontEdittextView) findViewById(R.id.etProfileAddress);
        etProfileZipcode = (MyFontEdittextView) findViewById(R.id.etProfileZipCode);
        etProfileModel = (MyFontEdittextView) findViewById(R.id.etProfileModel);
        etProfileVehicleNo = (MyFontEdittextView) findViewById(R.id.etProfileVehicleNo);
        tvProfileFName = (MyFontTextView) findViewById(R.id.tvProfileFName);
        tvProfileLName = (MyFontTextView) findViewById(R.id.tvProfileLName);
        // tvProfileSubmit = (MyFontButton) findViewById(R.id.tvProfileSubmit);
        ivProfile = (ImageView) findViewById(R.id.ivProfileProfile);
        llCurrentPassword = (LinearLayout) findViewById(R.id.llCurrentPassword);
        llConfirmPassword = (LinearLayout) findViewById(R.id.llConfirmPassword);
        llNewPassword = (LinearLayout) findViewById(R.id.llNewPassword);

//        btnProfileEmailInfo = (ImageView) findViewById(R.id.btnProfileEmailInfo);
        // btnProfileModelInfo = (MyFontButton)
        // findViewById(R.id.btnProfileModelInfo);
        // btnProfileVehicleNoInfo = (MyFontButton)
        // findViewById(R.id.btnProfileVehicleNoInfo);
        btnActionMenu.setVisibility(View.VISIBLE);
        setActionBarTitle(getString(R.string.text_profile));
        setActionBarIcon(R.drawable.back);
        setIcon(R.drawable.edit_profile);
        btnEditProfile.setVisibility(View.GONE);
        btnNotification.setVisibility(View.VISIBLE);
        btnEditProfile.setOnClickListener(this);

        ivProfile.setOnClickListener(this);
        // tvProfileSubmit.setOnClickListener(this);
//        btnProfileEmailInfo.setOnClickListener(this);
        // btnProfileModelInfo.setOnClickListener(this);
        // btnProfileVehicleNoInfo.setOnClickListener(this);
        // tvProfileSubmit.setText(getResources().getString(
        // R.string.text_edit_profile));
        parseContent = new ParseContent(this);
        preferenceHelper = new PreferenceHelper(this);
        // socialId = preferenceHelper.getSocialId();
        loginType = preferenceHelper.getLoginBy();
        if (loginType.equals(AndyConstants.MANUAL)) {
            llCurrentPassword.setVisibility(View.VISIBLE);
            llNewPassword.setVisibility(View.VISIBLE);
            llConfirmPassword.setVisibility(View.VISIBLE);
            // etProfileCurrentPassword.setVisibility(View.VISIBLE);

        }

        // popup
        LayoutInflater inflate = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflate.inflate(
                R.layout.popup_notification_window, null);
        tvPopupMsg = (MyFontTextView) layout.findViewById(R.id.tvPopupMsg);
        registerInfoPopup = new PopupWindow(layout, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        layout.setOnClickListener(this);
        registerInfoPopup.setBackgroundDrawable(new BitmapDrawable());
        registerInfoPopup.setOutsideTouchable(true);
        aQuery = new AQuery(this);
        disableViews();
        imageOptions = new ImageOptions();
        imageOptions.memCache = true;
        imageOptions.fileCache = true;
        imageOptions.targetWidth = 200;
        imageOptions.fallback = R.drawable.user;
        setData();

        etProfileFname.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                tvProfileFName.setText(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etProfileLName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                tvProfileLName.setText(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void disableViews() {
        etProfileFname.setEnabled(false);
        etProfileLName.setEnabled(false);
        etProfileEmail.setEnabled(false);
        etProfileNumber.setEnabled(false);
        // etProfileBio.setEnabled(false);
        etProfileAddress.setEnabled(false);
        etProfileZipcode.setEnabled(false);
        etProfileCurrentPassword.setEnabled(false);
        etProfileNewPassword.setEnabled(false);
        etProfileRetypePassword.setEnabled(false);
        etProfileModel.setEnabled(false);
        etProfileVehicleNo.setEnabled(false);
        ivProfile.setEnabled(false);
    }

    private void enableViews() {
        etProfileFname.setEnabled(true);
        etProfileLName.setEnabled(true);
        etProfileNumber.setEnabled(true);
        // etProfileBio.setEnabled(true);
        etProfileAddress.setEnabled(true);
        etProfileZipcode.setEnabled(true);
        etProfileCurrentPassword.setEnabled(true);
        etProfileNewPassword.setEnabled(true);
        etProfileRetypePassword.setEnabled(true);
        ivProfile.setEnabled(true);
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

    private void setData() {
        dbHelper = new DBHelper(getApplicationContext());
        final User user = dbHelper.getUser();

        aQuery.id(ivProfile).progress(R.id.pBar)
                .image(user.getPicture(), imageOptions);
        tvProfileFName.setText(user.getFname());
        tvProfileLName.setText(user.getLname());
        etProfileFname.setText(user.getFname());
        etProfileLName.setText(user.getLname());
        etProfileEmail.setText(user.getEmail());
        etProfileNumber.setText(user.getContact());
        // etProfileBio.setText(user.getBio());
        etProfileAddress.setText(user.getAddress());
        etProfileZipcode.setText(user.getZipcode());
        etProfileModel.setText(user.getCarModel());
        etProfileVehicleNo.setText(user.getCarNumber());
        if (user != null && !TextUtils.isEmpty(user.getPicture())) {
            aQuery.id(R.id.ivProfileProfile).image(user.getPicture(), true,
                    true, 200, 0, new BitmapAjaxCallback() {
                        @Override
                        public void callback(String url, ImageView iv,
                                             Bitmap bm, AjaxStatus status) {
                            AppLog.Log(TAG, "URL FROM AQUERY::" + url);
                            File file = aQuery.getCachedFile(user.getPicture());
                            if (!TextUtils.isEmpty(file.getAbsolutePath())
                                    && file != null) {
                                profileImageData = file.getAbsolutePath();
                                AppLog.Log(TAG, "URL path FROM AQUERY::" + url);
                                iv.setImageBitmap(bm);
                            }
                        }
                    });
        }
    }

    private void onUpdateButtonClick() {
        if (etProfileFname.getText().length() == 0) {
            AndyUtils.showToast(
                    getResources().getString(R.string.error_empty_fname), this);
            return;
        } else if (etProfileLName.getText().length() == 0) {
            AndyUtils.showToast(
                    getResources().getString(R.string.error_empty_lname), this);
            return;
        } else if (etProfileEmail.getText().length() == 0) {
            AndyUtils.showToast(
                    getResources().getString(R.string.error_empty_email), this);
            return;
        } else if (!AndyUtils.eMailValidation(etProfileEmail.getText()
                .toString())) {
            AndyUtils.showToast(
                    getResources().getString(R.string.error_valid_email), this);
            return;

        } else if (etProfileCurrentPassword.getVisibility() == View.VISIBLE) {
            if (!TextUtils.isEmpty(etProfileNewPassword.getText())) {
                if (TextUtils.isEmpty(etProfileCurrentPassword.getText())) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_empty_password), this);
                    return;
                } else if (etProfileCurrentPassword.getText().length() < 6) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_valid_password), this);
                    return;
                } else if (TextUtils.isEmpty(etProfileRetypePassword.getText())) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_empty_retypepassword), this);
                    return;
                } else if (!etProfileRetypePassword.getText().toString()
                        .equals(etProfileNewPassword.getText().toString())) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_mismatch_password), this);
                    return;
                }
            } else if (etProfileCurrentPassword.getVisibility() == View.INVISIBLE) {
                etProfileRetypePassword.setVisibility(View.INVISIBLE);
                etProfileRetypePassword.setVisibility(View.INVISIBLE);
            } else if (!TextUtils.isEmpty(etProfileCurrentPassword.getText())) {
                if (TextUtils.isEmpty(etProfileNewPassword.getText())) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_empty_newpassword), this);
                    return;
                } else if (etProfileNewPassword.getText().length() < 6) {
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.error_valid_password), this);
                    return;
                }
            }
        }

        if (etProfileNumber.getText().length() == 0) {
            AndyUtils
                    .showToast(
                            getResources().getString(
                                    R.string.error_empty_number), this);
            return;
        }
        // else if (profileImageData == null || profileImageData.equals("")) {
        // AndyUtils.showToast(
        // getResources().getString(R.string.error_empty_image), this);
        // return;
        // }
        else {
            updateSimpleProfile(loginType);
        }
    }

    private void updateSimpleProfile(String type) {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.toast_no_internet), this);
            return;
        }

        AndyUtils.showCustomProgressDialog(this,
                getResources().getString(R.string.progress_update_profile),
                false);

        if (type.equals(AndyConstants.MANUAL)) {
            AppLog.Log(TAG, "Simple Profile update method");
            HashMap<String, String> map = new HashMap<>();
            map.put(AndyConstants.URL, AndyConstants.ServiceType.UPDATE_PROFILE);
            map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
            map.put(AndyConstants.Params.TOKEN,
                    preferenceHelper.getSessionToken());
            map.put(AndyConstants.Params.FIRSTNAME, etProfileFname.getText()
                    .toString());
            map.put(AndyConstants.Params.LAST_NAME, etProfileLName.getText()
                    .toString());
            map.put(AndyConstants.Params.EMAIL, etProfileEmail.getText()
                    .toString());
            map.put(AndyConstants.Params.OLD_PASSWORD, etProfileCurrentPassword
                    .getText().toString());
            map.put(AndyConstants.Params.NEW_PASSWORD, etProfileNewPassword
                    .getText().toString());
            map.put(AndyConstants.Params.PICTURE, profileImageData);
            map.put(AndyConstants.Params.PHONE, etProfileNumber.getText()
                    .toString());
            // map.put(AndyConstants.Params.BIO,
            // etProfileBio.getText().toString());
            map.put(AndyConstants.Params.ADDRESS, etProfileAddress.getText()
                    .toString());
            map.put(AndyConstants.Params.STATE, "");
            map.put(AndyConstants.Params.COUNTRY, "");
            map.put(AndyConstants.Params.ZIPCODE, etProfileZipcode.getText()
                    .toString().trim());
            new MultiPartRequester(this, map,
                    AndyConstants.ServiceCode.UPDATE_PROFILE, this);
        } else {
            updateSocialProfile(type);
        }
    }

    private void updateSocialProfile(String loginType) {
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.UPDATE_PROFILE);
        map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
        map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
        map.put(AndyConstants.Params.FIRSTNAME, etProfileFname.getText()
                .toString());
        map.put(AndyConstants.Params.LAST_NAME, etProfileLName.getText()
                .toString());
        map.put(AndyConstants.Params.ADDRESS, etProfileAddress.getText()
                .toString());
        map.put(AndyConstants.Params.EMAIL, etProfileEmail.getText().toString());
        map.put(AndyConstants.Params.PHONE, etProfileNumber.getText()
                .toString());
        map.put(AndyConstants.Params.PICTURE, profileImageData);
        map.put(AndyConstants.Params.STATE, "");
        map.put(AndyConstants.Params.COUNTRY, "");
        // map.put(AndyConstants.Params.BIO, etProfileBio.getText().toString());
        map.put(AndyConstants.Params.ZIPCODE, etProfileZipcode.getText()
                .toString().trim());
        new MultiPartRequester(this, map,
                AndyConstants.ServiceCode.UPDATE_PROFILE, this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // case R.id.tvProfileSubmit:
            // if (tvProfileSubmit
            // .getText()
            // .toString()
            // .equals(getResources()
            // .getString(R.string.text_edit_profile))) {
            // enableViews();
            // etProfileFname.requestFocus();
            // tvProfileSubmit.setText(getResources().getString(
            // R.string.text_update_profile));
            // } else {
            // onUpdateButtonClick();
            // }
            // break;
            case R.id.ivProfileProfile:
                showPictureDialog();
                break;

            case R.id.btnActionNotification:
                btnEditProfile.setVisibility(View.VISIBLE);
                btnNotification.setVisibility(View.GONE);
                enableViews();
                etProfileFname.requestFocus();
                break;

            case R.id.btnEditProfile:
                onUpdateButtonClick();
                break;

            case R.id.btnActionMenu:
                onBackPressed();
                break;

//            case R.id.btnProfileEmailInfo:
//                openPopup(btnProfileEmailInfo,
//                        getString(R.string.text_profile_popup));
//                break;
            //
            // case R.id.btnProfileModelInfo:
            // openPopup(btnProfileModelInfo,
            // getString(R.string.text_profile_popup));
            // break;
            //
            // case R.id.btnProfileVehicleNoInfo:
            // openPopup(btnProfileVehicleNoInfo,
            // getString(R.string.text_profile_popup));
            // break;
            default:
                break;
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

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(getResources().getString(
                R.string.dialog_chhose_photo));
        String[] pictureDialogItems = {
                getResources().getString(R.string.dialog_from_gallery),
                getResources().getString(R.string.dialog_from_camera)};

        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {

                            case 0:
                                choosePhotoFromGallary();
                                break;

                            case 1:
                                takePhotoFromCamera();
                                break;

                        }
                    }
                });
        pictureDialog.show();
    }

    @SuppressWarnings("deprecation")
    private void choosePhotoFromGallary() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, AndyConstants.PERMISSION_STORAGE_REQUEST_CODE);
        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(galleryIntent, AndyConstants.CHOOSE_PHOTO);
        }
    }

    @SuppressWarnings("deprecation")
    private void takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, AndyConstants.PERMISSION_CAMERA_REQUEST_CODE);
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
            startActivityForResult(cameraIntent, AndyConstants.TAKE_PHOTO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case AndyConstants.PERMISSION_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        choosePhotoFromGallary();
                    }
                }
                break;
            case AndyConstants.PERMISSION_CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        takePhotoFromCamera();
                    }
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AndyConstants.CHOOSE_PHOTO) {
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
                                getContentResolver(), photoBitmap, Calendar
                                        .getInstance().getTimeInMillis()
                                        + ".jpg", null);

                        beginCrop(Uri.parse(path));

                    }
                } catch (OutOfMemoryError e) {
                    System.out.println("out of bound");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.toast_unable_to_selct_image),
                        Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == AndyConstants.TAKE_PHOTO) {
            if (uri != null) {

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
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
                            rotationAngle = 90;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                            rotationAngle = 180;
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                            rotationAngle = 270;

                        System.out.println("Rotation : " + rotationAngle);

                        options.inJustDecodeBounds = false;
                        options.inSampleSize = ratio;

                        Bitmap bmp = BitmapFactory.decodeFile(imageFilePath,
                                options);
                        File myFile = new File(imageFilePath);
                        // bmp = new ImageHelper().decodeFile(myFile);
                        FileOutputStream outStream = new FileOutputStream(
                                myFile);

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
                                getContentResolver(), bmp, Calendar
                                        .getInstance().getTimeInMillis()
                                        + ".jpg", null);
                        beginCrop(Uri.parse(path));

                        // AQuery aQuery = new AQuery(this);
                        // aQuery.id(ivProfile).image(bmp);
                        //
                        profileImageData = imageFilePath;
                        // rlDescription.setVisibility(View.VISIBLE);
                        // llPicture.setVisibility(View.GONE);
                    } catch (OutOfMemoryError e) {
                        System.out.println("out of bound");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.toast_unable_to_selct_image),
                        Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == Crop.REQUEST_CROP) {
            AppLog.Log(TAG, "Crop photo on activity result");
            handleCrop(resultCode, data);
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null,
                null, null);

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
                                getResources().getString(
                                        R.string.text_error_get_image), this);
                result = "";
            }
            cursor.close();
        }
        return result;
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        AndyUtils.removeCustomProgressDialog();
        AppLog.Log(TAG, response);
        switch (serviceCode) {
            case AndyConstants.ServiceCode.UPDATE_PROFILE:
                if (!parseContent.isSuccess(response)) {
                    return;
                }
                if (parseContent.isSuccessWithId(response)) {
                    AndyUtils.removeCustomProgressDialog();
                    AndyUtils.showToast(
                            getResources().getString(
                                    R.string.toast_update_profile_success), this);
                    new DBHelper(this).deleteUser();
                    parseContent.parseUserAndStoreToDb(response);
                    new PreferenceHelper(this).putPassword(etProfileCurrentPassword
                            .getText().toString());
                    btnEditProfile.setVisibility(View.GONE);
                    btnNotification.setVisibility(View.VISIBLE);
                    onBackPressed();
                }

                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AndyUtils.removeCustomProgressDialog();
    }

    private void beginCrop(Uri source) {
        // Uri outputUri = Uri.fromFile(new File(registerActivity.getCacheDir(),
        // "cropped"));
        Uri outputUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(), (Calendar.getInstance()
                .getTimeInMillis() + ".jpg")));
        new Crop(source).output(outputUri).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            AppLog.Log(TAG, "Handle crop");
            profileImageData = getRealPathFromURI(Crop.getOutput(result));
            ivProfile.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
