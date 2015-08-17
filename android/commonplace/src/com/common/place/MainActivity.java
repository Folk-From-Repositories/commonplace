package com.common.place;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		Toast toast = Toast.makeText(getApplicationContext(), "TEST Activity", Toast.LENGTH_LONG);
//		toast.setGravity(Gravity.CENTER, 0, 0);
//		toast.show();
		
		//Intent intent = new Intent(this, GcmHandler.class);

		findViewById(R.id.apple).setOnClickListener(mClickListener);
		findViewById(R.id.orange).setOnClickListener(mClickListener);
		
		startActivity(new Intent(this, InitManager.class));
		startActivity(new Intent(this, GetPhoneNumberList.class));
		
		  Log.d("KMC","1");
		  Log.d("jonghun", "11111");
		
		
		
		//startActivityForResult(intent,0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    Log.d("KMC","resultCode: " + resultCode);

//	    if(resultCode == RESULT_OK){
//	    	findViewById(R.id.apple).setOnClickListener(mClickListener);
//			findViewById(R.id.orange).setOnClickListener(mClickListener);
//	    }
	    Vector<String> phoneArray = null;
		try{
			GetPhoneNumberList instance = new GetPhoneNumberList();
			phoneArray = ((GetPhoneNumberList)instance.getPhoneNumberListContext()).getPhoneNumbers();
		}catch(Exception e){
			StringWriter sw = new StringWriter();
		    e.printStackTrace(new PrintWriter(sw));
		    String exceptionAsStrting = sw.toString();
		    Log.e("KMC aa", exceptionAsStrting);
		    e.printStackTrace();
		}
		
		for(int i=0;i<phoneArray.size();i++){
			Log.d("KMC","phone Number: " + phoneArray.get(i));
		}
	}
	
	Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			TextView textFruit=(TextView)findViewById(R.id.fruit);
			switch (v.getId()) {
			case R.id.apple:
				textFruit.setText("Apple");
				Intent intent = new Intent(MainActivity.this, CreateMapView.class);
				startActivity(intent);
				finish();
				break;
			case R.id.orange:
				textFruit.setText("Orange");
				break;
			}
		}
	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
