package com.common.place;

import java.util.ArrayList;
import java.util.HashMap;

import com.common.place.db.Provider;
import com.common.place.model.Group;
import com.common.place.model.GroupMember;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

public class MapActivity extends FragmentActivity implements View.OnClickListener, OnMapClickListener {
	
	private GoogleMap gmap;
	private String groupId;
	
	int requestType;
	Button restaurantSearch;
	public MarkerOptions markerOptions;
	LatLng selectedLatLng;
	
	InnerReceiver innerReceiver;
	IntentFilter filter;
	
	Group group;
	LatLng cameraLatLng;
	
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
		
		markerOptions = new MarkerOptions();
		
		makeCameraMove();
		
		if(requestType == Constants.REQUEST_TYPE_GPS_GETHERING){
			restaurantSearch.setVisibility(View.GONE);
			updateMemberPositions();
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
		makeCameraMove(false);
	}
	
	private void makeCameraMove(boolean isRefresh) {
		
		if(!isRefresh){
			Cursor cursor = getContentResolver().query(Provider.GROUP_CONTENT_URI, null, Provider.GROUP_ID+"=\'"+groupId+"\'", null, null);
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
			if(cursor != null){
				cursor.close();
			}
		}
		
		
		try {
            if (gmap == null) {
            	gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            }
        	if(group != null){
    			cameraLatLng = new LatLng(Double.parseDouble(group.getLocationLat()), 
    					Double.parseDouble(group.getLocationLon()));
    			markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.goal));
    			markerOptions.position(cameraLatLng);
    			gmap.addMarker(markerOptions);
        	}else{
        		cameraLatLng = new LatLng(37.541, 126.986);
        	}
        	if(!isRefresh){
        		gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng, 12));
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(requestType == Constants.REQUEST_TYPE_GPS_GETHERING){
			registerReceiver(innerReceiver, filter);
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(requestType == Constants.REQUEST_TYPE_GPS_GETHERING){
			unregisterReceiver(innerReceiver);
		}
	}
	
	public class InnerReceiver extends BroadcastReceiver{
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	Logger.i("InnerReceiver onReceive intent:"+intent);
	    	updateMemberPositions();
	    }
	     
	}
	
	private void updateMemberPositions(){
		new AsyncTask<Void, Void, ArrayList<GroupMember>>() {
			@Override
			protected ArrayList<GroupMember> doInBackground(Void... params) {
				ArrayList<GroupMember> group = new ArrayList<GroupMember>();
				
				Cursor cursor = getContentResolver().query(Provider.MEMBER_CONTENT_URI, null, Provider.GROUP_ID+"=\'"+groupId+"\'", null, null);
				if(cursor != null && cursor.getCount() > 0){
					
					if(cursor.moveToFirst()){
						do{
							GroupMember member = new GroupMember(
									cursor.getString(cursor.getColumnIndex(Provider.GROUP_ID)), 
									cursor.getString(cursor.getColumnIndex(Provider.NAME)), 
									cursor.getString(cursor.getColumnIndex(Provider.PHONE_NUMBER)), 
									cursor.getDouble(cursor.getColumnIndex(Provider.LOCATION_LAT)), 
									cursor.getDouble(cursor.getColumnIndex(Provider.LOCATION_LON))
									);
							Logger.i(member.toString());
							group.add(member);
						}while(cursor.moveToNext());
					}
				}
				if(cursor != null){
					cursor.close();
				}
				
				return group;
			}
			@Override
			protected void onPostExecute(ArrayList<GroupMember> group) {
				super.onPostExecute(group);
				setGpsToMap(group);
			}
			
		}.execute(null, null, null);
	}
	
	private HashMap<String, Marker> memberMarkers = new HashMap<String, Marker>();
	
    public void setGpsToMap(ArrayList<GroupMember> group){
    	
    	//gmap.clear();
    	//makeCameraMove(true);
    	
		for(int i = 0; i < group.size(); i++){
			
			GroupMember gMember = group.get(i);
			
			Marker marker = memberMarkers.get(gMember.getPhone());
			
			if(marker == null){
				
				try{
					LatLng latLng = new LatLng(gMember.getLocationLat(), gMember.getLocationLon());
					
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
					markerOptions.position(latLng);
					
					IconGenerator tc = new IconGenerator(this);
					tc.setColor(Color.argb(255, 255, 94, 0));
					tc.setTextAppearance(R.style.SpecialText);
					Bitmap bmp = tc.makeIcon(gMember.getName());
					markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp));
					
					Marker mk = gmap.addMarker(markerOptions);
					
					memberMarkers.put(gMember.getPhone(), mk);
					
					//gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng, 12));
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}else{
				
				Logger.i("UPDATE USER "+gMember.getName()+" ["+gMember.getPhone()+"]");
				Logger.i(marker.getPosition().latitude+" > "+gMember.getLocationLat());
				Logger.i(marker.getPosition().longitude+" > "+gMember.getLocationLon());
				
				animateMarker(marker, new LatLng(gMember.getLocationLat(), gMember.getLocationLon()), false);
			}
		}
    }
    
//    private void move(){
//    	LatLng oldLatLng = dummyMarker.getPosition();
//    	double switcher = 1.0;
//    	LatLng newLatLng = new LatLng(oldLatLng.latitude, oldLatLng.longitude + 1.0);
//    	switcher = (-1) * switcher;
//    	animateMarker(dummyMarker, newLatLng, false);
//    }
    public void animateMarker(final Marker marker, final LatLng toPosition,
            final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = gmap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
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