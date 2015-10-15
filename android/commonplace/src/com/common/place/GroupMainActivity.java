package com.common.place;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import com.common.place.model.ContactsModel;
import com.common.place.model.GroupModel;
import com.common.place.service.GPSService;
import com.common.place.uicomponents.CustomGridAdapter;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.common.place.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupMainActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener{

	GridView grid;
	TextView warningText;
	
	Button btn_1, btn_3;
	
	int count = 1;
	
	ArrayList<String> groupNameList = new ArrayList<String>();
	ArrayList<Integer> groupIdList = new ArrayList<Integer>();
	
	public static ArrayList<GroupModel> groupList = new ArrayList<GroupModel>();
	
	public static GroupModel group;
	Intent serviceIntent;

	ProgressDialog dialog;
	
	private List<ActivityManager.RunningServiceInfo> runningServices;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressWarnings("deprecation")
		@Override
        public void onReceive(Context context, Intent intent) {
        	
        	Bundle bundle = intent.getExtras();
    		Iterator<String> iterator = bundle.keySet().iterator();
            Toast.makeText(getApplicationContext(), "received", Toast.LENGTH_SHORT).show();
    		Logger.d("KMC TEST AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAaaa");
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = bundle.get(key).toString();
                Logger.d("onMessage :: key = ^" + key 
                        + "^, value = ^" + URLDecoder.decode(value) + "^");
                
                if(key.compareTo("member")==0){
                	Logger.d("KMC TEST 01");
                	//new Gson().fromJson(, ContactsModel.class);
                	//ArrayList<ContactsModel> memeber = new Gson().fromJson(value, ArrayList<ContactsModel>);
                	
                	JsonElement jelement = new JsonParser().parse(value);
                    JsonArray  json = jelement.getAsJsonArray();
                   
                    
                    ArrayList<ContactsModel> aaa = new ArrayList<ContactsModel>();
                    
                    
                    for(int i=0;i<json.size();i++){
                    	
                    	ContactsModel a = new ContactsModel("",json.get(i).getAsJsonObject().get("phone").getAsString(), 
                    			json.get(i).getAsJsonObject().get("latitude").getAsString(), 
                    			json.get(i).getAsJsonObject().get("longitude").getAsString()
                    			);
                    	aaa.add(a);
                    }
                    //if(groupList.size() > 0){
                    	groupList.get(0).setMemeber(aaa);
                    //}
                	
                	//CreateMapView.group = 
//                	Intent i = new Intent(CreateMapView.context, CreateMapView.class);
//                	i.putExtra("requestType", Constants.REQUEST_TYPE_GPS_GETHERING);
//                	i.putExtra("memeber", value);
//                	Logger.d("KMC TEST 02");
//                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Logger.d("KMC TEST 03");
//                    context.startActivity(i);
//                    Logger.d("KMC TEST 04");
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_main_view);
        
        Logger.d("INIT GROUP MAIN VIEW");
        
        warningText=(TextView)findViewById(R.id.group_warning);
           
        btn_1 = (Button) findViewById(R.id.nextBtn1);
        btn_3 = (Button) findViewById(R.id.nextBtn3);
        
        btn_1.setOnClickListener(this);
        btn_3.setOnClickListener(this);
        
		grid = (GridView)findViewById(R.id.grid);
        grid.setOnItemClickListener(this);
        
        IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        registerReceiver(receiver, filter);
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		if(isRunning()){
        	btn_1.setText(GroupMainActivity.this.getResources().getString(R.string.btn_stop_service));
        }else{
        	btn_1.setText(GroupMainActivity.this.getResources().getString(R.string.btn_start_service));
        }
		
		dialog = ProgressDialog.show(GroupMainActivity.this, "", GroupMainActivity.this.getResources().getText(R.string.loading), true);
		retrieveGroupList();
	}

	private void retrieveGroupList(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    retrieveGroupListInBackground();
                } catch (Exception e) {
                	Logger.e(e.getMessage());
                }
				return null;
            }
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				dialog.dismiss();
			}
        }.execute(null, null, null);
	}
	
	private void retrieveGroupListInBackground() {
		
		List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("phone", Utils.getPhoneNumber(GroupMainActivity.this)));
		
    	String response = Utils.callToServer(Constants.SVR_RETRIEVE_GROUP, nameValuePairs);
    	Logger.d(response);
    }
	
	
	
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//	    Logger.d("GrouopMainView's onActivityResult: " + resultCode);
//	    switch(resultCode){
//	    	case Constants.GROUP_MAIN_VIEW_REQ_CODE:
//	    		Serializable groupInfo = data.getSerializableExtra("group");
//	    		group = (GroupModel)groupInfo;
//	    		groupList.add(group);
//	    		addGroup(group.getLocationName(),R.drawable.soju);
//	    		break;
//	    	case PLACE_PICKER_REQUEST:
//	    		if (resultCode == RESULT_OK) {
//	    	        Place place = PlacePicker.getPlace(data, this);
//	    	        String toastMsg = String.format("Place: %s", place.getName());
//	    	        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
//	    	    }
//	    		break;
//	    }
//	    setGridViewAndText();
//	}
	
	public void setGridViewAndText(){
    	if(groupIdList.size() > 0) warningText.setText(""); else warningText.setText(R.string.grouplist_not_regist);
		
		CustomGridAdapter adapter = new CustomGridAdapter(GroupMainActivity.this, groupNameList, groupIdList);
        grid.setAdapter(adapter);
    }
	
    public void addGroup(String groupName, int imageId){
    	groupNameList.add(groupName);
    	groupIdList.add(imageId);
    }
    
    public void removeGroup(int position){
    	groupNameList.remove(position);
		groupIdList.remove(position);
    }
    
    public void removeGroupByName(String groupName){
    	for (int index =0 ;index < groupNameList.size() ; index++) {
			if(groupNameList.get(index) == groupName){
				removeGroup(index);
			}
		}	
    }
    
    public void removeGroupById(int groupId){
    	for (int index =0 ;index < groupIdList.size() ; index++) {
			if(groupIdList.get(index) == groupId){
				removeGroup(index);
			}
		}	
    }
    
    public void removeAllGroup(){
    	groupNameList.removeAll(groupNameList);
    	groupIdList.removeAll(groupIdList);
    	groupList.removeAll(groupList);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.nextBtn1:
			serviceIntent = new Intent(getApplication(), GPSService.class);
	        if(isRunning()){
	        	stopService(serviceIntent);
	        	btn_1.setText(GroupMainActivity.this.getResources().getString(R.string.btn_start_service));
	        }else{
		        startService(serviceIntent);
		        btn_1.setText(GroupMainActivity.this.getResources().getString(R.string.btn_stop_service));
	        }
			break;
		case R.id.nextBtn3:
			Logger.d("REGISTER GROUP");
			startActivityForResult(new Intent(getApplicationContext(), RegistGroupActivity.class),0);
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(getApplicationContext(), MapActivity.class);
		intent.putExtra("requestType",Constants.REQUEST_TYPE_GPS_GETHERING);
		intent.putExtra("group", groupList.get(position));
		startActivityForResult(intent,Constants.MEMBER_ACTIVITY_REQ_CODE);
	}
    
    private boolean isRunning(){
    	boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager)getSystemService("activity");

        runningServices = activityManager.getRunningServices(100);
        for(int inj = 0 ; inj < runningServices.size() ; inj ++){
            if(runningServices.get(inj).service.getClassName().equals(GPSService.class.getName())){
            	isRunning = true;
            }
        }
        return isRunning;
    }
}
