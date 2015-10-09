package com.common.place;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class RegistGroupActivity extends Activity implements OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

	public ArrayList<GroupModel> groupList = new ArrayList<GroupModel>();
	public ArrayList<ContactsModel> getArrayList;
	public RestaurantModel restaurant;
	
	private GroupModel group;
	
	ImageView retaurant_image;
	TextView retaurant_description, contact_list;
	Button btn_date, btn_time;
	
	int id_count = 1;
	
	public static RestaurantModel selectedRestaurant;
	
	int year, month, day, hour, minute;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist_group);

		btn_date = (Button) findViewById(R.id.btn_date);
		btn_time = (Button) findViewById(R.id.btn_time);
		btn_date.setOnClickListener(this);
		btn_time.setOnClickListener(this);
		
		contact_list = (TextView)findViewById(R.id.contacts_description);
		retaurant_image = (ImageView)findViewById(R.id.retaurant_image);
		retaurant_description= (TextView)findViewById(R.id.retaurant_description);

		findViewById(R.id.seachAddr).setOnClickListener(this);
		findViewById(R.id.searchMap).setOnClickListener(this);
		findViewById(R.id.btn_contacts).setOnClickListener(this);
		findViewById(R.id.btn_regist_group).setOnClickListener(this);
		
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
			//retaurant_image.setImageResource(restaurant.getIcon());
			retaurant_description.setText(restaurant.getName() +"\n" + restaurant.getDescription() +"\n" + restaurant.getPhone());
			retaurant_image.setVisibility(View.VISIBLE);
		}else{
			retaurant_image.setVisibility(View.GONE);
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
	
	public void getCurrentCalendar(){
		GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day= calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_date:
			getCurrentCalendar();
			new DatePickerDialog(RegistGroupActivity.this, RegistGroupActivity.this, year, month, day).show();
			break;
		case R.id.btn_time:
			getCurrentCalendar();
			new TimePickerDialog(RegistGroupActivity.this, RegistGroupActivity.this, hour, minute, false).show();
			break;
		case R.id.searchMap:
			Intent intent = new Intent(getApplicationContext(), MapActivity.class);
			intent.putExtra("requestType",Constants.REQUEST_TYPE_MAP_CREATE);
			startActivity(intent);
			break;
		case R.id.btn_contacts:
			startActivity(new Intent(getApplicationContext(), MemberActivity.class));
			break;
		case R.id.btn_regist_group:
			/*
			 * you must save data to server
			 */		
			group = new GroupModel();
			
			EditText groupName=(EditText)findViewById(R.id.name_edit);
			//EditText meetTime=(EditText)findViewById(R.id.time_edit);
			
			group.setTitle(groupName.getText().toString());
			//group.setTime(meetTime.getText().toString());
			group.setId(String.valueOf(id_count));
			group.setLocationDesc(restaurant.getDescription());
			//group.setLocationImageUrl(String.valueOf(restaurant.getIcon()));
			group.setLocationLat(restaurant.getLocationLat());
			group.setLocationLon(restaurant.getLocationLon());
			group.setLocationName(restaurant.getName());
			group.setLocationPhone(restaurant.getPhone());
			group.setMemeber(getArrayList);
			group.setOwner(Utils.getPhoneNumber(RegistGroupActivity.this));
			
			Intent intentGroupMain = new Intent(getApplicationContext(), GroupMainActivity.class);
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
			Utils.callToServer(Constants.SVR_MOIM_REGIST_URL, new StringEntity(groupInfo));
		} catch (UnsupportedEncodingException e) {
			Logger.e(e.getMessage());
		}
    }

    @Override
    public void onDateSet(DatePicker arg0, int year, int monthOfYear, int dayOfMonth) {
    	Toast.makeText(RegistGroupActivity.this, year+"/"+monthOfYear+"/"+dayOfMonth, Toast.LENGTH_SHORT).show();
    }
    
	@Override
	public void onTimeSet(TimePicker arg0, int hourOfDay, int minute) {
        Toast.makeText(RegistGroupActivity.this, hourOfDay+":"+minute, Toast.LENGTH_SHORT).show();
	}

}
