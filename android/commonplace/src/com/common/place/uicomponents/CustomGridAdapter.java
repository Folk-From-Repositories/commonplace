package com.common.place.uicomponents;

import java.util.ArrayList;

import com.common.place.R;
import com.common.place.model.Group;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class CustomGridAdapter extends BaseAdapter{
      
	private Context mContext;
      
	private ArrayList<Group> groupList;
	private final ImageDownloader imageDownloader = new ImageDownloader();
	
	public CustomGridAdapter(Context context, ArrayList<Group> groupList) {
		this.mContext = context;
		this.groupList = groupList;
	}
 
	@Override
	public int getCount() {
		return groupList.size();
    }
 
	@Override
	public Object getItem(int position) {
		return groupList.get(position);
	}
 
	@Override
	public long getItemId(int position) {
		return 0;
	}
 
	public void setArr(ArrayList<Group> grouplist){
		this.groupList = grouplist;
	}
        
	private class ViewHolder {
		ImageView locationImage;
        TextView title;
    }    
        
	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.grid_single, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.grid_text);
			holder.locationImage = (ImageView) convertView.findViewById(R.id.grid_image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
 
		Group group = groupList.get(position);
		holder.title.setText(group.getTitle());
		
		holder.locationImage.setImageResource(R.drawable.icon);
        if(group.getLocationImageUrl() != null && !"".equals(group.getLocationImageUrl())){
        	imageDownloader.download(group.getLocationImageUrl(), (ImageView) holder.locationImage, mContext, false);
        }
		
		return convertView;
	}
}
