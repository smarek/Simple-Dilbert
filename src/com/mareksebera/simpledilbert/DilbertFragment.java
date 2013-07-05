package com.mareksebera.simpledilbert;

import org.joda.time.DateMidnight;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class DilbertFragment extends SherlockFragment {

	public static final String ARGUMENT_DATE = "string_ARGUMENT_DATE";
	private ImageView image;
	private ProgressBar progress;
	private DilbertPreferences preferences;
	private GetStripUrl loadTask;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private GetStripUrlInterface getStripUrilListener = new GetStripUrlInterface() {

		@Override
		public void imageLoadFailed(String url, FailReason reason) {
			dilbertImageLoadingListener.onLoadingFailed(url, image, reason);
		}

		@Override
		public void displayImage(String url) {
			boolean hqIsEnabled = preferences.isHighQualityOn();
			url = hqIsEnabled ? preferences.toHighQuality(url) : preferences
					.toLowQuality(url);
			imageLoader.displayImage(url, image, dilbertImageLoadingListener);
		}
	};

	private ImageLoadingListener dilbertImageLoadingListener = new ImageLoadingListener() {

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			Log.d("onLoadingCancelled", "onLoadingCancelled");
			image.setImageResource(R.drawable.cancel);
			progress.setVisibility(View.GONE);
		}

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			Log.d("onLoadingComplete", "onLoadingComplete");
			progress.setVisibility(View.GONE);
		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			Log.d("onLoadingFailed", "onLoadingFailed");
			progress.setVisibility(View.GONE);
			image.setImageResource(R.drawable.cancel);
			Toast.makeText(getActivity(), R.string.loading_exception_error,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onLoadingStarted(String imageUri, View view) {
			Log.d("onLoadingStarted", "onLoadingStarted");
			progress.setVisibility(View.VISIBLE);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.preferences = new DilbertPreferences(getActivity());
	}

	private DateMidnight getDateFromArguments() {
		return DateMidnight.parse(getArguments().getString(ARGUMENT_DATE),
				DilbertPreferences.DATE_FORMATTER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragment = inflater.inflate(R.layout.fragment_dilbert, container,
				false);
		this.image = (ImageView) fragment.findViewById(R.id.fragment_imageview);
		this.progress = (ProgressBar) fragment
				.findViewById(R.id.fragment_progressbar);
		this.loadTask = new GetStripUrl(getStripUrilListener, preferences,
				getDateFromArguments());
		this.loadTask.execute();
		return fragment;
	}

}
