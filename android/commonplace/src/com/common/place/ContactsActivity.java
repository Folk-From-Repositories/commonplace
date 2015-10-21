package com.common.place;

import java.util.ArrayList;
import java.util.HashMap;

import com.common.place.db.Provider;
import com.common.place.util.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ContactsActivity extends Activity implements OnClickListener, OnItemClickListener{

    Intent mIntent;
    
    private TextView howmany;
    private ListView recipientList;
    private EditText search_text;
    private ListView contactList;
    
    private Button confirm, selectAll;
    
    ArrayList<HashMap<String, String>> contactArrayList;
    SimpleAdapter adapter;
    ListAdapter recipientAdapter;
    Cursor recipientCursor;
    
    private ProgressDialog loagindDialog;
    
    int count = 0;
    

    private final int CONTACT_RESULT = 1905;
    
    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipient);
        
        mIntent = getIntent();
        
        howmany = (TextView) findViewById(R.id.howmany);
        recipientList = (ListView) findViewById(R.id.recipient_list);
        search_text = (EditText) findViewById(R.id.search_text);
        contactList = (ListView) findViewById(R.id.contact_list);
        confirm = (Button) findViewById(R.id.btn_confirm);
        selectAll = (Button) findViewById(R.id.btn_select_all);
        
        recipientCursor = getContentResolver().query(Provider.RECIPIENT_CONTENT_URI, null, null, null, null);
        recipientAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, 
                recipientCursor, new String[]{"recipient", "phone_number"}, new int[]{android.R.id.text1, android.R.id.text2});        
        recipientList.setAdapter(recipientAdapter);
        recipientList.setOnItemClickListener(this);
        
        howmany.setText(""+recipientCursor.getCount()+" "+getText(R.string.recipient_selected_string));
        if(recipientCursor.getCount()>0){
            confirm.setVisibility(View.VISIBLE);
        }else{
            confirm.setVisibility(View.GONE);
        }
        
        //recipientCursor.close();
        
        search_text.addTextChangedListener(textWatcherInput);

        contactArrayList = getContacts("");
        adapter = new SimpleAdapter(ContactsActivity.this, contactArrayList,
                android.R.layout.simple_list_item_2, new String[] {
                        "name", "phone_number"
                }, new int[] {
                        android.R.id.text1, android.R.id.text2
                });

        contactList.setAdapter(adapter);
        contactList.setOnItemClickListener(this);
        
        confirm.setOnClickListener(this);
        selectAll.setOnClickListener(this);
        
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()){
        case R.id.btn_confirm:
            ContactsActivity.this.finish();
            break;
            
        case R.id.btn_select_all:
            addFromSearched(search_text.getText().toString());
            break;
        }
        
    }
    
    private void addFromSearched(String searchString){
        loagindDialog = ProgressDialog.show(this, getText(R.string.insert_to),
                getText(R.string.loading), true, false);
        final String searchText = searchString;
        
        
        
        Thread thread = new Thread(new Runnable() {
            public void run() {
                
                ContentResolver resolver = getContentResolver();
                
                Cursor cursor = resolver.query(Phone.CONTENT_URI, null,
                        Phone.DISPLAY_NAME + " LIKE \"%" + searchText + "%\"", null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                count = 0;
                
                if(cursor.moveToFirst() && cursor.getCount() > 0){
                    do{
                        
                        Cursor beforeCursor = resolver.query(Provider.RECIPIENT_CONTENT_URI, 
                                null, 
                                Provider.RECIPIENT+" =\'"+cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME))+"\'", 
                                null, 
                                null);
                        
                        if(beforeCursor.getCount() <= 0){
                            ContentValues values = new ContentValues();
                            values.put(Provider.RECIPIENT, cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME)));
                            values.put(Provider.PHONE_NUMBER, cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                            
                            getContentResolver().insert(Provider.RECIPIENT_CONTENT_URI, values);
                            count++;
                        }
                        beforeCursor.close();
                        
                        
                        
                    }while(cursor.moveToNext());
                }
                
                cursor.close();

                int total = resolver.query(Provider.RECIPIENT_CONTENT_URI, null, null, null, null).getCount();
                
                addhandler.sendMessage(addhandler.obtainMessage(count, total));
            }
        });
        thread.start();
    }
    
    @SuppressLint("HandlerLeak")
	private Handler addhandler = new Handler() {
        public void handleMessage(Message msg) {
            loagindDialog.dismiss(); 
            
            howmany.setText(""+msg.obj+" "+getText(R.string.recipient_selected_string));
            
            refreshFooter();
            
            Utils.makeToast(ContactsActivity.this, ""+msg.what+" "+getText(R.string.multiinsert));
        }
    };
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        
        Cursor cursor = getContentResolver().query(Provider.RECIPIENT_CONTENT_URI, null, null, null, null);
        howmany.setText(cursor.getCount()+" "+getText(R.string.recipient_selected_string));
        cursor.close();
        
        if(cursor.getCount()>0){
            confirm.setVisibility(View.VISIBLE);
        }else{
            confirm.setVisibility(View.GONE);
        }
        
        
    }
    
    private ArrayList<HashMap<String, String>> getContacts(String search) {

        ArrayList<HashMap<String, String>> returnArray = new ArrayList<HashMap<String, String>>();

        Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, null,
                Phone.DISPLAY_NAME + " LIKE \"%" + search + "%\"", null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

        if (cursor.moveToFirst()) {
            do {

                String name = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                if (returnArray.size() == 0) {
                    HashMap<String, String> oneRowMap = new HashMap<String, String>();
                    oneRowMap.put("name", name);
                    oneRowMap.put("phone_number", cursor.getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

                    returnArray.add(oneRowMap);
                } else {

                    if (!returnArray.get(returnArray.size() - 1).get("name").equals(name)) {
                        HashMap<String, String> oneRowMap = new HashMap<String, String>();
                        oneRowMap.put("name", name);
                        oneRowMap.put("phone_number", cursor.getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

                        returnArray.add(oneRowMap);
                    }
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        
        return returnArray;

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
        switch(parent.getId()){
            case R.id.contact_list:
                String name = contactArrayList.get(position).get("name");
                String phoneNumber = contactArrayList.get(position).get("phone_number");

                insertOrDeleteRow(name, phoneNumber);
                break;
                
            case R.id.recipient_list:
                getContentResolver().delete(Provider.RECIPIENT_CONTENT_URI, "_id =\'"+id+"\'", null);
                
                recipientCursor = getContentResolver().query(Provider.RECIPIENT_CONTENT_URI, null, null, null, null);
                synchronized (recipientAdapter) {
                    recipientAdapter.notify();    
                }
                howmany.setText(""+recipientCursor.getCount()+" "+getText(R.string.recipient_selected_string));
                
                if(recipientCursor.getCount()>0){
                    confirm.setVisibility(View.VISIBLE);
                }else{
                    confirm.setVisibility(View.GONE);
                }
                
                recipientCursor.close();
                break;
        }
        
    }
    
    public void insertOrDeleteRow(String recipient, String phoneNumber){
        ContentValues values = new ContentValues();
        values.put(Provider.RECIPIENT, recipient);
        values.put(Provider.PHONE_NUMBER, phoneNumber);
        
        Cursor cursor = getContentResolver().query(Provider.RECIPIENT_CONTENT_URI, null, Provider.PHONE_NUMBER+" = \'"+phoneNumber+"\'", null, null);
        if(cursor.getCount() > 0){
            getContentResolver().delete(Provider.RECIPIENT_CONTENT_URI, Provider.PHONE_NUMBER+" = \'"+phoneNumber+"\'", null);
        }else{
            getContentResolver().insert(Provider.RECIPIENT_CONTENT_URI, values);    
        }
        
        cursor.close();
        
        refreshFooter();
        
        setResult(CONTACT_RESULT, mIntent);
    }
    
    public void refreshFooter(){
        recipientCursor = getContentResolver().query(Provider.RECIPIENT_CONTENT_URI, null, null, null, null);
        synchronized (recipientAdapter) {
            recipientAdapter.notify();    
        }
        howmany.setText(""+recipientCursor.getCount()+" "+getText(R.string.recipient_selected_string));

        if(recipientCursor.getCount()>0){
            confirm.setVisibility(View.VISIBLE);
        }else{
            confirm.setVisibility(View.GONE);
        }
        
        recipientCursor.close();
    }
    

    TextWatcher textWatcherInput = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String edit = s.toString();
            contactArrayList = getContacts(edit);

            adapter = new SimpleAdapter(ContactsActivity.this, contactArrayList,
                    android.R.layout.simple_list_item_2, new String[] {
                            "name", "phone_number"
                    }, new int[] {
                            android.R.id.text1, android.R.id.text2
                    });
                    
            contactList.setAdapter(adapter);

        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void afterTextChanged(Editable s) {
        }
    };

}
