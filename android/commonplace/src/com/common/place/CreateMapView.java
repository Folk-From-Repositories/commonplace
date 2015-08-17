package com.common.place;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.common.place.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class CreateMapView extends FragmentActivity   {
	
	private GoogleMap gmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		Log.d("KMC", "TEST 01");

		//final GoogleMap gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		
		try {
            if (gmap == null) {
            	gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            }
        } catch (Exception e) {
        	Log.d("KMC", "TEST EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
		
		Log.d("KMC", "TEST 02");
		
		gmap.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng latLng) {
				// TODO Auto-generated method stub
				Log.d("KMC", "TEST 03   " + latLng);
				MarkerOptions markerOptions = new MarkerOptions();
				markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
				markerOptions.position(latLng); //��Ŀ��ġ����
				Log.d("KMC", "TEST 04");

				gmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));   // ��Ŀ������ġ�� �̵�
				//gmap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));   // ��Ŀ������ġ�� �̵�
				gmap.addMarker(markerOptions); //��Ŀ ����
			}
		});
	}

	
	
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
