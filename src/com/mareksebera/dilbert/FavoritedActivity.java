package com.mareksebera.dilbert;

import android.os.Bundle;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class FavoritedActivity extends SherlockActivity {

	private DilbertPreferences preferences = null;
	private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorited);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		preferences = new DilbertPreferences(this);
		listView = (ListView) findViewById(R.id.activity_favorited_listview);
		listView.setAdapter(new FavoritedAdapter(this, preferences.getFavoritedItems()));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		}
		return false;
	}
	
}
