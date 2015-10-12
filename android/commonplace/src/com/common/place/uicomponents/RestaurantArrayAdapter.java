package com.common.place.uicomponents;
 
import java.util.ArrayList;

import com.common.place.R;
import com.common.place.model.RestaurantModel;

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
            TextView item_ratings;
            ImageView ratings_1, ratings_2, ratings_3, ratings_4, ratings_5;
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
                holder.item_ratings = (TextView) convertView.findViewById(R.id.item_ratings);
                holder.ratings_1 = (ImageView) convertView.findViewById(R.id.ratings_1);
                holder.ratings_2 = (ImageView) convertView.findViewById(R.id.ratings_2);
                holder.ratings_3 = (ImageView) convertView.findViewById(R.id.ratings_3);
                holder.ratings_4 = (ImageView) convertView.findViewById(R.id.ratings_4);
                holder.ratings_5 = (ImageView) convertView.findViewById(R.id.ratings_5);
                holder.check = (CheckBox) convertView.findViewById(R.id.cb_checkbox);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
        	
            RestaurantModel restaurant = (RestaurantModel) getItem(position);
            
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
        	
            
            String rating = restaurant.getRating();
            holder.item_ratings.setText(rating);
            
            if(rating != null && !"".equals(rating) && !"-".equals(rating)){
            	int orgRating = (int) (Double.parseDouble(rating) * 10);
            	int main = (int) (orgRating / 10);
            	int sub = (int) (orgRating % 10);
            	
            	int cursor = main + 1;
            	switch(main){
            	case 5:
            		holder.ratings_5.setImageResource(R.drawable.star_4);
            	case 4:
            		holder.ratings_4.setImageResource(R.drawable.star_4);
            	case 3:
            		holder.ratings_3.setImageResource(R.drawable.star_4);
            	case 2:
            		holder.ratings_2.setImageResource(R.drawable.star_4);
            	case 1:
            		holder.ratings_1.setImageResource(R.drawable.star_4);
            	default:
            		break;
            	}
            	
            	int starId = 0;
            	switch(sub){
            	case 9:
            	case 8:
            	case 7:
            		starId = R.drawable.star_3;
            		break;
            	case 6:
            	case 5:
            		starId = R.drawable.star_2;
            		break;
            	case 4:
            	case 3:
            	case 2:
            		starId = R.drawable.star_1;
            		break;
            	case 1:
            	default:
            		starId = R.drawable.star_0;
            		break;
            	}
            	
            	switch(cursor){
            	case 5:
            		holder.ratings_5.setImageResource(starId);
            		break;
            	case 4:
            		holder.ratings_5.setImageResource(R.drawable.star_0);
            		holder.ratings_4.setImageResource(starId);
            		break;
            	case 3:
            		holder.ratings_5.setImageResource(R.drawable.star_0);
            		holder.ratings_4.setImageResource(R.drawable.star_0);
            		holder.ratings_3.setImageResource(starId);
            		break;
            	case 2:
            		holder.ratings_5.setImageResource(R.drawable.star_0);
            		holder.ratings_4.setImageResource(R.drawable.star_0);
            		holder.ratings_3.setImageResource(R.drawable.star_0);
            		holder.ratings_2.setImageResource(starId);
            		break;
            	case 1:
            		holder.ratings_5.setImageResource(R.drawable.star_0);
            		holder.ratings_4.setImageResource(R.drawable.star_0);
            		holder.ratings_3.setImageResource(R.drawable.star_0);
            		holder.ratings_2.setImageResource(R.drawable.star_0);
            		holder.ratings_1.setImageResource(starId);
            		break;
            	}
            }else{
            	holder.ratings_5.setImageResource(R.drawable.star_0);
        		holder.ratings_4.setImageResource(R.drawable.star_0);
        		holder.ratings_3.setImageResource(R.drawable.star_0);
        		holder.ratings_2.setImageResource(R.drawable.star_0);
        		holder.ratings_1.setImageResource(R.drawable.star_0);
            }
            
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