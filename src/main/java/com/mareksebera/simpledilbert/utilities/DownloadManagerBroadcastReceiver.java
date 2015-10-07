package com.mareksebera.simpledilbert.utilities;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.mareksebera.simpledilbert.preferences.DilbertPreferences;

import java.io.File;

public final class DownloadManagerBroadcastReceiver extends BroadcastReceiver {

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onReceive(Context context, Intent intent) {
        try {
            DilbertPreferences preferences = new DilbertPreferences(context);
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadedFilePath = dm.getUriForDownloadedFile(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
            if (downloadedFilePath == null) {
                Log.d("DlManagerBReceiver", "No Uri for downloaded file from DownloadManager");
                return;
            }
            File downloadedFile = new File(downloadedFilePath.getPath());
            if (downloadedFilePath.getLastPathSegment() == null) {
                return;
            }
            String scheduledTargetPath = preferences.getScheduledTargetPath(downloadedFilePath.getLastPathSegment().substring(0, 10));
            if (scheduledTargetPath != null) {
                if (!downloadedFile.renameTo(new File(scheduledTargetPath))) {
                    Toast.makeText(context, "Couldn't move picture to selected Folder", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Throwable t) {
            Log.e("DlManagerBReceiver", "Error while moving downloaded file to desired target folder", t);
        }
    }
}
