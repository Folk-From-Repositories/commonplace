package com.common.place;

import java.util.ArrayList;

import com.common.place.model.Model;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class RestaurantListView extends Activity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant_list_view);
		
		Log.d("KMC", "INIT RestaurantListView");
		
		 // 1. pass context and data to the custom adapter
		RestaurantArrayAdapter adapter = new RestaurantArrayAdapter(this, generateData());
		Log.d("KMC", "INIT RestaurantListView 2");
        // if extending Activity 2. Get ListView from activity_main.xml
		ListView listView = (ListView) findViewById(R.id.restaurantList);
 
        // 3. setListAdapter
        listView.setAdapter(adapter);// if extending Activity
        //setListAdapter(adapter);
        Log.d("KMC", "INIT RestaurantListView 3");
        
//        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//	    	@Override
//	    	public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
//	    		Toast.makeText(GroupMainView.this, "You Clicked at " +groupNameList.get(position), Toast.LENGTH_SHORT).show();
//            }
//    	});
	}

	private ArrayList<Model> generateData(){
        ArrayList<Model> models = new ArrayList<Model>();
        //models.add(new Model("맛집 정보"));
        models.add(new Model(R.drawable.example_1,"회먹고 술한잔","1"));
        models.add(new Model(R.drawable.example_2,"치킨먹고 뒤져보자","2"));
        models.add(new Model(R.drawable.example_3,"막걸리만 파는곳","3"));
 
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
