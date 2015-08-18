package com.common.place;

import java.util.ArrayList;

import android.app.Activity;
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
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_main_view);
        
        Log.d("KMC", "INIT GROUP MAIN VIEW");
        
        warningText=(TextView)findViewById(R.id.group_warning);
           
        findViewById(R.id.nextBtn3).setOnClickListener(mClickListener);
		findViewById(R.id.nextBtn2).setOnClickListener(mClickListener);
        
		grid=(GridView)findViewById(R.id.grid);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	@Override
	    	public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
	    		Toast.makeText(GroupMainView.this, "You Clicked at " +groupNameList.get(position), Toast.LENGTH_SHORT).show();
            }
    	});
    }
    
    Button.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.nextBtn2:
				removeAllGroup();
				Log.d("KMC", "REMOVE ALL GROUP");
				break;
			case R.id.nextBtn3:
				Log.d("KMC", "REGISTER GROUP");
				initGroup();
				break;
			}
			
			if(groupIdList.size() > 0) warningText.setText(""); else warningText.setText("등록된 회식 그룹이 없습니다.");
			
			CustomGrid adapter = new CustomGrid(GroupMainView.this, groupNameList, groupIdList);
	        grid.setAdapter(adapter);
		}
	};
    
    /* Add Dummy Data */
    public void initGroup(){
    	groupNameList.add("회식모임_" + count++); groupIdList.add(R.drawable.soju);
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
