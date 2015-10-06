package com.common.place;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.common.place.model.RestaurantModel;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.common.place.util.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class RestaurantListView extends Activity implements View.OnClickListener {

	public RestaurantArrayAdapter adapter;
	private ArrayList<RestaurantModel> models = new ArrayList<RestaurantModel>();
	
	LatLng selectedlatLng;
	ProgressDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant_list_view);
		
		Intent intent = getIntent();
		selectedlatLng = intent.getParcelableExtra("location");
    	
	    dialog = ProgressDialog.show(RestaurantListView.this, "", "로딩 중입니다. 잠시 기다려주세요", true);
		
		getRestaurantListFromWeb();
		
		findViewById(R.id.btn_selectRestaurant).setOnClickListener(this);

	}

	private void getRestaurantListFromWeb() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                sendReatuanrantDataToBackend();
                return null;
            }

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				createList();
				dialog.dismiss();
			}
        }.execute(null, null, null);
    }
	
	private void createList() {
		ListView listView = (ListView) findViewById(R.id.restaurantList);
        listView.setAdapter(adapter);
	}
	
    private void sendReatuanrantDataToBackend() {
    	
    	List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("key", "AIzaSyDDlcMIjePsgpoGy9MmVpZJVV6veblp9xU"));
        nameValuePairs.add(new BasicNameValuePair("location", selectedlatLng.latitude+","+selectedlatLng.longitude));
        nameValuePairs.add(new BasicNameValuePair("radius", "500"));
        nameValuePairs.add(new BasicNameValuePair("types", "restaurant"));

        String responseString = "";
    	try {
    		responseString = Utils.callToServer(RestaurantListView.this, Constants.RESTAURANT_URL, new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e) {
			Logger.e(e.getMessage());
		}
    	
    	makeRestaurantList(responseString);
    	
    }
    
    private void makeRestaurantList(String rawData){
    	if(rawData == null || rawData.equals("")){
    		return;
    	}
    	JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject  jobject = jelement.getAsJsonObject();
        JsonArray result = jobject.getAsJsonArray("results");
        
        for(int i=0 ; i < result.size() ; i++){
        	try{
            	JsonObject restaurant= result.get(i).getAsJsonObject();
            	if(!restaurant.isJsonNull()){
                	JsonObject geometry = restaurant.get("geometry").getAsJsonObject();
                	if(!geometry.isJsonNull()){
                    	JsonObject location = geometry.get("location").getAsJsonObject();
                    	if(!location.isJsonNull()){
                        	String lat = location.get("lat").getAsString();
                        	String lon = location.get("lng").getAsString();
                        	String icon = restaurant.get("icon").getAsString();
                        	String name = restaurant.get("name").getAsString();
                        	String rating = restaurant.get("rating").getAsString();
                        	
                        	models.add(new RestaurantModel(R.drawable.example_1, name, rating, icon, "029999999", lat, lon, false));
                    	}
                	}
            	}
        	}catch(Exception e){
        		Logger.e(e.getMessage());
        	}
        }
        adapter = new RestaurantArrayAdapter(getApplicationContext(), models);
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_selectRestaurant:
			RegistGroup.selectedRestaurant = models.get(adapter.selected_position);
			finish();
			break;
		}	
	}
}
