package com.mareksebera.simpledilbert.picker;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mareksebera.simpledilbert.R;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import androidx.fragment.app.FragmentActivity;

public final class FolderPickerAdapter extends BaseAdapter {

    private FragmentActivity context;
    private File currentPath;
    private File[] currentFolder;
    private boolean hasParent = false;
    private boolean shouldShowHidden;
    private boolean shouldShowFiles;

    FolderPickerAdapter(FragmentActivity activity, boolean shouldShowHidden, boolean shouldShowFiles) {
        assert activity != null;
        this.context = activity;
        this.shouldShowFiles = shouldShowFiles;
        this.shouldShowHidden = shouldShowHidden;
        setPath(null);
    }

    void setPath(File path) {
        if (path == null) path = Environment.getExternalStorageDirectory();
        if (!path.isDirectory()) {
            return;
        }
        if (!path.canRead()) {
            Toast.makeText(context, R.string.folder_picker_cannot_open, Toast.LENGTH_SHORT).show();
            return;
        }
        this.currentPath = path;
        this.currentFolder = path.listFiles(getFileFilter());
        if (this.currentFolder == null)
            currentFolder = new File[0];
        Arrays.sort(this.currentFolder, new DirAlphaComparator());
        this.hasParent = currentPath.getParentFile() != null;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return currentFolder.length + (hasParent ? 1 : 0);
    }

    @Override
    public Object getItem(int position) {
        if (hasParent)
            position--;
        if (position == -1) {
            return currentPath.getParentFile();
        }
        if (currentFolder.length >= position) {
            return currentFolder[position];
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_folder_picker, parent, false);
            if (convertView != null) {
                vh.icon = convertView.findViewById(R.id.item_folder_picker_icon);
                vh.title = convertView.findViewById(R.id.item_folder_picker_text);
                convertView.setTag(vh);
            }
        } else {
            if (convertView.getTag() instanceof ViewHolder) {
                vh = (ViewHolder) convertView.getTag();
            } else {
                return convertView;
            }
        }
        File item = (File) getItem(position);
        if (item != null) {
            if (hasParent && currentPath.getParentFile().getAbsolutePath().equals(item.getAbsolutePath())) {
                vh.title.setText(String.format(".. (%s)", item.getName().equalsIgnoreCase("") ? "/" : item.getName()));
            } else {
                vh.title.setText(item.getName());
            }
            vh.icon.setImageResource(item.isDirectory() ? R.drawable.folder : R.drawable.file);
        }
        return convertView;
    }

    private FileFilter getFileFilter() {
        return pathname -> !(!shouldShowHidden && pathname.getName().startsWith(".")) && !(!shouldShowFiles && !pathname.isDirectory());
    }

    void setShowFiles(boolean showFiles) {
        this.shouldShowFiles = showFiles;
        setPath(currentPath);
    }

    void setShowHidden(boolean showHidden) {
        this.shouldShowHidden = showHidden;
        setPath(currentPath);
    }

    File getCurrentFolder() {
        return currentPath;
    }

    static class ViewHolder {
        TextView title;
        ImageView icon;
    }

    final static class DirAlphaComparator implements Comparator<File> {

        public int compare(File filea, File fileb) {
            if (filea.isDirectory() && !fileb.isDirectory()) {
                return -1;
            } else if (!filea.isDirectory() && fileb.isDirectory()) {
                return 1;
            } else {
                return filea.getName().compareToIgnoreCase(fileb.getName());
            }
        }
    }
}
