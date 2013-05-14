package com.mareksebera.dilbert;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class ImageZoomActivity extends SherlockActivity {

	public static final String IN_IMAGE_URL = "string_image_url";
	public static final String IN_IMAGE_DATE = "string_image_date";
	private ImageView imageView;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private PhotoViewAttacher imageViewAttacher;
	private ImageLoadingListener imageLoadingListener = new ImageLoadingListener() {

		@Override
		public void onLoadingStarted(String imageUri, View view) {
		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			imageViewAttacher.update();
		}

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			imageViewAttacher.update();
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			imageViewAttacher.update();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!getIntent().hasExtra(IN_IMAGE_URL)
				|| !getIntent().hasExtra(IN_IMAGE_DATE)) {
			finish();
			return;
		}
		setTitle(getIntent().getStringExtra(IN_IMAGE_DATE));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		imageView = new ImageView(this);
		setContentView(imageView);
		imageViewAttacher = new PhotoViewAttacher(imageView);
		imageLoader.displayImage(getIntent().getStringExtra(IN_IMAGE_URL),
				imageView, imageLoadingListener);
		Toast.makeText(this, R.string.zoom_hint, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
