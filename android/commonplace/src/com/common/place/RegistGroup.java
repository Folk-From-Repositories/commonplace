package com.common.place;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegistGroup extends Activity {

	Context registGroupContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist_group);
		Log.d("KMC", "INIT RegistGroup");
		
		//registGroupContext = getApplicationContext();
		
		EditText groupName=(EditText)findViewById(R.id.name_edit);
		EditText meetTime=(EditText)findViewById(R.id.time_edit);
		
		findViewById(R.id.seachAddr).setOnClickListener(mClickListener);
		findViewById(R.id.searchMap).setOnClickListener(mClickListener);
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.seachAddr:
				Log.d("KMC", "Search Addr");
				break;
			case R.id.searchMap:
				Log.d("KMC", "Search Map");
				startActivityForResult(new Intent(getApplicationContext(), CreateMapView.class),0);
				break;
			}	
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    Log.d("KMC","RegistGroup's onActivityResult: " + resultCode);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.regist_group, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
