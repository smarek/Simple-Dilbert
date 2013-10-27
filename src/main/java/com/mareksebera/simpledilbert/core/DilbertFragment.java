package com.mareksebera.simpledilbert.core;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.preferences.DilbertPreferences;
import com.mareksebera.simpledilbert.utilities.GetStripUrl;
import com.mareksebera.simpledilbert.utilities.GetStripUrlInterface;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

public final class DilbertFragment extends Fragment {

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

    private final GetStripUrlInterface getStripUrilListener = new GetStripUrlInterface() {

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

    private final ImageLoadingListener dilbertImageLoadingListener = new ImageLoadingListener() {

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
            if (getActivity() != null)
                Toast.makeText(getActivity(),
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
    private final OnLongClickListener imageLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (getActivity() != null) {
                FragmentActivity sfa = getActivity();
                if (sfa instanceof DilbertFragmentInterface) {
                    ((DilbertFragmentInterface) sfa).toggleActionBar();
                }
            }
            return true;
        }
    };
    private final OnPhotoTapListener photoTapListener = new OnPhotoTapListener() {

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

    private LocalDate getDateFromArguments() {
        return LocalDate.parse(getArguments().getString(ARGUMENT_DATE),
                DilbertPreferences.DATE_FORMATTER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_dilbert, container,
                false);
        assert fragment != null;
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
                    switch (zoomLevel) {
                        case 0:
                            image.setScale(image.getMediumScale(), true);
                            break;
                        case 1:
                            image.setScale(image.getMaximumScale(), true);
                            break;
                        case 2:
                            image.setScale(image.getMinimumScale(), true);
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
                preferences.downloadImageViaManager(getActivity(),
                        preferences.getCachedUrl(getDateFromArguments()),
                        getDateFromArguments());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshAction() {
        preferences.removeCache(getDateFromArguments());
        if (this.loadTask == null
                || this.loadTask.getStatus() != Status.PENDING) {
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
                                            getActivity()
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

                } catch (Throwable e) {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(),
                                R.string.loading_exception_error,
                                Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final int category = 0;
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_FAVORITE, 1, R.string.menu_favorite_remove)
                        .setIcon(R.drawable.ic_menu_not_favorited),
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_ZOOM, 4, R.string.menu_zoom)
                        .setIcon(R.drawable.ic_menu_zoom),
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_SAVE, 3, R.string.menu_download)
                        .setIcon(R.drawable.ic_menu_save),
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_SHARE, 2, R.string.menu_share)
                        .setIcon(R.drawable.ic_menu_share),
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_REFRESH, 5, R.string.menu_refresh)
                        .setIcon(R.drawable.ic_menu_refresh),
                MenuItemCompat.SHOW_AS_ACTION_NEVER);
    }

}
