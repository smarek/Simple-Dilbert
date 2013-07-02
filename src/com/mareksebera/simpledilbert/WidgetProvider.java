package com.mareksebera.simpledilbert;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class WidgetProvider extends AppWidgetProvider {

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
		Log.d(TAG, "updateAppWidget for " + appWidgetId);

		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		views.setOnClickPendingIntent(R.id.widget_previous,
				getPendingIntent(INTENT_PREVIOUS, context, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_next,
				getPendingIntent(INTENT_NEXT, context, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_latest,
				getPendingIntent(INTENT_LATEST, context, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_random,
				getPendingIntent(INTENT_RANDOM, context, appWidgetId));

		final DilbertPreferences prefs = new DilbertPreferences(context);
		final DateMidnight currentDate = prefs.getDateForWidgetId(appWidgetId);
		final String cachedUrl = prefs.getCachedUrl(currentDate);
		views.setViewVisibility(R.id.widget_progress, View.VISIBLE);
		appWidgetManager.updateAppWidget(appWidgetId, views);
		if (cachedUrl == null) {
			new GetStripUrl(new GetStripUrlInterface() {

				@Override
				public void imageLoadFailed(String url, FailReason reason) {
					Log.d(TAG, "url: " + url);
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
								views.setTextViewText(
										R.id.widget_title,
										prefs.getDateForWidgetId(appWidgetId)
												.toString(
														DilbertPreferences.DATE_FORMATTER));
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
		final String action = intent.getAction();
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
		if (action.equals(INTENT_PREVIOUS)) {
			preferences.saveDateForWidgetId(appWidgetId, preferences
					.getDateForWidgetId(appWidgetId).minusDays(1));
		} else if (action.equals(INTENT_NEXT)) {
			preferences.saveDateForWidgetId(appWidgetId, preferences
					.getDateForWidgetId(appWidgetId).plusDays(1));
		} else if (action.equals(INTENT_LATEST)) {
			preferences.saveDateForWidgetId(appWidgetId, DateMidnight.now());
		} else if (action.equals(INTENT_RANDOM)) {
			preferences.saveDateForWidgetId(appWidgetId,
					DilbertPreferences.getRandomDateMidnight());
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
