package com.virtugos.uberapp.driver;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AndyUtils;
import com.virtugos.uberapp.driver.utills.AppLog;
import com.virtugos.uberapp.driver.utills.PreferenceHelper;
import com.virtugos.uberapp.driver.widget.MyFontTextView;

public class MainActivity extends Activity implements OnClickListener {

	private boolean isRecieverRegister = false;
	private static final String TAG = "FirstFragment";
	private PreferenceHelper preferenceHelper;
	private MyFontTextView tvExitOk, tvExitCancel;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_main);
		preferenceHelper = new PreferenceHelper(this);

		TextView copyRight = (TextView) findViewById(R.id.copyRight);
		copyRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.criptusweb.com.br/"));
				startActivity(browserIntent);
			}
		});

		findViewById(R.id.btnFirstSignIn).setOnClickListener(this);
		findViewById(R.id.btnFirstRegister).setOnClickListener(this);

		if (TextUtils.isEmpty(preferenceHelper.getDeviceToken())) {
			AndyUtils.showCustomProgressDialog(this, getString(R.string.progress_loading), false);
			isRecieverRegister = true;
			registerReceiver(mHandleMessageReceiver, new IntentFilter(
					AndyConstants.DISPLAY_MESSAGE_REGISTER));
		} else {

			AppLog.Log(TAG, "device already registerd with :"
					+ new PreferenceHelper(MainActivity.this).getDeviceToken());
		}

		if (!TextUtils.isEmpty(preferenceHelper.getUserId())) {
			startActivity(new Intent(this, MapActivity.class));
			this.finish();
		}

		/*if(!AndyConstants.isShowNoteDialog) {
			AndyConstants.isShowNoteDialog = true;
			AndyUtils.showNoteDialog(this);
		}*/
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("onReceive", "************onReceive**************" + preferenceHelper.getDeviceToken());
			AndyUtils.removeCustomProgressDialog();
			if (intent.getAction().equals(AndyConstants.DISPLAY_MESSAGE_REGISTER)) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					int resultCode = bundle.getInt(AndyConstants.RESULT);
					AppLog.Log("", "Result code-----> " + resultCode);
					if (resultCode == Activity.RESULT_OK) {
						AppLog.Log("", "Activity BroadCast FCMId :"
								+ preferenceHelper.getDeviceToken());
					} else {
						AndyUtils.showToast(MainActivity.this.getResources().getString(
								R.string.register_gcm_failed), MainActivity.this);
						setResultCode(Activity.RESULT_CANCELED);
						finish();
					}
				}
			}
		}
	};

//	public void registerGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
//		if (mHandleMessageReceiver != null) {
//			AndyUtils.showCustomProgressDialog(this, getResources()
//					.getString(R.string.progress_loading), false);
//			new GCMRegisterHendler(MainActivity.this, mHandleMessageReceiver);
//
//		}
//	}
//
//	public void unregisterGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
//		if (mHandleMessageReceiver != null) {
//
//			if (mHandleMessageReceiver != null) {
//				unregisterReceiver(mHandleMessageReceiver);
//			}
//
//		}
//
//	}

	@Override
	public void onClick(View v) {

		Intent startRegisterActivity = new Intent(MainActivity.this,
				RegisterActivity.class);
		switch (v.getId()) {

		case R.id.btnFirstRegister:
			if (!AndyUtils.isNetworkAvailable(MainActivity.this)) {
				AndyUtils.showToast(
						getResources().getString(R.string.toast_no_internet),
						MainActivity.this);
				return;
			}
			startRegisterActivity.putExtra("isSignin", false);
			startActivity(startRegisterActivity);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			finish();
			break;

		case R.id.btnFirstSignIn:
			startRegisterActivity.putExtra("isSignin", true);
			startActivity(startRegisterActivity);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			finish();
			break;

		default:
			break;
		}


	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		if (isRecieverRegister) {
			unregisterReceiver(mHandleMessageReceiver);
			isRecieverRegister = false;
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

}
