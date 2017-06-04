package com.virtugos.uberapp.driver.fragment;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.virtugos.uberapp.driver.MapActivity;
import com.virtugos.uberapp.driver.R;
import com.virtugos.uberapp.driver.base.BaseMapFragment;
import com.virtugos.uberapp.driver.locationupdate.LocationHelper;
import com.virtugos.uberapp.driver.locationupdate.LocationHelper.OnLocationReceived;
import com.virtugos.uberapp.driver.model.BeanRoute;
import com.virtugos.uberapp.driver.model.BeanStep;
import com.virtugos.uberapp.driver.model.RequestDetail;
import com.virtugos.uberapp.driver.parse.AsyncTaskCompleteListener;
import com.virtugos.uberapp.driver.parse.HttpRequester;
import com.virtugos.uberapp.driver.parse.ParseContent;
import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AndyUtils;
import com.virtugos.uberapp.driver.utills.AppLog;
import com.virtugos.uberapp.driver.utills.PreferenceHelper;
import com.virtugos.uberapp.driver.widget.MyFontTextView;
import com.virtugos.uberapp.driver.widget.MyFontTextViewMedium;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class JobFragment extends BaseMapFragment implements
        AsyncTaskCompleteListener, OnLocationReceived {
    private GoogleMap googleMap;
    private PolylineOptions lineOptions, lineOptionsDest, lineOptionsClient;
    private BeanRoute routeDest, routeClient;
    private ArrayList<LatLng> points;
    private MyFontTextViewMedium tvClientName, tvClientRating;
    private MyFontTextView tvJobTime, tvJobDistance, tvJobStatus,
            tvDestinationAddress;
    private ImageView ivClientProfilePicture;
    private ParseContent parseContent;
    private Location location;
    private LocationHelper locationHelper;
    private AQuery aQuery;
    private RequestDetail requestDetail;
    private Marker markerDriverLocation, markerClientLocation;
    private Timer elapsedTimer;
    private int jobStatus = 0;
    private String time, strAddress = null; // , distance = "0";
    private final String TAG = "JobFragment";
    DecimalFormat decimalFormat;
    private BroadcastReceiver mReceiver, modeReceiver, destReceiver;
    // private MyFontTextView tvPaymentType;
    private View jobFragmentView;
    private MapView mMapView;
    public static final long ELAPSED_TIME_SCHEDULE = 60 * 1000;
    private Bundle mBundle;
    private ArrayList<LatLng> pointsDest;
    private Polyline polyLineDest, polyLineClient;
    private Marker markerDestination;
    private ImageButton btnNavigate;
    private boolean isAddMarker = false;
    private ArrayList<LatLng> pointsClient;
    private Address address;
    private String estimatedTimeTxt;
    private TextView titleTime;
    private TextView titleDistance;
    private boolean isTripStart = false;
    LatLng clientlatLng;
    double client_latitude, client_longitude;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        jobFragmentView = inflater.inflate(R.layout.fragment_job, container,
                false);
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
        }

        preferenceHelper = new PreferenceHelper(getActivity());
        tvJobTime = (MyFontTextView) jobFragmentView
                .findViewById(R.id.tvJobTime);
        tvJobDistance = (MyFontTextView) jobFragmentView
                .findViewById(R.id.tvJobDistance);
        tvJobStatus = (MyFontTextView) jobFragmentView
                .findViewById(R.id.tvJobStatus);
        tvClientName = (MyFontTextViewMedium) jobFragmentView
                .findViewById(R.id.tvClientName);
        titleTime = (TextView) jobFragmentView.findViewById(R.id.titleTime);
        titleDistance = (TextView) jobFragmentView
                .findViewById(R.id.titleDistance);
        // tvPaymentType = (MyFontTextView) jobFragmentView
        // .findViewById(R.id.tvPaymentType);
        // tvClientPhoneNumber = (MyFontTextView) jobFragmentView
        // .findViewById(R.id.tvClientNumber);
        tvClientRating = (MyFontTextViewMedium) jobFragmentView
                .findViewById(R.id.tvClientRating);
        ivClientProfilePicture = (ImageView) jobFragmentView
                .findViewById(R.id.ivClientImage);
        btnNavigate = (ImageButton) jobFragmentView
                .findViewById(R.id.btnNavigate);
        tvDestinationAddress = (MyFontTextView) jobFragmentView
                .findViewById(R.id.tvDestinationAddress);

        tvJobStatus.setOnClickListener(this);
        // if (preferenceHelper.isNavigate()) {
        // btnNavigate.setVisibility(View.GONE);
        // } else {
        // btnNavigate.setVisibility(View.VISIBLE);

        btnNavigate.setOnClickListener(this);
        jobFragmentView.findViewById(R.id.tvJobCallClient).setOnClickListener(
                this);

        setPaymentType();
        return jobFragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = savedInstanceState;
        // getDestinationAddress(preferenceHelper.getDestinationLatitude(),
        // preferenceHelper.getDestinationLongitude());

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapActivity.tvPaymentStatus.setVisibility(View.VISIBLE);

        parseContent = new ParseContent(mapActivity);
        decimalFormat = new DecimalFormat("0.00");
        points = new ArrayList<>();
        aQuery = new AQuery(mapActivity);
        // dbHelper = new DBHelper(mapActivity);
        jobStatus = getArguments().getInt(AndyConstants.JOB_STATUS,
                AndyConstants.IS_WALKER_ARRIVED);
        requestDetail = (RequestDetail) getArguments().getSerializable(
                AndyConstants.REQUEST_DETAIL);
        client_latitude = Double.parseDouble(requestDetail.getClientLatitude());
        client_longitude= Double.parseDouble(requestDetail.getClientLongitude());


        clientlatLng = new LatLng(client_latitude,client_longitude);
        if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
            titleTime.setText(mapActivity.getResources()
                    .getString(R.string.text_total_time));
            titleDistance.setText(mapActivity.getResources().getString(
                    R.string.text_total_distance));
            startElapsedTimer();
            getPathFromServer();
        }

        setClientDetails(requestDetail);
        setjobStatus(jobStatus);

        mMapView = (MapView) jobFragmentView.findViewById(R.id.jobMap);
        mMapView.onCreate(mBundle);
        setUpMap();

        locationHelper = new LocationHelper(getActivity());
        locationHelper.setLocationReceivedLister(this);
        locationHelper.onStart();

        // getDistance();
    }

    /**
     *
     */

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
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // AndyConstants.ServiceCode.CHECK_REQUEST_STATUS, this, this));
    }

    private void getPathFromServer() {
        AndyUtils.showCustomProgressDialog(mapActivity, mapActivity.getResources()
                .getString(R.string.progress_loading), false);
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL,
                AndyConstants.ServiceType.PATH_REQUEST
                        + AndyConstants.Params.ID + "="
                        + preferenceHelper.getUserId() + "&"
                        + AndyConstants.Params.TOKEN + "="
                        + preferenceHelper.getSessionToken() + "&"
                        + AndyConstants.Params.REQUEST_ID + "="
                        + preferenceHelper.getRequestId());
        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.PATH_REQUEST, true, this);

        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // AndyConstants.ServiceCode.PATH_REQUEST, this, this));
    }

    private void setClientDetails(RequestDetail requestDetail) {
        tvClientName.setText(requestDetail.getClientName());
        // tvClientPhoneNumber.setText(requestDetail.getClientPhoneNumber());
        if (requestDetail.getClientRating() != 0) {
            tvClientRating.setText("" + requestDetail.getClientRating());
        }
        if (TextUtils.isEmpty(requestDetail.getClientProfile())) {
            aQuery.id(ivClientProfilePicture).progress(R.id.pBar)
                    .image(R.drawable.user);
        } else {
            aQuery.id(ivClientProfilePicture).progress(R.id.pBar)
                    .image(requestDetail.getClientProfile());

        }
    }

    public void getDestinationAddress(LatLng destLatLong) {
        Log.d("client","*******clientlatLng********"+destLatLong);
        if (destLatLong == null || destLatLong.latitude==0.0 ||destLatLong.longitude==0.0) {
            // tvClientRating.setVisibility(View.VISIBLE);

            Log.d("client","*******in null********"+destLatLong);

            tvDestinationAddress.setText(mapActivity.getResources().getString(R.string.text_no_destination_address));
            tvDestinationAddress.setVisibility(View.VISIBLE);
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

                tvDestinationAddress.setText(strAddress);
                tvDestinationAddress.setVisibility(View.VISIBLE);
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
            }
        }

        if (googleMap == null) {
            return;
        }
    }

    private void gettingAddress(final LatLng latlng) {
        if (latlng != null) {
            if (!AndyUtils.isNetworkAvailable(mapActivity)) {
                AndyUtils.showToast(
                        mapActivity.getResources().getString(R.string.toast_no_internet),
                        mapActivity);
                return;
            }

            AndyUtils.showCustomProgressDialog(mapActivity, mapActivity.getResources()
                    .getString(R.string.progress_waiting_for_invoice), false);
            new Thread(new Runnable() {
                private String strAddress;

                @Override
                public void run() {
                    Geocoder gCoder = new Geocoder(mapActivity);
                    try {
                        final List<Address> list = gCoder.getFromLocation(
                                latlng.latitude, latlng.longitude, 1);

                        if (list != null && list.size() > 0) {
                            Address address = list.get(0);
                            StringBuilder sb = new StringBuilder();
                            if (address.getAddressLine(0) != null) {
                                for (int i = 0; i < address
                                        .getMaxAddressLineIndex(); i++) {
                                    sb.append(address.getAddressLine(i))
                                            .append("\n");
                                }
                            }
                            strAddress = sb.toString();
                            strAddress = strAddress.replace(",null", "");
                            strAddress = strAddress.replace("null", "");
                            strAddress = strAddress.replace("Unnamed", "");
                        }
                        mapActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(strAddress)) {
                                    walkCompleted(strAddress);
                                }
                            }
                        });
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * it is used for seeting text for jobstatus on textview
     */
    private void setjobStatus(int jobStatus) {

        switch (jobStatus) {
            case AndyConstants.IS_WALKER_STARTED:
                tvJobStatus.setText(mapActivity.getResources().getString(
                        R.string.text_walker_started));
                Log.d("client","****setjob***"+clientlatLng);
                getDestinationAddress(clientlatLng);
                break;
            case AndyConstants.IS_WALKER_ARRIVED:
                tvJobStatus.setText(mapActivity.getResources().getString(
                        R.string.text_walker_arrived));
                getDestinationAddress(clientlatLng);

                break;
            case AndyConstants.IS_WALK_STARTED:
                tvJobStatus.setText(mapActivity.getResources().getString(
                        R.string.text_walk_started));
                getDestinationAddress(preferenceHelper.getClientDestination());
                break;
            case AndyConstants.IS_WALK_COMPLETED:
                tvJobStatus.setText(mapActivity.getResources().getString(
                        R.string.text_walk_completed));
                getDestinationAddress(preferenceHelper.getClientDestination());
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvJobStatus:
                switch (jobStatus) {
                    case AndyConstants.IS_WALKER_STARTED:
                        mapActivity.clearAll();
                        walkerStarted();
                        break;
                    case AndyConstants.IS_WALKER_ARRIVED:
                        mapActivity.clearAll();
                        walkerArrived();
                        break;
                    case AndyConstants.IS_WALK_STARTED:
                        isTripStart = true;
                        mapActivity.clearAll();
                        walkStarted();
                        break;
                    case AndyConstants.IS_WALK_COMPLETED:
                        isTripStart = false;
                        mapActivity.clearAll();
                        mapActivity.tvPaymentStatus.setVisibility(View.GONE);
                        //walkCompleted(tvDestinationAddress.getText().toString());
                        if (preferenceHelper.getWalkerLatitude() != null) {
                            gettingAddress(new LatLng(
                                    Double.parseDouble(preferenceHelper
                                            .getWalkerLatitude()),
                                    Double.parseDouble(preferenceHelper
                                            .getWalkerLongitude())));
                        }
                        break;
                    default:
                        break;
                }

                break;
            case R.id.tvJobCallClient:
                mapActivity.callToClient(requestDetail.getClientPhoneNumber());
                break;

            case R.id.btnNavigate:
                if (markerClientLocation != null && markerDriverLocation != null) {
                    preferenceHelper.putIsNavigate(true);
                    animateCamera(markerDriverLocation.getPosition());
                    // v.setVisibility(View.GONE);
                    String address;
                    if (isTripStart) {
                        double lat = preferenceHelper.getClientDestination().latitude;
                        double lag = preferenceHelper.getClientDestination().longitude;
                        if (lat == 0.0 && lag == 0.0) {
                            Toast.makeText(mapActivity, mapActivity.getResources().getString(R
                                    .string
                                    .msg_destination_not_available), Toast
                                    .LENGTH_SHORT).show();
                            return;
                        }
                        drawPathToClient(markerClientLocation.getPosition(),
                                preferenceHelper.getClientDestination());
                        address = getAddressFromLocation(preferenceHelper.getClientDestination());
                    } else {
                        drawPathToClient(markerDriverLocation.getPosition(),
                                markerClientLocation.getPosition());
                        address = getAddressFromLocation(markerClientLocation
                                .getPosition());
                    }


                    String uri = String.format(Locale.ENGLISH,
                            "google.navigation:q=%s", address);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        AppLog.Log("Map application",
                                "you have to install map application");
                    }
                } else {
                    AndyUtils.showToast(mapActivity
                                    .getString(R.string.text_waiting_for_location),
                            getActivity());
                }
                break;

            default:
                break;
        }
    }

    private String getAddressFromLocation(final LatLng latlng) {
        Geocoder gCoder = new Geocoder(getActivity());
        try {
            final List<Address> list = gCoder.getFromLocation(latlng.latitude,
                    latlng.longitude, 1);
            if (list != null && list.size() > 0) {
                address = list.get(0);
                StringBuilder sb = new StringBuilder();
                if (address.getAddressLine(0) != null) {
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        if (i == (address.getMaxAddressLineIndex() - 1))
                            sb.append(address.getAddressLine(i));
                        else
                            sb.append(address.getAddressLine(i)).append(", ");
                    }
                }
                strAddress = sb.toString();
                strAddress = strAddress.replace(",null", "");
                strAddress = strAddress.replace("null", "");
                strAddress = strAddress.replace("Unnamed", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return strAddress;
    }

    private void animateCamera(LatLng latlng) {
        if (markerClientLocation != null) {
            try {
                Location dest = new Location("dest");
                dest.setLatitude(markerClientLocation.getPosition().latitude);
                dest.setLongitude(markerClientLocation.getPosition().longitude);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latlng).bearing(location.bearingTo(dest))
                        .zoom(googleMap.getCameraPosition().zoom).tilt(45)
                        .build();
                googleMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void drawPathToClient(LatLng source, LatLng destination) {
        if (source == null || destination == null) {
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL,
                "https://maps.googleapis.com/maps/api/directions/json?origin="
                        + source.latitude + "," + source.longitude
                        + "&destination=" + destination.latitude + ","
                        + destination.longitude + "&sensor=false&key="
                        + preferenceHelper.getBrowserKey());

        AppLog.Log("Navigation Path",
                "https://maps.googleapis.com/maps/api/directions/json?origin="
                        + source.latitude + "," + source.longitude
                        + "&destination=" + destination.latitude + ","
                        + destination.longitude + "&sensor=false&key="
                        + preferenceHelper.getBrowserKey());

        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.DRAW_PATH_CLIENT, true, this);
        // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
        // AndyConstants.ServiceCode.DRAW_PATH_CLIENT, this, this));
    }

    /**
     * send this when walk completed
     */
    private void walkCompleted(String destAddress) {
        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_COMPLETED);
        map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
        map.put(AndyConstants.Params.REQUEST_ID,
                String.valueOf(preferenceHelper.getRequestId()));
        map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
        map.put(AndyConstants.Params.LATITUDE,
                preferenceHelper.getWalkerLatitude());
        map.put(AndyConstants.Params.LONGITUDE,
                preferenceHelper.getWalkerLongitude());
        map.put(AndyConstants.Params.DISTANCE, preferenceHelper.getDistance()
                + "");
        map.put(AndyConstants.Params.TIME, preferenceHelper.getRequestTime()+"");
        map.put(AndyConstants.Params.DEST_ADDRESS, destAddress);

        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.WALK_COMPLETED, this);
    }

    /**
     * send this when job started
     */
    private void walkStarted() {
        if (!AndyUtils.isNetworkAvailable(mapActivity)) {
            AndyUtils.showToast(
                    mapActivity.getResources().getString(R.string.toast_no_internet),
                    mapActivity);
            return;
        }
        AndyUtils.showCustomProgressDialog(mapActivity, mapActivity.getResources()
                .getString(R.string.progress_send_request), false);

        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_STARTED);
        map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
        map.put(AndyConstants.Params.REQUEST_ID,
                String.valueOf(preferenceHelper.getRequestId()));
        map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
        map.put(AndyConstants.Params.LATITUDE,
                preferenceHelper.getWalkerLatitude());
        map.put(AndyConstants.Params.LONGITUDE,
                preferenceHelper.getWalkerLongitude());

        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.WALK_STARTED, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.WALK_STARTED, this, this));
    }

    /**
     * send this when walker arrived client's location
     */
    private void walkerArrived() {
        if (!AndyUtils.isNetworkAvailable(mapActivity)) {
            AndyUtils.showToast(
                    mapActivity.getResources().getString(R.string.toast_no_internet),
                    mapActivity);
            return;
        }

        AndyUtils.showCustomProgressDialog(mapActivity, mapActivity.getResources()
                .getString(R.string.progress_send_request), false);

        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.WALK_ARRIVED);
        map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
        map.put(AndyConstants.Params.REQUEST_ID,
                String.valueOf(preferenceHelper.getRequestId()));
        map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
        map.put(AndyConstants.Params.LATITUDE,
                preferenceHelper.getWalkerLatitude());
        map.put(AndyConstants.Params.LONGITUDE,
                preferenceHelper.getWalkerLongitude());

        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.WALKER_ARRIVED, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.WALKER_ARRIVED, this, this));
    }

    /**
     * send this when walker started his/her run
     */
    private void walkerStarted() {
        if (!AndyUtils.isNetworkAvailable(mapActivity)) {
            AndyUtils.showToast(
                    mapActivity.getResources().getString(R.string.toast_no_internet),
                    mapActivity);
            return;
        }

        AndyUtils.showCustomProgressDialog(mapActivity, mapActivity.getResources()
                .getString(R.string.progress_send_request), false);

        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, AndyConstants.ServiceType.WALKER_STARTED);
        map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
        map.put(AndyConstants.Params.REQUEST_ID,
                String.valueOf(preferenceHelper.getRequestId()));
        map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
        map.put(AndyConstants.Params.LATITUDE,
                preferenceHelper.getWalkerLatitude());
        map.put(AndyConstants.Params.LONGITUDE,
                preferenceHelper.getWalkerLongitude());

        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.WALKER_STARTED, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // AndyConstants.ServiceCode.WALKER_STARTED, this, this));
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (googleMap == null) {
            googleMap = ((MapView) jobFragmentView.findViewById(R.id.jobMap))
                    .getMap();
            initPreviousDrawPath();

            // googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            //
            // // Use default InfoWindow frame
            //
            // @Override
            // public View getInfoWindow(Marker marker) {
            // View v = mapActivity.getLayoutInflater().inflate(
            // R.layout.info_window_layout, null);
            //
            // ((TextView) v).setText(marker.getTitle());
            // return v;
            // }
            //
            // // Defines the contents of the InfoWindow
            //
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

            googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.showInfoWindow();
                    return true;
                }
            });

            addMarker();

        }
    }

    // It will add marker on map of walker location
    private void addMarker() {
        if (googleMap == null) {
            setUpMap();
            return;
        }

    }

    public void onDestroyView() {
        SupportMapFragment f = (SupportMapFragment) getFragmentManager()
                .findFragmentById(R.id.jobMap);
        if (f != null) {
            try {
                getFragmentManager().beginTransaction().remove(f).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        googleMap = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        stopElapsedTimer();
        unregisterCancelReceiver();
        unRegisterPaymentModeReceiver();
        unRegisterDestinationReceiver();
        super.onDestroy();
    }

    private void initPreviousDrawPath() {
        // points = dbHelper.getLocations();
        lineOptions = new PolylineOptions();
        lineOptions.addAll(points);
        lineOptions.width(15);
        lineOptions.geodesic(true);
        lineOptions.color(mapActivity.getResources().getColor(R.color.skyblue));
        googleMap.addPolyline(lineOptions);
        points.clear();
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {
        AndyUtils.removeCustomProgressDialog();
        if (!this.isVisible())
            return;
        switch (serviceCode) {
            case AndyConstants.ServiceCode.CHECK_REQUEST_STATUS:
                AppLog.Log(TAG, "checkrequeststatus Response :" + response);

                RequestDetail requestDetail = parseContent
                        .parseRequestStatus(response);
                if (requestDetail == null) {
                    return;
                }
                if (requestDetail.getJobStatus() == AndyConstants.NO_REQUEST) {
                    // mapActivity.addFragment(new ClientRequestFragment(), false,
                    // AndyConstants.CLIENT_REQUEST_TAG, true);
                    Intent mapintent = new Intent(getActivity(), MapActivity.class);
                    startActivity(mapintent);
                }

            case AndyConstants.ServiceCode.WALKER_STARTED:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log(TAG, "walker started response " + response);
                if (parseContent.isSuccess(response)) {
                    jobStatus = AndyConstants.IS_WALKER_ARRIVED;
                    setjobStatus(jobStatus);
                }

                break;
            case AndyConstants.ServiceCode.WALKER_ARRIVED:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log(TAG, "walker arrived response " + response);
                if (parseContent.isSuccess(response)) {
                    jobStatus = AndyConstants.IS_WALK_STARTED;
                    setjobStatus(jobStatus);
                }
                break;
            case AndyConstants.ServiceCode.WALK_STARTED:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log(TAG, "walk started response " + response);
                if (parseContent.isSuccess(response)) {
                    preferenceHelper.putIsTripStart(true);
                    jobStatus = AndyConstants.IS_WALK_COMPLETED;
                    setjobStatus(jobStatus);
                    // getDistance();
//                    preferenceHelper.putRequestTime(Calendar.getInstance()
//                            .getTimeInMillis());
                    if (markerClientLocation != null) {
                        markerClientLocation.setTitle(mapActivity.getResources()
                                .getString(R.string.job_start_location));
                    }
                    titleTime.setText(mapActivity.getResources().getString(
                            R.string.text_total_time));
                    titleDistance.setText(mapActivity.getResources().getString(
                            R.string.text_total_distance));
                    tvJobTime.setText("0 "
                            + mapActivity.getResources().getString(
                            R.string.text_mins));
                    tvJobDistance.setText("0.0 " + preferenceHelper.getUnit());

                    startElapsedTimer();
                }

                break;
            case AndyConstants.ServiceCode.WALK_COMPLETED:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log(TAG, "walk completed response " + response);
                if (parseContent.isSuccess(response)) {
                    FeedbackFrament feedbackFrament = new FeedbackFrament();
                    requestDetail = parseContent.parseRequestStatus(response);
                    Bundle bundle = new Bundle();

                    // bundle.putString(
                    // AndyConstants.Params.TIME,
                    // time
                    // + " "
                    // + mapActivity.getResources().getString(
                    // R.string.text_mins));
                    // bundle.putString(
                    // AndyConstants.Params.DISTANCE,
                    // decimalFormat.format(preferenceHelper.getDistance())
                    // // / (1000 * 1.62))
                    // + " "
                    // + mapActivity.getResources().getString(
                    // R.string.text_miles));
                    try {
                        requestDetail.setAmount(new JSONObject(response)
                                .getString("total"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // requestDetail.setTime(time);
                    // requestDetail.setDistance(" " +
                    // preferenceHelper.getDistance());
                    // requestDetail.setUnit(preferenceHelper.getUnit());
                    bundle.putSerializable(AndyConstants.REQUEST_DETAIL,
                            requestDetail);
                    feedbackFrament.setArguments(bundle);
                    if (this.isVisible())
                        mapActivity.addFragment(feedbackFrament, false,
                                AndyConstants.FEEDBACK_FRAGMENT_TAG, true);
                }
                break;

            case AndyConstants.ServiceCode.GET_ROUTE:
                // if (parseContent.isSuccess(response)) {
                // jobStatus = AndyConstants.;
                // setjobStatus(jobStatus);
                // }
            case AndyConstants.ServiceCode.PATH_REQUEST:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log(TAG, "Path request :" + response);
                if (parseContent.isSuccess(response)) {
                    parseContent.parsePathRequest(response, points);
                    initPreviousDrawPath();
                }
                break;
            case AndyConstants.ServiceCode.DRAW_PATH:
                AndyUtils.removeCustomProgressDialog();
                Log.d("DRAW_PATH","***********************"+response);
                if (!TextUtils.isEmpty(response)) {
                    routeDest = new BeanRoute();
                    parseContent.parseRoute(response, routeDest);

                    final ArrayList<BeanStep> step = routeDest.getListStep();
//                    System.out.println("step size=====> " + step.size());
                    pointsDest = new ArrayList<>();
                    lineOptionsDest = new PolylineOptions();

                    for (int i = 0; i < step.size(); i++) {
                        List<LatLng> path = step.get(i).getListPoints();
//                        System.out.println("step =====> " + i + " and "
//                                + path.size());
                        pointsDest.addAll(path);
                    }
                    if (polyLineDest != null)
                        polyLineDest.remove();
                    lineOptionsDest.addAll(pointsDest);
                    lineOptionsDest.width(15);
                    lineOptionsDest.geodesic(true);
                    lineOptionsDest.color(ContextCompat.getColor(mapActivity,
                            R.color.color_path)); // #00008B rgb(0,0,139)

                    if (lineOptionsDest != null && googleMap != null) {
                        polyLineDest = googleMap.addPolyline(lineOptionsDest);
                        boundLatLang();
                        // LatLngBounds.Builder bld = new LatLngBounds.Builder();
                        //
                        // bld.include(markerClientLocation.getPosition());
                        // bld.include(markerDestination.getPosition());
                        // LatLngBounds latLngBounds = bld.build();
                        // googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        // latLngBounds, 15));
                    }
                }
                break;

            case AndyConstants.ServiceCode.DRAW_PATH_CLIENT:
                AndyUtils.removeCustomProgressDialog();
                AppLog.Log("JobFragment", "PATH Response : " + response);
                if (!TextUtils.isEmpty(response)) {
                    routeClient = new BeanRoute();
                    parseContent.parseRoute(response, routeClient);

                    final ArrayList<BeanStep> step = routeClient.getListStep();
                    pointsClient = new ArrayList<>();
                    lineOptionsClient = new PolylineOptions();

                    for (int i = 0; i < step.size(); i++) {
                        List<LatLng> path = step.get(i).getListPoints();
                        pointsClient.addAll(path);
                    }
                    if (polyLineClient != null)
                        polyLineClient.remove();
                    lineOptionsClient.addAll(pointsClient);
                    lineOptionsClient.width(17);
                    lineOptionsClient.color(Color.BLUE);

                    if (lineOptionsClient != null && googleMap != null) {
                        polyLineClient = googleMap.addPolyline(lineOptionsClient);
                    }
                }
            case AndyConstants.ServiceCode.GET_DURATION:
                AppLog.Log("", "Duration Response : " + response);
                // pBar.setVisibility(View.GONE);
                // layoutDuration.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(response)) {
                    estimatedTimeTxt = mapActivity.parseContent
                            .parseNearestDriverDurationString(response);
                    tvJobTime.setText(estimatedTimeTxt);
                    tvJobDistance.setText(mapActivity.parseContent
                            .parseNearestDriverDistanceString(response));
                }
                break;
            default:
                break;
        }
    }

    /**
     *
     */
    private void startElapsedTimer() {
        elapsedTimer = new Timer();
        elapsedTimer.scheduleAtFixedRate(new TimerRequestStatus(),
                AndyConstants.DELAY, ELAPSED_TIME_SCHEDULE);
    }

    private void stopElapsedTimer() {
        if (elapsedTimer != null) {
            elapsedTimer.cancel();
            elapsedTimer = null;
        }
    }

    private class TimerRequestStatus extends TimerTask {
        @Override
        public void run() {
            // isContinueRequest = false;
            AppLog.Log(TAG, "In elapsed time timer");
            mapActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isVisible()) {
//                        if (preferenceHelper.getRequestTime() == AndyConstants.NO_TIME) {
//                            preferenceHelper.putRequestTime(System
//                                    .currentTimeMillis());
//                        }
                        time = String.valueOf((Calendar.getInstance()
                                .getTimeInMillis() - preferenceHelper
                                .getRequestTime())
                                / (1000 * 60));
//                        tvJobTime.setText(time + " " + mapActivity.getResources().getString(
//                                R.string.text_mins));
                    }
                }
            });
        }
    }

    @Override
    public void onLocationReceived(LatLng latlong) {
        if (googleMap == null) {
            return;
        }

        if (markerDriverLocation != null && markerClientLocation != null) {
            if (preferenceHelper.isNavigate()) {
                drawPathToClient(markerDriverLocation.getPosition(),
                        markerClientLocation.getPosition());
            }
        }
   //     getDestinationAddress(preferenceHelper.getClientDestination());
        if (markerClientLocation == null) {
            markerClientLocation = googleMap.addMarker(new MarkerOptions()
                    .position(
                            new LatLng(Double.parseDouble(requestDetail
                                    .getClientLatitude()), Double
                                    .parseDouble(requestDetail
                                            .getClientLongitude()))).icon(
                            BitmapDescriptorFactory
                                    .fromResource(R.drawable.pin_client_org)));

            if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
                markerClientLocation.setTitle(mapActivity.getResources()
                        .getString(R.string.job_start_location));
            } else {
                markerClientLocation.setTitle(mapActivity.getResources()
                        .getString(R.string.client_location));
            }
            drawPath(markerClientLocation.getPosition(),
                    preferenceHelper.getClientDestination());
        }

        if (latlong != null) {
            if (googleMap != null) {
                if (markerDriverLocation == null) {
                    markerDriverLocation = googleMap
                            .addMarker(new MarkerOptions()
                                    .position(
                                            new LatLng(latlong.latitude,
                                                    latlong.longitude))
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.pin_driver))
                                    .title(mapActivity.getResources().getString(
                                            R.string.my_location)));
                    boundLatLang();
                    // googleMap.animateCamera(CameraUpdateFactory
                    // .newLatLngZoom(new LatLng(latlong.latitude,
                    // latlong.longitude), 16));
                } else {
                    markerDriverLocation.setPosition(new LatLng(
                            latlong.latitude, latlong.longitude));
                    if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
                        drawTrip(new LatLng(latlong.latitude, latlong.longitude));

                        // distance = decimalFormat.format(distanceMeter / (1000
                        // * 1.62));

                        // tvJobDistance.setText(decimalFormat
                        // .format(preferenceHelper.getDistance()
                        // / (1000 * 1.62))
                        // + " "
                        // + mapActivity.getResources().getString(
                        // R.string.text_miles));

                        tvJobDistance.setText(decimalFormat
                                .format(preferenceHelper.getDistance()
                                        // / (1000 * 1.62))
                                ) + " " + preferenceHelper.getUnit());

                        tvJobTime.setText(preferenceHelper.getRequestTime()+" "+mapActivity.getResources().getString(R.string.text_mins));
                    } else {
                        getDurationAndDistance(
                                latlong,
                                new LatLng(Double.parseDouble(requestDetail
                                        .getClientLatitude()), Double
                                        .parseDouble(requestDetail
                                                .getClientLongitude())));
                    }
                }
            }
        }

    }

    // private void getDistance() {
    // if (googleMap == null || markerDriverLocation == null) {
    // return;
    // }
    // if (jobStatus == AndyConstants.IS_WALK_COMPLETED) {
    //
    // ArrayList<LatLng> latLngs = dbHelper.getLocations();
    // int distanceMeter = 0;
    // if (latLngs.size() >= 2) {
    // for (int i = 0; i < latLngs.size() - 1; i++) {
    // Location location1 = new Location("");
    // Location location2 = new Location("");
    // location1.setLatitude(latLngs.get(i).latitude);
    // location1.setLongitude(latLngs.get(i).longitude);
    // location2.setLatitude(latLngs.get(i +
    // 1).latitude);googleMap.setInfoWindowAdapter(this);
    // location2.setLongitude(latLngs.get(i + 1).longitude);
    // distanceMeter = distanceMeter
    // + (int) location1.distanceTo(location2);
    //
    // }
    // }
    // // AndyUtils.showToast("Meter:" + distanceMeter, mapActivity);
    // DecimalFormat decimalFormat = new DecimalFormat("0.00");
    // distance = decimalFormat.format(distanceMeter / (1000 * 1.62));
    // tvJobDistance
    // .setText(distance
    // + " "
    // + mapActivity.getResources().getString(
    // R.string.text_miles));
    // // Location jobStartLocation = new Location("");
    // // Location currentLocation = new Location("");
    // // jobStartLocation.setLatitude(Double.parseDouble(requestDetail
    // // .getClientLatitude()));
    // // jobStartLocation.setLongitude(Double.parseDouble(requestDetail
    // // .getClientLongitude()));
    // // currentLocation
    // // .setLatitude(markerDriverLocation.getPosition().latitude);
    // // currentLocation
    // // .setLongitude(markerDriverLocation.getPosition().longitude);
    // // AppLog.Log(TAG, jobStartLocation.distanceTo(currentLocation)
    // // + " METERS ");
    // // int distanceMeter = (int) jobStartLocation
    // // .distanceTo(currentLocation);
    // // DecimalFormat decimalFormat = new DecimalFormat("0.0");
    // // distance = decimalFormat.format(distanceMeter / (1000 * 1.62));
    // // tvJobDistance
    // // .setText(distance
    // // + " "
    // // + mapActivity.getResources().getString(
    // // R.string.text_miles));
    // }
    // }

    private void drawTrip(LatLng latlng) {

        if (googleMap != null) {
            // setMarker(latlng);
            points.add(latlng);
            // dbHelper.addLocation(latlng);
            lineOptions = new PolylineOptions();
            lineOptions.addAll(points);
            lineOptions.width(15);
            lineOptions.geodesic(true);
            lineOptions.color(ContextCompat.getColor(mapActivity, R.color.skyblue));

            googleMap.addPolyline(lineOptions);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        registerCancelReceiver();
        registerPaymentModeReceiver();
        registerDestinationReceiver();
        if (isAddMarker && preferenceHelper.isNavigate()) {
            if (jobStatus == AndyConstants.IS_WALKER_ARRIVED) {
                drawPathToClient(markerDriverLocation.getPosition(),
                        markerClientLocation.getPosition());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    private void registerCancelReceiver() {
        IntentFilter intentFilter = new IntentFilter("CANCEL_REQUEST");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppLog.Log("JobFragment", "CANCEL_REQUEST");
                stopElapsedTimer();
                mapActivity.startActivity(new Intent(mapActivity,
                        MapActivity.class));
                mapActivity.finish();
            }
        };
        mapActivity.registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterCancelReceiver() {
        if (mReceiver != null) {
            mapActivity.unregisterReceiver(mReceiver);
        }
    }

    private void registerPaymentModeReceiver() {
        IntentFilter intentFilter = new IntentFilter("PAYMENT_MODE");
        modeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppLog.Log("JobFragment", "PAYMENT_MODE");
                if (JobFragment.this.isVisible()) {
                    setPaymentType();
                }
            }
        };
        mapActivity.registerReceiver(modeReceiver, intentFilter);
    }

    private void unRegisterPaymentModeReceiver() {
        if (modeReceiver != null) {
            mapActivity.unregisterReceiver(modeReceiver);
        }
    }

    private void unRegisterDestinationReceiver() {
        if (destReceiver != null) {
            mapActivity.unregisterReceiver(destReceiver);
        }
    }

    private void registerDestinationReceiver() {
        IntentFilter intentFilter = new IntentFilter("CLIENT_DESTINATION");
        destReceiver = new BroadcastReceiver() {
            private LatLng destLatLng;

            @Override
            public void onReceive(Context context, Intent intent) {
                AppLog.Log("JobFragment", "CLIENT_DESTINATION");
                destLatLng = preferenceHelper.getClientDestination();
                drawPath(markerClientLocation.getPosition(), destLatLng);
                getDestinationAddress(preferenceHelper.getClientDestination());
            }
        };
        mapActivity.registerReceiver(destReceiver, intentFilter);
    }

    private void setPaymentType() {
        if (preferenceHelper.getPaymentType() == AndyConstants.CASH) {
            mapActivity.tvPaymentStatus
                    .setText(getString(R.string.text_type_cash));
            mapActivity.tvPaymentStatus
                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.cash,
                            0, 0, 0);
            mapActivity.tvPaymentStatus.setCompoundDrawablePadding(5);
        } else {
            mapActivity.tvPaymentStatus
                    .setText(getString(R.string.text_type_card));
            mapActivity.tvPaymentStatus
                    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.card,
                            0, 0, 0);
            mapActivity.tvPaymentStatus.setCompoundDrawablePadding(5);
        }
    }

    private void drawPath(LatLng source, LatLng destination) {
        if (source == null || destination == null) {
            return;
        }
//         AndyUtils.showToast("" + destination.latitude, mapActivity);
        if (destination.latitude != 0) {
            setDestinationMarker(destination);
            boundLatLang();
            HashMap<String, String> map = new HashMap<>();
            map.put(AndyConstants.URL,
                    "http://maps.googleapis.com/maps/api/directions/json?origin="
                            + source.latitude + "," + source.longitude
                            + "&destination=" + destination.latitude + ","
                            + destination.longitude + "&sensor=false");

            new HttpRequester(mapActivity, map,
                    AndyConstants.ServiceCode.DRAW_PATH, true, this);

            // requestQueue.add(new VolleyHttpRequest(Method.GET, map,
            // AndyConstants.ServiceCode.DRAW_PATH, this, this));
        }
//        else{
//           tvDestinationAddress.setText(mapActivity.getResources().getString(R.string.text_no_destination_address));
//            tvDestinationAddress.setVisibility(View.VISIBLE);
//        }
    }

    private void setDestinationMarker(LatLng latLng) {
        if (latLng != null) {
            if (googleMap != null && this.isVisible()) {
                if (markerDestination == null) {
                    MarkerOptions opt = new MarkerOptions();
                    opt.position(latLng);
                    opt.icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.pin_client_destination));
                    opt.title(getString(R.string.text_destination));
                    markerDestination = googleMap.addMarker(opt);
                } else {
                    markerDestination.setPosition(latLng);
                }
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
        if (location != null) {
            if (googleMap != null) {
                if (markerDriverLocation == null) {
                    markerDriverLocation = googleMap
                            .addMarker(new MarkerOptions()
                                    .position(
                                            new LatLng(location.getLatitude(),
                                                    location.getLongitude()))
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.pin_driver))
                                    .title(mapActivity.getResources().getString(
                                            R.string.my_location)));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location
                                    .getLongitude()), 16));
                    googleMap
                            .setOnCameraChangeListener(new OnCameraChangeListener() {

                                @Override
                                public void onCameraChange(CameraPosition arg0) {
                                    if (!isAddMarker) {
                                        isAddMarker = true;
                                        if (preferenceHelper.isNavigate())
                                            animateCamera(markerDriverLocation
                                                    .getPosition());
                                    }
                                }
                            });
                } else {
                    markerDriverLocation.setPosition(new LatLng(location
                            .getLatitude(), location.getLongitude()));
                }
            }
        }
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

    }

    private void boundLatLang() {

        try {
            if (markerDriverLocation != null && markerClientLocation != null
                    && markerDestination != null) {
                LatLngBounds.Builder bld = new LatLngBounds.Builder();
                bld.include(new LatLng(
                        markerDriverLocation.getPosition().latitude,
                        markerDriverLocation.getPosition().longitude));
                bld.include(new LatLng(
                        markerClientLocation.getPosition().latitude,
                        markerClientLocation.getPosition().longitude));
                bld.include(new LatLng(
                        markerDestination.getPosition().latitude,
                        markerDestination.getPosition().longitude));
                LatLngBounds latLngBounds = bld.build();

                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        latLngBounds, 50));
            } else if (markerDriverLocation != null
                    && markerClientLocation != null) {
                LatLngBounds.Builder bld = new LatLngBounds.Builder();
                bld.include(new LatLng(
                        markerDriverLocation.getPosition().latitude,
                        markerDriverLocation.getPosition().longitude));
                bld.include(new LatLng(
                        markerClientLocation.getPosition().latitude,
                        markerClientLocation.getPosition().longitude));
                LatLngBounds latLngBounds = bld.build();

                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        latLngBounds, 100));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        HashMap<String, String> map = new HashMap<>();
        map.put(AndyConstants.URL, url);

        new HttpRequester(mapActivity, map,
                AndyConstants.ServiceCode.GET_DURATION, true, this);
    }
}