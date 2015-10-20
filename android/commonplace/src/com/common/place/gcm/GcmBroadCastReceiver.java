package com.common.place.gcm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.common.place.db.Provider;
import com.common.place.model.GroupMember;
import com.common.place.util.Constants;
import com.common.place.util.Logger;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class GcmBroadCastReceiver extends BroadcastReceiver{

	// key:member, value:[{"phone":"01020702175","latitude":"37.6236219","name":"01020702175","longitude":"127.0852128"}] 
    // key:moimId, value:25 
    // key:from, value:1073384423107 
    // key:category, value:GPS Push 
    // key:collapse_key, value:CommonPlace Notification 

    // key:registration_id, value:APA91bH7th-R7YF7QRzc9S8FJbQ0OY9iKJ8raDurQQNGJdPQq4aNT1nI0azc2LlaB8XTeOW2mgzAeh5BLefKt_D-uw5IHdjA6QwqMGDmKmq_c30gUMnfNvYRbpBRbcnz19if7FGxAyZi
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Iterator<String> iterator = bundle.keySet().iterator();
		
		Logger.i("---------------------------");
		
		HashMap<String, Object> msgMap = new HashMap<String, Object>();
		
		String m = "";
		
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = bundle.get(key).toString();
            msgMap.put(key, value);
            m += key + ":" + value + "\n";
        }
        
        Logger.i(m);
        Toast.makeText(context, m, Toast.LENGTH_SHORT).show();
        
        if(msgMap.get(Constants.MSG_KEY_CATEGORY) != null){
        	
        	String category = msgMap.get(Constants.MSG_KEY_CATEGORY).toString();
        	
        	if(category != null && category.equals(Constants.MSG_VALUE_CATEGORY)){
        		
        		String memberVal = msgMap.get(Constants.MSG_KEY_MEMBER).toString();
        		ArrayList<GroupMember> members = new ArrayList<GroupMember>();
        		try {
        			JSONArray memberArr = new JSONArray(memberVal);
        			
        			for(int i = 0 ; i < memberArr.length() ; i++){
        				JSONObject member = (JSONObject) memberArr.get(i);
        				
        				GroupMember gMember = new GroupMember("", member.getString("name"), member.getString("phone"), member.getString("latitude"), member.getString("longitude"));
        				members.add(gMember);
        			}
        		} catch (JSONException e) {
        			e.printStackTrace();
        		}
        		
        		saveMemberLocationToDb(context, members);
        		
        		sendBroadcastForRefresh(context);
        	}
        	
        }
        
	}
	
	private void saveMemberLocationToDb(Context context, ArrayList<GroupMember> members){
		
		if(members != null && members.size() > 0){
			for(int i = 0 ; i < members.size() ; i++){
				
				GroupMember member = members.get(i);
				
				ContentValues values = new ContentValues();
				values.put(Provider.LOCATION_LAT, member.getLocationLat());
				values.put(Provider.LOCATION_LON, member.getLocationLon());
				
				context.getContentResolver().update(Provider.MEMBER_CONTENT_URI, values, Provider.PHONE_NUMBER + " = " + member.getPhone(), null);
			}
		}
	}
	
	private void sendBroadcastForRefresh(Context context){
		Intent i = new Intent(Constants.INNER_BROADCAST_RECEIVER);
        context.sendBroadcast(i);
	}
}
