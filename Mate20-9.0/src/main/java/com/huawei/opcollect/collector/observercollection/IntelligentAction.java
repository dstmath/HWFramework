package com.huawei.opcollect.collector.observercollection;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import com.huawei.nb.model.collectencrypt.RawHotelInfo;
import com.huawei.nb.model.collectencrypt.RawTrainFlightTickInfo;
import com.huawei.nb.query.Query;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectConstant;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.regex.Pattern;

public class IntelligentAction extends Action {
    private static final String HOTEL_ADDRESS = "hotel_address";
    private static final String HOTEL_CHECK_IN_TIME = "hotel_check_in_time";
    private static final String HOTEL_NAME = "hotel_name";
    private static final int MESSAGE_ON_CHANGE = 1;
    private static final String TAG = "IntelligentAction";
    private static final long TIME_THRESHOLD = 100;
    private static final String TRIP_ARRIVAL_PLACE = "trip_end_place";
    private static final String TRIP_ARRIVAL_TIME = "trip_end_time";
    private static final String TRIP_NO = "trip_event_number";
    private static final String TRIP_SEAT = "trip_seat";
    private static final String TRIP_START_PLACE = "trip_begin_place";
    private static final String TRIP_START_TIME = "trip_begin_time";
    private static final String TYPE = "type";
    private static final String TYPE_FLIGHT = "flight";
    public static final String TYPE_HOTEL = "hotel";
    private static final String TYPE_TRAIN = "train";
    public static final String TYPE_TRIP = "trip";
    private static IntelligentAction sHotelInstance = null;
    private static IntelligentAction sTripInstance = null;
    private final Uri intelligentUri = Uri.parse("content://com.huawei.provider.intelligent/intelligent");
    private String mActionType = "";
    /* access modifiers changed from: private */
    public Handler mHandler = null;
    /* access modifiers changed from: private */
    public String mLastChangeUri = null;
    /* access modifiers changed from: private */
    public long mLastchangeTimeStamp = System.currentTimeMillis();
    private ContentObserver mObserver = null;

    private static class IntelligentHandler extends Handler {
        private final WeakReference<IntelligentAction> service;

