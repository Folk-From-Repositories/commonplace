package com.common.place.gcm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.common.place.DialogActivity;
import com.common.place.R;
import com.common.place.model.GroupMember;
import com.common.place.util.Constants;
import com.common.place.util.Logger;
import com.common.place.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class GcmBroadCastReceiver extends BroadcastReceiver{

	// key:member, value:[{"phone":"01020702175","latitude":"37.6236219","name":"01020702175","longitude":"127.0852128"}] 
    // key:moimId, value:25 
    // key:from, value:1073384423107 
    // key:category, value:GPS Push 
    // key:collapse_key, value:CommonPlace Notification 

    // key:registration_id, value:APA91bH7th-R7YF7QRzc9S8FJbQ0OY9iKJ8raDurQQNGJdPQq4aNT1nI0azc2LlaB8XTeOW2mgzAeh5BLefKt_D-uw5IHdjA6QwqMGDmKmq_c30gUMnfNvYRbpBRbcnz19if7FGxAyZi

	
	Handler hd = new Handler();
	
	
	 
	
	
	
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
        //Utils.makeToast(context, m);
        
        if(msgMap.get(Constants.MSG_KEY_CATEGORY) != null){
        	
        	String category = msgMap.get(Constants.MSG_KEY_CATEGORY).toString();
        	
        	Logger.i("category:"+category);
        	
        	if(category != null && category.equals(Constants.GCM_CATEGORY_GPS_LOCATION)){
        		
        		String memberVal = msgMap.get(Constants.MSG_KEY_MEMBER).toString();
        		ArrayList<GroupMember> members = new ArrayList<GroupMember>();
        		try {
        			JSONArray memberArr = new JSONArray(memberVal);
        			Logger.i("## "+memberArr.toString());
        			String toastMsg = "";
        			
        			for(int i = 0 ; i < memberArr.length() ; i++){
        				JSONObject member = (JSONObject) memberArr.get(i);
        				
        				Logger.i("## "+Double.parseDouble(member.getString("latitude")));
        				Logger.i("## "+Double.parseDouble(member.getString("longitude")));
        				toastMsg += "["+i+"]"+member.getString("name")+"("+Double.parseDouble(member.getString("latitude"))+
            					", "+Double.parseDouble(member.getString("longitude"))+")";
        				if(i < memberArr.length() - 1){
        					toastMsg += "\n";
        				}
        				
        				GroupMember gMember = new GroupMember("", member.getString("name"), member.getString("phone"), 
        						Double.parseDouble(member.getString("latitude")), Double.parseDouble(member.getString("longitude")));
        				members.add(gMember);
        			}
        			Utils.makeToast(context, toastMsg);
        		} catch (JSONException e) {
        			e.printStackTrace();
        		}
        		
        		saveMemberLocationToDb(context, members);
        		sendBroadcastForRefresh(context);
        		
        		return;
        	}
        	
        	
        	else if(category != null && category.equals(Constants.GCM_CATEGORY_CAMPAIGN_119)){
        		
        		
        		
        		
        		String title = "";
        		if(msgMap.get(Constants.MSG_KEY_TITLE) != null){
        			title = msgMap.get(Constants.MSG_KEY_TITLE).toString();
        		}
        		String body = "";
        		if(msgMap.get(Constants.MSG_KEY_MESSAGE) != null){
        			body = msgMap.get(Constants.MSG_KEY_MESSAGE).toString();
        		}
        		
        		Logger.i("title:"+title);
        		Logger.i("body:"+body);
        		
        		//Utils.createDialog(context, title, body);
        		
        		Intent i = new Intent(context, DialogActivity.class);
        		i.putExtra("title", title);
        		i.putExtra("body", body);
        		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        		context.startActivity(i);
        		
        		return;
        		
        	}
        	
        	
        	
        	else if(category != null && category.equals(Constants.GCM_CATEGORY_NEW_MOIM)){
        		
        		Utils.acquireCpuWakeLock(context);
        		
        		Utils.showNewMoimNotification(context,
        				context.getResources().getString(R.string.noti_newmoim_title),
        				context.getResources().getString(R.string.noti_newmoim_body));
        		
        		release();
        		
        		return;
        	}
        	
        }
        
        Utils.sendBroadcastForGridRefresh(context);
        
	}
	
	private void saveMemberLocationToDb(Context context, ArrayList<GroupMember> members){
		
		if(members != null && members.size() > 0){
			for(int i = 0 ; i < members.size() ; i++){
				GroupMember member = members.get(i);
				Utils.updateMemberLocation(context, member);
			}
		}
	}
	
	private void sendBroadcastForRefresh(Context context){
		Intent i = new Intent(Constants.INNER_BROADCAST_RECEIVER);
        context.sendBroadcast(i);
	}
	
	
	private void release(){
        hd.postDelayed(new Runnable() {
            @Override
            public void run() {
        		Utils.releaseCpuLock();
            }
        }, 3000);
	}
}
