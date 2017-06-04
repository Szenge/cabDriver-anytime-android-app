/**
 * 
 */
package com.virtugos.uberapp.driver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.hb.views.PinnedSectionListView;
import com.virtugos.uberapp.driver.adapter.HistoryAdapter;
import com.virtugos.uberapp.driver.base.ActionBarBaseActivitiy;
import com.virtugos.uberapp.driver.model.History;
import com.virtugos.uberapp.driver.parse.AsyncTaskCompleteListener;
import com.virtugos.uberapp.driver.parse.HttpRequester;
import com.virtugos.uberapp.driver.parse.ParseContent;
import com.virtugos.uberapp.driver.utills.AndyConstants;
import com.virtugos.uberapp.driver.utills.AndyUtils;
import com.virtugos.uberapp.driver.utills.AppLog;
import com.virtugos.uberapp.driver.utills.PreferenceHelper;

/**
 * @author Elluminati elluminati.in
 * 
 */
public class HistoryActivity extends ActionBarBaseActivitiy implements
		OnItemClickListener, AsyncTaskCompleteListener {

	private HistoryAdapter historyAdapter;
	private ArrayList<History> historyList;
	private PreferenceHelper preferenceHelper;
	private ParseContent parseContent;
	private ImageView tvEmptyHistory;
	private TreeSet<Integer> mSeparatorsSet = new TreeSet<>();
	private PinnedSectionListView lvHistory;
	private ArrayList<Date> dateList = new ArrayList<>();
	private ArrayList<History> historyListOrg = new ArrayList<>();
	// private ImageView tvNoHistory;
	private TextView fromDateBtn;
	private TextView toDateBtn;
	Calendar cal = Calendar.getInstance();
	private int day;
	private int month;
	private int year;
	private DatePickerDialog fromPiker;
	private OnDateSetListener dateset;
	private DatePickerDialog toPiker;
	Date fromDate, toDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		// getSupportActionBar().setTitle(getString(R.string.text_history));
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setHomeButtonEnabled(true);
		lvHistory = (PinnedSectionListView) findViewById(R.id.lvHistory);
		tvEmptyHistory = (ImageView) findViewById(R.id.tvHistoryEmpty);
		lvHistory.setOnItemClickListener(this);
		historyList = new ArrayList<>();
		preferenceHelper = new PreferenceHelper(this);
		dateList = new ArrayList<>();
		parseContent = new ParseContent(this);
		setActionBarTitle(getString(R.string.text_history));
		setActionBarIcon(R.drawable.back);
		btnNotification.setVisibility(View.GONE);

		historyListOrg = new ArrayList<>();

		historyAdapter = new HistoryAdapter(this, historyListOrg,
				mSeparatorsSet);
		lvHistory.setAdapter(historyAdapter);

		day = cal.get(Calendar.DAY_OF_MONTH);
		month = cal.get(Calendar.MONTH);
		year = cal.get(Calendar.YEAR);
		findViewById(R.id.btnSearch).setOnClickListener(this);
		fromDateBtn = (TextView) findViewById(R.id.fromDateBtn);
		toDateBtn = (TextView) findViewById(R.id.toDateBtn);
		fromDateBtn.setOnClickListener(this);
		toDateBtn.setOnClickListener(this);
		fromDate = new Date();
		toDate = new Date();

		getHistory();

		dateset = new OnDateSetListener() {
			private String userDate;

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				userDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
				try {
					if (view == fromPiker.getDatePicker()) {
						fromDateBtn.setText(userDate);
						fromDate = sdf.parse(userDate);
					} else {
						toDateBtn.setText(userDate);
						toDate = sdf.parse(userDate);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		};

		fromPiker = new DatePickerDialog(this, dateset, year, month, day);
		fromPiker.getDatePicker().setMaxDate(System.currentTimeMillis());
		toPiker = new DatePickerDialog(this, dateset, year, month, day);
		toPiker.getDatePicker().setMaxDate(System.currentTimeMillis());
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

	private void getHistory() {
		if (!AndyUtils.isNetworkAvailable(this)) {
			AndyUtils.showToast(
					getResources().getString(R.string.toast_no_internet), this);
			return;
		}
		AndyUtils.showCustomProgressDialog(this,
				getResources().getString(R.string.progress_getting_history),
				false);
		AppLog.Log("Histoy", "UserId : " + preferenceHelper.getUserId()
				+ " Tocken : " + preferenceHelper.getSessionToken());
		HashMap<String, String> map = new HashMap<>();

		if (fromDateBtn.getText().toString()
				.equals(getString(R.string.text_select_date))
				&& toDateBtn.getText().toString()
						.equals(getString(R.string.text_select_date))) {
			map.put(AndyConstants.URL,
					AndyConstants.ServiceType.HISTORY + AndyConstants.Params.ID
							+ "=" + preferenceHelper.getUserId() + "&"
							+ AndyConstants.Params.TOKEN + "="
							+ preferenceHelper.getSessionToken());
		} else {

			map.put(AndyConstants.URL,
					AndyConstants.ServiceType.HISTORY + AndyConstants.Params.ID
							+ "=" + preferenceHelper.getUserId() + "&"
							+ AndyConstants.Params.TOKEN + "="
							+ preferenceHelper.getSessionToken() + "&"
							+ AndyConstants.Params.FROM_DATE + "="
							+ fromDateBtn.getText().toString() + "&"
							+ AndyConstants.Params.TO_DATE + "="
							+ toDateBtn.getText().toString());
		}
		new HttpRequester(this, map, AndyConstants.ServiceCode.HISTORY, true,
				this);
		// requestQueue.add(new VolleyHttpRequest(Method.GET, map,
		// AndyConstants.ServiceCode.HISTORY, this, this));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (mSeparatorsSet.contains(position))
			return;

		History history = historyListOrg.get(position);
		Intent detailIntent = new Intent(this, HistoryDetailsActivity.class);
		detailIntent.putExtra(AndyConstants.HISTORY_DETAILS, history);
		startActivity(detailIntent);
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		case AndyConstants.ServiceCode.HISTORY:
			AppLog.Log("TAG", "History Response :" + response + "response");
			if (!parseContent.isSuccess(response)) {
				return;
			}
			historyListOrg.clear();
			historyList.clear();
			dateList.clear();
			parseContent.parseHistory(response, historyList);

			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				final Calendar cal = Calendar.getInstance();

				parseContent.parseHistory(response, historyList);

				Collections.sort(historyList, new Comparator<History>() {
					@Override
					public int compare(History o1, History o2) {

						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd hh:mm:ss");
						try {

							String firstStrDate = o1.getDate();
							String secondStrDate = o2.getDate();
							Date date2 = dateFormat.parse(secondStrDate);
							Date date1 = dateFormat.parse(firstStrDate);
							int value = date2.compareTo(date1);
							return value;
						} catch (ParseException e) {
							e.printStackTrace();
						}
						return 0;
					}
				});
				HashSet<Date> listToSet = new HashSet<>();

				for (int i = 0; i < historyList.size(); i++) {
					AppLog.Log("date", historyList.get(i).getDate() + "");
					if (listToSet.add(sdf.parse(historyList.get(i).getDate()))) {
						dateList.add(sdf.parse(historyList.get(i).getDate()));
					}

				}

				for (int i = 0; i < dateList.size(); i++) {

					cal.setTime(dateList.get(i));
					History item = new History();
					item.setDate(sdf.format(dateList.get(i)));
					historyListOrg.add(item);

					mSeparatorsSet.add(historyListOrg.size() - 1);
					for (int j = 0; j < historyList.size(); j++) {
						Calendar messageTime = Calendar.getInstance();
						messageTime.setTime(sdf.parse(historyList.get(j)
								.getDate()));
						if (cal.getTime().compareTo(messageTime.getTime()) == 0) {
							historyListOrg.add(historyList.get(j));
						}
					}
				}
				if (historyList.size() > 0) {
					lvHistory.setVisibility(View.VISIBLE);
					tvEmptyHistory.setVisibility(View.GONE);

				} else {
					lvHistory.setVisibility(View.GONE);
					tvEmptyHistory.setVisibility(View.VISIBLE);
				}
				historyAdapter = new HistoryAdapter(this, historyListOrg,
						mSeparatorsSet);
				lvHistory.setAdapter(historyAdapter);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			AndyUtils.removeCustomProgressDialog();
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnActionNotification:
			onBackPressed();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;
		case R.id.fromDateBtn:

			fromPiker.show();
			break;
		case R.id.toDateBtn:

			toPiker.show();
			break;
		case R.id.btnSearch:
			if (toDate.before(fromDate)) {
				AndyUtils.showToast(
						getResources()
								.getString(R.string.validate_history_date),
						getApplicationContext());
				return;
			} else {
				getHistory();
			}
			break;
		case R.id.btnActionMenu:
			onBackPressed();
			break;

		default:
			break;
		}
	}
}
