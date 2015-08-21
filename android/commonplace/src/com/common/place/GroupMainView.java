package com.common.place;

import java.io.Serializable;
import java.util.ArrayList;

import com.common.place.model.GroupModel;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupMainView extends Activity {

	GridView grid;
	TextView warningText;
	int count = 1;
	
	ArrayList<String> groupNameList = new ArrayList<String>();
	ArrayList<Integer> groupIdList = new ArrayList<Integer>();
	
	public static GroupModel group;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_main_view);
        
        Logger.d("INIT GROUP MAIN VIEW");
        
        warningText=(TextView)findViewById(R.id.group_warning);
           
        findViewById(R.id.nextBtn3).setOnClickListener(mClickListener);
		findViewById(R.id.nextBtn2).setOnClickListener(mClickListener);
        
		grid=(GridView)findViewById(R.id.grid);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
	    		//Toast.makeText(GroupMainView.this, "You Clicked at " +groupNameList.get(position), Toast.LENGTH_SHORT).show();
	    		
	    		Intent intent = new Intent(getApplicationContext(), CreateMapView.class);
				intent.putExtra("requestType",Constants.REQUEST_TYPE_GPS_GETHERING);
	    		
	    		startActivityForResult(intent,Constants.MEMBER_ACTIVITY_REQ_CODE);
            }
    	});
    }
    
    Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.nextBtn2:
				removeAllGroup();
				setGridViewAndText();
				Logger.d("REMOVE ALL GROUP");
				break;
			case R.id.nextBtn3:
				Logger.d("REGISTER GROUP");
				//initGroup();
				startActivityForResult(new Intent(getApplicationContext(), RegistGroup.class),0);
				break;
			}	
		}
	};
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    Log.d("KMC","GrouopMainView's onActivityResult: " + resultCode);
	    switch(resultCode){
	    	case Constants.GROUP_MAIN_VIEW_REQ_CODE:
	    		Serializable groupInfo = data.getSerializableExtra("group");
	    		group = (GroupModel)groupInfo;
	    		
	    		addGroup(group.getLocationName(),R.drawable.soju);
	    		break;
	    }
	    setGridViewAndText();
	}
	
	public void setGridViewAndText(){
    	if(groupIdList.size() > 0) warningText.setText(""); else warningText.setText("등록된 모임 그룹이 없습니다.");
		
		CustomGrid adapter = new CustomGrid(GroupMainView.this, groupNameList, groupIdList);
        grid.setAdapter(adapter);
    }
	
    /* Add Dummy Data */
    public void initGroup(){
    	groupNameList.add("모임그룹_" + count++); groupIdList.add(R.drawable.soju);
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
    }
}
