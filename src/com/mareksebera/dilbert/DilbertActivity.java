package com.mareksebera.dilbert;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.CharArrayBuffer;
import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

/**
 * This code is roughly optimized and for sure could be improved.
 * 
 * I just needed better version of Dilbert reader than "Quick Dilbert Reader",
 * so I wrote one myself.
 * 
 * Links to images are parsed from dilbert website directly, as I haven't found
 * any better solution.
 * 
 * Supports caching images (Universal Image Loader feature) and caching parsing
 * ({@link #loadCachedUrl(String)}, {@link #saveCurrentUrl(String, String)},
 * {@link #getLastUrl()})
 * */
public class DilbertActivity extends SherlockActivity implements
		OnDateSetListener, SwipeInterface, ImageLoadingListener {

	private static final int MENU_DATEPICKER = 1, MENU_ABOUT = 2,
			MENU_LATEST = 3, MENU_REFRESH = 4, MENU_LICENSE = 5,
			MENU_HIGHQUALITY = 6;
	private static final String PREF_CURRENT_DATE = "dilbert_current_date";
	private static final String PREF_CURRENT_URL = "dilbert_current_url";
	private static final String PREF_HIGH_QUALITY_ENABLED = "dilbert_use_high_quality";
	private DateMidnight currentDate;
	private DateTimeFormatter dateFormatter = DateTimeFormat
			.forPattern("yyyy-MM-dd");

	private EnhancedImageView imageView;

	private ProgressBar progressBar;
	private FrameLayout layout;

	@Override
	public void bottom2top(View v) {
	}

	private void configureImageLoader() {
		DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
				.cacheInMemory().cacheOnDisc().build();
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
				this).defaultDisplayImageOptions(displayOptions).build();
		ImageLoader.getInstance().init(configuration);
	}

	public void displayImage(String url) {
		if (url != null) {
			if (isHighQualityOn() && !url.contains("zoom")) {
				url = url.replace(".gif", ".zoom.gif");
			} else if (!isHighQualityOn() && url.contains("zoom")) {
				url = url.replace(".zoom.gif", ".gif");
			}
			ImageLoader.getInstance().displayImage(url, imageView,
					DilbertActivity.this);
		}
	}

	private void getCurrentDate() {
		String savedDate = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(PREF_CURRENT_DATE, null);
		if (savedDate == null)
			currentDate = DateMidnight.now();
		else
			currentDate = DateMidnight.parse(savedDate, dateFormatter);
	}

	public String getLastUrl() {
		return PreferenceManager.getDefaultSharedPreferences(this).getString(
				PREF_CURRENT_URL, null);
	}

	private CharSequence getLicenseText() {
		String rtn = "";
		try {
			InputStream stream = getAssets().open("LICENSE.txt");
			java.util.Scanner s = new java.util.Scanner(stream)
					.useDelimiter("\\A");
			rtn = s.hasNext() ? s.next() : "";
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		return rtn;
	}

	private void initLayout() {
		imageView = (EnhancedImageView) findViewById(R.id.imageview);
		progressBar = (ProgressBar) findViewById(R.id.progressbar);
		layout = (FrameLayout) findViewById(R.id.framelayout);
		layout.setOnTouchListener(new ActivitySwipeDetector(this));
	}

	private boolean isHighQualityOn() {
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				PREF_HIGH_QUALITY_ENABLED, true);
	}

	@Override
	public void left2right(View v) {
		if (!currentDate
				.equals(DateMidnight.parse("1989-04-16", dateFormatter)))
			setCurrentDate(currentDate.minusDays(1));
		else
			Toast.makeText(this, R.string.no_older_strip, Toast.LENGTH_SHORT)
					.show();
	}

	private boolean loadCachedUrl(String dateKey) {
		String cached = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(dateKey, null);
		if (cached == null) {
			return false;
		} else {
			displayImage(cached);
			return true;
		}
	}

	private void loadImage() {
		String dateKey = currentDate.toString(dateFormatter);
		if (!loadCachedUrl(dateKey))
			new GetStripUrl().execute(dateKey);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dilbert);
		configureImageLoader();
		getCurrentDate();
		initLayout();
		displayImage(getLastUrl());
		setCurrentDate(currentDate);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_DATEPICKER, Menu.NONE,
				R.string.menu_datepicker)
				.setIcon(R.drawable.ic_menu_datepicker)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, MENU_REFRESH, Menu.NONE, R.string.menu_refresh)
				.setIcon(R.drawable.ic_menu_refresh)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.menu_about)
				.setIcon(R.drawable.ic_menu_about)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(Menu.NONE, MENU_HIGHQUALITY, Menu.NONE, "Vysoká kvalita")
				.setCheckable(true).setChecked(isHighQualityOn());
		menu.add(Menu.NONE, MENU_LATEST, Menu.NONE, R.string.menu_latest)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(Menu.NONE, MENU_LICENSE, Menu.NONE, R.string.menu_license)
				.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		DateMidnight selDate = DateMidnight.parse(
				String.format("%d-%d-%d", year, monthOfYear + 1, dayOfMonth),
				dateFormatter);
		if (!selDate.equals(currentDate))
			setCurrentDate(selDate);
	}

	@Override
	public void onLoadingCancelled(String imageUri, View view) {
		imageView.setImageResource(R.drawable.cancel);
		Toast.makeText(this, R.string.loading_interrupted, Toast.LENGTH_SHORT)
				.show();
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onLoadingFailed(String imageUri, View view,
			FailReason failReason) {
		imageView.setImageResource(R.drawable.cancel);
		Toast.makeText(this, R.string.loading_exception_error,
				Toast.LENGTH_SHORT).show();
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onLoadingStarted(String imageUri, View view) {
		progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DATEPICKER:
			showDatePicker();
			return true;
		case MENU_ABOUT:
			showAboutDialog();
			return true;
		case MENU_LATEST:
			setCurrentDate(DateMidnight.now());
			return true;
		case MENU_REFRESH:
			removeCache(currentDate);
			setCurrentDate(currentDate);
			return true;
		case MENU_LICENSE:
			showLicenseDialog();
			return true;
		case MENU_HIGHQUALITY:
			toggleHighQuality();
			loadImage();
			return true;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu.findItem(MENU_HIGHQUALITY) != null)
			menu.findItem(MENU_HIGHQUALITY).setChecked(isHighQualityOn());
		return true;
	}

	private void putDateToTitle() {
		setTitle(currentDate.toString(dateFormatter));
	}

	private void removeCache(DateMidnight currentDate) {
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.remove(currentDate.toString(dateFormatter)).commit();
	}

	@Override
	public void right2left(View v) {
		if (!currentDate.equals(DateMidnight.now()))
			setCurrentDate(currentDate.plusDays(1));
		else
			Toast.makeText(this, R.string.no_newer_strip, Toast.LENGTH_SHORT)
					.show();
	}

	private void saveCurrentDate() {
		PreferenceManager
				.getDefaultSharedPreferences(this)
				.edit()
				.putString(PREF_CURRENT_DATE,
						currentDate.toString(dateFormatter)).commit();
	}

	public void saveCurrentUrl(String date, String s) {
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putString(PREF_CURRENT_URL, s);
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putString(date, s).commit();
	}

	private void setCurrentDate(DateMidnight newDate) {
		currentDate = newDate;
		saveCurrentDate();
		putDateToTitle();
		loadImage();
	}

	private void showAboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.about_title);
		builder.setMessage(Html.fromHtml(getString(R.string.about_contents)));
		builder.setNeutralButton(android.R.string.cancel,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	private void showDatePicker() {
		Calendar c = Calendar.getInstance();
		c.setTime(currentDate.toDate());
		DatePickerDialog dialog = new DatePickerDialog(this, this,
				c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH));
		dialog.show();
	}

	private void showLicenseDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.apache_license_2_0);
		builder.setMessage(getLicenseText());
		builder.setNeutralButton(android.R.string.cancel,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	private void toggleHighQuality() {
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putBoolean(PREF_HIGH_QUALITY_ENABLED, !isHighQualityOn())
				.commit();
	}

	@Override
	public void top2bottom(View v) {
	}

	/**
	 * Taken from EntityUtils HttpCore 4.2.3 and altered so the utf-8lias is
	 * handled correctly
	 * */
	public static String toString(final HttpEntity entity) throws IOException,
			ParseException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = entity.getContent();
		if (instream == null) {
			return null;
		}
		try {
			if (entity.getContentLength() > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						"HTTP entity too large to be buffered in memory");
			}
			int i = (int) entity.getContentLength();
			if (i < 0) {
				i = 4096;
			}
			Charset charset = Charset.defaultCharset();
			Reader reader = new InputStreamReader(instream, charset);
			CharArrayBuffer buffer = new CharArrayBuffer(i);
			char[] tmp = new char[1024];
			int l;
			while ((l = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
			return buffer.toString();
		} finally {
			instream.close();
		}
	}

	private class GetStripUrl extends AsyncTask<String, Void, String> {

		private String date = null;

		@Override
		protected String doInBackground(String... params) {
			if (params.length == 0)
				return null;
			date = params[0];
			HttpGet get = new HttpGet("http://dilbert.com/strips/comic/"
					+ params[0] + "/");
			try {
				HttpClient client = new DefaultHttpClient();
				HttpResponse response = client.execute(get);
				return DilbertActivity.toString(response.getEntity());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				for (String s : FindUrls.extractUrls(result)) {
					if (s.endsWith(".strip.gif") || s.endsWith(".sunday.gif")) {
						s = s.replace(".strip.gif", ".strip.zoom.gif");
						s = s.replace(".sunday.gif", ".strip.zoom.gif");
						s = s.replace(".strip.strip", ".strip");
						saveCurrentUrl(date, s);
						displayImage(s);
						return;
					}
				}
			}
			onLoadingFailed(null, null, null);
		}

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

	}

}
