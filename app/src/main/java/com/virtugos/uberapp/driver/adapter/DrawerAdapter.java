package com.virtugos.uberapp.driver.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.virtugos.uberapp.driver.R;
import com.virtugos.uberapp.driver.model.ApplicationPages;

public class DrawerAdapter extends BaseAdapter {

	private ArrayList<ApplicationPages> arrayListApplicationPages;
	private ViewHolder holder;
	private LayoutInflater inflater;

	public DrawerAdapter(Context context,
			ArrayList<ApplicationPages> arrayListApplicationPages) {
		this.arrayListApplicationPages = arrayListApplicationPages;
		// items = context.getResources().getStringArray(R.array.menu_items);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {
		return arrayListApplicationPages.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.drawer_item, parent, false);
			holder = new ViewHolder();
			holder.tvMenuItem = (TextView) convertView
					.findViewById(R.id.tvMenuItem);
			// holder.ivMenuImage = (ImageView) convertView
			// .findViewById(R.id.ivMenuImage);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvMenuItem.setText(arrayListApplicationPages.get(position)
				.getTitle());
		// if (position == 0) {
		// aQuery.id(holder.ivMenuImage).image(R.drawable.nav_profile);
		// } else if (position == 1) {
		// aQuery.id(holder.ivMenuImage).image(R.drawable.ub__nav_history);
		// } else if (position == 2) {
		// aQuery.id(holder.ivMenuImage).image(R.drawable.promotion);
		// } else if (position == 3) {
		// aQuery.id(holder.ivMenuImage).image(R.drawable.share_menu);
		// } else if (position == (arrayListApplicationPages.size() - 1)) {
		// aQuery.id(holder.ivMenuImage).image(R.drawable.ub__nav_logout);
		// } else {
		// if (TextUtils.isEmpty(arrayListApplicationPages.get(position)
		// .getIcon())) {
		// aQuery.id(holder.ivMenuImage).image(R.drawable.taxi);
		// } else {
		// aQuery.id(holder.ivMenuImage).image(
		// arrayListApplicationPages.get(position).getIcon());
		// }

		// }
		// holder.ivMenuImage.setImageResource(images[position]);
		return convertView;
	}

	class ViewHolder {
		public TextView tvMenuItem;
		// public ImageView ivMenuImage;
	}

}
