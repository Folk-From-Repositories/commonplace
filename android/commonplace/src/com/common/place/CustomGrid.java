package com.common.place;

import java.util.ArrayList;

import com.common.place.util.Logger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class CustomGrid extends BaseAdapter{
      private Context mContext;
      
      private ArrayList<String> web = new ArrayList<String>();
      private ArrayList<Integer> Imageid = new ArrayList<Integer>();
//      private final String[] web;
//      private final int[] Imageid;
 
        public CustomGrid(Context c,ArrayList<String> web,ArrayList<Integer> Imageid ) {
        	Logger.d("CUSTOM GRID VIEW");
            mContext = c;
            this.Imageid = Imageid;
            this.web = web;
        }
 
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return web.size();
        }
 
        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }
 
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View grid;
            LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
            if (convertView == null) {
 
                grid = new View(mContext);
                grid = inflater.inflate(R.layout.grid_single, null);
                TextView textView = (TextView) grid.findViewById(R.id.grid_text);
                ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
                textView.setText(web.get(position));
                imageView.setImageResource(Imageid.get(position));
            } else {
                grid = (View) convertView;
            }
 
            return grid;
        }
}
