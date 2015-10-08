package com.common.place.uicomponents;
 
import java.util.ArrayList;

import com.common.place.R;
import com.common.place.model.RestaurantModel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
 
public class RestaurantArrayAdapter extends ArrayAdapter<RestaurantModel> {
 
        private final Context context;
        private final ArrayList<RestaurantModel> modelsArrayList;
        public int selected_position = -1;
 
        public RestaurantArrayAdapter(Context context, ArrayList<RestaurantModel> modelsArrayList) {
 
            super(context, R.layout.target_item, modelsArrayList);
 
            this.context = context;
            this.modelsArrayList = modelsArrayList;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
 
            // 1. Create inflater 
            LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
            // 2. Get rowView from inflater
            
            View rowView = null;
            if(!modelsArrayList.get(position).isGroupHeader()){
                rowView = inflater.inflate(R.layout.target_item, parent, false);
 
                // 3. Get icon,title & counter views from the rowView
                ImageView imgView = (ImageView) rowView.findViewById(R.id.item_icon); 
                TextView titleView = (TextView) rowView.findViewById(R.id.item_title);
                CheckBox checkbox = (CheckBox) rowView.findViewById(R.id.cb_checkbox);
                // 4. Set the text for textView 
                imgView.setImageResource(modelsArrayList.get(position).getIcon());
                titleView.setText(modelsArrayList.get(position).getName());
                
                checkbox.setChecked(position == selected_position);
                checkbox.setTag(position);
                checkbox.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {

                	   selected_position = (Integer) view.getTag();

                       notifyDataSetInvalidated();
                       notifyDataSetChanged();
                   }
               });     
            }
            else{
                    rowView = inflater.inflate(R.layout.group_header_item, parent, false);
                    TextView titleView = (TextView) rowView.findViewById(R.id.header);
                    titleView.setText(modelsArrayList.get(position).getName());
 
            }
 
            // 5. retrn rowView
            return rowView;
        }
}