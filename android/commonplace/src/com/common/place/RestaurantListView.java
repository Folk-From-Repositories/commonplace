package com.common.place;

import java.util.ArrayList;

import com.common.place.model.RestaurantModel;
import com.common.place.util.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class RestaurantListView extends Activity  {

	public RestaurantArrayAdapter adapter;
	private ArrayList<RestaurantModel> models = new ArrayList<RestaurantModel>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant_list_view);
		
		Log.d("KMC", "INIT RestaurantListView");
		
		 // 1. pass context and data to the custom adapter
		adapter = new RestaurantArrayAdapter(this, generateData());
		Log.d("KMC", "INIT RestaurantListView 2");
        // if extending Activity 2. Get ListView from activity_main.xml
		ListView listView = (ListView) findViewById(R.id.restaurantList);
		findViewById(R.id.btn_selectRestaurant).setOnClickListener(mClickListener);

		Log.d("KMC", "INIT RestaurantListView 3");
		
        // 3. setListAdapter
		
        listView.setAdapter(adapter);// if extending Activity
        
        Log.d("KMC", "INIT RestaurantListView 4");
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_selectRestaurant:
				Log.d("KMC", "Select Restaurant");
				//initGroup();
				Intent intent = new Intent(getApplicationContext(), CreateMapView.class);
				
//				models.get(adapter.selected_position);
				intent.putExtra("restaurantInfo",models.get(adapter.selected_position));
//				intent.putExtra("description",adapter.getItem(adapter.selected_position).getDescription());
//				intent.putExtra("imageUrl",adapter.getItem(adapter.selected_position).getImageUrl());
//				intent.putExtra("locationLat",adapter.getItem(adapter.selected_position).getLocationLat());
//				intent.putExtra("locationLon",adapter.getItem(adapter.selected_position).getLocationLon());
//				intent.putExtra("name",adapter.getItem(adapter.selected_position).getName());
//				intent.putExtra("phone",adapter.getItem(adapter.selected_position).getPhone());
				
				Log.d("KMC", "INIT RestaurantListView 5 : " + adapter.getItem(adapter.selected_position).getIcon());
				Log.d("KMC", "INIT RestaurantListView 5 : " + adapter.getItem(adapter.selected_position).getName());
				
				RestaurantListView.this.setResult(Constants.RESTAURANT_LIST_REQ_CODE, intent);
				
				finish();
				break;
			}	
		}
	};
	
	private ArrayList<RestaurantModel> generateData(){
        
        models.add(new RestaurantModel(R.drawable.example_1,"풍년 숯불갈비","맛있는 곳","","029919999","12.1","13.1",false));
        models.add(new RestaurantModel(R.drawable.example_2,"란나 타이","가까운 곳","","023333333","25.2","44.4",false));
        models.add(new RestaurantModel(R.drawable.example_3,"중국 음식점","분위기 좋은곳","","027777777","66.6","77.7",false));

        return models;
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.restaurant_list_view, menu);
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
