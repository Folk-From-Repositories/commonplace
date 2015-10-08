package com.common.place;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import com.common.place.model.RestaurantModel;
import com.common.place.uicomponents.RestaurantArrayAdapter;
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

public class RestaurantListActivity extends Activity implements View.OnClickListener {

	public RestaurantArrayAdapter adapter;
	private ArrayList<RestaurantModel> models;
	
	LatLng selectedlatLng;
	ProgressDialog dialog;
	
	int requestCount = 0;
	String pageToken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant_list_view);
		
		models = new ArrayList<RestaurantModel>();
		
		Intent intent = getIntent();
		selectedlatLng = intent.getParcelableExtra("location");
    	
	    dialog = ProgressDialog.show(RestaurantListActivity.this, "", "로딩 중입니다. 잠시 기다려주세요", true);
		
	    pageToken = null;
	    
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
    	String pipe = "";
    	try {
    		pipe = URLEncoder.encode("|","UTF-8");
    	} catch (Exception e) {
    		Logger.e(e.getMessage());
    	}
    	List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("key", "AIzaSyDDlcMIjePsgpoGy9MmVpZJVV6veblp9xU"));
        if(pageToken != null){
        	nameValuePairs.add(new BasicNameValuePair("pagetoken", pageToken));
        }else{
        	nameValuePairs.add(new BasicNameValuePair("location", selectedlatLng.latitude+","+selectedlatLng.longitude));
            nameValuePairs.add(new BasicNameValuePair("radius", "3000"));
            nameValuePairs.add(new BasicNameValuePair("types", "restaurant"+pipe+"food"+pipe+"cafe"));        	
        }
        
        String responseString = "";
    	try {
    		responseString = Utils.callToServer(Constants.RESTAURANT_URL, nameValuePairs);
		} catch (Exception e) {
			Logger.e(e.getMessage());
		}
    	requestCount++;
    	makeRestaurantList(responseString);
    	
    }
    
    private void makeRestaurantList(String rawData){
    	if(rawData == null || rawData.equals("")){
    		return;
    	}
    	JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject  jobject = jelement.getAsJsonObject();
        JsonArray result = jobject.getAsJsonArray("results");
        
        JsonElement nextPageTokenElement = jobject.get("next_page_token");
        if(nextPageTokenElement != null){
        	pageToken = nextPageTokenElement.getAsString();
        }
        
        JsonElement statusElement = jobject.get("status");
        String statusString = null;
        if(statusElement != null){
        	statusString = statusElement.getAsString();
        	if(statusString.equals("INVALID_REQUEST")){
        		try {
        	        Thread.sleep(800); // WAIT FOR NEXT PAGE (BY GOOGLE)         
        	    } catch (InterruptedException e) {
        	       e.printStackTrace();
        	    }
        		requestCount--;
        		sendReatuanrantDataToBackend();
        		return;
        	}
        }
        
        
        for(int i=0 ; i < result.size() ; i++){
        	try{
            	JsonObject restaurant= result.get(i).getAsJsonObject();
            	if(!restaurant.isJsonNull()){
                	JsonObject geometry = restaurant.get("geometry").getAsJsonObject();
                	if(!geometry.isJsonNull()){
                    	JsonObject location = geometry.get("location").getAsJsonObject();
                    	if(!location.isJsonNull()){
                        	String lat = location.get("lat") != null ? location.get("lat").getAsString() : "";
                        	String lon = location.get("lng") != null ? location.get("lng").getAsString() : "";
                        	String icon = restaurant.get("icon") != null ? restaurant.get("icon").getAsString() : "";
                        	String name = restaurant.get("name") != null ? restaurant.get("name").getAsString() : "";
                        	String rating = restaurant.get("rating") != null ? restaurant.get("rating").getAsString() : "";
                        	
                        	models.add(new RestaurantModel(R.drawable.example_1, name, rating, icon, "02-927-3745", lat, lon, false));
                    	}
                	}
            	}
        	}catch(Exception e){
        		Logger.e(e.getMessage());
        	}
        }
        if(pageToken != null && requestCount < 3){
        	sendReatuanrantDataToBackend();
        	return;
        }
        adapter = new RestaurantArrayAdapter(getApplicationContext(), models);
        requestCount = 0;
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_selectRestaurant:
			RegistGroupActivity.selectedRestaurant = models.get(adapter.selected_position);
			finish();
			break;
		}	
	}
}
