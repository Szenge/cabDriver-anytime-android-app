package com.virtugos.uberapp.driver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.virtugos.uberapp.driver.adapter.DrawerAdapter;
import com.virtugos.uberapp.driver.base.ActionBarBaseActivitiy;
import com.virtugos.uberapp.driver.db.DBHelper;
import com.virtugos.uberapp.driver.fragment.ClientRequestFragment;
import com.virtugos.uberapp.driver.fragment.FeedbackFrament;
import com.virtugos.uberapp.driver.fragment.JobFragment;
import com.virtugos.uberapp.driver.model.ApplicationPages;
import com.virtugos.uberapp.driver.model.RequestDetail;
import com.virtugos.uberapp.driver.model.User;
import com.virtugos.uberapp.driver.parse.AsyncTaskCompleteListener;
import com.virtugos.uberapp.driver.parse.HttpRequester;
import com.virtugos.uberapp.driver.parse.ParseContent;
import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AndyUtils;
import com.virtugos.uberapp.driver.utills.AppLog;
import com.virtugos.uberapp.driver.utills.PreferenceHelper;
import com.virtugos.uberapp.driver.widget.MyFontTextView;
import com.virtugos.uberapp.driver.widget.MyFontTextViewDrawer;
import com.splunk.mint.Mint;

import java.util.ArrayList;
import java.util.HashMap;

