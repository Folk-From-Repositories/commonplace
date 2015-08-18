package com.common.place;

import com.common.place.db.Provider;
import com.common.place.util.Constants;
import com.common.place.util.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegistGroup extends Activity implements OnClickListener{

	Context registGroupContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist_group);
		Log.d("KMC", "INIT RegistGroup");
		
		//registGroupContext = getApplicationContext();
		
		EditText groupName=(EditText)findViewById(R.id.name_edit);
		EditText meetTime=(EditText)findViewById(R.id.time_edit);
		
		findViewById(R.id.seachAddr).setOnClickListener(this);
		findViewById(R.id.searchMap).setOnClickListener(this);
		findViewById(R.id.btn_contacts).setOnClickListener(this);
		
		deleteAllMemberListInDB();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case Constants.MAP_VIEW_REQ_CODE:
			Log.d("KMC","RegistGroup's onActivityResult: " + resultCode);
			break;
//		case Constants.MEMBER_ACTIVITY_REQ_CODE:
//			Logger.i("onActivityResult("+Constants.MEMBER_ACTIVITY_REQ_CODE+")");
//			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		deleteAllMemberListInDB();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// when select member from contacts....
		Cursor memberCursor = getMemberListFromDB();
		
		if(memberCursor.getCount() > 0){
			Toast.makeText(this, memberCursor.getCount() + " selected!!!!", Toast.LENGTH_SHORT).show();
		}
		
		super.onResume();
	}

	public int deleteAllMemberListInDB(){
		return getContentResolver().delete(Provider.CONTENT_URI, null, null);
	}
	// you can use member list like this...
	public Cursor getMemberListFromDB(){
		return getContentResolver().query(Provider.CONTENT_URI, null, null, null, null);
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



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.seachAddr:
			Log.d("KMC", "Search Addr");
			break;
		case R.id.searchMap:
			Log.d("KMC", "Search Map");
			startActivityForResult(new Intent(getApplicationContext(), CreateMapView.class),Constants.MAP_VIEW_REQ_CODE);
			break;
		case R.id.btn_contacts:
			Logger.i("Contacts button clicked");
			// call back is not needed!! because member list is stored in Database!!
			startActivity(new Intent(getApplicationContext(), MemberActivity.class));
			break;
		}	
		
	}
}
