package com.mareksebera.simpledilbert.picker;

import android.os.Bundle;
import android.view.MenuItem;

import com.mareksebera.simpledilbert.R;

import androidx.appcompat.app.AppCompatActivity;

public final class FolderPickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_folder_picker);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