public class MapActivity extends ActionBarBaseActivitiy implements
        OnItemClickListener, AsyncTaskCompleteListener {

    // Drawer Initialization
    private DrawerLayout drawerLayout;
    private DrawerAdapter adapter;
    private ListView drawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private MyFontTextView tvLogoutOk, tvLogoutCancel, tvExitOk, tvExitCancel,
            tvApprovedClose;

    private PreferenceHelper preferenceHelper;
    public ParseContent parseContent;
    private static final String TAG = "MapActivity";
    private ArrayList<ApplicationPages> arrayListApplicationPages;
    private boolean isRecieverRegistered = false,
            isNetDialogShowing = false, isGpsDialogShowing = false;
    private AlertDialog internetDialog, gpsAlertDialog;
    private LocationManager manager;
    private DBHelper dbHelper;
    private AQuery aQuery;
    private ImageOptions imageOptions;
    private ImageView ivMenuProfile, ivSound;
    private MyFontTextView tvSound;
    private MyFontTextViewDrawer tvMenuName;
    private boolean isLogoutCheck = true, isApprovedCheck = true;
    private BroadcastReceiver mReceiver;
    public Dialog mDialog;
    private View headerView, footerView;
    private View drawerFooterView;
    private String clientNumber;
    private TextView tvfooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(this, "0b40c895");

        AndyConstants.isTokenExpired = false;
        /*if(!AndyConstants.isShowNoteDialog) {
            AndyConstants.isShowNoteDialog = true;
            AndyUtils.showNoteDialog(this);
        }*/
        // Mint.initAndStartSession(MapActivity.this, "fdd1b971");
        actionBar.show();
        setContentView(R.layout.activity_main);
        preferenceHelper = new PreferenceHelper(this);
        // mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        // mMenuDrawer.setContentView(R.layout.activity_map);
        // mMenuDrawer.setMenuView(R.layout.menu_drawer);
        // mMenuDrawer.setDropShadowEnabled(false);
        arrayListApplicationPages = new ArrayList<ApplicationPages>();
        parseContent = new ParseContent(this);
        // mTitle = mDrawerTitle = getTitle();

        // drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
        // GravityCompat.START);
        btnActionMenu.setVisibility(View.VISIBLE);
        btnActionMenu.setOnClickListener(this);
        tvTitle.setOnClickListener(this);
        btnNotification.setVisibility(View.GONE);
        setActionBarIcon(R.drawable.menu);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeButtonEnabled(true);

        // mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
        // R.drawable.slide_btn, R.string.drawer_open,
        // R.string.drawer_close) {
        //
        // public void onDrawerClosed(View view) {
        // getSupportActionBar().setTitle(mTitle);
        // // supportInvalidateOptionsMenu(); // creates call to
        // // onPrepareOptionsMenu()
        // }
        //
        // public void onDrawerOpened(View drawerView) {
        // getSupportActionBar().setTitle(mDrawerTitle);
        // supportInvalidateOptionsMenu();
        // }
        // };
        // drawerLayout.setDrawerListener(mDrawerToggle);

        moveDrawerToTop();
        initActionBar();
        initDrawer();



        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        aQuery = new AQuery(this);
        imageOptions = new ImageOptions();
        imageOptions.memCache = true;
        imageOptions.fileCache = true;
        imageOptions.targetWidth = 200;
        imageOptions.fallback = R.drawable.user;

        dbHelper = new DBHelper(getApplicationContext());
        registerIsApproved();
        // if (savedInstanceState == null) {
        // selectItem(-1);
        // }
        if (preferenceHelper.getIsApproved() != null
                && preferenceHelper.getIsApproved().equals("1")) {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }

        if (AndyUtils.isNetworkAvailable(this)
                && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("isNetworkAvailable","********************isNetworkAvailable***********");
            requestRequiredPermission();
        }
    }

    public void initDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerLayout.setDrawerListener(createDrawerToggle());
        drawerList.setOnItemClickListener(this);
        adapter = new DrawerAdapter(this, arrayListApplicationPages);
        headerView = getLayoutInflater().inflate(R.layout.menu_drawer, null);
        footerView = getLayoutInflater().inflate(R.layout.drawer_footer_view,
                null);

        drawerList.addHeaderView(headerView);
        drawerList.setAdapter(adapter);
        drawerList.addFooterView(footerView, null, true);

        ivMenuProfile = (ImageView) headerView.findViewById(R.id.ivMenuProfile);
        tvMenuName = (MyFontTextViewDrawer) headerView
                .findViewById(R.id.tvMenuName);
        tvSound = (MyFontTextView) footerView.findViewById(R.id.tvSound);
        ivSound = (ImageView) footerView.findViewById(R.id.ivSound);
        tvSound.setText(getString(R.string.text_sound_on));
        ivSound.setSelected(true);
        footerView.setOnClickListener(this);
        tvSound.setOnClickListener(this);
        ivSound.setOnClickListener(this);

    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        // actionBar.setDisplayHomeAsUpEnabled(true);
        // actionBar.setHomeButtonEnabled(true);
    }

    private DrawerListener createDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.menu, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int state) {
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
//                drawerLayout.openDrawer(drawerList);
                Display display =getWindowManager().getDefaultDisplay();

                if(display.getHeight()>getTotalHeightofListView(drawerList)){
                    drawerFooterView = getLayoutInflater().inflate(R.layout.menu_footer, null);

                    tvfooter = (TextView) drawerFooterView.findViewById(R.id.tvfooter);
                    ViewGroup.LayoutParams params = tvfooter.getLayoutParams();
                    params.height =display.getHeight()-(getTotalHeightofListView(drawerList));
                    if(params.height>0){
                        drawerList.addFooterView(drawerFooterView);
                        tvfooter.setLayoutParams(params);
                        tvfooter.requestLayout();
                    }
                }
            }
        };
        return mDrawerToggle;
    }

    private void moveDrawerToTop() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        DrawerLayout drawer = (DrawerLayout) inflater.inflate(
                R.layout.activity_map, null); // "null" is important.

        // HACK: "steal" the first child of decor view
        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        View child = decor.getChildAt(0);
        decor.removeView(child);
        LinearLayout container = (LinearLayout) drawer
                .findViewById(R.id.llContent); // This is the container we
        // defined just now.
        container.addView(child, 0);
        drawer.findViewById(R.id.left_drawer).setPadding(0,
                (actionBar.getHeight() + getStatusBarHeight()), 0, 0);

        // Make the drawer replace the first child
        decor.addView(drawer);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // MenuInflater inflater = getMenuInflater();
        // inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if (mDrawerToggle.onOptionsItemSelected(item)) {
        // return true;
        // }
        return super.onOptionsItemSelected(item);
    }

    public void checkStatus() {
        if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {
            AppLog.Log(TAG, "onResume getreuest in progress");
            getRequestsInProgress();
        } else {
            AppLog.Log(TAG, "onResume check request status");
            checkRequestStatus();
        }
    }

    private void getMenuItems() {
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.APPLICATION_PAGES);
        new HttpRequester(this, map,
                AndyConstants.ServiceCode.APPLICATION_PAGES, true, this);

        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // AndyConstants.ServiceCode.APPLICATION_PAGES, this, this));
    }

    public BroadcastReceiver GpsChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppLog.Log(TAG, "On recieve GPS provider broadcast");
            final LocationManager manager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // do something
                removeGpsDialog();
            } else {
                // do something else
                if (isGpsDialogShowing) {
                    return;
                }
                ShowGpsDialog();
            }

        }
    };
    public BroadcastReceiver internetConnectionReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo activeWIFIInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (activeWIFIInfo.isConnected() || activeNetInfo.isConnected()) {
                removeInternetDialog();
            } else {
                if (isNetDialogShowing) {
                    return;
                }
                showInternetDialog();
            }
        }
    };

    private void registerIsApproved() {
        IntentFilter intentFilter = new IntentFilter("IS_APPROVED");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppLog.Log("MapFragment", "IS_APPROVED");
                if (preferenceHelper.getIsApproved() != null
                        && preferenceHelper.getIsApproved().equals("1")) {
                    // startActivity(new Intent(MapActivity.this,
                    // MapActivity.class));
                    // mDialog.dismiss();
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                        getRequestsInProgress();
                    }
                }
            }
        };
        registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterIsApproved() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private void ShowGpsDialog() {
        AndyUtils.removeCustomProgressDialog();
        isGpsDialogShowing = true;
        AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(
                MapActivity.this);
        gpsBuilder.setCancelable(false);
        gpsBuilder
                .setTitle(getString(R.string.dialog_no_gps))
                .setMessage(getString(R.string.dialog_no_gps_messgae))
                .setPositiveButton(getString(R.string.dialog_enable_gps),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // continue with delete
                                Intent intent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                                removeGpsDialog();
                            }
                        })

                .setNegativeButton(getString(R.string.dialog_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // do nothing
                                removeGpsDialog();
                                finish();
                            }
                        });
        gpsAlertDialog = gpsBuilder.create();
        gpsAlertDialog.show();
    }

    private void removeGpsDialog() {
        if (gpsAlertDialog != null && gpsAlertDialog.isShowing()) {
            gpsAlertDialog.dismiss();
            isGpsDialogShowing = false;
            gpsAlertDialog = null;
        }
    }

    private void removeInternetDialog() {
        if (internetDialog != null && internetDialog.isShowing()) {
            internetDialog.dismiss();
            isNetDialogShowing = false;
            internetDialog = null;

        }
    }

    private void showInternetDialog() {
        AndyUtils.removeCustomProgressDialog();
        isNetDialogShowing = true;
        AlertDialog.Builder internetBuilder = new AlertDialog.Builder(
                MapActivity.this);
        internetBuilder.setCancelable(false);
        internetBuilder
                .setTitle(getString(R.string.dialog_no_internet))
                .setMessage(getString(R.string.dialog_no_inter_message))
                .setPositiveButton(getString(R.string.dialog_enable_3g),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // continue with delete
                                Intent intent = new Intent(
                                        android.provider.Settings.ACTION_SETTINGS);
                                startActivity(intent);
                                removeInternetDialog();
                            }
                        })
                .setNeutralButton(getString(R.string.dialog_enable_wifi),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // User pressed Cancel button. Write
                                // Logic Here
                                startActivity(new Intent(
                                        Settings.ACTION_WIFI_SETTINGS));
                                removeInternetDialog();
                            }
                        })
                .setNegativeButton(getString(R.string.dialog_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // do nothing
                                removeInternetDialog();
                                finish();
                            }
                        });
        internetDialog = internetBuilder.create();
        internetDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ShowGpsDialog();
        } else {
            removeGpsDialog();
        }
        registerReceiver(internetConnectionReciever, new IntentFilter(
                "android.net.conn.CONNECTIVITY_CHANGE"));
        registerReceiver(GpsChangeReceiver, new IntentFilter(
                LocationManager.PROVIDERS_CHANGED_ACTION));
        isRecieverRegistered = true;

        User user = dbHelper.getUser();
        if (user != null) {
            aQuery.id(ivMenuProfile).progress(R.id.pBar)
                    .image(user.getPicture(), imageOptions);
            tvMenuName.setText(user.getFname() + " " + user.getLname());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mint.closeSession(this);
        AndyUtils.removeCustomProgressDialog();
        // Mint.closeSession(this);
        if (isRecieverRegistered) {
            unregisterReceiver(internetConnectionReciever);
            unregisterReceiver(GpsChangeReceiver);

        }
        unregisterIsApproved();

    }

    // @Override
    // protected void onPause() {
    // super.onPause();
    // unregisterIsApproved();
    // }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
                            long arg3) {
        // AndyUtils.showToast("Postion :" + arg2, this);
        if (position == 0) {
            return;
        }
        drawerLayout.closeDrawer(drawerList);
        // mMenuDrawer.closeMenu();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (position == 1) {
                    startActivity(new Intent(MapActivity.this,
                            ProfileActivity.class));
                } else if (position == 2) {
                    startActivity(new Intent(MapActivity.this,
                            HistoryActivity.class));
                    // } else if (position == 3) {
                    // startActivity(new Intent(MapActivity.this,
                    // SettingActivity.class));
                } else if (position == 3) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent
                            .putExtra(
                                    Intent.EXTRA_TEXT,
                                    getString(R.string.text_i_am_using)
                                            + getString(R.string.app_name)
                                            + getString(R.string.text_try_app)
                                            + getString(R.string.app_name)
                                            + getString(R.string.text_now)
                                            + "https://play.google.com/store/apps/details?id="
                                            + getPackageName());
                    sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                            getString(R.string.app_name) + " App !");
                    sendIntent.setType("text/plain");

                    startActivity(Intent.createChooser(sendIntent,
                            getString(R.string.text_share_app)));
                } else if (position == (arrayListApplicationPages.size())) {
                    if (isLogoutCheck) {
                        openLogoutDialog();
                        isLogoutCheck = false;
                        return;
                    }

                    // new AlertDialog.Builder(this)
                    // .setTitle(getString(R.string.dialog_logout))
                    // .setMessage(getString(R.string.dialog_logout_text))
                    // .setPositiveButton(android.R.string.yes,
                    // new DialogInterface.OnClickListener() {
                    // public void onClick(DialogInterface dialog,
                    // int which) {
                    // // continue with delete
                    // preferenceHelper.Logout();
                    // goToMainActivity();
                    // }
                    // })
                    // .setNegativeButton(android.R.string.no,
                    // new DialogInterface.OnClickListener() {
                    // public void onClick(DialogInterface dialog,
                    // int which) {
                    // // do nothing
                    // dialog.cancel();
                    // }
                    // }).setIcon(android.R.drawable.ic_dialog_alert)
                    // .show();

                } else {
                    Intent intent = new Intent(MapActivity.this,
                            MenuDescActivity.class);
                    intent.putExtra(AndyConstants.Params.TITLE,
                            arrayListApplicationPages.get(position - 1)
                                    .getTitle());
                    intent.putExtra(AndyConstants.Params.CONTENT,
                            arrayListApplicationPages.get(position - 1)
                                    .getData());
                    startActivity(intent);
                }
            }
        }, 350);

    }

    public void openLogoutDialog() {
        final Dialog logoutDialog = new Dialog(this);
        logoutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        logoutDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        logoutDialog.setContentView(R.layout.logout);
        tvLogoutOk = (MyFontTextView) logoutDialog
                .findViewById(R.id.tvLogoutOk);
        tvLogoutCancel = (MyFontTextView) logoutDialog
                .findViewById(R.id.tvLogoutCancel);
        tvLogoutOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logout();
                logoutDialog.dismiss();

            }
        });
        tvLogoutCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isLogoutCheck = true;
                logoutDialog.dismiss();
            }
        });
        logoutDialog.show();
    }

    // @Override
    // public void setTitle(CharSequence title) {
    // mTitle = title;
    // getSupportActionBar().setTitle(mTitle);
    // }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnActionMenu:
            case R.id.tvTitle:
                drawerLayout.openDrawer(drawerList);
                Display display =getWindowManager().getDefaultDisplay();

                if(display.getHeight()>getTotalHeightofListView(drawerList)){
                    drawerFooterView = getLayoutInflater().inflate(R.layout.menu_footer, null);

                    tvfooter = (TextView) drawerFooterView.findViewById(R.id.tvfooter);
                    ViewGroup.LayoutParams params = tvfooter.getLayoutParams();
                    params.height =display.getHeight()-(getTotalHeightofListView(drawerList));
                    if(params.height>0){
                        drawerList.addFooterView(drawerFooterView);
                        tvfooter.setLayoutParams(params);
                        tvfooter.requestLayout();
                    }
                }
                // mMenuDrawer.toggleMenu();
                drawerLayout.openDrawer(drawerList);
                // mMenuDrawer.toggleMenu();
                break;

            case R.id.tvSound:
            case R.id.ivSound:
            case R.layout.drawer_footer_view:
                if (ivSound.isSelected()) {
                    tvSound.setText(getString(R.string.text_sound_off));
                    ivSound.setSelected(false);
                } else {
                    tvSound.setText(getString(R.string.text_sound_on));
                    ivSound.setSelected(true);
                }
                preferenceHelper.putSoundAvailability(ivSound.isSelected());
                break;
            default:
                break;
        }
    }
    public static int getTotalHeightofListView(ListView listView) {

        ListAdapter mAdapter = listView.getAdapter();

        int totalHeight = 0;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = mAdapter.getView(i, null, listView);

            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            totalHeight += mView.getMeasuredHeight();
            Log.w("HEIGHT" + i, String.valueOf(totalHeight));

        }
        totalHeight = totalHeight
                + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
