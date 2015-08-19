package com.common.place;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

import com.common.place.util.Logger;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class GetPhoneNumberList extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_phone_number_list);
		getPhoneNumberContext = getApplicationContext();
		
		readFromDB();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.get_phone_number_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private Vector<String> arrPhoneList = new Vector<String>();
	private Vector<String> arrNameList = new Vector<String>();
	
	public static Context getPhoneNumberContext;
	
	public GetPhoneNumberList(){
		Logger.d("INIT getPhoneNumberContext");
		getPhoneNumberContext = this;
	}
	
	public Context getPhoneNumberListContext(){
		return getPhoneNumberContext;
	}
	
	public void readFromDB()
	{
		String [] arrProjection = { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME };
		String [] arrPhoneProjection = { ContactsContract.CommonDataKinds.Phone.NUMBER };
  
		Cursor clsCursor = null;
		try{
			clsCursor = getContentResolver().query( ContactsContract.Contacts.CONTENT_URI, arrProjection, ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1" , null, null );
//			ContentResolver a = getContentResolver();
		}catch(Exception e){
			StringWriter sw = new StringWriter();
		    e.printStackTrace(new PrintWriter(sw));
		    String exceptionAsStrting = sw.toString();
		    Log.e("KMC cc", exceptionAsStrting);
		    e.printStackTrace();
		}
//		clsCursor = getPhoneNumberContext.getContentResolver().query( ContactsContract.Contacts.CONTENT_URI, arrProjection, ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1" , null, null );

		while( clsCursor.moveToNext() )
		{
			String strContactId = clsCursor.getString( 0 );
   
			Cursor clsPhoneCursor = getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrPhoneProjection
					, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + strContactId, null, null );
			while( clsPhoneCursor.moveToNext() )
			{
					// �̸��� ��ȭ ��ȣ�� ������ ����Ʈ�� �����Ѵ�.
				arrNameList.add( clsCursor.getString( 1 ) );
				arrPhoneList.add( clsPhoneCursor.getString( 0 ) );
			}
			
			clsPhoneCursor.close();   
		}
		clsCursor.close( );
		
		finish();
	}
	
	public Vector<String> getPhoneNumbers(){
		return arrPhoneList;
	}
	
	public Vector<String> getNames(){
		return arrNameList;
	}
}
