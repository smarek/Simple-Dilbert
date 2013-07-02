package com.mareksebera.simpledilbert;

import org.joda.time.DateMidnight;

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
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class WidgetProvider extends AppWidgetProvider {

	/**
	 * TODO: Widget configuration class, allow rotation +-90 TODO: Add widget
	 * buttons for browsing and launching internal classes (zoom, favorite)
	 * */

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		final int widgetCount = appWidgetIds.length;
		for (int i = 0; i < widgetCount; i++) {
			int appWidgetId = appWidgetIds[i];
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	@Override
	public void onEnabled(Context context) {
		AppController.configureImageLoader(context);
	}

	public static final String TAG = "Dilbert Widget";
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
			currentToast = Toast.makeText(context, "Showing Previous",
					Toast.LENGTH_SHORT);
		} else if (action.equals(INTENT_NEXT)) {
			preferences.saveDateForWidgetId(appWidgetId, preferences
					.getDateForWidgetId(appWidgetId).plusDays(1));
			currentToast = Toast.makeText(context, "Showing Next",
					Toast.LENGTH_SHORT);
		} else if (action.equals(INTENT_LATEST)) {
			preferences.saveDateForWidgetId(appWidgetId, DateMidnight.now());
			currentToast = Toast.makeText(context, "Showing Latest",
					Toast.LENGTH_SHORT);
		} else if (action.equals(INTENT_RANDOM)) {
			preferences.saveDateForWidgetId(appWidgetId, DilbertPreferences.getRandomDateMidnight());
			currentToast = Toast.makeText(context, "Showing Random",
					Toast.LENGTH_SHORT);
		}
		updateAppWidget(context, AppWidgetManager.getInstance(context),
				appWidgetId);
		if (currentToast != null)
			currentToast.show();
		super.onReceive(context, intent);
	}

	static void updateAppWidget(Context context,
			final AppWidgetManager appWidgetManager, final int appWidgetId) {
		Log.d(TAG, "updateAppWidget for " + appWidgetId);
		// create views and hook buttons
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
		// display image to widget
		final DilbertPreferences prefs = new DilbertPreferences(context);
		final DateMidnight currentDate = prefs.getDateForWidgetId(appWidgetId);
		final String cachedUrl = prefs.getCachedUrl(currentDate);
		if (cachedUrl == null) {
			Log.d(TAG,
					"Must load url for "
							+ currentDate
									.toString(DilbertPreferences.DATE_FORMATTER));
		} else {
			ImageLoader.getInstance().loadImage(cachedUrl,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							views.setImageViewBitmap(R.id.widget_image,
									loadedImage);
							views.setTextViewText(
									R.id.widget_title,
									prefs.getCurrentDate().toString(
											DilbertPreferences.DATE_FORMATTER));
							appWidgetManager
									.updateAppWidget(appWidgetId, views);
						}
					});
		}

	}
}
