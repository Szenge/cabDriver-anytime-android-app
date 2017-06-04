package com.virtugos.uberapp.driver.base;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.virtugos.uberapp.driver.MainActivity;
import com.virtugos.uberapp.driver.R;
import com.virtugos.uberapp.driver.locationupdate.LocationUpdateService;
import com.virtugos.uberapp.driver.parse.AsyncTaskCompleteListener;
import com.virtugos.uberapp.driver.utills.AndyUtils;
import com.virtugos.uberapp.driver.widget.MyFontButton;

import java.text.DecimalFormat;
import java.util.Calendar;

@SuppressLint("NewApi")
abstract public class ActionBarBaseActivitiy extends AppCompatActivity
        implements OnClickListener, AsyncTaskCompleteListener {

    protected ActionBar actionBar;
    private int mFragmentId = 0;
    private String mFragmentTag = null, basePriceDouble, distCostTmp, totalTmp,
            timeCostDouble, referralBonusDouble, promoBonusDouble;
    public ImageButton btnNotification, btnActionMenu, btnEditProfile;
    public TextView tvTitle, tvPaymentStatus;
    public MyFontButton btnActionInfo;
    public String currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SaveLog.getInstance(this);

        actionBar = getSupportActionBar();
        // setStatusBarColor(getResources().getColor(R.color.color_action_bar));

        // Custom Action Bar
        LayoutInflater inflater = (LayoutInflater) actionBar.getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.custom_action_bar,
                null);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        btnNotification = (ImageButton) customActionBarView
                .findViewById(R.id.btnActionNotification);
        btnNotification.setOnClickListener(this);

        tvTitle = (TextView) customActionBarView.findViewById(R.id.tvTitle);
        tvTitle.setOnClickListener(this);

