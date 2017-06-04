package com.virtugos.uberapp.driver.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.androidquery.AQuery;
import com.virtugos.uberapp.driver.R;
import com.virtugos.uberapp.driver.base.BaseMapFragment;
import com.virtugos.uberapp.driver.model.RequestDetail;
import com.virtugos.uberapp.driver.parse.AsyncTaskCompleteListener;
import com.virtugos.uberapp.driver.parse.HttpRequester;
import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AndyUtils;
import com.virtugos.uberapp.driver.utills.AppLog;
import com.virtugos.uberapp.driver.widget.MyFontEdittextView;
import com.virtugos.uberapp.driver.widget.MyFontTextView;

import java.text.DecimalFormat;
import java.util.HashMap;

public class FeedbackFrament extends BaseMapFragment implements
		AsyncTaskCompleteListener {

	private MyFontEdittextView etFeedbackComment;
	private ImageView ivClientImage;
	private RatingBar ratingFeedback;
	private MyFontTextView tvTime, tvDistance, tvClientName;

	private final String TAG = "FeedbackFrament";
	private AQuery aQuery;

	// private MyFontTextView tvAmount;
	// private String paymentMode;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View feedbackFragmentView = inflater.inflate(
				R.layout.fragment_feedback, container, false);

		etFeedbackComment = (MyFontEdittextView) feedbackFragmentView
				.findViewById(R.id.etFeedbackComment);
		tvTime = (MyFontTextView) feedbackFragmentView
				.findViewById(R.id.tvFeedBackTime);
		tvDistance = (MyFontTextView) feedbackFragmentView
				.findViewById(R.id.tvFeedbackDistance);
		// tvAmount = (MyFontTextView) feedbackFragmentView
		// .findViewById(R.id.tvFeedbackAmount);
		ratingFeedback = (RatingBar) feedbackFragmentView
				.findViewById(R.id.ratingFeedback);
		ivClientImage = (ImageView) feedbackFragmentView
				.findViewById(R.id.ivFeedbackDriverImage);
		tvClientName = (MyFontTextView) feedbackFragmentView
				.findViewById(R.id.tvClientName);

		mapActivity.setActionBarTitle(mapActivity.getResources().getString(
				R.string.text_feedback));

		feedbackFragmentView.findViewById(R.id.tvFeedbackSubmit)
				.setOnClickListener(this);
		// feedbackFragmentView.findViewById(R.id.tvFeedbackSkip)
		// .setOnClickListener(this);

		return feedbackFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		aQuery = new AQuery(mapActivity);
		RequestDetail requestDetail = (RequestDetail) getArguments()
				.getSerializable(AndyConstants.REQUEST_DETAIL);
		if (!TextUtils.isEmpty(requestDetail.getClientProfile()))
			aQuery.id(ivClientImage).image(requestDetail.getClientProfile());
		else
			aQuery.id(ivClientImage).image(R.drawable.user);
		// tvTime.setText(getArguments().getString(AndyConstants.Params.TIME));
		// tvDistance.setText(getArguments().getString(
		// AndyConstants.Params.DISTANCE));

		mapActivity.showBillDialog(requestDetail.getBasePrice(),
				requestDetail.getTotal(), requestDetail.getDistanceCost(),
				requestDetail.getTimecost(), requestDetail.getDistance(),
				requestDetail.getTime(), requestDetail.getReferralBonus(),
				requestDetail.getPromoBonus(),
				requestDetail.getPricePerDistance(),
				requestDetail.getPricePerTime(), requestDetail.getUnit(),requestDetail.getPayment_type(),requestDetail.getCurrency());
		Log.d("TIME AND DISTANCE=", requestDetail.getTime() + "\n"
				+ requestDetail.getDistance());
		tvTime.setText((int) (Double.parseDouble(requestDetail.getTime()))
				+ " " + getString(R.string.text_mins));
		tvDistance.setText(new DecimalFormat("0.00").format(Double
				.parseDouble(requestDetail.getDistance()))
				+ " "
				+ requestDetail.getUnit());
		// if (preferenceHelper.getPaymentType() == AndyConstants.CASH)
		// paymentMode = getString(R.string.text_type_cash);
		// else
		// paymentMode = getString(R.string.text_type_card);
		// tvAmount.setText("$"
		// + new DecimalFormat("0.00").format(Double
		// .parseDouble(requestDetail.getAmount())) + " "
		// + paymentMode);
		tvClientName.setText(requestDetail.getClientName());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.tvFeedbackSubmit:

			// if (TextUtils.isEmpty(etFeedbackComment.getText().toString())) {
			// AndyUtils.showToast(
			// mapActivity.getResources().getString(
			// R.string.text_empty_feedback), mapActivity);
			// return;
			if (ratingFeedback.getRating() == 0) {
				AndyUtils.showToast(
						mapActivity.getResources().getString(
								R.string.text_empty_rating), mapActivity);
			} else {
				giveRating();
			}
			break;
		// case R.id.tvFeedbackSkip:
		// preferenceHelper.clearRequestData();
		// mapActivity.addFragment(new ClientRequestFragment(), false,
		// AndyConstants.CLIENT_REQUEST_TAG, true);
		// break;
		default:
			break;
		}
	}

	// giving feedback for perticular job
	private void giveRating() {
		if (!AndyUtils.isNetworkAvailable(mapActivity)) {
			AndyUtils.showToast(
					mapActivity.getResources().getString(R.string.toast_no_internet),
					mapActivity);
			return;
		}

		AndyUtils.showCustomProgressDialog(mapActivity, mapActivity.getResources()
				.getString(R.string.progress_rating), false);

		HashMap<String, String> map = new HashMap<>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.RATING);
		map.put(AndyConstants.Params.ID, preferenceHelper.getUserId());
		map.put(AndyConstants.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(AndyConstants.Params.REQUEST_ID,
				String.valueOf(preferenceHelper.getRequestId()));
		map.put(AndyConstants.Params.RATING,
				String.valueOf(ratingFeedback.getRating()));
		map.put(AndyConstants.Params.COMMENT, etFeedbackComment.getText()
				.toString().trim());

		new HttpRequester(mapActivity, map, AndyConstants.ServiceCode.RATING,
				this);
		// requestQueue.add(new VolleyHttpRequest(Method.POST, map,
		// AndyConstants.ServiceCode.RATING, this, this));
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		case AndyConstants.ServiceCode.RATING:
			AppLog.Log(TAG, "rating response" + response);
			if (parseContent.isSuccess(response)) {
				preferenceHelper.clearRequestData();
				AndyUtils.showToast(
						mapActivity.getResources().getString(
								R.string.toast_feedback_success), mapActivity);
				mapActivity.addFragment(new ClientRequestFragment(), false,
						AndyConstants.CLIENT_REQUEST_TAG, true);
			}
			break;

		default:
			break;
		}
	}
}
