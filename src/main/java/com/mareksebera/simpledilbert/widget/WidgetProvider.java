package com.mareksebera.simpledilbert.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.Target;
import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.core.DilbertFragmentActivity;
import com.mareksebera.simpledilbert.preferences.DilbertPreferences;
import com.mareksebera.simpledilbert.utilities.GetStripUrl;
import com.mareksebera.simpledilbert.utilities.GetStripUrlInterface;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;

import androidx.annotation.Nullable;

public final class WidgetProvider extends AppWidgetProvider {

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
        intent.setPackage(context.getPackageName());
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void updateAppWidget(final Context context,
                                        final AppWidgetManager appWidgetManager, final int appWidgetId) {
        final DilbertPreferences prefs = new DilbertPreferences(context);
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
        views.setOnClickPendingIntent(R.id.widget_image,
                getPendingIntent(INTENT_DISPLAY, context, appWidgetId));
        views.setOnClickPendingIntent(R.id.widget_refresh,
                getPendingIntent(INTENT_REFRESH, context, appWidgetId));
        views.setOnClickPendingIntent(R.id.widget_full_layout,
                getPendingIntent(INTENT_DISPLAY, context, appWidgetId));

        final LocalDate currentDate = prefs.getDateForWidgetId(appWidgetId);

        views.setViewVisibility(R.id.widget_next, prefs.isWidgetAlwaysShowLatest() ? View.GONE : View.VISIBLE);
        views.setViewVisibility(R.id.widget_previous, prefs.isWidgetAlwaysShowLatest() ? View.GONE : View.VISIBLE);
        views.setViewVisibility(R.id.widget_random, prefs.isWidgetAlwaysShowLatest() ? View.GONE : View.VISIBLE);
        views.setViewVisibility(R.id.widget_latest, prefs.isWidgetAlwaysShowLatest() ? View.GONE : View.VISIBLE);

        final String cachedUrl = prefs.getCachedUrl(currentDate);
        final String cachedTitle = prefs.getCachedTitle(currentDate);
        views.setViewVisibility(R.id.widget_progress, View.VISIBLE);
        views.setTextViewText(R.id.widget_title, prefs.getDateForWidgetId(appWidgetId)
                .toString(DilbertPreferences.NICE_DATE_FORMATTER));
        if (prefs.isWidgetShowTitle() && cachedTitle != null && !cachedTitle.isEmpty()) {
            views.setViewVisibility(R.id.widget_strip_title, View.VISIBLE);
            views.setTextViewText(R.id.widget_strip_title, cachedTitle);
        } else {
            views.setViewVisibility(R.id.widget_strip_title, View.GONE);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
        if (cachedUrl == null) {
            new GetStripUrl(context, new GetStripUrlInterface() {

                @Override
                public void imageLoadFailed(String url, Throwable reason) {
                    currentToast = Toast.makeText(context, "Image Loading failed",
                            Toast.LENGTH_SHORT);
                    currentToast.show();
                    views.setImageViewResource(R.id.widget_image,
                            R.drawable.cancel);
                    views.setViewVisibility(R.id.widget_progress, View.GONE);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }

                @Override
                public void displayImage(String url, String title) {
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                }
            }, prefs, currentDate).execute();
        } else {
            Glide.with(context)
                    .asBitmap()
                    .load(cachedUrl)
                    .apply(new RequestOptions().dontAnimate())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            updateAppWidget(context, appWidgetManager, appWidgetId);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            views.setViewVisibility(R.id.widget_progress, View.GONE);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                            Glide.with(context)
                                    .asBitmap()
                                    .load(cachedUrl)
                                    .apply(new RequestOptions().dontAnimate())
                                    .into(new AppWidgetTarget(context, R.id.widget_image, views, appWidgetId));
                            return false;
                        }
                    })
                    .into(new AppWidgetTarget(context, R.id.widget_image, views, appWidgetId));
        }

    }

    @Override
    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        String action = intent.getAction();
        if (intent.getExtras() == null)
            return;
        final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        final DilbertPreferences preferences = new DilbertPreferences(context);
        if (action == null || appWidgetId == -1) {
            super.onReceive(context, intent);
            return;
        }
        if (currentToast != null)
            currentToast.cancel();
        switch (action) {
            case INTENT_PREVIOUS:
                preferences.saveDateForWidgetId(appWidgetId, preferences
                        .getDateForWidgetId(appWidgetId).minusDays(1));
                break;
            case INTENT_NEXT:
                preferences.saveDateForWidgetId(appWidgetId, preferences
                        .getDateForWidgetId(appWidgetId).plusDays(1));
                break;
            case INTENT_LATEST:
                preferences.saveDateForWidgetId(appWidgetId,
                        LocalDate.now());
                break;
            case INTENT_RANDOM:
                preferences.saveDateForWidgetId(appWidgetId,
                        DilbertPreferences.getRandomDate());
                break;
            case INTENT_REFRESH:
                preferences
                        .removeCache(preferences.getDateForWidgetId(appWidgetId));
                break;
            case INTENT_DISPLAY:
                preferences.saveCurrentDate(preferences
                        .getDateForWidgetId(appWidgetId));
                Intent display = new Intent(context, DilbertFragmentActivity.class);
                display.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(display);
                break;
            case AppWidgetManager.ACTION_APPWIDGET_UPDATE:
                LocalDate current = preferences.getDateForWidgetId(appWidgetId);
                if (current.equals(LocalDate.now()
                        .minusDays(1))) {
                    preferences.saveDateForWidgetId(appWidgetId,
                            LocalDate.now());
                }
                break;
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
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
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
