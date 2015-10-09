package com.common.place.util;

public class Constants {
	

	public static final String APP_NAME = "Common Place";
	
	public static final String GOOGLE_API_KEY = "AIzaSyDDlcMIjePsgpoGy9MmVpZJVV6veblp9xU";
	//public static final String GOOGLE_API_KEY = "AIzaSyALxswOwsqnWslA65rqrQ2qJ8peKsC0aG4";
	
	public static final String HOST                = "http://rambling.synology.me:52015";
	public static final String SVR_REGIST_URL      = HOST + "/commonplace/regist";
	public static final String SVR_MOIM_REGIST_URL = HOST + "/commonplace/moim/regist";
	public static final String TEST_URL            = HOST + "/test/commonplace/gcm/send";
	public static final String SVR_USER_LOCATION   = HOST + "/commonplace/user/location";
	
	public static final String RESTAURANT_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
	
	public static final int MAP_VIEW_REQ_CODE        = 100;
	public static final int MEMBER_ACTIVITY_REQ_CODE = 200;
	public static final int RESTAURANT_LIST_REQ_CODE = 300;
	public static final int GROUP_MAIN_VIEW_REQ_CODE = 400;
	
	public static final String REQUEST_TYPE_GPS_GETHERING = "gethering";
	public static final String REQUEST_TYPE_MAP_CREATE = "create";
	
	public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    
	public static final String PROPERTY_REG_ID               = "registration_id";
    public static final String PROPERTY_APP_VERSION          = "appVersion";
    public static final String PROPERTY_USER_NAME            = "userName";
    
    
    public static final String SENDER_ID = "1073384423107";
    
    public static final String SHARED_PREFERENCE_FILE_NAME = "commonPlaceSpref";
    
    public static String PHONE_NUMBER = "";
    
}
