package com.common.place;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.entity.StringEntity;

import com.common.place.db.Provider;
import com.common.place.model.ContactsModel;
import com.common.place.model.GroupModel;
import com.common.place.model.RestaurantModel;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.common.place.util.Utils;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RegistGroup extends Activity implements OnClickListener{

	public ArrayList<GroupModel> groupList = new ArrayList<GroupModel>();
	public ArrayList<ContactsModel> getArrayList;
	public RestaurantModel restaurant;
	
	private GroupModel group;
	
	ImageView retaurant_image;
	TextView retaurant_description, contact_list;
	int id_count = 1;
	
	public static RestaurantModel selectedRestaurant;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist_group);

		contact_list = (TextView)findViewById(R.id.contacts_description);
		retaurant_image = (ImageView)findViewById(R.id.retaurant_image);
		retaurant_description= (TextView)findViewById(R.id.retaurant_description);

		findViewById(R.id.seachAddr).setOnClickListener(this);
		findViewById(R.id.searchMap).setOnClickListener(this);
		findViewById(R.id.btn_contacts).setOnClickListener(this);
		findViewById(R.id.registGroup).setOnClickListener(this);
		
		deleteAllMemberListInDB();
	}

	@Override
	protected void onDestroy() {
		deleteAllMemberListInDB();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		showSelectedRestaurant();
		createMemberList();
		super.onResume();
	}
	
	public void showSelectedRestaurant(){
		if(selectedRestaurant != null){
			restaurant =(RestaurantModel)selectedRestaurant;
			retaurant_image.setImageResource(restaurant.getIcon());
			retaurant_description.setText(restaurant.getName() +"\n" + restaurant.getDescription() +"\n" + restaurant.getPhone());
			retaurant_image.setVisibility(1);
			retaurant_image.getLayoutParams().width = 500;
			retaurant_image.getLayoutParams().height = 500;
		}else{
			retaurant_image.setVisibility(0);
			retaurant_image.getLayoutParams().width = 1;
			retaurant_image.getLayoutParams().height = 1;
			retaurant_description.setText(R.string.group_not_regist);
		}
	}
	
	public void createMemberList(){
		Cursor memberCursor = getMemberListFromDB();
		if(memberCursor != null && memberCursor.getCount() > 0){
			contact_list.setText("");
			contact_list.setGravity(Gravity.LEFT);
			Toast.makeText(this, memberCursor.getCount() + " selected!!!!", Toast.LENGTH_SHORT).show();
			
			if(memberCursor.moveToFirst()){
				do{
					contact_list.append(memberCursor.getString(memberCursor.getColumnIndex(Provider.RECIPIENT)) + "  " + memberCursor.getString(memberCursor.getColumnIndex(Provider.PHONE_NUMBER)) + "\n");
				}while(memberCursor.moveToNext());
			}
		}else{
			contact_list.setGravity(Gravity.CENTER);
			contact_list.setText("...");
		}
	}

	public int deleteAllMemberListInDB(){
		return getContentResolver().delete(Provider.RECIPIENT_CONTENT_URI, null, null);
	}
	// you can use member list like this...
	public Cursor getMemberListFromDB(){
		return getContentResolver().query(Provider.RECIPIENT_CONTENT_URI, null, null, null, null);
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.seachAddr:
			Logger.d( "Search Addr");

			break;
		case R.id.searchMap:
			Logger.d( "Search Map");
			Intent intent = new Intent(getApplicationContext(), MapActivity.class);
			intent.putExtra("requestType",Constants.REQUEST_TYPE_MAP_CREATE);
			startActivityForResult(intent,Constants.MAP_VIEW_REQ_CODE);
			break;
		case R.id.btn_contacts:
			startActivity(new Intent(getApplicationContext(), MemberActivity.class));
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
			group.setLocationImageUrl(String.valueOf(restaurant.getIcon()));
			group.setLocationLat(restaurant.getLocationLat());
			group.setLocationLon(restaurant.getLocationLon());
			group.setLocationName(restaurant.getName());
			group.setLocationPhone(restaurant.getPhone());
			group.setMemeber(getArrayList);
			group.setOwner(Utils.getPhoneNumber(RegistGroup.this));
			
			Intent intentGroupMain = new Intent(getApplicationContext(), GroupMainView.class);
			intentGroupMain.putExtra("group",group);
			
			Logger.d( "groupName: " + groupName);
				
    		registerInBackground();
    		
			setResult(Constants.GROUP_MAIN_VIEW_REQ_CODE, intentGroupMain);
			
			finish();
		}	
	}
	private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                	String groupInfo = new Gson().toJson(group);
                    sendReatuanrantDataToBackend(groupInfo);
                } catch (Exception e) {
                	Logger.e(e.getMessage());
                }
				return null;
            }
        }.execute(null, null, null);
    }
	
	//Transfer Data to Server(httpRequest)
    private void sendReatuanrantDataToBackend(String groupInfo) {
    	try {
			Utils.callToServer(RegistGroup.this, Constants.SVR_MOIM_REGIST_URL, new StringEntity(groupInfo));
		} catch (UnsupportedEncodingException e) {
			Logger.e(e.getMessage());
		}
    }
}
