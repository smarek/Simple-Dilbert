package com.mareksebera.simpledilbert.core;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.favorites.DilbertFavoritedActivity;
import com.mareksebera.simpledilbert.preferences.DilbertPreferences;
import com.mareksebera.simpledilbert.utilities.CustomTarget;
import com.mareksebera.simpledilbert.utilities.GetStripUrl;
import com.mareksebera.simpledilbert.utilities.GetStripUrlInterface;

import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;

import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;

public final class DilbertFragment extends Fragment {

    static final String BROADCAST_TITLE_UPDATE = "com.mareksebera.simpledilbert.broadcast.TITLE";
    public static final String ARGUMENT_DATE = "string_ARGUMENT_DATE";
    private static final int MENU_SAVE = -1, MENU_FAVORITE = -2,
            MENU_ZOOM = -3, MENU_SHARE = -4, MENU_REFRESH = -5, MENU_OPEN_AT = -6, MENU_OPEN_IN_BROWSER = -7;
    private final OnLongClickListener imageLongClickListener = v -> {
        if (getActivity() != null) {
            FragmentActivity sfa = getActivity();
            if (sfa instanceof DilbertFragmentInterface) {
                ((DilbertFragmentInterface) sfa).toggleActionBar();
            }
        }
        return true;
    };
    private PhotoView image;
    private ProgressBar progress;
    private final RequestListener<Bitmap> dilbertImageLoadingListener = new RequestListener<Bitmap>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
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
        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
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
            dilbertImageLoadingListener.onLoadFailed(null, url, null, true);
        }

        @Override
        public void displayImage(String url, String title) {
            if (image == null || getArguments() == null)
                return;
            Log.d("GetStripUrlListener", "url: " + url);
            if (url != null) {
                preferences.saveCurrentUrl(getArguments().getString(ARGUMENT_DATE), url);
                preferences.saveCurrentTitle(getArguments().getString(ARGUMENT_DATE), title);
            }
            Glide.with(DilbertFragment.this.getContext())
                    .asBitmap()
                    .load(url)
                    .apply(new RequestOptions().dontAnimate().fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.cancel))
                    .listener(dilbertImageLoadingListener)
                    .into(image);
            Context c = getContext();
            if (c != null) {
                LocalBroadcastManager.getInstance(c).sendBroadcast(new Intent(BROADCAST_TITLE_UPDATE));
            }
        }
    };
    private DilbertPreferences preferences;
    private GetStripUrl loadTask;
    private final OnPhotoTapListener photoTapListener = (view, x, y) -> refreshAction();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_dilbert, container,
                false);
        assert fragment != null;
        this.image = fragment.findViewById(R.id.fragment_imageview);
        this.image.setOnLongClickListener(imageLongClickListener);
        fragment.setOnLongClickListener(imageLongClickListener);
        this.image.setOnPhotoTapListener(photoTapListener);
        this.progress = fragment
                .findViewById(R.id.fragment_progressbar);
        String cachedUrl = preferences.getCachedUrl(getDateFromArguments());
        String cachedTitle = preferences.getCachedTitle(getDateFromArguments());
        if (null != cachedUrl) {
            getStripURIlListener.displayImage(cachedUrl, cachedTitle);
        } else {
            this.loadTask = new GetStripUrl(getContext(), getStripURIlListener, preferences,
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
                if (getActivity() != null) {
                    getActivity().finish();
                }
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
        if (image != null && image.isZoomable()) {
            final float scale;
            switch (zoomLevel) {
                case 0:
                default:
                    scale = image.getMinimumScale();
                    break;
                case 1:
                    scale = image.getMediumScale();
                    break;
                case 2:
                    scale = image.getMaximumScale();
                    break;
            }
            image.setScale(scale, 0, 0, true);
        }
    }

    private void refreshAction() {
        Glide.get(getContext()).clearMemory();
        preferences.removeCache(getDateFromArguments());
        if (this.loadTask == null
                || this.loadTask.getStatus() != Status.PENDING) {
            this.loadTask = new GetStripUrl(getContext(), getStripURIlListener, preferences,
                    getDateFromArguments(), progress);
        }
        this.loadTask.execute();
    }

    private void shareCurrentStrip() {
        // Share only text
        String date = getArguments().getString(ARGUMENT_DATE);
        if (preferences.isSharingImage()) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Dilbert " + date
                    + " #simpledilbert");
            i.putExtra(
                    Intent.EXTRA_TEXT,
                    "Dilbert "
                            + date
                            + " #simpledilbert https://dilbert.com/strip/"
                            + date
            );
            startActivity(Intent.createChooser(i,
                    getString(R.string.share_chooser)));
            return;
        }

        // Share image
        String url = preferences.getCachedUrl(getDateFromArguments());
        if (url == null) {
            Log.d("DilbertFragment", "Will not share null URL");
            return;
        }
        Glide.with(DilbertFragment.this.getContext()).asBitmap().load(url)
                .into(new CustomTarget<Bitmap>() {
                          @Override
                          public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                              try {
                                  String date = getArguments().getString(ARGUMENT_DATE);
                                  Intent i = new Intent(Intent.ACTION_SEND);
                                  i.setType("image/jpeg");
                                  i.putExtra(Intent.EXTRA_SUBJECT, "Dilbert " + date
                                          + " #simpledilbert");
                                  i.putExtra(Intent.EXTRA_TEXT, "Dilbert " + date
                                          + " #simpledilbert");
                                  File tmp = File
                                          .createTempFile("dilbert_", ".jpg",
                                                  getActivity()
                                                          .getExternalCacheDir()
                                          );
                                  FileOutputStream out = new FileOutputStream(tmp);
                                  resource.compress(CompressFormat.JPEG, 100, out);
                                  out.close();
                                  FragmentActivity activity = getActivity();
                                  Uri u = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", tmp);
                                  i.putExtra(Intent.EXTRA_STREAM, u);
                                  startActivity(Intent.createChooser(i,
                                          getString(R.string.share_chooser)));

                              } catch (Throwable e) {
                                  if (getActivity() != null)
                                      Toast.makeText(getActivity(),
                                              R.string.loading_exception_error,
                                              Toast.LENGTH_LONG).show();
                              }
                          }

                          @Override
                          public void onLoadCleared(@Nullable Drawable placeholder) {

                          }
                      }
                );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final int category = 0;
        menu.add(category, MENU_FAVORITE, 1, R.string.menu_favorite_remove)
                .setIcon(R.drawable.ic_menu_not_favorited).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        menu.add(category, MENU_ZOOM, 4, R.string.menu_zoom)
                .setIcon(R.drawable.ic_menu_zoom).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        menu.add(category, MENU_SAVE, 3, R.string.menu_download)
                .setIcon(R.drawable.ic_menu_save).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        menu.add(category, MENU_SHARE, 2, R.string.menu_share)
                .setIcon(R.drawable.ic_menu_share).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);

        if (getActivity() != null && getActivity() instanceof DilbertFavoritedActivity) {
            menu.add(category, MENU_OPEN_AT, 5, R.string.menu_open_at)
                    .setIcon(R.drawable.ic_menu_open_at).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        }

        menu.add(category, MENU_REFRESH, 4, R.string.menu_refresh)
                .setIcon(R.drawable.ic_menu_refresh).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
        menu.add(category, MENU_OPEN_IN_BROWSER, 3, R.string.menu_open_in_browser)
                .setIcon(R.drawable.ic_menu_open_at).setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
    }

}
