package com.mareksebera.simpledilbert;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public final class WidgetProvider extends AppWidgetProvider {

	public static final String TAG = "Dilbert Widget";
	static {
		/**
		 * Set default time-zone, because strips are published in New York
		 * timezone on midnight
		 * */
		DateTimeZone.setDefault(DilbertPreferences.TIME_ZONE);
	}
	private static final String INTENT_PREVIOUS = "com.mareksebera.simpledilbert.widget.PREVIOUS";
	private static final String INTENT_NEXT = "com.mareksebera.simpledilbert.widget.NEXT";
	private static final String INTENT_LATEST = "com.mareksebera.simpledilbert.widget.LATEST";
	private static final String INTENT_RANDOM = "com.mareksebera.simpledilbert.widget.RANDOM";
	private static final String INTENT_REFRESH = "com.mareksebera.simpledilbert.widget.REFRESH";
	private static final String INTENT_DISPLAY = "com.mareksebera.simpledilbert.widget.DISPLAY";

	private static Toast currentToast = null;

	private static PendingIntent getPendingIntent(String INTENT,
			Context context, int appWidgetId) {
		Intent intent = new Intent(INTENT);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		return PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	static void updateAppWidget(final Context context,
			final AppWidgetManager appWidgetManager, final int appWidgetId) {
		final DilbertPreferences prefs = new DilbertPreferences(context);
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		views.setInt(R.id.widget_layout, "setBackgroundColor",
				prefs.isDarkWidgetLayoutEnabled() ? Color.BLACK : Color.WHITE);
		views.setOnClickPendingIntent(R.id.widget_previous,
				getPendingIntent(INTENT_PREVIOUS, context, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_next,
				getPendingIntent(INTENT_NEXT, context, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_latest,
				getPendingIntent(INTENT_LATEST, context, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_random,
				getPendingIntent(INTENT_RANDOM, context, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_image,
				getPendingIntent(INTENT_DISPLAY, context, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_refresh,
				getPendingIntent(INTENT_REFRESH, context, appWidgetId));

		final DateMidnight currentDate = prefs.getDateForWidgetId(appWidgetId);
		final String cachedUrl = prefs.getCachedUrl(currentDate);
		views.setViewVisibility(R.id.widget_progress, View.VISIBLE);
		views.setTextViewText(
				R.id.widget_title,
				prefs.getDateForWidgetId(appWidgetId)
						.toString(
								DilbertPreferences.DATE_FORMATTER));
		appWidgetManager.updateAppWidget(appWidgetId, views);
		if (cachedUrl == null) {
			new GetStripUrl(new GetStripUrlInterface() {

				@Override
				public void imageLoadFailed(String url, FailReason reason) {
					Toast.makeText(context, "Image Loading failed",
							Toast.LENGTH_SHORT).show();
					views.setImageViewResource(R.id.widget_image,
							R.drawable.cancel);
					views.setViewVisibility(R.id.widget_progress, View.GONE);
					appWidgetManager.updateAppWidget(appWidgetId, views);
				}

				@Override
				public void displayImage(String url) {
					updateAppWidget(context, appWidgetManager, appWidgetId);
				}
			}, prefs, currentDate).execute();
		} else {
			ImageLoader.getInstance().loadImage(cachedUrl,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							if (imageUri.equals(prefs.getCachedUrl(prefs
									.getDateForWidgetId(appWidgetId)))) {
								views.setViewVisibility(R.id.widget_progress,
										View.GONE);
								views.setImageViewBitmap(R.id.widget_image,
										loadedImage);
								appWidgetManager.updateAppWidget(appWidgetId,
										views);
							}
						}
					});
		}

	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		AppController.configureImageLoader(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		final int appWidgetId = intent
				.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID) ? intent
				.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) : -1;
		final DilbertPreferences preferences = new DilbertPreferences(context);
		if (action == null || appWidgetId == -1) {
			super.onReceive(context, intent);
			return;
		}
		if (currentToast != null)
			currentToast.cancel();
		if (INTENT_PREVIOUS.equals(action)) {
			preferences.saveDateForWidgetId(appWidgetId, preferences
					.getDateForWidgetId(appWidgetId).minusDays(1));
		} else if (INTENT_NEXT.equals(action)) {
			preferences.saveDateForWidgetId(appWidgetId, preferences
					.getDateForWidgetId(appWidgetId).plusDays(1));
		} else if (INTENT_LATEST.equals(action)) {
			preferences.saveDateForWidgetId(appWidgetId,
					DateMidnight.now(DilbertPreferences.TIME_ZONE));
		} else if (INTENT_RANDOM.equals(action)) {
			preferences.saveDateForWidgetId(appWidgetId,
					DilbertPreferences.getRandomDateMidnight());
		} else if (INTENT_REFRESH.equals(action)) {
			preferences
					.removeCache(preferences.getDateForWidgetId(appWidgetId));
		} else if (INTENT_DISPLAY.equals(action)) {
			preferences.saveCurrentDate(preferences
					.getDateForWidgetId(appWidgetId));
			Intent display = new Intent(context, DilbertFragmentActivity.class);
			display.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(display);
		} else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
			DateMidnight current = preferences.getDateForWidgetId(appWidgetId);
			if (current.equals(DateMidnight.now(DilbertPreferences.TIME_ZONE)
					.minusDays(1))) {
				preferences.saveDateForWidgetId(appWidgetId,
						DateMidnight.now(DilbertPreferences.TIME_ZONE));
			}
		}
		updateAppWidget(context, AppWidgetManager.getInstance(context),
				appWidgetId);
		if (currentToast != null)
			currentToast.show();
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		final int widgetCount = appWidgetIds.length;
		for (int i = 0; i < widgetCount; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		if (appWidgetIds == null)
			return;
		DilbertPreferences prefs = new DilbertPreferences(context);
		for (int widgetId : appWidgetIds) {
			prefs.deleteDateForWidgetId(widgetId);
		}
	}
}
