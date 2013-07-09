package com.mareksebera.simpledilbert;

import java.util.List;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class FavoritedActivity extends SherlockActivity {

	private DilbertPreferences preferences = null;
	private ListView listView;
	private FavoritedAdapter listAdapter;
	private static final int CONTEXT_REMOVE = 1, CONTEXT_DOWNLOAD = 2,
			CONTEXT_DISPLAY = 3;
	private FavoritedItem contextMenuItem = null;

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			openContextMenu(view);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.title_favorited);
		setContentView(R.layout.activity_favorited);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		preferences = new DilbertPreferences(this);
		listView = (ListView) findViewById(R.id.activity_favorited_listview);
		listAdapter = new FavoritedAdapter(this,
				getFavoritedOrNotifyAndFinish());
		listView.setAdapter(listAdapter);
		registerForContextMenu(listView);
		listView.setOnItemClickListener(onItemClickListener);
	}

	private List<FavoritedItem> getFavoritedOrNotifyAndFinish() {
		List<FavoritedItem> list = preferences.getFavoritedItems();
		if (list != null && list.isEmpty()) {
			Toast.makeText(this, R.string.toast_no_favorites, Toast.LENGTH_LONG)
					.show();
			finish();
		}
		return list;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		contextMenuItem = (FavoritedItem) listAdapter.getItem(info.position);
		if (contextMenuItem != null) {
			menu.setHeaderTitle(contextMenuItem.getDate().toString(
					DilbertPreferences.DATE_FORMATTER));
			menu.add(Menu.NONE, CONTEXT_REMOVE, Menu.NONE,
					R.string.context_favorites_remove);
			menu.add(Menu.NONE, CONTEXT_DOWNLOAD, Menu.NONE,
					R.string.menu_download);
			menu.add(Menu.NONE, CONTEXT_DISPLAY, Menu.NONE, R.string.menu_open);
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_REMOVE:
			if (contextMenuItem != null) {
				preferences.toggleIsFavorited(contextMenuItem.getDate());
				listAdapter = new FavoritedAdapter(this,
						getFavoritedOrNotifyAndFinish());
				listView.setAdapter(listAdapter);
			}
			return true;
		case CONTEXT_DOWNLOAD:
			preferences.downloadImageViaManager(this, contextMenuItem.getUrl(),
					contextMenuItem.getDate());
			return true;
		case CONTEXT_DISPLAY:
			preferences.saveCurrentDate(contextMenuItem.getDate().minusDays(1));
			finish();
			return true;

		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return false;
	}
}
