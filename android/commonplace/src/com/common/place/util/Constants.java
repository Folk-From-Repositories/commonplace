package com.common.place.util;

public class Constants {
	

	public static final String APP_NAME = "Common Place";
	
	public static final String GOOGLE_API_KEY = "AIzaSyDDlcMIjePsgpoGy9MmVpZJVV6veblp9xU";
	//public static final String GOOGLE_API_KEY = "AIzaSyALxswOwsqnWslA65rqrQ2qJ8peKsC0aG4";
	
	public static final String HOST                = "http://54.64.165.15:58000";
	public static final String SVR_REGIST_URL      = HOST + "/commonplace/regist";
	public static final String SVR_RETRIEVE_GROUP  = HOST + "/commonplace/moim/my";
	public static final String SVR_MOIM_REGIST_URL = HOST + "/commonplace/moim/regist";
	public static final String TEST_URL            = HOST + "/test/commonplace/gcm/send";
	public static final String SVR_USER_LOCATION   = HOST + "/commonplace/user/location";
	public static final String DELETE_GROUP        = HOST + "/commonplace/moim/delete";
	
	public static final String RESTAURANT_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
	
	public static final int MAP_VIEW_REQ_CODE        = 100;
	public static final int MEMBER_ACTIVITY_REQ_CODE = 200;
	public static final int RESTAURANT_LIST_REQ_CODE = 300;
	public static final int GROUP_MAIN_VIEW_REQ_CODE = 400;
	
	public static final int REQUEST_TYPE_GPS_GETHERING = 7777;
	public static final int REQUEST_TYPE_MAP_CREATE    = 8888;
	
	public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    
	public static final String PROPERTY_REG_ID               = "registration_id";
    public static final String PROPERTY_APP_VERSION          = "appVersion";
    public static final String PROPERTY_USER_NAME            = "userName";
    public static final String PROPERTY_TOAST_ON             = "toast_on";
    
    
    public static final String SENDER_ID = "1073384423107";
    
    public static final String SHARED_PREFERENCE_FILE_NAME = "commonPlaceSpref";
    
    public static String INNER_BROADCAST_RECEIVER = "com.common.place.innerbroadcast";
    public static String MAIN_BROADCAST_RECEIVER  = "com.common.place.mainbroadcast";
    
    
    //-- push --
    public static String MSG_KEY_CATEGORY            = "category";
    public static String GCM_CATEGORY_GPS_LOCATION   = "GPS Push";
    public static String GCM_CATEGORY_CAMPAIGN_119   = "Campaign 119";
    public static String GCM_CATEGORY_NEW_MOIM       = "invitation";
    public static String MSG_KEY_TITLE               = "title";
    public static String MSG_KEY_MESSAGE             = "message";
    
    public static String MSG_KEY_MEMBER          = "member";
    
    public static int NOTIFICATION_ID_GPS      = 9999;
    public static int NOTIFICATION_ID_119      = 9991;
    public static int NOTIFICATION_ID_NEW_MOIM = 9992;
    
    public static final int MENU_ID_1      = 100;
    
}
