package com.common.place;

import com.common.place.util.Logger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.common.place.util.Constants;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class CreateMapView extends FragmentActivity   {
	
	private GoogleMap gmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		Logger.d("TEST 01");

		findViewById(R.id.restaurantSearch).setOnClickListener(mClickListener);
		
		try {
            if (gmap == null) {
            	LatLng cameraLatLng = new LatLng(37.541, 126.986);
            	
            	gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            	gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng,12));
            }
        } catch (Exception e) {
        	Logger.d("TEST EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
		
		Logger.d("TEST 02: " + gmap);
		
		gmap.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng latLng) {
				// TODO Auto-generated method stub
				Logger.d("TEST 03   " + latLng);
				MarkerOptions markerOptions = new MarkerOptions();
				markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
				markerOptions.position(latLng); //��Ŀ��ġ����
				Logger.d("TEST 04");

				gmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
				//gmap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));   // ��Ŀ������ġ�� �̵�
				gmap.clear();
				gmap.addMarker(markerOptions); 
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("KMC", "CREATE Map onActivityResult" + resultCode);
		
		switch(requestCode){
			case Constants.RESTAURANT_LIST_REQ_CODE:
				setResult(Constants.MAP_VIEW_REQ_CODE, data);
				finish();
				break;
			default:
				break;
		}
		
		
	}
	
    Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.restaurantSearch:
				Logger.d("Search Restaurant");
				//initGroup();
				//RegistGroup instance = new RegistGroup();
				//Context context = (CreateMapView)instance.registGroupContext;
				
				startActivityForResult(new Intent(getApplicationContext(), RestaurantListView.class),Constants.RESTAURANT_LIST_REQ_CODE);
				break;
			}	
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
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
