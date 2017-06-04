package com.virtugos.uberapp.driver.base;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.virtugos.uberapp.driver.MapActivity;
import com.virtugos.uberapp.driver.R;
import com.virtugos.uberapp.driver.parse.AsyncTaskCompleteListener;
import com.virtugos.uberapp.driver.parse.ParseContent;
import com.virtugos.uberapp.driver.utills.PreferenceHelper;

/**
 * @author Elluminati elluminati.in
 * 
 */
public abstract class BaseMapFragment extends Fragment implements
		OnClickListener, AsyncTaskCompleteListener {
	protected MapActivity mapActivity;
	protected PreferenceHelper preferenceHelper;
	protected ParseContent parseContent;
	private Address address;
	private String strAddress = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapActivity = (MapActivity) getActivity();
		preferenceHelper = new PreferenceHelper(mapActivity);
		parseContent = new ParseContent(mapActivity);
	}

	public void startActivityForResult(Intent intent, int requestCode,
			String fragmentTag) {
		mapActivity.startActivityForResult(intent, requestCode, fragmentTag);
	}

	@Override
	@Deprecated
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}

	public void getAddressFromLocation(final double latitude,
									   final double longitude, final TextView tv, final Activity activity) {
		tv.setText(mapActivity.getString(R.string.text_waiting_for_address));
		tv.setTextColor(Color.GRAY);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Geocoder gCoder = new Geocoder(activity);
				try {
					final List<Address> list = gCoder.getFromLocation(latitude,
							longitude, 1);
					if (list != null && list.size() > 0) {
						address = list.get(0);
						StringBuilder sb = new StringBuilder();
						if (address.getAddressLine(0) != null) {
							if (address.getMaxAddressLineIndex() > 0) {
								for (int i = 0; i < address
										.getMaxAddressLineIndex(); i++) {
									sb.append(address.getAddressLine(i))
											.append("\n");
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
					if (getActivity() == null)
						return;

					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (!TextUtils.isEmpty(strAddress)) {
								// tv.setFocusable(false);
								// tv.setFocusableInTouchMode(false);
								tv.setText(strAddress);
								// tv.setTextColor(getResources().getColor(
								// android.R.color.black));
								// tv.setFocusable(true);
								// tv.setFocusableInTouchMode(true);
							} else {
								tv.setText("");
								// tv.setTextColor(getResources().getColor(
								// android.R.color.black));
							}
							// etSource.setEnabled(true);
						}
					});
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}).start();
	}
}