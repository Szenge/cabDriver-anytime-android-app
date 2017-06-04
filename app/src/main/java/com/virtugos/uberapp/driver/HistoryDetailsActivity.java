package com.virtugos.uberapp.driver;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.google.android.gms.maps.model.LatLng;
import com.virtugos.uberapp.driver.base.ActionBarBaseActivitiy;
import com.virtugos.uberapp.driver.model.History;
import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AndyUtils;

public class HistoryDetailsActivity extends ActionBarBaseActivitiy implements
		OnItemClickListener {
	private TextView tvHistoryPickupAddr;
	private TextView tvHistoryDestAddr;
	private TextView tvHistoryDistance;
	private TextView tvHistoryDuration;
	private TextView tvHistoryTotalCost;
	private History history;
	private ImageOptions imageOptions;
	private AQuery aQuery;
	private ImageView ivHistoryMapBig;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_details);
		setActionBarTitle(getString(R.string.text_trip_details));
		btnActionMenu.setVisibility(View.VISIBLE);
		// setIcon(R.drawable.back);
		setActionBarIcon(R.drawable.back);
		btnNotification.setVisibility(View.INVISIBLE);
		// setActionBarIcon(R.drawable.nav_profile);
		// pHelper = new PreferenceHelper(this);
		// pContent = new ParseContent(this);
		history = (History) getIntent().getSerializableExtra(
				AndyConstants.HISTORY_DETAILS);

		imageOptions = new ImageOptions();
		imageOptions.fileCache = true;
		imageOptions.memCache = true;
		imageOptions.fallback = R.drawable.no_items;
		aQuery = new AQuery(this);

		tvHistoryPickupAddr = (TextView) findViewById(R.id.tvHistoryPickupAddr);
		tvHistoryDestAddr = (TextView) findViewById(R.id.tvHistoryDestAddr);
		tvHistoryDistance = (TextView) findViewById(R.id.tvHistoryDistance);
		tvHistoryDuration = (TextView) findViewById(R.id.tvHistoryDuration);
		tvHistoryTotalCost = (TextView) findViewById(R.id.tvHistoryTotalCost);
		ivHistoryMapBig = (ImageView) findViewById(R.id.ivHistoryMapBig);
		// findViewById(R.id.tvNeedHelp).setOnClickListener(this);
		tvHistoryPickupAddr.setText(history.getSrcAdd());
		tvHistoryDestAddr.setText(history.getDestAdd());

		aQuery.id(ivHistoryMapBig).progress(R.id.pBar)
				.image(history.getMapImage(), imageOptions);
		tvHistoryDuration.setText(new DecimalFormat("0.00").format(Double
				.parseDouble(history.getTime())) + " " + "mins");
		tvHistoryDistance.setText(new DecimalFormat("0.00").format(Double
				.parseDouble(history.getDistance())) + " " + history.getUnit());
		tvHistoryTotalCost.setText(history.getCurrency() + history.getTotal());
		// issueList = new ArrayList<Issue>();
		// lvIssues = (ListView) findViewById(R.id.lvIssues);
		// lvIssues.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnActionMenu:
			onBackPressed();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;
		// case R.id.tvNeedHelp:
		// // Intent detailIntent = new Intent(this, NeedHelpActivity.class);
		// // detailIntent.putExtra(AndyConstants.HISTORY_DETAILS, history);
		// // startActivity(detailIntent);
		// getIssues();
		// break;
		default:
			break;
		}
	}

	// private void getIssues() {
	// if (!AndyUtils.isNetworkAvailable(this)) {
	// AndyUtils.showToast(
	// getResources().getString(R.string.dialog_no_inter_message),
	// this);
	// return;
	// }
	// AndyUtils.showCustomProgressDialog(this, "",
	// getResources().getString(R.string.progress_loading), false);
	// HashMap<String, String> map = new HashMap<String, String>();
	// map.put(AndyConstants.URL, AndyConstants.ServiceType.ISSUE
	// + AndyConstants.Params.ID + "=" + pHelper.getUserId() + "&"
	// + AndyConstants.Params.TOKEN + "=" + pHelper.getSessionToken());
	// requestQueue.add(new VolleyHttpRequest(Method.GET, map,
	// AndyConstants.ServiceCode.ISSUE, this, this));
	// }

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	public String getAddress(String latitude, String longitude) {
		LatLng latLong = new LatLng(Double.parseDouble(latitude),
				Double.parseDouble(longitude));
		Geocoder geocoder;
		List<Address> addresses;
		String add = null;
		geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
		try {
			addresses = geocoder.getFromLocation(latLong.latitude,
					latLong.longitude, 1);
			if (addresses == null || addresses.size() == 0)
				return add;
			else {
				String address = addresses.get(0).getAddressLine(0);
				String city = addresses.get(0).getLocality();
				String state = addresses.get(0).getAdminArea();
				String country = addresses.get(0).getCountryName();
				String postalCode = addresses.get(0).getPostalCode();
				add = "" + address + " " + city + "\n " + state + " " + country
						+ " " + postalCode;
				return add;
			}

		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		return add;

	}

	// private void getAddressFromLocation(final LatLng latlng, final TextView
	// et) {
	// et.setText(getResources().getString(R.string.wating_for_address));
	// et.setTextColor(Color.GRAY);
	// new Thread(new Runnable() {
	// private String strAddress;
	//
	// @Override
	// public void run() {
	// Geocoder gCoder = new Geocoder(HistoryDetailsActivity.this);
	// try {
	// final List<Address> list = gCoder.getFromLocation(
	// latlng.latitude, latlng.longitude, 1);
	//
	// if (list != null && list.size() > 0) {
	// Address address = list.get(0);
	// StringBuilder sb = new StringBuilder();
	// if (address.getAddressLine(0) != null) {
	// for (int i = 0; i < address
	// .getMaxAddressLineIndex(); i++) {
	// sb.append(address.getAddressLine(i)).append(
	// "\n");
	// }
	// }
	// // sb.append(",");
	// // sb.append(address.getCountryName());
	// strAddress = sb.toString();
	// strAddress = strAddress.replace(",null", "");
	// strAddress = strAddress.replace("null", "");
	// strAddress = strAddress.replace("Unnamed", "");
	// }
	// runOnUiThread(new Runnable() {
	// @Override
	// public void run() {
	// if (!TextUtils.isEmpty(strAddress)) {
	// et.setFocusable(false);
	// et.setFocusableInTouchMode(false);
	// et.setText(strAddress);
	// et.setTextColor(getResources().getColor(
	// android.R.color.black));
	// et.setFocusable(true);
	// et.setFocusableInTouchMode(true);
	// } else {
	// et.setText("");
	// et.setTextColor(getResources().getColor(
	// android.R.color.black));
	// }
	// }
	// });
	// } catch (Exception exc) {
	// exc.printStackTrace();
	// }
	// }
	// }).start();
	// }

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		// case AndyConstants.ServiceCode.ISSUE:
		// AppLog.Log("NeedHelpActivity", "Issue Response :" + response);
		// if (!pContent.isSuccess(response)) {
		// return;
		// }
		// issueList.clear();
		// pContent.parseIssue(response, issueList);
		// issueAdapter = new IssueAdapter(this, issueList);
		// lvIssues.setAdapter(issueAdapter);
		// break;
		// case AndyConstants.ServiceCode.SEND_ISSUE:
		// AppLog.Log("NeedHelpActivity", "Send Issue Response :" + response);
		// if (pContent.isSuccess(response)) {
		// AndyUtils.showToast(
		// getResources().getString(R.string.msg_issue_success),
		// this);
		// }
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view,
			final int position, long id) {
		// new AlertDialog.Builder(this)
		// .setTitle(getString(R.string.dialog_issue))
		// .setMessage(getString(R.string.dialog_issue_text))
		// .setPositiveButton(android.R.string.yes,
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int which) {
		// sendIssue(issueList.get(position).getIssueId());
		// }
		// })
		// .setNegativeButton(android.R.string.no,
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int which) {
		// dialog.cancel();
		// }
		// }).show();
	}

	// private void sendIssue(int issueId) {
	// if (!AndyUtils.isNetworkAvailable(this)) {
	// AndyUtils.showToast(
	// getResources().getString(R.string.toast_no_internet), this);
	// return;
	// }
	// AndyUtils.showCustomProgressDialog(this, "",
	// getResources().getString(R.string.progress_loading), false);
	// HashMap<String, String> map = new HashMap<String, String>();
	// map.put(AndyConstants.URL, AndyConstants.ServiceType.SEND_ISSUE);
	// map.put(AndyConstants.Params.ID, pHelper.getUserId());
	// map.put(AndyConstants.Params.REQUEST_ID,
	// String.valueOf(history.getId()));
	// map.put(AndyConstants.Params.TOKEN, pHelper.getSessionToken());
	// map.put(AndyConstants.Params.ISSUE_ID, String.valueOf(issueId));
	// requestQueue.add(new VolleyHttpRequest(Method.POST, map,
	// AndyConstants.ServiceCode.SEND_ISSUE, this, this));
	// }
}