        IntelligentHandler(IntelligentAction service2) {
            this.service = new WeakReference<>(service2);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                IntelligentAction action = (IntelligentAction) this.service.get();
                if (action != null) {
                    String uriString = (String) msg.obj;
                    if (uriString != null) {
                        if (System.currentTimeMillis() - action.mLastchangeTimeStamp >= IntelligentAction.TIME_THRESHOLD || !uriString.equals(action.mLastChangeUri)) {
                            long unused = action.mLastchangeTimeStamp = System.currentTimeMillis();
                            String unused2 = action.mLastChangeUri = uriString;
                            action.perform();
                        }
                    }
                }
            }
        }
    }

    private final class MyContentObserver extends ContentObserver {
        MyContentObserver(Handler handler) {
            super(handler);
        }

        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri == null || IntelligentAction.this.mHandler == null) {
                OPCollectLog.e(IntelligentAction.TAG, "uri or handler is null.");
            } else if (!IntelligentAction.isLegalUri(uri.toString())) {
                OPCollectLog.e(IntelligentAction.TAG, "illegal uri.");
            } else {
                IntelligentAction.this.mHandler.obtainMessage(1, uri.toString()).sendToTarget();
            }
        }
    }

    public static synchronized IntelligentAction getInstance(Context context, String type) {
        IntelligentAction intelligentAction;
        synchronized (IntelligentAction.class) {
            if (TYPE_HOTEL.equalsIgnoreCase(type)) {
                if (sHotelInstance == null) {
                    sHotelInstance = new IntelligentAction(context, OPCollectConstant.HOTEL_ACTION_NAME, type);
                }
                intelligentAction = sHotelInstance;
            } else if (TYPE_TRIP.equalsIgnoreCase(type)) {
                if (sTripInstance == null) {
                    sTripInstance = new IntelligentAction(context, OPCollectConstant.TRIP_ACTION_NAME, type);
                }
                intelligentAction = sTripInstance;
            } else {
                intelligentAction = null;
            }
        }
        return intelligentAction;
    }

    private IntelligentAction(Context context, String name, String type) {
        super(context, name);
        this.mActionType = type;
        if (TYPE_HOTEL.equalsIgnoreCase(this.mActionType)) {
            setDailyRecordNum(queryDailyRecordNum(RawHotelInfo.class));
        } else {
            setDailyRecordNum(queryDailyRecordNum(RawTrainFlightTickInfo.class));
        }
    }

    /* access modifiers changed from: private */
    public static boolean isLegalUri(String str) {
        return Pattern.compile("content://com.huawei.provider.intelligent/intelligent/[0-9]+").matcher(str).matches();
    }

    public void enable() {
        super.enable();
        if (this.mObserver == null && this.mContext != null) {
            if (this.mHandler == null) {
                this.mHandler = new IntelligentHandler(this);
            }
            this.mObserver = new MyContentObserver(this.mHandler);
            try {
                this.mContext.getContentResolver().registerContentObserver(this.intelligentUri, true, this.mObserver);
            } catch (RuntimeException e) {
                OPCollectLog.e(TAG, "registerContentObserver failed: " + e.getMessage());
            }
        }
    }

    public boolean destroy() {
        super.destroy();
        synchronized (IntelligentAction.class) {
            if (TYPE_HOTEL.equalsIgnoreCase(this.mActionType)) {
                destroyHotelInstance();
            } else {
                destroyTripInstance();
            }
        }
        return true;
    }

    private static synchronized void destroyHotelInstance() {
        synchronized (IntelligentAction.class) {
            sHotelInstance = null;
        }
    }

    private static synchronized void destroyTripInstance() {
        synchronized (IntelligentAction.class) {
            sTripInstance = null;
        }
    }

    public void disable() {
        super.disable();
        if (!(this.mObserver == null || this.mContext == null)) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mObserver = null;
        }
        if (this.mHandler != null) {
            this.mHandler = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        try {
            return collectIntelligentData(Uri.parse(this.mLastChangeUri));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean collectIntelligentData(Uri uri) {
        if (uri == null || this.mContext == null) {
            return false;
        }
        OPCollectLog.r(TAG, "collectIntelligentData");
        Cursor cursor_trip = null;
        Cursor cursor_hotel = null;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(uri, new String[]{TYPE}, null, null, null);
            if (isNullOrEmptyCursor(cursor)) {
                if (cursor != null) {
                    cursor.close();
                }
                if (cursor_hotel != null) {
                    cursor_hotel.close();
                }
                if (cursor_trip == null) {
                    return false;
                }
                cursor_trip.close();
                return false;
            }
            if (cursor.moveToFirst()) {
                String type = cursor.getString(cursor.getColumnIndex(TYPE));
                String str = uri.toString();
                int intelligent_id = Integer.parseInt(str.substring(str.lastIndexOf(47) + 1));
                OPCollectLog.r(TAG, "type: " + type + " id: " + intelligent_id);
                if (TYPE_HOTEL.equalsIgnoreCase(this.mActionType) && TYPE_HOTEL.equalsIgnoreCase(type)) {
                    if (OdmfCollectScheduler.getInstance().getOdmfHelper().querySingleManageObject(Query.select(RawHotelInfo.class).equalTo("mReservedInt", Integer.valueOf(intelligent_id))) != null) {
                        OPCollectLog.i(TAG, intelligent_id + " already in.");
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (cursor_hotel != null) {
                            cursor_hotel.close();
                        }
                        if (cursor_trip == null) {
                            return false;
                        }
                        cursor_trip.close();
                        return false;
                    }
                    cursor_hotel = this.mContext.getContentResolver().query(uri, new String[]{HOTEL_NAME, HOTEL_ADDRESS, HOTEL_CHECK_IN_TIME}, null, null, null);
                    if (isNullOrEmptyCursor(cursor_hotel)) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (cursor_hotel != null) {
                            cursor_hotel.close();
                        }
                        if (cursor_trip == null) {
                            return false;
                        }
                        cursor_trip.close();
                        return false;
                    } else if (cursor_hotel.moveToFirst()) {
                        String hotel_name = cursor_hotel.getString(cursor_hotel.getColumnIndex(HOTEL_NAME));
                        String hotel_address = cursor_hotel.getString(cursor_hotel.getColumnIndex(HOTEL_ADDRESS));
                        Date check_in_time = new Date(cursor_hotel.getLong(cursor_hotel.getColumnIndex(HOTEL_CHECK_IN_TIME)));
                        RawHotelInfo rawHotelInfo = new RawHotelInfo();
                        rawHotelInfo.setMHotelName(hotel_name);
                        rawHotelInfo.setMHotelAddr(hotel_address);
                        rawHotelInfo.setMCheckinTime(check_in_time);
                        rawHotelInfo.setMTimeStamp(new Date(System.currentTimeMillis()));
                        rawHotelInfo.setMReservedText(OPCollectUtils.formatCurrentTime());
                        rawHotelInfo.setMReservedInt(Integer.valueOf(intelligent_id));
                        OdmfCollectScheduler.getInstance().getDataHandler().obtainMessage(4, rawHotelInfo).sendToTarget();
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (cursor_hotel != null) {
                            cursor_hotel.close();
                        }
                        if (cursor_trip == null) {
                            return true;
                        }
                        cursor_trip.close();
                        return true;
                    }
                } else if (TYPE_TRIP.equalsIgnoreCase(this.mActionType) && (TYPE_FLIGHT.equalsIgnoreCase(type) || TYPE_TRAIN.equalsIgnoreCase(type))) {
                    if (OdmfCollectScheduler.getInstance().getOdmfHelper().querySingleManageObject(Query.select(RawTrainFlightTickInfo.class).equalTo("mReservedInt", Integer.valueOf(intelligent_id))) != null) {
                        OPCollectLog.i(TAG, intelligent_id + " already in.");
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (cursor_hotel != null) {
                            cursor_hotel.close();
                        }
                        if (cursor_trip == null) {
                            return false;
                        }
                        cursor_trip.close();
                        return false;
                    }
                    cursor_trip = this.mContext.getContentResolver().query(uri, new String[]{TRIP_NO, TRIP_SEAT, TRIP_START_TIME, TRIP_ARRIVAL_TIME, TRIP_START_PLACE, TRIP_ARRIVAL_PLACE}, null, null, null);
                    if (isNullOrEmptyCursor(cursor_trip)) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (cursor_hotel != null) {
                            cursor_hotel.close();
                        }
                        if (cursor_trip == null) {
                            return false;
                        }
                        cursor_trip.close();
                        return false;
                    } else if (cursor_trip.moveToFirst()) {
                        String trip_event_number = cursor_trip.getString(cursor_trip.getColumnIndex(TRIP_NO));
                        String trip_seat = cursor_trip.getString(cursor_trip.getColumnIndex(TRIP_SEAT));
                        long trip_begin_time = cursor_trip.getLong(cursor_trip.getColumnIndex(TRIP_START_TIME));
                        long trip_end_time = cursor_trip.getLong(cursor_trip.getColumnIndex(TRIP_ARRIVAL_TIME));
                        String trip_begin_place = cursor_trip.getString(cursor_trip.getColumnIndex(TRIP_START_PLACE));
                        String trip_end_place = cursor_trip.getString(cursor_trip.getColumnIndex(TRIP_ARRIVAL_PLACE));
                        Date begin_time = new Date(trip_begin_time);
                        Date end_time = new Date(trip_end_time);
                        RawTrainFlightTickInfo rawTrainFlightTickInfo = new RawTrainFlightTickInfo();
                        rawTrainFlightTickInfo.setMTrainFlightNo(trip_event_number);
                        rawTrainFlightTickInfo.setMSeatNo(trip_seat);
                        rawTrainFlightTickInfo.setMTrainFlightStartTime(begin_time);
                        rawTrainFlightTickInfo.setMTrainFlightArrivalTime(end_time);
                        rawTrainFlightTickInfo.setMTrainFlightStartPlace(trip_begin_place);
                        rawTrainFlightTickInfo.setMTrainFlightArrivalPlace(trip_end_place);
                        rawTrainFlightTickInfo.setMTimeStamp(new Date(System.currentTimeMillis()));
                        rawTrainFlightTickInfo.setMReservedText(OPCollectUtils.formatCurrentTime());
                        rawTrainFlightTickInfo.setMReservedInt(Integer.valueOf(intelligent_id));
                        OdmfCollectScheduler.getInstance().getDataHandler().obtainMessage(4, rawTrainFlightTickInfo).sendToTarget();
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (cursor_hotel != null) {
                            cursor_hotel.close();
                        }
                        if (cursor_trip == null) {
                            return true;
                        }
                        cursor_trip.close();
                        return true;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            if (cursor_hotel != null) {
                cursor_hotel.close();
            }
            if (cursor_trip != null) {
                cursor_trip.close();
            }
            return false;
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "collect data failed: " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            if (cursor_hotel != null) {
                cursor_hotel.close();
            }
            if (cursor_trip != null) {
                cursor_trip.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            if (cursor_hotel != null) {
                cursor_hotel.close();
            }
            if (cursor_trip != null) {
                cursor_trip.close();
            }
            throw th;
        }
    }

    private static boolean isNullOrEmptyCursor(Cursor cursor) {
        if (cursor == null) {
            OPCollectLog.e(TAG, "cursor is null");
            return true;
        } else if (cursor.getCount() > 0) {
            return false;
        } else {
            OPCollectLog.e(TAG, "cursor size <= 0");
            cursor.close();
            return true;
        }
    }

    public void dump(int indentNum, PrintWriter pw) {
        super.dump(indentNum, pw);
        if (pw != null) {
            String indent = String.format("%" + indentNum + "s\\-", new Object[]{" "});
            if (this.mObserver == null) {
                pw.println(indent + "observer is null");
            } else {
                pw.println(indent + "observer not null");
            }
        }
    }
}
