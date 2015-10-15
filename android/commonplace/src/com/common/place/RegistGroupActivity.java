package com.common.place;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.common.place.model.ContactsModel;
import com.common.place.model.GroupModel;
import com.common.place.model.Member;
import com.common.place.model.RestaurantModel;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.common.place.util.Utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class RegistGroupActivity extends Activity implements OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

	public ArrayList<GroupModel> groupList = new ArrayList<GroupModel>();
	public ArrayList<ContactsModel> getArrayList;
	
	private GroupModel group;
	
	RelativeLayout restaurant_area;
	TextView retaurant_no_select, contact_list, item_title, item_vicinity;
	Button btn_date, btn_time;
	
	int id_count = 1;
	
	public static Bitmap selectedRestaurantImage;
	public static RestaurantModel selectedRestaurant;
	
	ImageView item_icon;
	
	int year, month, day, hour, minute;
	String dateString, timeString;
	
	EditText groupName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist_group);

		btn_date = (Button) findViewById(R.id.btn_date);
		btn_time = (Button) findViewById(R.id.btn_time);
		btn_date.setOnClickListener(this);
		btn_time.setOnClickListener(this);
		
		contact_list = (TextView)findViewById(R.id.contacts_description);
		
		restaurant_area = (RelativeLayout)findViewById(R.id.restaurant_area);
		item_icon = (ImageView) findViewById(R.id.item_icon);
		item_title = (TextView) findViewById(R.id.item_title);
		item_vicinity = (TextView) findViewById(R.id.item_vicinity);
		retaurant_no_select = (TextView)findViewById(R.id.retaurant_no_select);
		groupName = (EditText)findViewById(R.id.name_edit);

		findViewById(R.id.seachAddr).setOnClickListener(this);
		findViewById(R.id.searchMap).setOnClickListener(this);
		findViewById(R.id.btn_contacts).setOnClickListener(this);
		findViewById(R.id.btn_regist_group).setOnClickListener(this);
		
		selectedRestaurantImage = null;
		selectedRestaurant = null;
		
		Utils.deleteAllMemberListInDB(RegistGroupActivity.this);
	}

	@Override
	protected void onDestroy() {
		Utils.deleteAllMemberListInDB(RegistGroupActivity.this);
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
			retaurant_no_select.setVisibility(View.GONE);
			restaurant_area.setVisibility(View.VISIBLE);

			item_title.setText(selectedRestaurant.getName());
			item_vicinity.setText(selectedRestaurant.getVicinity());
			
			if(selectedRestaurantImage != null){
				item_icon.setImageBitmap(selectedRestaurantImage);
			}
		}else{
			restaurant_area.setVisibility(View.GONE);
			retaurant_no_select.setVisibility(View.VISIBLE);
		}
	}
	
	public void createMemberList(){
		ArrayList<Member> arr = Utils.getSelectedMemberList(RegistGroupActivity.this);
		if(arr != null && arr.size() > 0){
			contact_list.setText("");
			contact_list.setGravity(Gravity.LEFT);
			for(int i = 0 ; i < arr.size() ; i++){
				contact_list.append(arr.get(i).getName() + "  " + arr.get(i).getPhoneNumber() + "\n");
			}
		}else{
			contact_list.setGravity(Gravity.CENTER);
			contact_list.setText("...");
		}
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
			
			String title = groupName.getText().toString();
			String dateTime = dateString+" "+timeString;
			String locationName = selectedRestaurant.getName();
			String locationImageUrl = selectedRestaurant.getPhotoReference();
			String locationLat = selectedRestaurant.getLocationLat();
			String locationLon = selectedRestaurant.getLocationLon();
			String locationPhone = "";
			String locationDesc = selectedRestaurant.getVicinity();
			String owner = Utils.getPhoneNumber(RegistGroupActivity.this);
			String[] member = Utils.getPhoneNumArr(RegistGroupActivity.this); //Arrays.toString(member)
			
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(10);
			nameValuePairs.add(new BasicNameValuePair("title", title));
			nameValuePairs.add(new BasicNameValuePair("dateTime", dateTime));
			nameValuePairs.add(new BasicNameValuePair("locationName", locationName));
			nameValuePairs.add(new BasicNameValuePair("locationImageUrl", locationImageUrl));
			nameValuePairs.add(new BasicNameValuePair("locationLat", locationLat));
			nameValuePairs.add(new BasicNameValuePair("locationLon", locationLon));
			nameValuePairs.add(new BasicNameValuePair("locationPhone", locationPhone));
			nameValuePairs.add(new BasicNameValuePair("locationDesc", locationDesc));
			nameValuePairs.add(new BasicNameValuePair("owner", owner));
			nameValuePairs.add(new BasicNameValuePair("member", Arrays.toString(member)));
			
    		try {
				registerInBackground(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				Logger.e(e.getMessage());
			}
		}	
	}
	private void registerInBackground(HttpEntity entity) {
        new AsyncTask<HttpEntity, Void, String>() {
            @Override
            protected String doInBackground(HttpEntity... params) {
            	String response = "";
                try {
                    response = sendReatuanrantDataToBackend(params[0]);
                } catch (Exception e) {
                	Logger.e(e.getMessage());
                }
				return response;
            }

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				Logger.d(result);
			}
            
        }.execute(entity, null, null);
    }
	
	//Transfer Data to Server(httpRequest)
    private String sendReatuanrantDataToBackend(HttpEntity entity) {
    	return Utils.callToServer(Constants.SVR_MOIM_REGIST_URL, entity);
    }

    @Override
    public void onDateSet(DatePicker arg0, int year, int monthOfYear, int dayOfMonth) {
    	dateString = year+leadingZero(monthOfYear+1)+leadingZero(dayOfMonth);
    	btn_date.setText(dateString);
    }
    
	@Override
	public void onTimeSet(TimePicker arg0, int hourOfDay, int minute) {
		timeString = leadingZero(hourOfDay)+":"+leadingZero(minute);
		btn_time.setText(timeString);
	}
	
	public String leadingZero(int raw){
		if(raw < 10){
			return "0"+raw;
		}
		return ""+raw;
	}

}
