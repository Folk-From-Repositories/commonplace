package com.common.place;

import java.util.ArrayList;

import com.common.place.model.Model;
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
				intent.putExtra("icon",adapter.getItem(adapter.selected_position).getIcon());
				intent.putExtra("title",adapter.getItem(adapter.selected_position).getTitle());
				
				Log.d("KMC", "INIT RestaurantListView 5 : " + adapter.getItem(adapter.selected_position).getIcon());
				Log.d("KMC", "INIT RestaurantListView 5 : " + adapter.getItem(adapter.selected_position).getTitle());
				
				RestaurantListView.this.setResult(Constants.RESTAURANT_LIST_REQ_CODE, intent);
				
				finish();
				break;
			}	
		}
	};
	
	private ArrayList<Model> generateData(){
        ArrayList<Model> models = new ArrayList<Model>();
        //models.add(new Model("���� ����"));
        models.add(new Model(R.drawable.example_1,"풍년 숯불갈비","1"));
        models.add(new Model(R.drawable.example_2,"란나 타이","2"));
        models.add(new Model(R.drawable.example_3,"중국 음식점","3"));
 
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
