package com.virtugos.uberapp.driver.utills;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.virtugos.uberapp.driver.R;

@SuppressLint("NewApi")
public class AndyUtils {

	private static ProgressDialog mProgressDialog;
	private static Dialog mDialog;
	private static ImageView imageView;
	private static Animation anim;

	public static void showSimpleProgressDialog(Context context, String title,
			String msg, boolean isCancelable) {
		try {
			if (mProgressDialog == null) {
				mProgressDialog = ProgressDialog.show(context, title, msg);
				mProgressDialog.setCancelable(isCancelable);
			}

			if (!mProgressDialog.isShowing()) {
				mProgressDialog.show();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showNoteDialog(Context context){
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)));
		dialog.setContentView(R.layout.dialog_note);
		dialog.setCancelable(false);
		WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
		layoutParams.copyFrom(dialog.getWindow().getAttributes());
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		layoutParams.width = (int) (width - (width * 0.07));
		layoutParams.height = (int) (height - (height * 0.07));
		dialog.getWindow().setAttributes(layoutParams);
		dialog.findViewById(R.id.btnOkDialog).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		if(!((Activity)context).isFinishing()) {
			dialog.show();
		}
	}

	public static void showSimpleProgressDialog(Context context) {
		showSimpleProgressDialog(context, null,
				context.getString(R.string.progress_dialog_loading), false);
	}

	public static void removeSimpleProgressDialog() {
		try {
			if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String comaReplaceWithDot(String value) {

		value = value.replace(',', '.');
		return value;
		// DecimalFormat decimalFormat=new DecimalFormat("#.##");
		// DecimalFormatSymbols symbol=new DecimalFormatSymbols();
		// symbol.setDecimalSeparator('.');
		// decimalFormat.setDecimalFormatSymbols(symbol);
		// decimalFormat.format(value);
	}

	public static void showCustomProgressDialog(Context context, String title,
			boolean iscancelable) {
		removeCustomProgressDialog();
		mDialog = new Dialog(context, R.style.MyDialog);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mDialog.setContentView(R.layout.dialog_progress);
		imageView = (ImageView) mDialog.findViewById(R.id.iv_dialog_progress);
		((TextView) mDialog.findViewById(R.id.tv_dialog_title)).setText(title);
		mDialog.setCancelable(iscancelable);
		anim = AnimationUtils.loadAnimation(context, R.anim.scale_updown);
		imageView.startAnimation(anim);
		mDialog.show();
	}

	public static void removeCustomProgressDialog() {
		try {
			if (mDialog != null && imageView != null) {
				imageView.clearAnimation();
				mDialog.dismiss();
				mDialog = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean eMailValidation(String emailstring) {
		if (null == emailstring || emailstring.length() == 0) {
			return false;
		}
		Pattern emailPattern = Pattern
				.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
						+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
		Matcher emailMatcher = emailPattern.matcher(emailstring);
		return emailMatcher.matches();
	}

	public static void showToast(String msg, Context ctx) {

		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}

	public static void showErrorToast(int id, Context ctx) {
		String msg;
		String error_code = AndyConstants.ERROR_CODE_PREFIX + id;
		msg = ctx.getResources().getString(
				ctx.getResources().getIdentifier(error_code, "string",
						ctx.getPackageName()));
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}

	public static double distance(double lat1, double lon1, double lat2,
			double lon2, char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

}
