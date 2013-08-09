package com.mareksebera.simpledilbert;

import java.io.File;
import java.io.FileOutputStream;

import org.joda.time.DateMidnight;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class DilbertFragment extends SherlockFragment {

	private static final int MENU_SAVE = -1, MENU_FAVORITE = -2,
			MENU_ZOOM = -3, MENU_SHARE = -4, MENU_REFRESH = -5;
	public static final String ARGUMENT_DATE = "string_ARGUMENT_DATE";
	private PhotoView image;
	private ProgressBar progress;
	private DilbertPreferences preferences;
	private GetStripUrl loadTask;

	@Override
	public void onDestroyView() {
		progress = null;
		image = null;
		super.onDestroyView();
	}

	private GetStripUrlInterface getStripUrilListener = new GetStripUrlInterface() {

		@Override
		public void imageLoadFailed(String url, FailReason reason) {
			dilbertImageLoadingListener.onLoadingFailed(url, image, reason);
		}

		@Override
		public void displayImage(String url) {
			if (image == null)
				return;
			boolean hqIsEnabled = preferences.isHighQualityOn();
			url = hqIsEnabled ? preferences.toHighQuality(url) : preferences
					.toLowQuality(getDateFromArguments(), url);
			ImageLoader.getInstance().displayImage(url, image,
					dilbertImageLoadingListener);
		}
	};

	private ImageLoadingListener dilbertImageLoadingListener = new ImageLoadingListener() {

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			if (image != null)
				image.setImageResource(R.drawable.cancel);
			if (progress != null)
				progress.setVisibility(View.GONE);
		}

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (progress != null)
				progress.setVisibility(View.GONE);
			if (image != null)
				image.setVisibility(View.VISIBLE);
		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			if (progress != null)
				progress.setVisibility(View.GONE);
			if (image != null) {
				image.setVisibility(View.VISIBLE);
				image.setImageResource(R.drawable.cancel);
			}
			if (getSherlockActivity() != null)
				Toast.makeText(getSherlockActivity(),
						R.string.loading_exception_error, Toast.LENGTH_SHORT)
						.show();
		}

		@Override
		public void onLoadingStarted(String imageUri, View view) {
			if (image != null)
				image.setVisibility(View.GONE);
			if (progress != null)
				progress.setVisibility(View.VISIBLE);
		}
	};
	private OnLongClickListener imageLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			try {
				((DilbertFragmentInterface) getSherlockActivity())
						.toggleActionBar();
			} catch (Throwable t) {
				Log.e("DilbertFragment", "Toggle ActionBar failed", t);
			}
			return true;
		}
	};
	private OnPhotoTapListener photoTapListener = new OnPhotoTapListener() {

		@Override
		public void onPhotoTap(View view, float x, float y) {
			refreshAction();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.preferences = new DilbertPreferences(getActivity());
		setHasOptionsMenu(true);
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
		this.image = (PhotoView) fragment.findViewById(R.id.fragment_imageview);
		this.image.setOnLongClickListener(imageLongClickListener);
		fragment.setOnLongClickListener(imageLongClickListener);
		this.image.setOnPhotoTapListener(photoTapListener);
		this.progress = (ProgressBar) fragment
				.findViewById(R.id.fragment_progressbar);
		this.loadTask = new GetStripUrl(getStripUrilListener, preferences,
				getDateFromArguments());
		this.image.setVisibility(View.GONE);
		this.loadTask.execute();
		return fragment;
	}

	@Override
	public void onStop() {
		ImageLoader.getInstance().cancelDisplayTask(this.image);
		super.onStop();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (menu.findItem(MENU_FAVORITE) != null) {
			MenuItem favorite = menu.findItem(MENU_FAVORITE);
			modifyFavoriteItem(favorite);
		}
	}

	private void modifyFavoriteItem(MenuItem favorite) {
		boolean isFavorite = preferences.isFavorited(getDateFromArguments());
		favorite.setTitle(isFavorite ? R.string.menu_favorite_remove
				: R.string.menu_favorite_add);
		favorite.setIcon(isFavorite ? R.drawable.ic_menu_favorited
				: R.drawable.ic_menu_not_favorited);
	}

	private int zoomLevel = 0;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_FAVORITE:
			preferences.toggleIsFavorited(getDateFromArguments());
			modifyFavoriteItem(item);
			return true;
		case MENU_ZOOM:
			if (image != null && image.canZoom()) {
				int center_x = image.getWidth() / 2;
				int center_y = image.getHeight() / 2;
				switch (zoomLevel) {
				case 0:
					image.zoomTo(image.getMidScale(), center_x, center_y);
					break;
				case 1:
					image.zoomTo(image.getMaxScale(), center_x, center_y);
					break;
				case 2:
					image.zoomTo(image.getMinScale(), center_x, center_y);
					break;
				}
				zoomLevel = (zoomLevel + 1) % 3;
			}
			return true;
		case MENU_SHARE:
			shareCurrentStrip();
			return true;
		case MENU_REFRESH:
			refreshAction();
			break;
		case MENU_SAVE:
			preferences.downloadImageViaManager(getSherlockActivity(),
					preferences.getCachedUrl(getDateFromArguments()),
					getDateFromArguments());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshAction() {
		preferences.removeCache(getDateFromArguments());
		if (this.loadTask != null
				&& this.loadTask.getStatus() != Status.PENDING) {
			this.loadTask = new GetStripUrl(getStripUrilListener, preferences,
					getDateFromArguments(), progress);
		}
		this.loadTask.execute();
	}

	private void shareCurrentStrip() {
		String url = preferences.isHighQualityOn() ? preferences
				.toHighQuality(preferences.getCachedUrl(getDateFromArguments()))
				: preferences.toLowQuality(getDateFromArguments(),
						preferences.getCachedUrl(getDateFromArguments()));
		if (url == null)
			return;
		ImageLoader.getInstance().loadImage(url, new ImageLoadingListener() {

			@Override
			public void onLoadingStarted(String imageUri, View view) {
			}

			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				shareBitmap(null);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				shareBitmap(loadedImage);
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				shareBitmap(null);
			}

			private void shareBitmap(Bitmap b) {
				try {
					String date = getDateFromArguments().toString(
							DilbertPreferences.DATE_FORMATTER);
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("image/jpeg");
					i.putExtra(Intent.EXTRA_SUBJECT, "Dilbert " + date
							+ " #simpledilbert");
					if (preferences.isSharingImage()) {
						i.putExtra(Intent.EXTRA_TEXT, "Dilbert " + date
								+ " #simpledilbert");
						if (b != null) {
							File tmp = File
									.createTempFile("dilbert_", ".jpg",
											getSherlockActivity()
													.getExternalCacheDir());
							FileOutputStream out = new FileOutputStream(tmp);
							b.compress(CompressFormat.JPEG, 100, out);
							out.close();
							Uri u = Uri.parse("file://" + tmp.getAbsolutePath());
							i.putExtra(Intent.EXTRA_STREAM, u);
						}
					} else {
						i.putExtra(
								Intent.EXTRA_TEXT,
								"Dilbert "
										+ date
										+ " #simpledilbert http://dilbert.com/strips/comic/"
										+ date);
					}
					startActivity(Intent.createChooser(i,
							getString(R.string.share_chooser)));

				} catch (Exception e) {
					if (getSherlockActivity() != null)
						Toast.makeText(getSherlockActivity(),
								R.string.loading_exception_error,
								Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		int category = 0;
		menu.add(category, MENU_FAVORITE, 1, R.string.menu_favorite_remove)
				.setIcon(R.drawable.ic_menu_not_favorited)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(category, MENU_ZOOM, 4, R.string.menu_zoom)
				.setIcon(R.drawable.ic_menu_zoom)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(category, MENU_SAVE, 3, R.string.menu_download)
				.setIcon(R.drawable.ic_menu_save)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(category, MENU_SHARE, 2, R.string.menu_share)
				.setIcon(R.drawable.ic_menu_share)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(category, MENU_REFRESH, 5, R.string.menu_refresh)
				.setIcon(R.drawable.ic_menu_refresh)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
	}

}
