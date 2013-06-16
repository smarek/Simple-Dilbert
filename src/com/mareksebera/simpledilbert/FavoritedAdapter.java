package com.mareksebera.simpledilbert;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mareksebera.simpledilbert.R;
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
		return items.get(position).getDate().getMillis();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rtnView = convertView;
		ViewHolder vh = null;
		FavoritedItem item = items.get(position);
		if (convertView == null) {
			vh = new ViewHolder();
			rtnView = inflater.inflate(R.layout.item_favorite, parent, false);
			vh.setImage((ImageView) rtnView
					.findViewById(R.id.item_favorite_image));
			vh.setDate((TextView) rtnView.findViewById(R.id.item_favorite_date));
			rtnView.setTag(vh);
		} else {
			vh = (ViewHolder) rtnView.getTag();
		}
		if (item != null) {
			imageLoader.displayImage(item.getUrl(), vh.getImage());
			vh.getDate().setText(
					item.getDate().toString(DilbertPreferences.DATE_FORMATTER));
		}
		return rtnView;
	}

	static class ViewHolder {
		private TextView date;
		private ImageView image;

		public TextView getDate() {
			return date;
		}

		public void setDate(TextView date) {
			this.date = date;
		}

		public ImageView getImage() {
			return image;
		}

		public void setImage(ImageView image) {
			this.image = image;
		}
	}

	private class FavoritedComparator implements Comparator<FavoritedItem> {

		@Override
		public int compare(FavoritedItem lhs, FavoritedItem rhs) {
			return rhs.getDate().compareTo(lhs.getDate());
		}

	}

}
