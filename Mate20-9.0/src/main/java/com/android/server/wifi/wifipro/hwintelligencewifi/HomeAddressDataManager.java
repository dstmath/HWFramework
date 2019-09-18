package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.huawei.intelligent.main.common.mapservice.IDistanceCallback;

public class HomeAddressDataManager {
    private static final int ADDRESS_TYPE_HOME = 1;
    private static final int CALLBACK_ADDRESS_INVALID = -1;
    private static final int CALLBACK_ADDRESS_VALID = 256;
    public static final float DISTANCE_FAIL = -1.0f;
    public static final String DISTANCE_KEY = "dst";
    private static final String GET_ADDRESS_RESULT = "get_address_result";
    private static final String GET_ADDRESS_TYPE = "get_address_type";
    private static final String GET_DISTANCE_CALLBACK = "getDistanceStr";
    private static final String GET_DISTANCE_CALLBACK_KEY = "distance_callback";
    private static final String HOME_LOCATION_PREF_KEY = "home";
    public static final double HOME_RANGE = 200.0d;
    private static Uri INTELLIGENT_URI = Uri.parse("content://com.huawei.provider.intelligent/intelligent");
    public static final String LATITUDE_KEY = "lat";
    public static final String LONGITUDE_KEY = "lng";
    private static final String METHOD_GET_ADDRESS = "getAddressStr";
    public static final String RESULT_FLAG_KEY = "result_flag";
    private String isInvalid;
    private String isOversea;
    private double latitude;
    private double longitude;
    private Context mContext = null;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private IDistanceCallback.Stub mstub = new IDistanceCallback.Stub() {
        public void onResult(String currentLocationInfo, int flag) {
            Log.d(MessageUtil.TAG, "IDistanceCallback onResult flag " + flag);
            Bundle data = new Bundle();
            Message msg = new Message();
            if (-1 == flag || currentLocationInfo == null || currentLocationInfo.length() <= 0) {
                msg.what = 31;
                HomeAddressDataManager.this.mHandler.sendMessageDelayed(msg, 200);
                return;
            }
            int latIndex = currentLocationInfo.indexOf("\"lat\":");
            int lngIndex = currentLocationInfo.indexOf("\"lng\":");
            int dstIndex = currentLocationInfo.indexOf("\"distance\":");
            if (latIndex == -1 || lngIndex == -1 || dstIndex == -1) {
                Log.d(MessageUtil.TAG, "Invalid index return");
                return;
            }
            Log.d(MessageUtil.TAG, "IDistanceCallback distance =  " + currentLocationInfo.substring(dstIndex + 11, currentLocationInfo.length() - 1));
            data.putDouble(HomeAddressDataManager.LATITUDE_KEY, Double.parseDouble(currentLocationInfo.substring(latIndex + 6, lngIndex + -1)));
            data.putDouble(HomeAddressDataManager.LONGITUDE_KEY, Double.parseDouble(currentLocationInfo.substring(lngIndex + 6, dstIndex + -1)));
            data.putDouble(HomeAddressDataManager.DISTANCE_KEY, Double.parseDouble(currentLocationInfo.substring(dstIndex + 11, currentLocationInfo.length() + -1)));
            msg.what = 30;
            msg.setData(data);
            HomeAddressDataManager.this.mHandler.sendMessageDelayed(msg, 200);
        }
    };

    public HomeAddressDataManager(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    public void setHomeDistanceCallback(LocationAddress homeAddress) {
        if (homeAddress == null) {
            Log.d(MessageUtil.TAG, "setHomeDistanceCallback homeAddress is null");
            return;
        }
        Log.d(MessageUtil.TAG, "setHomeDistanceCallback enter");
        Bundle bundle = new Bundle();
        bundle.putDouble(LATITUDE_KEY, homeAddress.getLatitude());
        bundle.putDouble(LONGITUDE_KEY, homeAddress.getLongitude());
        bundle.putBinder(GET_DISTANCE_CALLBACK_KEY, this.mstub);
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (contentResolver == null) {
                Log.d(MessageUtil.TAG, "setHomeDistanceCallback contentResolver is null");
            } else {
                contentResolver.call(INTELLIGENT_URI, GET_DISTANCE_CALLBACK, null, bundle);
            }
        } catch (IllegalArgumentException e) {
            Log.d(MessageUtil.TAG, "setHomeDistanceCallback contentResolver IllegalArgumentException");
        } catch (IllegalStateException e2) {
            Log.d(MessageUtil.TAG, "setHomeDistanceCallback contentResolver IllegalStateException");
        } catch (Exception e3) {
            Log.d(MessageUtil.TAG, "setHomeDistanceCallback contentResolver Exception");
        }
    }

    public LocationAddress getLastHomeAddress() {
        if (this.mContext == null) {
            Log.d(MessageUtil.TAG, "getLastHomeAddress mContext is null or fileName is null");
            return null;
        }
        Log.d(MessageUtil.TAG, "getLastHomeAddress enter");
        Bundle bundle = new Bundle();
        bundle.putInt(GET_ADDRESS_TYPE, 1);
        Bundle result = null;
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (contentResolver == null) {
                Log.d(MessageUtil.TAG, "getLastHomeAddress contentResolver is null");
                return null;
            }
            result = contentResolver.call(INTELLIGENT_URI, METHOD_GET_ADDRESS, null, bundle);
            if (result == null) {
                Log.d(MessageUtil.TAG, "contentResolver.call result is null");
                return null;
            }
            String homeLocation = result.getString(GET_ADDRESS_RESULT, "Location Null");
            if (homeLocation == null || homeLocation.equals("Location Null")) {
            } else if (homeLocation.length() <= 0) {
                String str = homeLocation;
            } else {
                int latIndex = homeLocation.indexOf("\"lat\":");
                int lngIndex = homeLocation.indexOf("\"lng\":");
                int overseaIndex = homeLocation.indexOf("\"isOversea\":");
                int isInvalidIndex = homeLocation.indexOf("\"isInvalid\":");
                if (latIndex == -1 || lngIndex == -1 || overseaIndex == -1) {
                } else if (isInvalidIndex == -1) {
                    String str2 = homeLocation;
                } else {
                    this.latitude = Double.parseDouble(homeLocation.substring(latIndex + 6, lngIndex - 1));
                    this.longitude = Double.parseDouble(homeLocation.substring(lngIndex + 6, overseaIndex - 1));
                    this.isOversea = homeLocation.substring(overseaIndex + 12, isInvalidIndex - 1);
                    this.isInvalid = homeLocation.substring(isInvalidIndex + 12, homeLocation.length() - 1);
                    Log.d(MessageUtil.TAG, "getLastHomeAddress isOversea = " + this.isOversea + ", isInvalid =  " + this.isInvalid);
                    String str3 = homeLocation;
                    LocationAddress homeAddress = new LocationAddress(this.latitude, this.longitude, 0.0d, this.isOversea.equals("true"), this.isInvalid.equals("true"), Long.valueOf(System.currentTimeMillis()));
                    return homeAddress;
                }
                Log.d(MessageUtil.TAG, "String index error");
                return null;
            }
            Log.d(MessageUtil.TAG, "getLastHomeAddress homeLocation is null");
            return null;
        } catch (IllegalArgumentException e) {
            Log.d(MessageUtil.TAG, "contentResolver IllegalArgumentException");
        } catch (IllegalStateException e2) {
            Log.d(MessageUtil.TAG, "contentResolver IllegalStateException");
        } catch (Exception e3) {
            Log.d(MessageUtil.TAG, "contentResolver Exception");
        }
    }
}
