package com.common.place.uicomponents;
 
import java.util.ArrayList;

import com.common.place.R;
import com.common.place.model.RestaurantModel;
import com.common.place.util.Logger;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
 
public class RestaurantArrayAdapter extends ArrayAdapter<RestaurantModel> {
 
        Context context;
        ArrayList<RestaurantModel> modelsArrayList;
        public int selected_position = -1;
 
        private final ImageDownloader imageDownloader = new ImageDownloader();
        
        public RestaurantArrayAdapter(Context context, ArrayList<RestaurantModel> modelsArrayList) {
 
            super(context, R.layout.restaurant_list_item, modelsArrayList);
 
            this.context = context;
            this.modelsArrayList = modelsArrayList;
        }
 
        private class ViewHolder {
    		ImageView image;
    		ImageView image_small;
            TextView restaurantName;
            TextView vicinity;
            CheckBox check;
        }
        
        @SuppressLint("InflateParams")
		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
 
        	ViewHolder holder = null;
        	 
            LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.restaurant_list_item, null);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.item_icon);
                holder.image_small = (ImageView) convertView.findViewById(R.id.item_icon_small);
                holder.restaurantName = (TextView) convertView.findViewById(R.id.item_title);
                holder.vicinity = (TextView) convertView.findViewById(R.id.item_vicinity);
                holder.check = (CheckBox) convertView.findViewById(R.id.cb_checkbox);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
        	
            RestaurantModel restaurant = (RestaurantModel) getItem(position);
            
            Logger.d("GetView - restaurant.getPhotoReference():["+restaurant.getPhotoReference()+"]");
            
            holder.image.setImageResource(R.drawable.icon);
            if(restaurant.getPhotoReference() != null && !"".equals(restaurant.getPhotoReference())){
            	imageDownloader.download(restaurant.getPhotoReference(), (ImageView) holder.image, context);
            }
            holder.image_small.setImageResource(R.drawable.small);
            if(restaurant.getPhotoReference() != null && !"".equals(restaurant.getPhotoReference())){
            	imageDownloader.download(restaurant.getImageUrl(), (ImageView) holder.image_small, context);
            }
            
            holder.restaurantName.setText(restaurant.getName());
            holder.vicinity.setText(restaurant.getVicinity());
        	
            holder.check.setChecked(position == selected_position);
            holder.check.setTag(position);
            holder.check.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
            	   selected_position = (Integer) view.getTag();
                   notifyDataSetInvalidated();
                   notifyDataSetChanged();
               }
            });
 
            return convertView;
        }
}