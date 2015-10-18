package com.common.place;

import java.util.ArrayList;

import com.common.place.db.Provider;
import com.common.place.model.ContactsModel;
import com.common.place.model.Group;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.common.place.util.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MapActivity extends FragmentActivity implements View.OnClickListener, OnMapClickListener {
	
	private GoogleMap gmap;
	private String groupId;
	
	int requestType;
	Button restaurantSearch;
	public MarkerOptions markerOptions = new MarkerOptions();
	LatLng selectedLatLng;
	
	InnerReceiver innerReceiver;
	IntentFilter filter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		restaurantSearch = (Button) findViewById(R.id.restaurantSearch);
		restaurantSearch.setOnClickListener(this);
		
		Intent request = getIntent();
		requestType = request.getIntExtra("requestType", 0);
//		contactArray = request.getSerializableExtra("groupId");
		groupId = request.getStringExtra("groupId");
		
		makeCameraMove();
		
		if(requestType == Constants.REQUEST_TYPE_GPS_GETHERING){
			restaurantSearch.setVisibility(View.GONE);
		}else if(requestType == Constants.REQUEST_TYPE_MAP_CREATE){
			restaurantSearch.setVisibility(View.VISIBLE);
			gmap.setOnMapClickListener(this);
		}else{
			Logger.e("requestType" + requestType);
			MapActivity.this.finish();
		}
		
		innerReceiver = new InnerReceiver();
		filter = new IntentFilter(Constants.INNER_BROADCAST_RECEIVER);
	}
	
	private void makeCameraMove() {

		Group group = null;
		Cursor cursor = getContentResolver().query(Provider.GROUP_CONTENT_URI, null, Provider.GROUP_ID + " = " + groupId, null, null);
		if(cursor != null && cursor.getCount() > 0){
			if(cursor.moveToFirst()){
				do{
					group = new Group(cursor.getString(cursor.getColumnIndex(Provider.GROUP_ID)), 
							cursor.getString(cursor.getColumnIndex(Provider.TITLE)), 
							cursor.getString(cursor.getColumnIndex(Provider.OWNER)), 
							cursor.getString(cursor.getColumnIndex(Provider.TIME)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_NAME)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_IMAGE_URL)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_LAT)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_LON)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_PHONE)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_DESC)), 
							new ArrayList<String>());
					Logger.d("getMemberListFromDB() group title:"+group.getTitle());
				}while(cursor.moveToNext());
			}
		}
		
		LatLng cameraLatLng = null;
		
		try {
            if (gmap == null) {
            	gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            	
            	if(group != null){
        			cameraLatLng = new LatLng(Double.parseDouble(group.getLocationLat()), 
        					Double.parseDouble(group.getLocationLon()));
        			markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.goal));
        			markerOptions.position(cameraLatLng);
        			gmap.addMarker(markerOptions);
            	}else{
            		cameraLatLng = new LatLng(37.541, 126.986);
            	}
            	gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng, 12));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getMemberPositions();
		registerReceiver(innerReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(innerReceiver);
	}
	
	public class InnerReceiver extends BroadcastReceiver{
		 
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	getMemberPositions();
	    }
	     
	}
	
	private void getMemberPositions(){
		new AsyncTask<Void, Void, ArrayList<ContactsModel>>() {
			@Override
			protected ArrayList<ContactsModel> doInBackground(Void... params) {
				ArrayList<ContactsModel> group = new ArrayList<ContactsModel>();
				Cursor cursor = getContentResolver().query(Provider.MEMBER_CONTENT_URI, null, Provider.GROUP_ID + " = " + groupId, null, null);
				if(cursor != null && cursor.getCount() > 0){
					if(cursor.moveToFirst()){
						do{
							ContactsModel contactsModel = new ContactsModel(
									cursor.getString(cursor.getColumnIndex(Provider.GROUP_ID)), 
									cursor.getString(cursor.getColumnIndex(Provider.NAME)), 
									cursor.getString(cursor.getColumnIndex(Provider.PHONE_NUMBER)), 
									cursor.getString(cursor.getColumnIndex(Provider.LOCATION_LAT)), 
									cursor.getString(cursor.getColumnIndex(Provider.LOCATION_LON))
									);
							group.add(contactsModel);
						}while(cursor.moveToNext());
					}
				}
				return group;
			}
			@Override
			protected void onPostExecute(ArrayList<ContactsModel> group) {
				super.onPostExecute(group);
				setGpsToMap(group);
			}
			
		}.execute(null, null, null);
	}
	
    public void setGpsToMap(ArrayList<ContactsModel> group){
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
		for(int i = 0; i < group.size(); i++){
			try{
				LatLng latLng = new LatLng(Double.parseDouble(group.get(i).getLocationLat()), 
					Double.parseDouble(group.get(i).getLocationLon()));
				markerOptions.position(latLng);
//				markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
				markerOptions.icon(Utils.getTextMarker(group.get(i).getName()));
				gmap.addMarker(markerOptions);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.restaurantSearch:
			if(selectedLatLng == null){
				Toast.makeText(MapActivity.this, "Select Location...", Toast.LENGTH_SHORT).show();
				return;
			}
			Intent i = new Intent(getApplicationContext(), RestaurantListActivity.class);
			i.putExtra("location", selectedLatLng);
			startActivity(i);
			MapActivity.this.finish();
			break;
		}	
	}

	@Override
	public void onMapClick(LatLng latLng) {
		markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
		markerOptions.position(latLng);
		gmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
		gmap.clear();
		gmap.addMarker(markerOptions); 
		selectedLatLng = latLng;
	}
}