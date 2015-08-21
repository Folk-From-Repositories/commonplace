package com.common.place;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.common.place.db.Provider;
import com.common.place.model.ContactsModel;
import com.common.place.model.GroupModel;
import com.common.place.model.RestaurantModel;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RegistGroup extends Activity implements OnClickListener{

	public static Context registGroupContext;
	public static String ownerPhoneNumber;
	
	public ArrayList<GroupModel> groupList = new ArrayList<GroupModel>();
	public ArrayList<ContactsModel> getArrayList;
	public RestaurantModel restaurant;
	
	private GroupModel group;
	
	ImageView retaurant_image;
	TextView retaurant_description;
	int id_count=1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist_group);
		Log.d("KMC", "INIT RegistGroup");
		
		registGroupContext = getApplicationContext();
		
		findViewById(R.id.seachAddr).setOnClickListener(this);
		findViewById(R.id.searchMap).setOnClickListener(this);
		findViewById(R.id.btn_contacts).setOnClickListener(this);
		findViewById(R.id.registGroup).setOnClickListener(this);
		
		deleteAllMemberListInDB();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case Constants.RESTAURANT_LIST_REQ_CODE:
			
			break;
		
		case Constants.MAP_VIEW_REQ_CODE:
			Log.d("KMC","RegistGroup's onActivityResult MAP_VIEW_REQ_CODE: " + resultCode);
			
			retaurant_image = (ImageView)findViewById(R.id.retaurant_image);
			retaurant_description= (TextView)findViewById(R.id.retaurant_description);
			
			
			
			if(data != null && data.getExtras() != null){
				Serializable restaurantInfo = data.getSerializableExtra("restaurantInfo");
				
				restaurant =(RestaurantModel)restaurantInfo;
							
//				int icon = data.getExtras().getInt("icon");
//				String title = data.getStringExtra("title");
				
				retaurant_image.setImageResource(restaurant.getIcon());
				retaurant_description.setText(restaurant.getDescription());
				retaurant_image.setVisibility(1);
				retaurant_image.getLayoutParams().width = 500;
				retaurant_image.getLayoutParams().height = 500;
				
			}else{
				retaurant_image.setVisibility(0);
				retaurant_image.getLayoutParams().width = 1;
				retaurant_image.getLayoutParams().height = 1;
				retaurant_description.setText("등록된 모임 장소가 없습니다.");
			}
			break;
			
		case Constants.MEMBER_ACTIVITY_REQ_CODE:
			Log.d("KMC","onActivityResult("+Constants.MEMBER_ACTIVITY_REQ_CODE+")");

			Serializable contactArray = data.getSerializableExtra("contactArrayList");
			TextView contact_list= (TextView)findViewById(R.id.contacts_description);
			
			if(contactArray != null){
				getArrayList = (ArrayList<ContactsModel>) contactArray;
				contact_list.setText("");
				for(int i=0;i<getArrayList.size();i++){
					String name = getArrayList.get(i).getName();
					String phone = getArrayList.get(i).getPhone();
					Log.d("KMC name", name);
					Log.d("KMC phone", phone);
					
					contact_list.setGravity(Gravity.LEFT);
					contact_list.append(name + "  " + phone + "\n");
				}
			}else{
				contact_list.setGravity(Gravity.CENTER);
				contact_list.setText("등록된 모임 인원이 없습니다.");
				Log.d("KMC"," is null");
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		deleteAllMemberListInDB();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// when select member from contacts....
		Cursor memberCursor = getMemberListFromDB();
		
		if(memberCursor.getCount() > 0){
			Toast.makeText(this, memberCursor.getCount() + " selected!!!!", Toast.LENGTH_SHORT).show();
		}
		
		super.onResume();
	}

	public int deleteAllMemberListInDB(){
		return getContentResolver().delete(Provider.CONTENT_URI, null, null);
	}
	// you can use member list like this...
	public Cursor getMemberListFromDB(){
		return getContentResolver().query(Provider.CONTENT_URI, null, null, null, null);
	}
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.regist_group, menu);
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



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.seachAddr:
			Log.d("KMC", "Search Addr");
			break;
		case R.id.searchMap:
			Log.d("KMC", "Search Map");
			Intent intent = new Intent(getApplicationContext(), CreateMapView.class);
			intent.putExtra("requestType",Constants.REQUEST_TYPE_MAP_CREATE);
			startActivityForResult(intent,Constants.MAP_VIEW_REQ_CODE);
			break;
		case R.id.btn_contacts:
			Logger.i("Contacts button clicked");
			// call back is not needed!! because member list is stored in Database!!
			startActivityForResult(new Intent(getApplicationContext(), MemberActivity.class),Constants.MEMBER_ACTIVITY_REQ_CODE);
			//startActivity(new Intent(getApplicationContext(), MemberActivity.class));
			break;
		case R.id.registGroup:
			/*
			 * you must save data to server
			 */		
			group = new GroupModel();
			
			EditText groupName=(EditText)findViewById(R.id.name_edit);
			EditText meetTime=(EditText)findViewById(R.id.time_edit);
			
			group.setTitle(groupName.getText().toString());
			group.setTime(meetTime.getText().toString());
			group.setId(String.valueOf(id_count));
			group.setLocationDesc(restaurant.getDescription());
			group.setLocationImageUrl(String.valueOf(restaurant.getIcon()));//current ImageUrl 은 없고 android png파일
			group.setLocationLat(restaurant.getLocationLat());
			group.setLocationLon(restaurant.getLocationLon());
			group.setLocationName(restaurant.getName());
			group.setLocationPhone(restaurant.getPhone());
			group.setMemeber(getArrayList);
			group.setOwner(ownerPhoneNumber);
			
			Intent intentGroupMain = new Intent(getApplicationContext(), GroupMainView.class);
			intentGroupMain.putExtra("group",group);
			
			Log.d("KMC", "groupName: " + groupName);
				
    		registerInBackground();
    		
			setResult(Constants.GROUP_MAIN_VIEW_REQ_CODE, intentGroupMain);
			
			finish();
		}	
	}
	private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";

                try {
                	String groupInfo = new Gson().toJson(group);
                    sendReatuanrantDataToBackend(groupInfo);
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

        }.execute(null, null, null);
    }
	
	//Transfer Data to Server(httpRequest)
    private void sendReatuanrantDataToBackend(final String groupInfo) {
    	Thread thread = new Thread() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();

        		Log.d("KMC"," TEST JSON: " + groupInfo);
        		
                
//                String urlString = "http://rambling.synology.me:52015/commonplace/gcm/regist";
                try {
                    URI url = new URI(Constants.SVR_MOIM_REGIST_URL); // use Constants.java file like this!!

                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(url);

//                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                    
                    StringEntity params =new StringEntity(groupInfo);
                    httpPost.setEntity(params);

                    HttpResponse response = httpClient.execute(httpPost);
                    String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                   Log.d("KMC responseString", responseString);
                    
                } catch (URISyntaxException e) {
                    Logger.e(e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    Logger.e(e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    Logger.e(e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }
}
