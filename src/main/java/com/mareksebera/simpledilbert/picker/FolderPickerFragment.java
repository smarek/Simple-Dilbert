package com.mareksebera.simpledilbert.picker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mareksebera.simpledilbert.R;
import com.mareksebera.simpledilbert.utilities.ActionBarUtility;

import java.io.File;

import androidx.fragment.app.ListFragment;
import androidx.preference.PreferenceManager;

public final class FolderPickerFragment extends ListFragment {

    private static final int MENU_SHOW_HIDDEN = 1, MENU_SHOW_FILES = 2, MENU_ACCEPT = 3, MENU_GO_DEFAULT = 4;
    private static final String PREF_SHOW_HIDDEN = "folder_picker_fragment.show_hidden";
    private static final String PREF_SHOW_FILES = "folder_picker_fragment.show_files";
    private SharedPreferences preferences;
    private FolderPickerAdapter folderPickerAdapter;
    private TextView currentPath;

    public FolderPickerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        folderPickerAdapter = new FolderPickerAdapter(getActivity(), isShowHidden(), isShowFiles());
        setListAdapter(folderPickerAdapter);
        setHasOptionsMenu(true);
        if (getActivity() != null && getActivity().getIntent() != null && getActivity().getIntent().getData() != null) {
            File requestedPath = new File(getActivity().getIntent().getData().getPath());
            if (requestedPath.isDirectory() && requestedPath.canRead()) {
                folderPickerAdapter.setPath(requestedPath);
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        File click = (File) folderPickerAdapter.getItem(position);
        if (click != null) {
            if (click.isDirectory()) {
                folderPickerAdapter.setPath(click);
                currentPath.setText(folderPickerAdapter.getCurrentFolder().getAbsolutePath());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflated = inflater.inflate(R.layout.fragment_folder_picker, container, false);
        assert inflated != null;
        inflated.setPadding(0, ActionBarUtility.getActionBarHeightDip(container == null ? inflated.getContext() : container.getContext()), 0, 0);
        currentPath = inflated.findViewById(R.id.fragment_folder_picker_current_path);
        currentPath.setText(folderPickerAdapter.getCurrentFolder().getAbsolutePath());
        return inflated;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, MENU_ACCEPT, Menu.NONE, R.string.folder_picker_select_this)
                .setIcon(R.drawable.ic_navigation_accept)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, MENU_SHOW_HIDDEN, Menu.NONE, R.string.folder_picker_show_hidden).setCheckable(true);
        menu.add(Menu.NONE, MENU_SHOW_FILES, Menu.NONE, R.string.folder_picker_show_files).setCheckable(true);
        menu.add(Menu.NONE, MENU_GO_DEFAULT, Menu.NONE, R.string.folder_picker_go_to_default);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem show_files, show_hidden;
        if ((show_files = menu.findItem(MENU_SHOW_FILES)) != null) {
            show_files.setChecked(isShowFiles());
        }
        if ((show_hidden = menu.findItem(MENU_SHOW_HIDDEN)) != null) {
            show_hidden.setChecked(isShowHidden());
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SHOW_FILES:
                toggleShowFiles();
                folderPickerAdapter.setShowFiles(isShowFiles());
                return true;
            case MENU_SHOW_HIDDEN:
                toggleShowHidden();
                folderPickerAdapter.setShowHidden(isShowHidden());
                return true;
            case MENU_ACCEPT:
                File current = folderPickerAdapter.getCurrentFolder();
                if (!current.canWrite()) {
                    Toast.makeText(getActivity(), R.string.folder_picker_cannot_write, Toast.LENGTH_SHORT).show();
                    return true;
                }
                Intent result = new Intent(null, Uri.fromFile(current));
                getActivity().setResult(Activity.RESULT_OK, result);
                getActivity().finish();
                return true;
            case MENU_GO_DEFAULT:
                folderPickerAdapter.setPath(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isShowHidden() {
        return preferences.getBoolean(PREF_SHOW_HIDDEN, false);
    }

    private boolean isShowFiles() {
        return preferences.getBoolean(PREF_SHOW_FILES, false);
    }

    private void toggleShowHidden() {
        preferences.edit().putBoolean(PREF_SHOW_HIDDEN, !isShowHidden()).apply();
    }

    private void toggleShowFiles() {
        preferences.edit().putBoolean(PREF_SHOW_FILES, !isShowFiles()).apply();
    }
}