//        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = totalHeight
//                + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
//        listView.setLayoutParams(params);
//        listView.requestLayout();
        return totalHeight;
    }

    public void logout() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.toast_no_internet), this);
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.LOGOUT);
        map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
        map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
        new HttpRequester(this, map, AndyConstants.ServiceCode.LOGOUT, false,
                this);

        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.LOGOUT, this, this));
    }

    public void getRequestsInProgress() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.toast_no_internet), this);
            return;
        }

        AndyUtils.showCustomProgressDialog(this,
                getResources().getString(R.string.progress_dialog_loading),
                false);
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL,
                AndyConstants.ServiceType.REQUEST_IN_PROGRESS
                        + AndyConstants.Params.ID + "="
                        + preferenceHelper.getUserId() + "&"
                        + AndyConstants.Params.TOKEN + "="
                        + preferenceHelper.getSessionToken());
        new HttpRequester(this, map,
                AndyConstants.ServiceCode.REQUEST_IN_PROGRESS, true, this);

        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // AndyConstants.ServiceCode.REQUEST_IN_PROGRESS, this, this));
    }

    public void checkRequestStatus() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.toast_no_internet), this);
            return;
        }
        AndyUtils.showCustomProgressDialog(this,
                getResources().getString(R.string.progress_dialog_request),
                false);
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL,
                AndyConstants.ServiceType.CHECK_REQUEST_STATUS
                        + AndyConstants.Params.ID + "="
                        + preferenceHelper.getUserId() + "&"
                        + AndyConstants.Params.TOKEN + "="
                        + preferenceHelper.getSessionToken() + "&"
                        + AndyConstants.Params.REQUEST_ID + "="
                        + preferenceHelper.getRequestId());
        new HttpRequester(this, map,
                AndyConstants.ServiceCode.CHECK_REQUEST_STATUS, true, this);

        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // AndyConstants.ServiceCode.CHECK_REQUEST_STATUS, this, this));
    }

    public void openApprovedDialog() {
        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(R.layout.provider_approve_dialog);
        mDialog.setCancelable(false);
        tvApprovedClose = (MyFontTextView) mDialog
                .findViewById(R.id.tvApprovedClose);
        tvApprovedClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                finish();
            }
        });
        mDialog.show();
    }

    public void callToClient(String number){
        clientNumber = number;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE, android.Manifest.permission.CALL_PHONE}, AndyConstants.PERMISSION_CALL);
        } else {
            if (!TextUtils.isEmpty(number)) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"
                        + number));
                startActivity(callIntent);
            } else {
                Toast.makeText(this, this.getResources().getString(R.string.toast_number_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        openExitDialog();
    }

    public void openExitDialog() {
        final Dialog mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(R.layout.exit_layout);
        tvExitOk = (MyFontTextView) mDialog.findViewById(R.id.tvExitOk);
        tvExitCancel = (MyFontTextView) mDialog.findViewById(R.id.tvExitCancel);
        tvExitOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AndyConstants.isShowNoteDialog = false;
                mDialog.dismiss();
                finish();
            }
        });
        tvExitCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        super.onTaskCompleted(response, serviceCode);

        switch (serviceCode) {
            case AndyConstants.ServiceCode.REQUEST_IN_PROGRESS:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log(TAG, "requestInProgress Response :" + response);
                if (!parseContent.isSuccess(response)) {
                    if (!parseContent.parseIsApproved(response)) {
                        if (isApprovedCheck) {
                            openApprovedDialog();
                            isApprovedCheck = false;
                        }
                        return;
                    }
                    if (parseContent.getErrorCode(response) == AndyConstants.REQUEST_ID_NOT_FOUND) {
                        AndyUtils.removeCustomProgressDialog();
                        preferenceHelper.clearRequestData();

                        getMenuItems();
                        addFragment(new ClientRequestFragment(), false,
                                AndyConstants.CLIENT_REQUEST_TAG, true);
                    } else if (parseContent.getErrorCode(response) == AndyConstants.INVALID_TOKEN) {
                        if (preferenceHelper.getLoginBy().equalsIgnoreCase(
                                AndyConstants.MANUAL))
                            login();
                        else
                            loginSocial(preferenceHelper.getUserId(),
                                    preferenceHelper.getLoginBy());
                    }
                    return;
                }
                AndyUtils.removeCustomProgressDialog();
                int requestId = parseContent.parseRequestInProgress(response);
                if (requestId == AndyConstants.NO_REQUEST) {
                    getMenuItems();
                    addFragment(new ClientRequestFragment(), false,
                            AndyConstants.CLIENT_REQUEST_TAG, true);
                } else {
                    checkRequestStatus();
                }
                break;
            case AndyConstants.ServiceCode.CHECK_REQUEST_STATUS:
                AppLog.Log(TAG, "checkrequeststatus Response :" + response);
                if (!parseContent.isSuccess(response)) {
                    if (parseContent.getErrorCode(response) == AndyConstants.REQUEST_ID_NOT_FOUND) {
                        preferenceHelper.clearRequestData();
                        AndyUtils.removeCustomProgressDialog();
                        addFragment(new ClientRequestFragment(), false,
                                AndyConstants.CLIENT_REQUEST_TAG, true);
                    } else if (parseContent.getErrorCode(response) == AndyConstants.INVALID_TOKEN) {
                        if (preferenceHelper.getLoginBy().equalsIgnoreCase(
                                AndyConstants.MANUAL))
                            login();
                        else
                            loginSocial(preferenceHelper.getUserId(),
                                    preferenceHelper.getLoginBy());
                    }
                    return;
                }
                AndyUtils.removeCustomProgressDialog();
                Bundle bundle = new Bundle();
                JobFragment jobFragment = new JobFragment();
                RequestDetail requestDetail = parseContent
                        .parseRequestStatus(response);
                if (requestDetail == null) {
                    return;
                }
                getMenuItems();
                switch (requestDetail.getJobStatus()) {

                    case AndyConstants.NO_REQUEST:
                        preferenceHelper.clearRequestData();
                        Intent i = new Intent(this, MapActivity.class);
                        startActivity(i);
                        break;

                    case AndyConstants.IS_WALKER_STARTED:
                        bundle.putInt(AndyConstants.JOB_STATUS,
                                AndyConstants.IS_WALKER_STARTED);
                        bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
                                requestDetail);
                        jobFragment.setArguments(bundle);
                        addFragment(jobFragment, false, AndyConstants.JOB_FRGAMENT_TAG,
                                true);
                        break;
                    case AndyConstants.IS_WALKER_ARRIVED:
                        bundle.putInt(AndyConstants.JOB_STATUS,
                                AndyConstants.IS_WALKER_ARRIVED);
                        bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
                                requestDetail);
                        jobFragment.setArguments(bundle);
                        addFragment(jobFragment, false, AndyConstants.JOB_FRGAMENT_TAG,
                                true);
                        break;
                    case AndyConstants.IS_WALK_STARTED:
                        bundle.putInt(AndyConstants.JOB_STATUS,
                                AndyConstants.IS_WALK_STARTED);
                        bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
                                requestDetail);
                        jobFragment.setArguments(bundle);
                        addFragment(jobFragment, false, AndyConstants.JOB_FRGAMENT_TAG,
                                true);
                        break;
                    case AndyConstants.IS_WALK_COMPLETED:
                        bundle.putInt(AndyConstants.JOB_STATUS,
                                AndyConstants.IS_WALK_COMPLETED);
                        bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
                                requestDetail);
                        jobFragment.setArguments(bundle);
                        addFragment(jobFragment, false, AndyConstants.JOB_FRGAMENT_TAG,
                                true);
                        break;
                    case AndyConstants.IS_DOG_RATED:
                        FeedbackFrament feedbackFrament = new FeedbackFrament();
                        bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
                                requestDetail);
                        bundle.putString(AndyConstants.Params.TIME, 0 + " "
                                + getResources().getString(R.string.text_mins));
                        bundle.putString(AndyConstants.Params.DISTANCE, 0 + " "
                                + getResources().getString(R.string.text_miles));
                        feedbackFrament.setArguments(bundle);
                        addFragment(feedbackFrament, false,
                                AndyConstants.FEEDBACK_FRAGMENT_TAG, true);
                        break;
                    default:
                        break;
                }

                break;
            case AndyConstants.ServiceCode.LOGIN:
                AndyUtils.removeCustomProgressDialog();
                if (parseContent.isSuccessWithId(response)) {
                    checkStatus();
                }
                break;
            case AndyConstants.ServiceCode.APPLICATION_PAGES:

//                AppLog.Log(TAG, "Menuitems Response::" + response);
                arrayListApplicationPages = parseContent.parsePages(
                        arrayListApplicationPages, response);
                ApplicationPages applicationPages = new ApplicationPages();
                applicationPages.setData("");
                applicationPages.setId(-4);
                applicationPages.setIcon("");
                applicationPages.setTitle(getString(R.string.text_logout));
                arrayListApplicationPages.add(applicationPages);
                adapter.notifyDataSetChanged();
                break;

            case AndyConstants.ServiceCode.LOGOUT:
                AppLog.Log("Logout Response", response);
                if (parseContent.isSuccess(response)) {
                    preferenceHelper.Logout();
                    goToMainActivity();
                    // Change by Elluminati elluminati.in
                    stopLocationUpdateService();
                }
                break;

            default:
                break;
        }
    }

    private void login() {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.toast_no_internet), this);
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.LOGIN);
        map.put(AndyConstants.Params.EMAIL, preferenceHelper.getEmail());
        map.put(AndyConstants.Params.PASSWORD, preferenceHelper.getPassword());
        map.put(AndyConstants.Params.DEVICE_TYPE,
                AndyConstants.DEVICE_TYPE_ANDROID);
        map.put(AndyConstants.Params.DEVICE_TOKEN,
                preferenceHelper.getDeviceToken());
        map.put(AndyConstants.Params.LOGIN_BY, AndyConstants.MANUAL);
        new HttpRequester(this, map, AndyConstants.ServiceCode.LOGIN, this);

        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.LOGIN, this, this));
    }

    private void loginSocial(String id, String loginType) {
        if (!AndyUtils.isNetworkAvailable(this)) {
            AndyUtils.showToast(
                    getResources().getString(R.string.toast_no_internet), this);
            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.LOGIN);
        map.put(AndyConstants.Params.SOCIAL_UNIQUE_ID, id);
        map.put(AndyConstants.Params.DEVICE_TYPE,
                AndyConstants.DEVICE_TYPE_ANDROID);
        map.put(AndyConstants.Params.DEVICE_TOKEN,
                preferenceHelper.getDeviceToken());
        map.put(AndyConstants.Params.LOGIN_BY, loginType);
        new HttpRequester(this, map, AndyConstants.ServiceCode.LOGIN, this);

        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.LOGIN, this, this));
    }

    private void requestRequiredPermission() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission
                    .ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, AndyConstants
                    .PERMISSION_LOCATION_REQUEST_CODE);
            return;
        }
        else {
            startLocationUpdateService();
            checkStatus();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        switch (requestCode) {
            case AndyConstants.PERMISSION_LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {


                    startLocationUpdateService();
                    checkStatus();

                }else{
                    requestRequiredPermission();
                }
                break;

//            case AndyConstants.PERMISSION_CALL:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                    callToClient(clientNumber);
//                }
//                break;
            default:
                startLocationUpdateService();
                checkStatus();
                break;

        }
    }
}