//        btnActionInfo = (MyFontButton) customActionBarView
//                .findViewById(R.id.btnActionInfo);
//        btnActionInfo.setOnClickListener(this);

        btnActionMenu = (ImageButton) customActionBarView
                .findViewById(R.id.btnActionMenu);
        btnActionMenu.setOnClickListener(this);

        btnEditProfile = (ImageButton) customActionBarView
                .findViewById(R.id.btnEditProfile);

        tvPaymentStatus = (TextView) customActionBarView
                .findViewById(R.id.tvPaymentStatus);

        try {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                            | ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView,
                    new ActionBar.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            Toolbar parent = (Toolbar) customActionBarView.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }

    public void setFbTag(String tag) {
        mFragmentId = 0;
        mFragmentTag = tag;
    }

    public void showBillDialog(String basePrice, String total,
                               String distanceCost, String timeCost, String distance, String time,
                               String referralBonus, String promoBonus, String pricePerDistance,
                               String pricePerTime, String distanceUnit, int i, String currency) {
        final Dialog mDialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(R.layout.bill_layout);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        DecimalFormat perHourFormat = new DecimalFormat("0.0");

        try {
            basePriceDouble = String.valueOf(decimalFormat.format(Double
                    .parseDouble(basePrice)));
            totalTmp = String.valueOf(decimalFormat.format(Double
                    .parseDouble(total)));
            distCostTmp = String.valueOf(decimalFormat.format(Double
                    .parseDouble(distanceCost)));
            timeCostDouble = String.valueOf(decimalFormat.format(Double
                    .parseDouble(timeCost)));
            referralBonusDouble = String.valueOf(decimalFormat.format(Double
                    .parseDouble(referralBonus)));
            promoBonusDouble = String.valueOf(decimalFormat.format(Double
                    .parseDouble(promoBonus)));
        } catch (NumberFormatException e) {
            basePrice = AndyUtils.comaReplaceWithDot(basePrice);
            total = AndyUtils.comaReplaceWithDot(total);
            distanceCost = AndyUtils.comaReplaceWithDot(distanceCost);
            timeCost = AndyUtils.comaReplaceWithDot(timeCost);
            referralBonus = AndyUtils.comaReplaceWithDot(referralBonus);
            promoBonus = AndyUtils.comaReplaceWithDot(promoBonus);

            basePriceDouble = String.valueOf(decimalFormat.format(Double
                    .parseDouble(basePrice)));
            totalTmp = String.valueOf(decimalFormat.format(Double
                    .parseDouble(total)));
            distCostTmp = String.valueOf(decimalFormat.format(Double
                    .parseDouble(distanceCost)));
            timeCostDouble = String.valueOf(decimalFormat.format(Double
                    .parseDouble(timeCost)));
            referralBonusDouble = String.valueOf(decimalFormat.format(Double
                    .parseDouble(referralBonus)));
            promoBonusDouble = String.valueOf(decimalFormat.format(Double
                    .parseDouble(promoBonus)));
        }
        ((TextView) mDialog.findViewById(R.id.tvBasePrice))
                .setText(currency+basePriceDouble);

        // if (Double.parseDouble(distanceCost) != 0) {
        // ((TextView) mDialog.findViewById(R.id.tvBillDistancePerMile))
        // .setText(String.valueOf(perHourFormat.format((Double
        // .parseDouble(distanceCost) / Double
        // .parseDouble(distance))))
        // + getResources().getString(
        // R.string.text_cost_per_mile));
        // } else {
        // ((TextView) mDialog.findViewById(R.id.tvBillDistancePerMile))
        // .setText(String.valueOf(perHourFormat.format(0.00))
        // + getResources().getString(
        // R.string.text_cost_per_mile));
        // }

        ((TextView) mDialog.findViewById(R.id.tvBillDistancePerMile))
                .setText(currency
                        + pricePerDistance + " "
                        + getResources().getString(R.string.text_cost_per_mile)
                        + " " + distanceUnit);

        ((TextView) mDialog.findViewById(R.id.tvBillTimePerHour))
                .setText(currency
                        + pricePerTime + " "
                        + getResources().getString(R.string.text_cost_per_hour));
        TextView pt = (TextView) mDialog.findViewById(R.id.payment_type);
        if (i == 1) {
            pt.setText(getString(R.string.text_payment_mode_cash));
        } else {
            pt.setText(getString(R.string.text_payment_mode_card));
        }
        // if (Double.parseDouble(timeCost) != 0) {
        // ((TextView) mDialog.findViewById(R.id.tvBillTimePerHour))
        // .setText(String.valueOf(perHourFormat.format((Double
        // .parseDouble(timeCost) / Double.parseDouble(time))))
        // + getResources().getString(
        // R.string.text_cost_per_hour));
        // } else {
        // ((TextView) mDialog.findViewById(R.id.tvBillTimePerHour))
        // .setText(String.valueOf(perHourFormat.format((0.00)))
        // + getResources().getString(
        // R.string.text_cost_per_hour));
        // }

        ((TextView) mDialog.findViewById(R.id.tvDis1)).setText(currency+distCostTmp);

        ((TextView) mDialog.findViewById(R.id.tvTime1)).setText(currency+timeCostDouble);

        ((TextView) mDialog.findViewById(R.id.tvTotal1)).setText(currency+totalTmp);

        ((TextView) mDialog.findViewById(R.id.tvReferralBonus))
                .setText(currency+referralBonusDouble);

        ((TextView) mDialog.findViewById(R.id.tvPromoBonus))
                .setText(currency+promoBonusDouble);

        Button btnConfirm = (Button) mDialog
                .findViewById(R.id.btnBillDialogClose);

        btnConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mDialog.setCancelable(true);
        mDialog.show();

    }
    public void startLocationUpdateService() {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        Intent intent = new Intent(this, LocationUpdateService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                1000 * 5, pintent);

    }

    // Change by Elluminati elluminati.in//
    protected void stopLocationUpdateService() {

        Intent intent = new Intent(this, LocationUpdateService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = null;

        if (mFragmentId > 0) {
            fragment = getSupportFragmentManager()
                    .findFragmentById(mFragmentId);
        } else if (mFragmentTag != null && !mFragmentTag.equalsIgnoreCase("")) {
            fragment = getSupportFragmentManager().findFragmentByTag(
                    mFragmentTag);
        }

        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void startActivityForResult(Intent intent, int requestCode,
                                       int fragmentId) {
        mFragmentId = fragmentId;
        mFragmentTag = null;
        super.startActivityForResult(intent, requestCode);
    }

    public void startActivityForResult(Intent intent, int requestCode,
                                       String fragmentTag) {
        mFragmentTag = fragmentTag;
        mFragmentId = 0;
        super.startActivityForResult(intent, requestCode);
    }

    public void setActionBarTitle(String title) {
        tvTitle.setText(title);
    }

    public void setActionBarIcon(int image) {
        btnActionMenu.setImageResource(image);
    }

    public void setIcon(int img) {
        btnNotification.setImageResource(img);
    }

    public void startActivityForResult(Intent intent, int requestCode,
                                       int fragmentId, Bundle options) {
        mFragmentId = fragmentId;
        mFragmentTag = null;
        super.startActivityForResult(intent, requestCode, options);
    }

    public void startActivityForResult(Intent intent, int requestCode,
                                       String fragmentTag, Bundle options) {
        mFragmentTag = fragmentTag;
        mFragmentId = 0;
        super.startActivityForResult(intent, requestCode, options);
    }

    public void startIntentSenderForResult(Intent intent, int requestCode,
                                           String fragmentTag, Bundle options) {
        mFragmentTag = fragmentTag;
        mFragmentId = 0;
        super.startActivityForResult(intent, requestCode, options);
    }

    @Override
    @Deprecated
    public void startIntentSenderForResult(IntentSender intent,
                                           int requestCode, Intent fillInIntent, int flagsMask,
                                           int flagsValues, int extraFlags) throws
            SendIntentException {
        super.startIntentSenderForResult(intent, requestCode, fillInIntent,
                flagsMask, flagsValues, extraFlags);
    }

    public void startIntentSenderForResult(IntentSender intent,
                                           int requestCode, Intent fillInIntent, int flagsMask,
                                           int flagsValues, int extraFlags, String fragmentTag)
            throws SendIntentException {
        mFragmentTag = fragmentTag;
        mFragmentId = 0;
        super.startIntentSenderForResult(intent, requestCode, fillInIntent,
                flagsMask, flagsValues, extraFlags);
    }

    @Override
    @Deprecated
    public void startIntentSenderForResult(IntentSender intent,
                                           int requestCode, Intent fillInIntent, int flagsMask,
                                           int flagsValues, int extraFlags, Bundle options)
            throws SendIntentException {
        super.startIntentSenderForResult(intent, requestCode, fillInIntent,
                flagsMask, flagsValues, extraFlags, options);
    }

    public void startIntentSenderForResult(IntentSender intent,
                                           int requestCode, Intent fillInIntent, int flagsMask,
                                           int flagsValues, int extraFlags, Bundle options, String fragmentTag)
            throws SendIntentException {
        mFragmentTag = fragmentTag;
        mFragmentId = 0;
        super.startIntentSenderForResult(intent, requestCode, fillInIntent,
                flagsMask, flagsValues, extraFlags, options);
    }

    @Override
    @Deprecated
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    @Deprecated
    public void startActivityForResult(Intent intent, int requestCode,
                                       Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

    }

    public void addFragment(Fragment fragment, boolean addToBackStack,
                            String tag, boolean isAnimate) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (isAnimate) {
            ft.setCustomAnimations(R.anim.slide_in_right,
                    R.anim.slide_out_left, R.anim.slide_in_left,
                    R.anim.slide_out_right);
        }

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(R.id.content_frame, fragment, tag);
        ft.commitAllowingStateLoss();
    }

    public void removeAllFragment(Fragment replaceFragment,
                                  boolean addToBackStack, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        manager.popBackStackImmediate(null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.replace(R.id.content_frame, replaceFragment);
        ft.commit();

    }

    protected void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    public void clearAll() {
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

}
