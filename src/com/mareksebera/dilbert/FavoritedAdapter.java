package com.mareksebera.dilbert;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class FavoritedAdapter extends BaseAdapter {

	private List<FavoritedItem> items;
	private LayoutInflater inflater;
	private ImageLoader imageLoader = ImageLoader.getInstance();

	public FavoritedAdapter(Context ctx, List<FavoritedItem> favorited) {
		inflater = LayoutInflater.from(ctx);
		Collections.sort(favorited, new FavoritedComparator());
		items = favorited;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).date.getMillis();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		FavoritedItem item = items.get(position);
		if (convertView == null) {
			vh = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_favorite, parent,
					false);
			vh.image = (EnhancedImageView) convertView
					.findViewById(R.id.item_favorite_image);
			vh.date = (TextView) convertView
					.findViewById(R.id.item_favorite_date);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		imageLoader.displayImage(item.url, vh.image);
		vh.date.setText(item.date.toString(DilbertPreferences.dateFormatter));
		return convertView;
	}

	static class ViewHolder {
		TextView date;
		EnhancedImageView image;
	}

	private class FavoritedComparator implements Comparator<FavoritedItem> {

		@Override
		public int compare(FavoritedItem lhs, FavoritedItem rhs) {
			return lhs.date.compareTo(rhs.date);
		}

	}

}
