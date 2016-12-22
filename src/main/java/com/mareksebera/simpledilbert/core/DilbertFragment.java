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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.favorites.DilbertFavoritedActivity;
import com.mareksebera.simpledilbert.preferences.DilbertPreferences;
import com.mareksebera.simpledilbert.utilities.GetStripUrl;
import com.mareksebera.simpledilbert.utilities.GetStripUrlInterface;

import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

public final class DilbertFragment extends Fragment {

    public static final String ARGUMENT_DATE = "string_ARGUMENT_DATE";
    private static final int MENU_SAVE = -1, MENU_FAVORITE = -2,
            MENU_ZOOM = -3, MENU_SHARE = -4, MENU_REFRESH = -5, MENU_OPEN_AT = -6, MENU_OPEN_IN_BROWSER = -7;
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
    private PhotoView image;
    private ProgressBar progress;
    private final RequestListener<String, Bitmap> dilbertImageLoadingListener = new RequestListener<String, Bitmap>() {
        @Override
        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
            if (image != null) {
                image.setImageResource(R.drawable.cancel);
            }
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
            if (getActivity() != null)
                Toast.makeText(getActivity(),
                        R.string.loading_exception_error, Toast.LENGTH_SHORT)
                        .show();
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
            applyZoomLevel();
            return false;
        }
    };
    private final GetStripUrlInterface getStripURIlListener = new GetStripUrlInterface() {

        @Override
        public void imageLoadFailed(String url, Throwable reason) {
            dilbertImageLoadingListener.onException(null, url, null, true);
        }

        @Override
        public void displayImage(String url) {
            if (image == null)
                return;
            Glide.with(DilbertFragment.this)
                    .load(url)
                    .asBitmap()
                    .fitCenter()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.cancel)
                    .listener(dilbertImageLoadingListener)
                    .into(image);
        }
    };
    private DilbertPreferences preferences;
    private GetStripUrl loadTask;
    private final OnPhotoTapListener photoTapListener = new OnPhotoTapListener() {

        @Override
        public void onPhotoTap(View view, float x, float y) {
            refreshAction();
        }

        @Override
        public void onOutsidePhotoTap() {

        }
    };
    private int zoomLevel = 0;

    public DilbertFragment() {
    }

    @Override
    public void onDestroyView() {
        progress = null;
        image = null;
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.preferences = new DilbertPreferences(getActivity());
        this.zoomLevel = preferences.getDefaultZoomLevel();
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
        String cachedUrl = preferences.getCachedUrl(getDateFromArguments());
        if (null != cachedUrl) {
            getStripURIlListener.displayImage(cachedUrl);
        } else {
            this.loadTask = new GetStripUrl(getStripURIlListener, preferences,
                    getDateFromArguments());
            this.loadTask.execute();
        }
        return fragment;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_FAVORITE:
                preferences.toggleIsFavorited(getDateFromArguments());
                modifyFavoriteItem(item);
                return true;
            case MENU_ZOOM:
                applyZoomLevel();
                return true;
            case MENU_SHARE:
                shareCurrentStrip();
                return true;
            case MENU_REFRESH:
                refreshAction();
                return true;
            case MENU_OPEN_AT:
                preferences.saveCurrentDate(getDateFromArguments());
                getActivity().finish();
                return true;
            case MENU_OPEN_IN_BROWSER:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(preferences.getCachedUrl(getDateFromArguments()))));
                } catch (Throwable t) {
                    Log.e("DilbertFragment", "Cannot ACTION_VIEW url", t);
                }
                return true;
            case MENU_SAVE:
                preferences.downloadImageViaManager(getActivity(),
                        preferences.getCachedUrl(getDateFromArguments()),
                        getDateFromArguments());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applyZoomLevel() {
        if (image != null && image.canZoom()) {
            switch (zoomLevel) {
                case 0:
                    image.setScale(image.getMinimumScale(), true);
                    break;
                case 1:
                    image.setScale(image.getMediumScale(), true);
                    break;
                case 2:
                    image.setScale(image.getMaximumScale(), true);
                    break;
            }
        }
    }

    private void refreshAction() {
        Glide.get(getContext()).clearMemory();
        preferences.removeCache(getDateFromArguments());
        if (this.loadTask == null
                || this.loadTask.getStatus() != Status.PENDING) {
            this.loadTask = new GetStripUrl(getStripURIlListener, preferences,
                    getDateFromArguments(), progress);
        }
        this.loadTask.execute();
    }

    private void shareCurrentStrip() {
        String url = preferences.getCachedUrl(getDateFromArguments());
        if (url == null)
            return;
        Glide.with(DilbertFragment.this).load(url)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap b, GlideAnimation<? super Bitmap> glideAnimation) {
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
                                                            .getExternalCacheDir()
                                            );
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
                                                + date
                                );
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
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
        );
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_ZOOM, 4, R.string.menu_zoom)
                        .setIcon(R.drawable.ic_menu_zoom),
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
        );
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_SAVE, 3, R.string.menu_download)
                        .setIcon(R.drawable.ic_menu_save),
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
        );
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_SHARE, 2, R.string.menu_share)
                        .setIcon(R.drawable.ic_menu_share),
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
        );
        if (getActivity() != null && getActivity() instanceof DilbertFavoritedActivity) {
            MenuItemCompat.setShowAsAction(
                    menu.add(category, MENU_OPEN_AT, 5, R.string.menu_open_at)
                            .setIcon(R.drawable.ic_menu_open_at),
                    MenuItemCompat.SHOW_AS_ACTION_NEVER
            );
        }
        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_REFRESH, 5, R.string.menu_refresh)
                        .setIcon(R.drawable.ic_menu_refresh),
                MenuItemCompat.SHOW_AS_ACTION_NEVER
        );

        MenuItemCompat.setShowAsAction(
                menu.add(category, MENU_OPEN_IN_BROWSER, 3, R.string.menu_open_in_browser)
                        .setIcon(R.drawable.ic_menu_open_at),
                MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
        );
    }

}
