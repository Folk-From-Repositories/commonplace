package com.common.place.util;

public class Constants {
	

	public static final String APP_NAME = "Common Place";
	
	private static final String HOST = "http://rambling.synology.me:52015";
	public static final String SVR_REGIST_URL = HOST + "/commonplace/regist";
	public static final String SVR_MOIM_REGIST_URL = HOST + "/commonplace/moim/regist";
	public static final String TEST_URL = HOST + "/test/commonplace/gcm/send";
	
	public static final String RESTAURANT_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyDDlcMIjePsgpoGy9MmVpZJVV6veblp9xU&location=37.55500949462912,126.98537103831768&radius=500&types=restaurant";
	
	public static final int MAP_VIEW_REQ_CODE        = 100;
	public static final int MEMBER_ACTIVITY_REQ_CODE = 200;
	public static final int RESTAURANT_LIST_REQ_CODE = 300;
	public static final int GROUP_MAIN_VIEW_REQ_CODE = 400;
	
	public static final String REQUEST_TYPE_GPS_GETHERING = "gethering";
	public static final String REQUEST_TYPE_MAP_CREATE = "create";
}
