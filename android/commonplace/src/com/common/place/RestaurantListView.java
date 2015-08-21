package com.common.place;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.common.place.model.RestaurantModel;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
	
	Context context;
	String location;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant_list_view);
		
		Logger.d( "INIT RestaurantListView"); 
		context= this;
		Intent intent = getIntent();
    	String location = intent.getStringExtra("location");
		 // 1. pass context and data to the custom adapter
		registerInBackground();
		
		//adapter = new RestaurantArrayAdapter(this, generateData());
		Logger.d( "INIT RestaurantListView 2");
        // if extending Activity 2. Get ListView from activity_main.xml
		
		findViewById(R.id.btn_selectRestaurant).setOnClickListener(mClickListener);

		Logger.d( "INIT RestaurantListView 3");
		
        // 3. setListAdapter
		
        //listView.setAdapter(adapter);// if extending Activity
        
        Logger.d( "INIT RestaurantListView 4");
	}

	Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_selectRestaurant:
				Logger.d( "Select Restaurant");
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
				
				Logger.d( "INIT RestaurantListView 5 : " + adapter.getItem(adapter.selected_position).getIcon());
				Logger.d( "INIT RestaurantListView 5 : " + adapter.getItem(adapter.selected_position).getName());
				
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
	
	private void registerInBackground() {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                String msg = "";

                try {
                    sendReatuanrantDataToBackend();
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

			@Override
			protected void onPostExecute(String result) {
				Logger.i("onPostExecute result:"+result);
				createList();
				super.onPostExecute(result);
			}

            

        }.execute(null, null, "aaa");
    }
	private void createList() {
		Logger.i("createList adapter:"+adapter);
		ListView listView = (ListView) findViewById(R.id.restaurantList);
        listView.setAdapter(adapter);
	}
	
	//Transfer Data to Server(httpRequest)
    private void sendReatuanrantDataToBackend() {
//    	Thread thread = new Thread() {
//            @Override
//            public void run() {
            	HttpClient httpClient = new DefaultHttpClient();

                try {
                    Log.d("COMMON", "TEST01");
                    URI url = new URI(Constants.RESTAURANT_URL);
                    Log.d("COMMON", "TEST02");
                    HttpPost httpPost = new HttpPost();
                    Log.d("COMMON", "TEST03");
                    httpPost.setURI(url);

                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("key", "AIzaSyDDlcMIjePsgpoGy9MmVpZJVV6veblp9xU"));
                    nameValuePairs.add(new BasicNameValuePair("location", "37.55500949462912,126.98537103831768"));
                    nameValuePairs.add(new BasicNameValuePair("radius", "500"));
                    nameValuePairs.add(new BasicNameValuePair("types", "restaurant"));

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);
                    String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                    
                    Log.d("COMMON", responseString);
                    
                    JsonElement jelement = new JsonParser().parse(responseString);
                    JsonObject  jobject = jelement.getAsJsonObject();
                    JsonArray result = jobject.getAsJsonArray("results");
                    
                    for(int i=0;i<result.size();i++){
                    	try{
                    		Log.d("COMMON","AAA02");
                        	JsonObject restaurant= result.get(i).getAsJsonObject();
                        	if(!restaurant.isJsonNull()){
                        		Log.d("COMMON","AAA03");
                            	JsonObject geometry = restaurant.get("geometry").getAsJsonObject();
                            	if(!geometry.isJsonNull()){
	                            	JsonObject location = geometry.get("location").getAsJsonObject();
	                            	Log.d("COMMON","AAA04");
	                            	if(!location.isJsonNull()){
		                            	String lat = location.get("lat").getAsString();
		                            	String lon = location.get("lng").getAsString();
		                            	
		                            	
		                            	String icon = restaurant.get("icon").getAsString();
		                            	String name = restaurant.get("name").getAsString();
		                            	String rating = restaurant.get("rating").getAsString();
		                            	Logger.d("lat: "+lat);
		                            	Logger.d("lon: "+lon);
		                            	Logger.d("icon"+icon);
		                            	Logger.d("rating"+rating);
		                            	Log.d("COMMON","AAA05");
		                            	
		                            	models.add(new RestaurantModel(R.drawable.example_1,name,rating,icon,"029999999",lat,lon,false));
		                            	Log.d("COMMON","AAA07");
	                            	}
                            	}
                        	}
                        	
                    	}catch(Exception e){
                    	}
                    	
                    }
                    
                    
    
                    adapter = new RestaurantArrayAdapter(getApplicationContext(), models);
                    
                    //JsonArray jarray = result.size()
                    
                    //String result = jobject.get("translatedText").toString();
                    //return result;

                    

                } catch (URISyntaxException e) {
                    Log.e("COMMON", e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    Log.e("COMMON", e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("COMMON", e.getLocalizedMessage());
                    e.printStackTrace();
                }
//            }
//        };
//
//        thread.start();
    }
}
