package com.common.place;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.common.place.db.Provider;
import com.common.place.model.Group;
import com.common.place.model.NetworkResponse;
import com.common.place.service.GPSService;
import com.common.place.uicomponents.CustomGridAdapter;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.common.place.util.Utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GroupGridActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemLongClickListener{

	GridView grid;
	TextView warningText;
	LinearLayout gridLayout;
	Button btn_1, btn_3;
	
	int count = 1;

	ProgressDialog dialog;
	
	CustomGridAdapter adapter;
	
	ArrayList<Group> groupList;
	
	Intent serviceIntent;
	private List<ActivityManager.RunningServiceInfo> runningServices;
	
	private AlertDialog mDialog = null;
	
	MainBroadcastReceiver mainReceiver;
	IntentFilter filter;
	
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
        grid.setOnItemLongClickListener(this);
        
        gridLayout = (LinearLayout) findViewById(R.id.gridLayout);
        
        adapter = new CustomGridAdapter(GroupGridActivity.this, new ArrayList<Group>());
        grid.setAdapter(adapter);
        
        
        mainReceiver = new MainBroadcastReceiver();
		filter = new IntentFilter(Constants.INNER_BROADCAST_RECEIVER);
		
		refreshGrid();
    }
    
	@Override
	protected void onResume() {
		super.onResume();
//		refreshGrid();
		
		if(isRunning()){
        	btn_1.setText(GroupGridActivity.this.getResources().getString(R.string.btn_stop_service));
        }else{
        	btn_1.setText(GroupGridActivity.this.getResources().getString(R.string.btn_start_service));
        }
		
		registerReceiver(mainReceiver, filter);
	}
	
	public class MainBroadcastReceiver extends BroadcastReceiver{
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	Logger.i("InnerReceiver onReceive intent:"+intent);
	    	refreshGrid();
	    }
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mainReceiver);
	}
	
	private void refreshGrid(){
		setGridVisible(false);
		
		dialog = ProgressDialog.show(GroupGridActivity.this, "", GroupGridActivity.this.getResources().getText(R.string.loading), true);
		
		Utils.deleteGroupList(GroupGridActivity.this);
		
		retrieveGroupList();
	}

	private void retrieveGroupList(){
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
            	int statusCode = -1;
                try {
                    statusCode = retrieveGroupListInBackground();
                } catch (Exception e) {
                	e.printStackTrace();
                }
                
                groupList = getMemberListFromDB();
                
				return statusCode;
            }
			@Override
			protected void onPostExecute(Integer statusCode) {
				super.onPostExecute(statusCode);
				
				adapter.setArr(groupList);
				adapter.notifyDataSetChanged();
				
				if(groupList != null && groupList.size() > 0){
					setGridVisible(true);
				}else{
					setGridVisible(false);
				}
				
				if(statusCode != 200){
					Toast.makeText(GroupGridActivity.this, "Server error occurred", Toast.LENGTH_SHORT).show();
				}
				
				dialog.dismiss();
			}
        }.execute(null, null, null);
	}
	
	private int retrieveGroupListInBackground() {
		
		List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("phone", Utils.getPhoneNumber(GroupGridActivity.this)));
		
    	HttpResponse response = null;
		try {
			response = Utils.callToServer(Constants.SVR_RETRIEVE_GROUP, new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		int statusCode = response.getStatusLine().getStatusCode();
		
		if(statusCode == 200){
			try {
//				Utils.deleteGroupList(GroupGridActivity.this);
				Utils.makeGroupListToDb(GroupGridActivity.this, EntityUtils.toString(response.getEntity(), HTTP.UTF_8));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusCode; 
		
    	//String test = "[{\"id\":1,\"title\":\"테스트 모임\",\"dateTime\":\"20151023 19:00\",\"locationName\":\"광화문 광장\",\"locationImageUrl\":\"https://geo1.ggpht.com/cbk?photoid=evqsbHDXmIMAAAQINlDYrA&output=photo&cb_client=search.TACTILE.gps&minw=408&minh=256\",\"locationLat\":\"37.574255\",\"locationLon\":\"126.976754\",\"locationPhone\":\"02-120\",\"locationDesc\":\"세종대왕 동상 앞에서 봅니다.\",\"broadcast\":0,\"owner\":\"01012340000\",\"member\":[\"01012340000\",\"01012340001\",\"01012340002\"]},{\"id\":2,\"title\":\"테스트 모임\",\"dateTime\":\"20151023 19:00\",\"locationName\":\"광화문 광장\",\"locationImageUrl\":\"https://geo1.ggpht.com/cbk?photoid=evqsbHDXmIMAAAQINlDYrA&output=photo&cb_client=search.TACTILE.gps&minw=408&minh=256\",\"locationLat\":\"37.574255\",\"locationLon\":\"126.976754\",\"locationPhone\":\"02-120\",\"locationDesc\":\"세종대왕 동상 앞에서 봅니다.\",\"broadcast\":1,\"owner\":\"01012340000\",\"member\":[\"01012340000\",\"01012340001\",\"01012340002\"]},{\"id\":3,\"title\":\"테스트 모임\",\"dateTime\":\"20151023 19:00\",\"locationName\":\"광화문 광장\",\"locationImageUrl\":\"https://geo1.ggpht.com/cbk?photoid=evqsbHDXmIMAAAQINlDYrA&output=photo&cb_client=search.TACTILE.gps&minw=408&minh=256\",\"locationLat\":\"37.574255\",\"locationLon\":\"126.976754\",\"locationPhone\":\"02-120\",\"locationDesc\":\"세종대왕 동상 앞에서 봅니다.\",\"broadcast\":1,\"owner\":\"01012340000\",\"member\":[\"01012340000\",\"01012340001\",\"01012340002\"]},{\"id\":4,\"title\":\"테스트 모임\",\"dateTime\":\"20151023 19:00\",\"locationName\":\"광화문 광장\",\"locationImageUrl\":\"https://geo1.ggpht.com/cbk?photoid=evqsbHDXmIMAAAQINlDYrA&output=photo&cb_client=search.TACTILE.gps&minw=408&minh=256\",\"locationLat\":\"37.574255\",\"locationLon\":\"126.976754\",\"locationPhone\":\"02-120\",\"locationDesc\":\"세종대왕 동상 앞에서 봅니다.\",\"broadcast\":0,\"owner\":\"01012340000\",\"member\":[\"01012340000\",\"01012340001\",\"01012340002\"]}]";
    }
	
	private ArrayList<Group> getMemberListFromDB(){
		ArrayList<Group> groupList = new ArrayList<Group>();
		
		Cursor cursor = getContentResolver().query(Provider.GROUP_CONTENT_URI, null, null, null, null);
		
		if(cursor != null && cursor.getCount() > 0){
			if(cursor.moveToFirst()){
				do{
					Group group = new Group(cursor.getString(cursor.getColumnIndex(Provider.GROUP_ID)), 
							cursor.getString(cursor.getColumnIndex(Provider.TITLE)), 
							cursor.getString(cursor.getColumnIndex(Provider.OWNER)), 
							cursor.getString(cursor.getColumnIndex(Provider.TIME)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_NAME)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_IMAGE_URL)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_LAT)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_LON)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_PHONE)), 
							cursor.getString(cursor.getColumnIndex(Provider.LOCATION_DESC)), 
							new ArrayList<String>());
					Logger.d("getMemberListFromDB() group title:"+group.getTitle()+"["+group.getId()+"]");
					groupList.add(group);
				}while(cursor.moveToNext());
			}
		}
		
		if(cursor != null){
			cursor.close();
		}
		
		return groupList;
		
	}
	
	private void setGridVisible(boolean visible){
		if(visible){
			gridLayout.setVisibility(View.VISIBLE);
			warningText.setVisibility(View.GONE);
		}else{
			gridLayout.setVisibility(View.GONE);
			warningText.setVisibility(View.VISIBLE);
		}
		
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
	        	btn_1.setText(GroupGridActivity.this.getResources().getString(R.string.btn_start_service));
	        }else{
		        startService(serviceIntent);
		        btn_1.setText(GroupGridActivity.this.getResources().getString(R.string.btn_stop_service));
	        }
			break;
		case R.id.nextBtn3:
			Logger.d("REGISTER GROUP");
			startActivityForResult(new Intent(getApplicationContext(), GroupRegistActivity.class),0);
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		Group selectedGroup = groupList.get(position);
		
		Intent intent = new Intent(GroupGridActivity.this, MapActivity.class);
		intent.putExtra("requestType", Constants.REQUEST_TYPE_GPS_GETHERING);
		intent.putExtra("groupId", selectedGroup.getId());
		
		startActivity(intent);
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

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		Logger.d("onItemLongClick index:"+index);
		
		mDialog = createDialog(index);
        mDialog.show();
		return false;
	}
	
	private AlertDialog createDialog(final int index) {
		Logger.d("createDialog("+index+")");
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("Watning!");
        ab.setMessage("Delete "+groupList.get(index).getTitle() + "?\n(groupId:"+groupList.get(index).getId()+")");
        ab.setCancelable(false);
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher));
          
        ab.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            	setDismiss(mDialog);
            	dialog = ProgressDialog.show(GroupGridActivity.this, "", GroupGridActivity.this.getResources().getText(R.string.loading), true);
            	deleteGroupList(Integer.parseInt(groupList.get(index).getId()));
            }
        });
          
        ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            	setDismiss(mDialog);
            }
        });
          
        return ab.create();
    }
	
	private void deleteGroupList(final int groupId){
        new AsyncTask<Integer, Void, NetworkResponse>() {
            @Override
            protected NetworkResponse doInBackground(Integer... params) {
                try {
                	List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
                	nameValuePairs.add(new BasicNameValuePair("id", ""+params[0]));
                	nameValuePairs.add(new BasicNameValuePair("phone", "\""+Utils.getPhoneNumber(GroupGridActivity.this)+"\""));
            		
                	Logger.i("id:"+params[0]+" phone:"+Utils.getPhoneNumber(GroupGridActivity.this));
                	
                	HttpResponse response = null;
            		try {
            			response = Utils.callToServer(Constants.DELETE_GROUP, new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            			return new NetworkResponse(response.getStatusLine().getStatusCode(),
            					EntityUtils.toString(response.getEntity(), HTTP.UTF_8));
            		} catch (UnsupportedEncodingException e) {
            			e.printStackTrace();
            		}
            		//Logger.d(response.toString());
                } catch (Exception e) {
                	e.printStackTrace();
                }
				return null;
            }
			@Override
			protected void onPostExecute(NetworkResponse result) {
				super.onPostExecute(result);
				dialog.dismiss();
				if(result != null && result.getResponseCode() == 200){
					Toast.makeText(GroupGridActivity.this, result.getReponseString(), Toast.LENGTH_SHORT).show();
					deleteDB(groupId);
					refreshGrid();
					
				}else{
					Logger.e(result.toString());
					Toast.makeText(GroupGridActivity.this, "failed", Toast.LENGTH_SHORT).show();
				}
			}
        }.execute(groupId, null, null);
	}
	
	private void deleteDB(int groupId){
		getContentResolver().delete(Provider.GROUP_CONTENT_URI, Provider.GROUP_ID+"=\'"+groupId+"\'", null);
		getContentResolver().delete(Provider.MEMBER_CONTENT_URI, Provider.GROUP_ID+"=\'"+groupId+"\'", null);
	}
	
	private void setDismiss(Dialog dialog){
        if(dialog != null && dialog.isShowing())
            dialog.dismiss();
    }
	
	
	
	
	
}
