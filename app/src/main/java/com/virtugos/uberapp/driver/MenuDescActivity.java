package com.virtugos.uberapp.driver;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.virtugos.uberapp.driver.base.ActionBarBaseActivitiy;
import com.virtugos.uberapp.driver.utills.AndyConstants;

/**
 * @author Elluminati elluminati.in
 * 
 */
public class MenuDescActivity extends ActionBarBaseActivitiy {
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sidemenu_discript);
		webView = (WebView) findViewById(R.id.wvDesc);
		webView.loadData(
				getIntent().getStringExtra(AndyConstants.Params.CONTENT),
				"text/html", "utf-8");
		setActionBarIcon(R.drawable.back);
		btnNotification.setVisibility(View.GONE);

		tvTitle.setText(getIntent().getStringExtra(AndyConstants.Params.TITLE));

		// getSupportActionBar().setTitle(
		// getIntent().getStringExtra(AndyConstants.Params.TITLE));
		// getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setHomeButtonEnabled(true);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnActionMenu:
			onBackPressed();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			break;

		default:
			break;
		}
	}
}
