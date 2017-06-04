package com.virtugos.uberapp.driver.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.virtugos.uberapp.driver.R;
import com.virtugos.uberapp.driver.base.BaseMapFragment;
import com.virtugos.uberapp.driver.locationupdate.LocationHelper;
import com.virtugos.uberapp.driver.locationupdate.LocationHelper.OnLocationReceived;
import com.virtugos.uberapp.driver.model.RequestDetail;
import com.virtugos.uberapp.driver.parse.AsyncTaskCompleteListener;
import com.virtugos.uberapp.driver.parse.HttpRequester;
import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AndyUtils;
import com.virtugos.uberapp.driver.utills.AppLog;
import com.virtugos.uberapp.driver.widget.MyFontTextView;
import com.virtugos.uberapp.driver.widget.MyFontTextViewMedium;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ClientRequestFragment extends BaseMapFragment implements
        AsyncTaskCompleteListener, OnLocationReceived {
    private GoogleMap mMap;
    private final String TAG = "ClientRequestFragment";
    private static LinearLayout llAcceptReject;
    private View llUserDetailView;
    private Button btnClientAccept, btnClientReject,
            btnClientReqRemainTime;
    private boolean isContinueRequest, isAccepted = false;
    private Timer timer;
    private static SeekbarTimer seekbarTimer;
    private RequestDetail requestDetail;
    private Marker markerDriverLocation, markerClientLocation;

    private Location location;
    private LocationHelper locationHelper;
    private MyFontTextViewMedium tvClientName, tvClientRating;
    private ImageView ivClientProfilePicture;
    private AQuery aQuery;
    private newRequestReciever requestReceiver;
    private CancelRequestReceiver cancelRequestReceiver;
    private View clientRequestView;
    private MapView mMapView;
    private Bundle mBundle;
    // private MyFontTextView tvApprovedClose;
    private boolean isApprovedCheck = true;
    // private Dialog mDialog;
    // private static SoundPool soundPool;
    // private int soundid;
    private MediaPlayer mMediaPlayer;
    private Button btnGoOffline;
    private RelativeLayout relMap;
    private RelativeLayout linearOffline;
    private String estimatedTimeTxt;
    private MyFontTextView tvEstTime, tvEstDistance,
            tvDestinationAddressAcceptReject;

    private Address address;
    private String strAddress = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;

        requestReceiver = new newRequestReciever();
        IntentFilter filter = new IntentFilter(AndyConstants.NEW_REQUEST);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                requestReceiver, filter);

        cancelRequestReceiver = new CancelRequestReceiver();
        IntentFilter filter1 = new IntentFilter(AndyConstants.CANCEL_REQUEST);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                cancelRequestReceiver, filter1);

        // counter = 0;
        // soundPool = new SoundPool(5, AudioManager.STREAM_ALARM, 100);
        // soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
        // @Override
        // public void onLoadComplete(SoundPool soundPool, int sampleId,
        // int status) {
        // loaded = true;
        // }
        // });
        // soundid = soundPool.load(mapActivity, R.raw.beep, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        clientRequestView = inflater.inflate(R.layout.fragment_client_requests,
                container, false);
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        llAcceptReject = (LinearLayout) clientRequestView
                .findViewById(R.id.llAcceptReject);
        llUserDetailView = (View) clientRequestView
                .findViewById(R.id.clientDetailView);
        btnClientAccept = (Button) clientRequestView
                .findViewById(R.id.btnClientAccept);
        btnClientReject = (Button) clientRequestView
                .findViewById(R.id.btnClientReject);
        linearOffline = (RelativeLayout) clientRequestView
                .findViewById(R.id.linearOffline);
        btnClientReqRemainTime = (Button) clientRequestView
                .findViewById(R.id.btnClientReqRemainTime);
        tvClientName = (MyFontTextViewMedium) clientRequestView
                .findViewById(R.id.tvClientNameAcceptReject);
        tvClientRating = (MyFontTextViewMedium) clientRequestView
                .findViewById(R.id.tvClientRatingAcceptReject);

        ivClientProfilePicture = (ImageView) clientRequestView
                .findViewById(R.id.ivClientImageAcceptReject);

        btnClientAccept.setOnClickListener(this);
        btnClientReject.setOnClickListener(this);
        clientRequestView.findViewById(R.id.btnMyLocation).setOnClickListener(
                this);
        btnGoOffline = (Button) clientRequestView.findViewById(R.id.btnOffline);
        relMap = (RelativeLayout) clientRequestView.findViewById(R.id.relMap);
        linearOffline.setVisibility(View.GONE);
        relMap.setVisibility(View.VISIBLE);
        btnGoOffline.setOnClickListener(this);
        btnGoOffline.setSelected(true);

        tvEstTime = (MyFontTextView) clientRequestView
                .findViewById(R.id.tvEstTime);
        tvEstDistance = (MyFontTextView) clientRequestView
                .findViewById(R.id.tvEstDistance);
        tvDestinationAddressAcceptReject = (MyFontTextView) clientRequestView
                .findViewById(R.id.tvDestinationAddressAcceptReject);

        return clientRequestView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        aQuery = new AQuery(mapActivity);
        mMapView = (MapView) clientRequestView.findViewById(R.id.clientReqMap);
        mMapView.onCreate(mBundle);

        setUpMap();
        locationHelper = new LocationHelper(getActivity());
        locationHelper.setLocationReceivedLister(this);
        locationHelper.onStart();
        checkState();

    }

    private void addMarker() {
        if (mMap == null) {
            setUpMap();
            return;
        }

    }

    public void showLocationOffDialog() {

        AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(mapActivity);
        gpsBuilder.setCancelable(false);
        gpsBuilder
                .setTitle(getString(R.string.dialog_no_location_service_title))
                .setMessage(getString(R.string.dialog_no_location_service))
                .setPositiveButton(
                        getString(R.string.dialog_enable_location_service),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // continue with delete
                                dialog.dismiss();
                                Intent viewIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(viewIntent);

                            }
                        })

                .setNegativeButton(getString(R.string.dialog_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // do nothing
                                dialog.dismiss();
                                mapActivity.finish();
                            }
                        });
        gpsBuilder.create();
        gpsBuilder.show();
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            mMap = ((MapView) clientRequestView.findViewById(R.id.clientReqMap)).getMap();
            mMap.getUiSettings().setZoomControlsEnabled(false);
            if (ActivityCompat.checkSelfPermission(mapActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mapActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mapActivity, new String[]{android.Manifest.permission
                        .ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION}, AndyConstants
                        .PERMISSION_LOCATION_REQUEST_CODE);

                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if(mMap != null){
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        marker.showInfoWindow();
                        return true;
                    }
                });
                addMarker();
            }


            // mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            // @Override
            // public View getInfoWindow(Marker marker) {
            // View v = mapActivity.getLayoutInflater().inflate(
            // R.layout.info_window_layout, null);
            //
            // ((TextView) v).setText(marker.getTitle());
            // return v;
            // }

            // @Override
            // public View getInfoContents(Marker marker) {
            //
            // // Getting view from the layout file info_window_layout View
            //
            // // Getting reference to the TextView to set title TextView
            //
            // // Returning the view containing InfoWindow contents return
            // return null;
            //
            // }
            //
            // });


        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClientAccept:
                mapActivity.clearAll();
                isAccepted = true;
                cancelSeekbarTimer();
                respondRequest(1);
                break;
            case R.id.btnClientReject:
                mapActivity.clearAll();
                isAccepted = false;
                cancelSeekbarTimer();
                respondRequest(0);
                break;
            case R.id.btnMyLocation:
                // Location loc = mMap.getMyLocation();
                // if (loc != null) {
                // LatLng latLang = new LatLng(loc.getLatitude(),
                // loc.getLongitude());
                // mMap.animateCamera(CameraUpdateFactory.newLatLng(latLang));
                // }
                LatLng latLng = new LatLng(location.getLatitude(),
                        location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        latLng, 18);
                mMap.animateCamera(cameraUpdate);
                break;

            case R.id.btnOffline:
                mapActivity.clearAll();
                changeState();
                break;

            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(mapActivity, R.raw.beep);

        if (btnGoOffline.isSelected()) {
            if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {
                startCheckingUpcomingRequests();
            }
        }
        mapActivity.setActionBarTitle(getString(R.string.app_name));

    }

    @Override
    public void onPause() {
        super.onPause();
        if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {
            stopCheckingUpcomingRequests();
        }
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
        stopCheckingUpcomingRequests();
        cancelSeekbarTimer();
        AndyUtils.removeCustomProgressDialog();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                requestReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                cancelRequestReceiver);

    }

    // public void openApprovedDialog() {
    // mDialog = new Dialog(mapActivity,
    // android.R.style.Theme_Translucent_NoTitleBar);
    // mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    //
    // mDialog.getWindow().setBackgroundDrawable(
    // new ColorDrawable(android.graphics.Color.TRANSPARENT));
    // mDialog.setContentView(R.layout.provider_approve_dialog);
    // mDialog.setCancelable(false);
    // tvApprovedClose = (MyFontTextView) mDialog
    // .findViewById(R.id.tvApprovedClose);
    // tvApprovedClose.setOnClickListener(new OnClickListener() {
    //
    // @Override
    // public void onClick(View v) {
    // mDialog.dismiss();
    // mapActivity.finish();
    // }
    // });
    // mDialog.show();
    // }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {
            case AndyConstants.ServiceCode.GET_ALL_REQUEST:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log(TAG, "getAllRequests Response :" + response);
                if(parseContent.isSuccess(response)) {
                    if (!parseContent.parseIsApproved(response)) {
                        if (isApprovedCheck) {
                            mapActivity.openApprovedDialog();
                            isApprovedCheck = false;
                            return;
                        }
                    } else if (mapActivity.mDialog != null
                            && mapActivity.mDialog.isShowing()) {
                        mapActivity.mDialog.dismiss();
                        isApprovedCheck = true;

                    }
                    if (!parseContent.isSuccess(response)) {
                        return;
                    }
                    requestDetail = parseContent.parseAllRequests(response);
                    if (requestDetail != null && mMap != null) {
                        try {
                            stopCheckingUpcomingRequests();
                            // startTimerForRespondRequest(requestDetail.getTimeLeft());
                            setComponentVisible();

                            // pbTimeLeft.setMax(requestDetail.getTimeLeft());
                            btnClientReqRemainTime.setText(""
                                    + requestDetail.getTimeLeft());
                            // pbTimeLeft.setProgress(requestDetail.getTimeLeft());
                            tvClientName.setText(requestDetail.getClientName());
                            // tvClientPhoneNumber.setText(requestDetail
                            // .getClientPhoneNumber());
                            if (requestDetail.getClientRating() != 0) {
                                tvClientRating.setText(requestDetail.getClientRating()
                                        + "");
                                Log.i("Rating", "" + requestDetail.getClientRating());
                            }
                            if (TextUtils.isEmpty(requestDetail.getClientProfile())) {
                                aQuery.id(ivClientProfilePicture).progress(R.id.pBar)
                                        .image(R.drawable.user);
                            } else {
                                aQuery.id(ivClientProfilePicture).progress(R.id.pBar)
                                        .image(requestDetail.getClientProfile());
                            }

                            if (markerClientLocation == null) {
                                markerClientLocation = mMap
                                        .addMarker(new MarkerOptions()
                                                .position(
                                                        new LatLng(
                                                                Double.parseDouble(requestDetail
                                                                        .getClientLatitude()),
                                                                Double.parseDouble(requestDetail
                                                                        .getClientLongitude())))
                                                .icon(BitmapDescriptorFactory
                                                        .fromResource(R.drawable.pin_client_org))
                                                .title(mapActivity
                                                        .getResources()
                                                        .getString(
                                                                R.string.client_location)));
                            } else {
                                markerClientLocation.setPosition(new LatLng(
                                        Double.parseDouble(requestDetail
                                                .getClientLatitude()), Double
                                        .parseDouble(requestDetail
                                                .getClientLongitude())));
                            }
                            if (seekbarTimer == null) {
                                seekbarTimer = new SeekbarTimer(
                                        requestDetail.getTimeLeft() * 1000, 1000);
                                seekbarTimer.start();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case AndyConstants.ServiceCode.CHECK_STATE:
            case AndyConstants.ServiceCode.TOGGLE_STATE:
                AndyUtils.removeCustomProgressDialog();
                preferenceHelper.putIsActive(false);
                preferenceHelper.putDriverOffline(false);
                if (!parseContent.isSuccess(response)) {
                    return;
                }
                AppLog.Log("TAG", "toggle state:" + response);
                if (parseContent.parseAvaibilty(response)) {
                    updateButtonUi(true);
                    if (preferenceHelper.getRequestId() == AndyConstants.NO_REQUEST) {
                        startCheckingUpcomingRequests();
                    }

                } else {
                    stopCheckingUpcomingRequests();
                    updateButtonUi(false);
                }

                break;
            case AndyConstants.ServiceCode.RESPOND_REQUEST:
                AppLog.Log(TAG, "respond Request Response :" + response);
                removeNotification();
                AndyUtils.removeCustomProgressDialog();
                if (parseContent.isSuccess(response)) {
                    if (isAccepted) {
                        preferenceHelper.putRequestId(requestDetail.getRequestId());
                        JobFragment jobFragment = new JobFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt(AndyConstants.JOB_STATUS,
                                AndyConstants.IS_WALKER_STARTED);
                        bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
                                requestDetail);
                        jobFragment.setArguments(bundle);
                        if (this.isVisible())
                            mapActivity.addFragment(jobFragment, false,
                                    AndyConstants.JOB_FRGAMENT_TAG, true);
                    } else {
                        setComponentInvisible();
                        if (markerClientLocation != null
                                && markerClientLocation.isVisible()) {
                            markerClientLocation.remove();
                            markerClientLocation = null;
                        }
                        preferenceHelper.putRequestId(AndyConstants.NO_REQUEST);
                        startCheckingUpcomingRequests();
                    }
                } else {
                    setComponentInvisible();
                }
                break;

            case AndyConstants.ServiceCode.GET_DURATION:
                AppLog.Log("", "Duration Response : " + response);
                // pBar.setVisibility(View.GONE);
                // layoutDuration.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(response)) {
                    tvEstTime.setText(mapActivity.parseContent
                            .parseNearestDriverDurationString(response));
                    tvEstDistance.setText(mapActivity.parseContent
                            .parseNearestDriverDistanceString(response));
                }
                break;
            // case AndyConstants.ServiceCode.GET_FARE_QUOTE:
            // if (!TextUtils.isEmpty(response)) {
            // try {
            // JSONArray jsonArray = new JSONObject(response)
            // .getJSONArray("routes");
            // JSONArray jArrSub = jsonArray.getJSONObject(0)
            // .getJSONArray("legs");
            // JSONObject legObj = jArrSub.getJSONObject(0);
            //
            // JSONObject durationObj = legObj.getJSONObject("duration");
            // JSONObject distanceObj = legObj.getJSONObject("distance");
            //
            // double minute = durationObj.getDouble("value") / 60;
            // double kms = distanceObj.getDouble("value") / 1000;
            //
            // AppLog.Log("TAG",
            // "Duration Seconds: " + durationObj.getLong("value"));
            // AppLog.Log("TAG",
            // "Distance meter: " + distanceObj.getLong("value"));
            //
            // AppLog.Log("TAG", "Duration kms: " + kms);
            // AppLog.Log("TAG", "Distance minute: " + minute);
            //
            // // String totalQuote = MathUtils.getRound(basePrice
            // // + (preference.getDistancePrice() * kms)
            // // + (preference.getTimePrice() * minute));
            // // AppLog.Log("TAG", "totalQuote: " + totalQuote);
            // pbMinFare.setVisibility(View.GONE);
            // tvTotalFare.setVisibility(View.VISIBLE);
            // tvTotalFare.setText(getString(R.string.payment_unit)
            // + getFareCalculation(kms));
            // } catch (Exception e) {
            // AppLog.Log("UberMapFragment=====",
            // "GET_FARE_QUOTE Response: " + e);
            // }
            // }
            // break;
            default:
                break;

        }
    }

    private class SeekbarTimer extends CountDownTimer {

        public SeekbarTimer(long startTime, long interval) {
            super(startTime, interval);
            // pbTimeLeft.setProgressDrawable(getResources().getDrawable(
            // R.drawable.customprogress));
        }

        @Override
        public void onFinish() {
            if (seekbarTimer == null) {
                return;
            }
            AndyUtils.showToast(
                    mapActivity.getResources().getString(
                            R.string.toast_time_over), mapActivity);
            setComponentInvisible();
            preferenceHelper.clearRequestData();
            if (markerClientLocation != null
                    && markerClientLocation.isVisible()) {
                markerClientLocation.remove();
                markerClientLocation = null;
            }
            removeNotification();
            startCheckingUpcomingRequests();
            this.cancel();
            // seekbarTimer = null;
            // if (mMediaPlayer.isPlaying()) {
            // mMediaPlayer.stop();
            // mMediaPlayer.release();
            // }
            cancelSeekbarTimer();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int time = (int) (millisUntilFinished / 1000);

            if (!isVisible()) {
                return;
            }
            if (preferenceHelper.getSoundAvailability()) {
//				if (time <= 15) {
                AppLog.Log("ClientRequest Timer Beep", "Beep started");
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer = MediaPlayer.create(mapActivity, R.raw.beep);
                }
                if (mMediaPlayer != null) {
                    mMediaPlayer.start();
                }
//				}
            }

            btnClientReqRemainTime.setText("" + time);
            // pbTimeLeft.setProgress(time);
            // if (time <= 5) {
            // pbTimeLeft.setProgressDrawable(getResources().getDrawable(
            // R.drawable.customprogressred));
            // }

        }
    }

    // if status = 1 then accept if 0 then reject
    private void respondRequest(int status) {
        if (!AndyUtils.isNetworkAvailable(mapActivity)) {
            AndyUtils.showToast(
                    mapActivity.getResources().getString(R.string.toast_no_internet),
                    mapActivity);
            return;
        }

        AndyUtils.showCustomProgressDialog(mapActivity, mapActivity.getResources()
                .getString(R.string.progress_respond_request), false);

        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.RESPOND_REQUESTS);
        map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
        if(requestDetail != null) {
            map.put(AndyConstants.Params.REQUEST_ID,
                    String.valueOf(requestDetail.getRequestId()));
        }
        map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
        map.put(AndyConstants.Params.ACCEPTED, String.valueOf(status));

        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.RESPOND_REQUEST, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.RESPOND_REQUEST, this, this));
    }

    public void checkRequestStatus() {
        if (!AndyUtils.isNetworkAvailable(mapActivity)) {
            AndyUtils.showToast(
                    mapActivity.getResources().getString(R.string.toast_no_internet),
                    mapActivity);
            return;
        }
        AndyUtils.showCustomProgressDialog(mapActivity, mapActivity.getResources()
                .getString(R.string.progress_dialog_request), false);
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL,
                AndyConstants.ServiceType.CHECK_REQUEST_STATUS
                        + AndyConstants.Params.ID + "="
                        + preferenceHelper.getUserId() + "&"
                        + AndyConstants.Params.TOKEN + "="
                        + preferenceHelper.getSessionToken() + "&"
                        + AndyConstants.Params.REQUEST_ID + "="
                        + preferenceHelper.getRequestId());
        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.CHECK_REQUEST_STATUS, true, this);

        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.CHECK_REQUEST_STATUS, this, this));
    }

    public void getAllRequests() {
        if (!AndyUtils.isNetworkAvailable(mapActivity)) {
            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL,
                AndyConstants.ServiceType.GET_ALL_REQUESTS
                        + AndyConstants.Params.ID + "="
                        + preferenceHelper.getUserId() + "&"
                        + AndyConstants.Params.TOKEN + "="
                        + preferenceHelper.getSessionToken());

        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.GET_ALL_REQUEST, true, this);

        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // AndyConstants.ServiceCode.GET_ALL_REQUEST, this, this));
    }

    private class TimerRequestStatus extends TimerTask {
        @Override
        public void run() {
            if (isContinueRequest) {
                getAllRequests();
                // checkRequestStatus();
            }
        }
    }

    private void startCheckingUpcomingRequests() {
        AppLog.Log(TAG, "start checking upcomingRequests");
        stopCheckingUpcomingRequests();
        isContinueRequest = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerRequestStatus(),
                AndyConstants.DELAY, AndyConstants.TIME_SCHEDULE);
    }

    private void stopCheckingUpcomingRequests() {
        AppLog.Log(TAG, "stop checking upcomingRequests");
        isContinueRequest = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @SuppressWarnings("static-access")
    private void removeNotification() {
        try {
            NotificationManager manager = (NotificationManager) mapActivity
                    .getSystemService(mapActivity.NOTIFICATION_SERVICE);
            manager.cancel(AndyConstants.NOTIFICATION_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationReceived(LatLng latlong) {
        if (latlong != null) {
            if (mMap != null) {
                if (markerDriverLocation == null) {
                    markerDriverLocation = mMap.addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(latlong.latitude,
                                            latlong.longitude))
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.pin_driver))
                            .title(mapActivity.getResources().getString(
                                    R.string.my_location)));
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(latlong.latitude,
                                    latlong.longitude), 18));
                } else {
                    markerDriverLocation.setPosition(new LatLng(
                            latlong.latitude, latlong.longitude));
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(latlong.latitude,
                                    latlong.longitude), 18));
                }
            }
        }
    }

    public void setComponentVisible() {
        btnGoOffline.setVisibility(View.GONE);
        llAcceptReject.setVisibility(View.VISIBLE);
        btnClientReqRemainTime.setVisibility(View.VISIBLE);
        // rlTimeLeft.setVisibility(View.VISIBLE);
        llUserDetailView.setVisibility(View.VISIBLE);
        getDurationAndDistance(
                new LatLng(location.getLatitude(), location.getLongitude()),
                new LatLng(
                        Double.parseDouble(requestDetail.getClientLatitude()),
                        Double.parseDouble(requestDetail.getClientLongitude())));

            getAddressFromLocation(
                    Double.parseDouble(requestDetail.getClientLatitude()),
                    (Double.parseDouble(requestDetail.getClientLongitude())),
                    tvDestinationAddressAcceptReject, mapActivity);
//       getDestinationAddress(preferenceHelper.getClientDestination());
    }
    public void getDestinationAddress(LatLng destLatLong) {
        Log.d("destLatLong","*********destLatLong**********"+destLatLong);
        if (destLatLong == null || destLatLong.latitude==0.0 ||destLatLong.longitude==0.0) {
            // tvClientRating.setVisibility(View.VISIBLE);


            tvDestinationAddressAcceptReject.setText(mapActivity.getResources().getString(R.string.text_no_destination_address));
            tvDestinationAddressAcceptReject.setVisibility(View.VISIBLE);
            //   preferenceHelper.putClientDestination(null);


        } else {
            Geocoder geocoder;
            geocoder = new Geocoder(mapActivity, Locale.getDefault());
            try {
                final List<Address> list = geocoder.getFromLocation(
                        destLatLong.latitude, destLatLong.longitude, 1);
                if (list == null || list.size() == 0)
                    return;
                if (list != null && list.size() > 0) {
                    address = list.get(0);
                    StringBuilder sb = new StringBuilder();
                    if (address.getAddressLine(0) != null) {
                        if (address.getMaxAddressLineIndex() > 0) {
                            for (int i = 0; i < address
                                    .getMaxAddressLineIndex(); i++) {
                                sb.append(address.getAddressLine(i));
                            }
                            sb.append(",");
                            sb.append(address.getCountryName());
                        } else {
                            sb.append(address.getAddressLine(0));
                        }
                    }

                    strAddress = sb.toString();
                    strAddress = strAddress.replace(",null", "");
                    strAddress = strAddress.replace("null", "");
                    strAddress = strAddress.replace("Unnamed", "");
                }
                // addresses = geocoder.getFromLocation(
                // Double.parseDouble(latitude),
                // Double.parseDouble(longitude), 1);

                // String address = addresses.get(0).getAddressLine(0);
                // String city = addresses.get(0).getLocality();
                // String state = addresses.get(0).getAdminArea();
                // String country = addresses.get(0).getCountryName();
                // String postalCode = addresses.get(0).getPostalCode();
                // String knownName = addresses.get(0).getFeatureName();
                // tvClientRating.setVisibility(View.GONE);

                tvDestinationAddressAcceptReject.setText(strAddress);
                tvDestinationAddressAcceptReject.setVisibility(View.VISIBLE);
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
            }
        }

        if (mMap == null) {
            return;
        }
    }
    public void setComponentInvisible() {
        btnGoOffline.setVisibility(View.VISIBLE);
        llAcceptReject.setVisibility(View.GONE);
        btnClientReqRemainTime.setVisibility(View.GONE);
        // rlTimeLeft.setVisibility(View.GONE);
        llUserDetailView.setVisibility(View.GONE);
    }

    public void cancelSeekbarTimer() {
        if (seekbarTimer != null) {
            seekbarTimer.cancel();
            seekbarTimer = null;
        }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    }

    public void onDestroyView() {
        SupportMapFragment f = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.clientReqMap);
        if (f != null) {
            try {
                getFragmentManager().beginTransaction().remove(f).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mMap = null;
        super.onDestroyView();
    }

    private class newRequestReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra(AndyConstants.NEW_REQUEST);
            AppLog.Log(TAG, "FROM BROAD CAST-->" + response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.getInt(AndyConstants.Params.UNIQUE_ID) == 1) {
                    requestDetail = parseContent.parseNotification(response);
                    if (requestDetail != null) {
                        stopCheckingUpcomingRequests();
                        // startTimerForRespondRequest(requestDetail.getTimeLeft());

                        setComponentVisible();
                        // pbTimeLeft.setMax(requestDetail.getTimeLeft());
                        btnClientReqRemainTime.setText(""
                                + requestDetail.getTimeLeft());
                        // pbTimeLeft.setProgress(requestDetail.getTimeLeft());
                        tvClientName.setText(requestDetail.getClientName());
                        // tvClientPhoneNumber.setText(requestDetail
                        // .getClientPhoneNumber());
                        if (requestDetail.getClientRating() != 0) {
                            tvClientRating.setText(""
                                    + requestDetail.getClientRating());
                            Log.i("Rating",
                                    "" + requestDetail.getClientRating());
                        }
                        if (TextUtils.isEmpty(requestDetail.getClientProfile())) {
                            aQuery.id(ivClientProfilePicture)
                                    .progress(R.id.pBar).image(R.drawable.user);
                        } else {
                            aQuery.id(ivClientProfilePicture)
                                    .progress(R.id.pBar)
                                    .image(requestDetail.getClientProfile());
                        }

                        if(mMap != null) {
                            if (markerClientLocation == null) {
                                markerClientLocation = mMap
                                        .addMarker(new MarkerOptions()
                                                .position(
                                                        new LatLng(
                                                                Double.parseDouble(requestDetail
                                                                        .getClientLatitude()),
                                                                Double.parseDouble(requestDetail
                                                                        .getClientLongitude())))
                                                .icon(BitmapDescriptorFactory
                                                        .fromResource(R.drawable.pin_client_org))
                                                .title(mapActivity
                                                        .getResources()
                                                        .getString(
                                                                R.string.client_location)));
                            } else {
                                markerClientLocation.setPosition(new LatLng(Double
                                        .parseDouble(requestDetail
                                                .getClientLatitude()), Double
                                        .parseDouble(requestDetail
                                                .getClientLongitude())));
                            }
                        }
                        if(isVisible()) {
                            if (seekbarTimer == null) {
                                seekbarTimer = new SeekbarTimer(
                                        requestDetail.getTimeLeft() * 1000, 1000);
                                seekbarTimer.start();
                            }
                        }
                        AppLog.Log(TAG, "From broad cast recieved request");
                    }
                } else {
                    setComponentInvisible();
                    preferenceHelper.clearRequestData();
                    if (markerClientLocation != null
                            && markerClientLocation.isVisible()) {
                        markerClientLocation.remove();
                        markerClientLocation = null;
                    }
                    cancelSeekbarTimer();
                    removeNotification();
                    startCheckingUpcomingRequests();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class CancelRequestReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String response = intent
                    .getStringExtra(AndyConstants.CANCEL_REQUEST);

            AppLog.Log(TAG, "FROM BROAD CAST------>" + response);

            try {
                JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getInt(AndyConstants.Params.UNIQUE_ID) == 2) {
                    requestDetail = parseContent.parseNotification(response);
                    if (requestDetail != null) {
                        setComponentInvisible();
                        if (markerClientLocation != null
                                && markerClientLocation.isVisible()) {
                            markerClientLocation.remove();
                            markerClientLocation = null;
                        }
                        cancelSeekbarTimer();
                        removeNotification();
                        startCheckingUpcomingRequests();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.automated.taxinow.driver.locationupdate.LocationHelper.OnLocationReceived
     * #onLocationReceived(android.location.Location)
     */
    @Override
    public void onLocationReceived(Location location) {
        if (location != null)
            this.location = location;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.automated.taxinow.driver.locationupdate.LocationHelper.OnLocationReceived
     * #onConntected(android.os.Bundle)
     */
    @Override
    public void onConntected(Bundle bundle) {

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.automated.taxinow.driver.locationupdate.LocationHelper.OnLocationReceived
     * #onConntected(android.location.Location)
     */
    @Override
    public void onConntected(Location location) {
        this.location = location;
        if (location != null) {
            if (mMap != null) {
                if (markerDriverLocation == null) {
                    markerDriverLocation = mMap.addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(location.getLatitude(), location
                                            .getLongitude()))
                            .icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.pin_driver))
                            .title(mapActivity.getResources().getString(
                                    R.string.my_location)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location
                                    .getLongitude()), 18));
                } else {
                    markerDriverLocation.setPosition(new LatLng(location
                            .getLatitude(), location.getLongitude()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location
                                    .getLongitude()), 18));
                }
            }
        } else {
            showLocationOffDialog();
        }
    }

    private void updateButtonUi(boolean state) {
        btnGoOffline.setSelected(state);
        if (btnGoOffline.isSelected()) {
            mapActivity.tvTitle.setText(mapActivity.getResources().getString(
                    R.string.app_name));
            btnGoOffline.setText(mapActivity.getResources().getString(
                    R.string.text_go_offline));
            linearOffline.setVisibility(View.GONE);
            relMap.setVisibility(View.VISIBLE);
        } else {
            mapActivity.tvTitle.setText(mapActivity.getString(R.string.text_offline));
            btnGoOffline.setText(getString(R.string.text_go_online));
            linearOffline.setVisibility(View.VISIBLE);
            relMap.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    private void checkState() {
        if (!AndyUtils.isNetworkAvailable(mapActivity)) {
            AndyUtils.showToast(
                    mapActivity.getResources().getString(R.string.toast_no_internet),
                    mapActivity);
            return;
        }
        // AndyUtils.showCustomProgressDialog(mapActivity, "", getResources()
        // .getString(R.string.progress_getting_avaibility), false);
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL,
                AndyConstants.ServiceType.CHECK_STATE + AndyConstants.Params.ID
                        + "=" + preferenceHelper.getUserId() + "&"
                        + AndyConstants.Params.TOKEN + "="
                        + preferenceHelper.getSessionToken());
        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.CHECK_STATE, true, this);

        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // AndyConstants.ServiceCode.CHECK_STATE, this, this));
    }

    private void changeState() {
        if (!AndyUtils.isNetworkAvailable(mapActivity)) {
            AndyUtils.showToast(
                    mapActivity.getResources().getString(R.string.toast_no_internet),
                    mapActivity);
            return;
        }

        AndyUtils.showCustomProgressDialog(mapActivity, mapActivity.getResources()
                .getString(R.string.progress_changing_avaibilty), false);

        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.TOGGLE_STATE);
        map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
        map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());

        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.TOGGLE_STATE, this);

        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.TOGGLE_STATE, this, this));
    }

    private void getDurationAndDistance(LatLng origin, LatLng destination) {
        if (!AndyUtils.isNetworkAvailable(mapActivity)) {
            return;
        } else if (origin == null || destination == null) {
            return;
        }
        String str_origin = "origins=" + origin.latitude + ","
                + origin.longitude;
        String str_dest = "destinations=" + destination.latitude + ","
                + destination.longitude;
        String parameters;
        // if (preferenceHelper.getUnit().equals("kms")) {
        parameters = str_origin + "&" + str_dest + "&key="
                + preferenceHelper.getBrowserKey();
        // }
        // else {
        // parameters = str_origin + "&" + str_dest + "&units=imperial"
        // + "&key=" + AndyConstants.DIRECTION_API_KEY;
        // }
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/distancematrix/"
                + output + "?" + parameters;

        AppLog.Log("MapFragment", "Url : " + url);
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, url);

        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.GET_DURATION, true, this);
    }
}