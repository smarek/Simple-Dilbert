package com.mareksebera.simpledilbert;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class WidgetProvider extends AppWidgetProvider {

	/**
	 * TODO: Widget configuration class, allow rotation +-90
	 * TODO: Add widget buttons for browsing and launching internal classes (zoom, favorite)
	 * */
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		final int widgetCount = appWidgetIds.length;
		for (int i = 0; i < widgetCount; i++) {
			int appWidgetId = appWidgetIds[i];
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
		Log.d("onUpdate", "onUpdate");
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions) {
		Log.d("onAppWidgetOptionsChanged", "onAppWidgetOptionsChanged");
	}

	@Override
	public void onEnabled(Context context) {
		Log.d("onEnabled", "onEnabled");
		AppController.configureImageLoader(context);
	}

	static void updateAppWidget(Context context,
			final AppWidgetManager appWidgetManager, final int appWidgetId) {
		Log.d("updateAppWidget", "updateAppWidget");
		final RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);

		final DilbertPreferences prefs = new DilbertPreferences(context);
		ImageLoader.getInstance().loadImage(prefs.getLastUrl(),
				new ImageSize(180, 110), new SimpleImageLoadingListener() {
					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						views.setImageViewBitmap(R.id.widget_image, loadedImage);
						views.setTextViewText(R.id.widget_title,
								prefs.getCurrentDate().toString(DilbertPreferences.DATE_FORMATTER));
						appWidgetManager.updateAppWidget(appWidgetId, views);
					}
				});

	}
}
