package com.common.place;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import com.common.place.model.Restaurant;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class RestaurantListActivity extends Activity implements View.OnClickListener {

	public RestaurantArrayAdapter adapter;
	private ArrayList<Restaurant> models;
	
	LatLng selectedlatLng;
	ProgressDialog dialog;
	
	int requestCount = 0;
	String pageToken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant_list_view);
		
		models = new ArrayList<Restaurant>();
		
		Intent intent = getIntent();
		selectedlatLng = intent.getParcelableExtra("location");
    	
	    dialog = ProgressDialog.show(RestaurantListActivity.this, "", RestaurantListActivity.this.getResources().getText(R.string.loading), true);
		
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
				dialog.dismiss();
				
				adapter = new RestaurantArrayAdapter(RestaurantListActivity.this, models);
				createList();
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
    		e.printStackTrace();
    	}
    	List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("key", Constants.GOOGLE_API_KEY));
        if(pageToken != null){
        	nameValuePairs.add(new BasicNameValuePair("pagetoken", pageToken));
        }else{
        	nameValuePairs.add(new BasicNameValuePair("location", selectedlatLng.latitude+","+selectedlatLng.longitude));
            nameValuePairs.add(new BasicNameValuePair("radius", "3000"));
            nameValuePairs.add(new BasicNameValuePair("types", "restaurant"+pipe+"food"+pipe+"cafe"));        	
        }
        
        //https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyDDlcMIjePsgpoGy9MmVpZJVV6veblp9xU&location=37,127&radius=3000&types=restaurant|food|cafe
        
        String responseString = "";
    	try {
    		responseString = Utils.callToServer(Constants.RESTAURANT_URL, nameValuePairs);
		} catch (Exception e) {
			e.printStackTrace();
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
        
        Logger.d(rawData);
        
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
            		String lat = "";
                	String lon = "";
                	String icon = "";
                	String name = "";
                	String rating = "";
                	String vicinity = "";
                	String photo_reference = "";
            		JsonObject geometry = restaurant.get("geometry").getAsJsonObject();
                	if(!geometry.isJsonNull()){
                    	JsonObject location = geometry.get("location").getAsJsonObject();
                    	if(!location.isJsonNull()){
                        	lat = location.get("lat") != null ? location.get("lat").getAsString() : "";
                        	lon = location.get("lng") != null ? location.get("lng").getAsString() : "";
                    	}
                	}
            		JsonArray photos = restaurant.getAsJsonArray("photos");
            		if(photos != null && photos.size() > 0){
	            		JsonObject photoJsonObject= photos.get(0).getAsJsonObject();
	                	if(!photoJsonObject.isJsonNull()){
	                		photo_reference = photoJsonObject.get("photo_reference") != null ? photoJsonObject.get("photo_reference").getAsString() : "";
	                	}
            		}
                	icon = restaurant.get("icon") != null ? restaurant.get("icon").getAsString() : "";
                	name = restaurant.get("name") != null ? restaurant.get("name").getAsString() : "";
                	rating = restaurant.get("rating") != null ? restaurant.get("rating").getAsString() : "-";
                	vicinity = restaurant.get("vicinity") != null ? restaurant.get("vicinity").getAsString() : "";
                	
                	String url = "";
                	
                	if(!"".equals(photo_reference)){
	                	List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
	                    nameValuePairs.add(new BasicNameValuePair("key", Constants.GOOGLE_API_KEY));
	                    nameValuePairs.add(new BasicNameValuePair("maxwidth", "200"));  
	                    nameValuePairs.add(new BasicNameValuePair("maxheight", "200"));  
	                    nameValuePairs.add(new BasicNameValuePair("photoreference", photo_reference));
	                    url = "https://maps.googleapis.com/maps/api/place/photo" + Utils.makeGetParams(nameValuePairs);
                	}
                	models.add(new Restaurant(url, name, rating, icon, "02-927-3745", lat, lon, false, vicinity));
            	}
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
        if(pageToken != null && requestCount < 3){
        	sendReatuanrantDataToBackend();
        	return;
        }
        requestCount = 0;
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_selectRestaurant:
			dialog = ProgressDialog.show(RestaurantListActivity.this, "", RestaurantListActivity.this.getResources().getText(R.string.loading), true);
			RegistGroupActivity.selectedRestaurant = models.get(adapter.selected_position);
			downloadImage(models.get(adapter.selected_position).getPhotoReference());
			break;
		}	
	}
	
	private void downloadImage(String url) {
        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... imageUrl) {
            	Bitmap bitmap = null;
	      		try {
	      			URL url = new URL(imageUrl[0]);
	      			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	      			connection.setDoInput(true);
	      			connection.connect();
	      			InputStream input = connection.getInputStream();
	      			bitmap = BitmapFactory.decodeStream(input);
	      		   
	      		}catch (Exception e) {
	      			e.printStackTrace();
	      		}
	      		return bitmap;
            }

			@Override
			protected void onPostExecute(Bitmap result) {
				super.onPostExecute(result);
				RegistGroupActivity.selectedRestaurantImage = result;
				dialog.dismiss();
				finish();
			}

        }.execute(url, null, null);
    }
	
	/*
	 * Bitmap downloadBitmap(String imageUrl) {
		
		Bitmap bitmap = null;
		  try {
		         URL url = new URL(imageUrl);
		         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		         connection.setDoInput(true);
		         connection.connect();
		         InputStream input = connection.getInputStream();
		         bitmap = BitmapFactory.decodeStream(input);
		   
		  }catch (Exception e) {
		   e.printStackTrace();
		  }
		  
		  return bitmap;
    }
	 * 
	 * */
	
}
