package com.huawei.opcollect.collector.pullcollection;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.huawei.nb.model.collectencrypt.RawMediaAppStastic;
import com.huawei.opcollect.odmf.OdmfCollectScheduler;
import com.huawei.opcollect.odmf.OdmfHelper;
import com.huawei.opcollect.strategy.Action;
import com.huawei.opcollect.utils.OPCollectConstant;
import com.huawei.opcollect.utils.OPCollectLog;
import com.huawei.opcollect.utils.OPCollectUtils;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

public class MediaAppAction extends Action {
    private static final Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final int COLUMN_FIRST = 0;
    private static final String EIGHTY_SONGS = "eighty_songs";
    private static final Uri FRONT_PHOTO_NUM_URI = Uri.parse("content://com.huawei.gallery.provider/open_api/user_profile/get_front_photo_num");
    private static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final Object LOCK = new Object();
    private static final String NEW_CENTURY_SONGS = "new_century_songs";
    private static final String NINETY_SONGS = "ninety_songs";
    private static final Uri PHOTO_TAG_INFO_URI = Uri.parse("content://com.huawei.gallery.provider/open_api/user_profile/get_photo_tag_info");
    private static final String SEVENTY_SONGS = "seventy_songs";
    private static final String TAG = "MediaAppAction";
    private static final Uri TOP_CAMERA_MODE_URI = Uri.parse("content://com.huawei.gallery.provider/open_api/user_profile/get_top_camera_mode");
    private static final Uri TOURISM_PHOTO_NUM_URI = Uri.parse("content://com.huawei.gallery.provider/open_api/user_profile/get_tourism_photo_num");
    private static final Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private static MediaAppAction instance = null;

    private MediaAppAction(Context context, String name) {
        super(context, name);
        setDailyRecordNum(queryDailyRecordNum(RawMediaAppStastic.class));
    }

    public static MediaAppAction getInstance(Context context) {
        MediaAppAction mediaAppAction;
        synchronized (LOCK) {
            if (instance == null) {
                instance = new MediaAppAction(context, OPCollectConstant.MEDIA_ACTION_NAME);
            }
            mediaAppAction = instance;
        }
        return mediaAppAction;
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

    public boolean destroy() {
        super.destroy();
        destroyInstance();
        return true;
    }

    private static void destroyInstance() {
        synchronized (LOCK) {
            instance = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean execute() {
        return collectRawMediaAppStatics();
    }

    private boolean collectRawMediaAppStatics() {
        new Thread(new Runnable() {
            public void run() {
                OdmfCollectScheduler.getInstance().getDataHandler().obtainMessage(4, MediaAppAction.this.getRawMediaAppStatics()).sendToTarget();
            }
        }).start();
        return true;
    }

    /* access modifiers changed from: private */
    public RawMediaAppStastic getRawMediaAppStatics() {
        RawMediaAppStastic rawMediaAppStastic = new RawMediaAppStastic();
        rawMediaAppStastic.setMTourismPhotoNum(Integer.valueOf(getTourismPhotoNum()));
        rawMediaAppStastic.setMFrontPhotoNum(Integer.valueOf(getFrontPhotoNum()));
        rawMediaAppStastic.setMPhotoTagInfo(getPhotoTagInfo());
        rawMediaAppStastic.setMTopCameraMode(getTopCameraMode());
        if (OPCollectUtils.checkODMFApiVersion(this.mContext, OdmfHelper.ODMF_API_VERSION_2_11_2)) {
            rawMediaAppStastic.setMMusicNum(Integer.valueOf(getAudioNum()));
            rawMediaAppStastic.setMVideoNum(Integer.valueOf(getVideoNum()));
            rawMediaAppStastic.setMPhotoNum(Integer.valueOf(getImageNum()));
        }
        rawMediaAppStastic.setMTimeStamp(new Date());
        rawMediaAppStastic.setMMusicYear(analyseAudioYearList());
        rawMediaAppStastic.setMReservedText(OPCollectUtils.formatCurrentTime());
        return rawMediaAppStastic;
    }

    private int getTourismPhotoNum() {
        int num = 0;
        Cursor cursor = queryDataFromGallery2(TOURISM_PHOTO_NUM_URI);
        if (isNullOrEmptyCursor(cursor)) {
            return num;
        }
        cursor.moveToFirst();
        try {
            num = Integer.parseInt(cursor.getString(0));
        } catch (NumberFormatException e) {
            OPCollectLog.e(TAG, e.getMessage());
        } finally {
            cursor.close();
        }
        OPCollectLog.i(TAG, "getTourismPhotoNum: " + num);
        return num;
    }

    private int getFrontPhotoNum() {
        int num = 0;
        Cursor cursor = queryDataFromGallery2(FRONT_PHOTO_NUM_URI);
        if (isNullOrEmptyCursor(cursor)) {
            return 0;
        }
        cursor.moveToFirst();
        try {
            num = Integer.parseInt(cursor.getString(0));
            OPCollectLog.i(TAG, "getFrontPhotoNum: " + num);
        } catch (NumberFormatException e) {
            OPCollectLog.e(TAG, e.getMessage());
        } finally {
            cursor.close();
        }
        return num;
    }

    private String getPhotoTagInfo() {
        Cursor cursor = queryDataFromGallery2(PHOTO_TAG_INFO_URI);
        if (isNullOrEmptyCursor(cursor)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        cursor.moveToFirst();
        do {
            sb.append(cursor.getString(0));
            sb.append(";");
        } while (cursor.moveToNext());
        cursor.close();
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private String getTopCameraMode() {
        Cursor cursor = queryDataFromGallery2(TOP_CAMERA_MODE_URI);
        if (isNullOrEmptyCursor(cursor)) {
            return "";
        }
        cursor.moveToFirst();
        String str = cursor.getString(0);
        OPCollectLog.i(TAG, "getTopCameraMode: " + str);
        cursor.close();
        return str;
    }

    private int getAudioNum() {
        int count = 0;
        if (this.mContext == null) {
            return 0;
        }
        Cursor audioCursor = null;
        try {
            audioCursor = this.mContext.getContentResolver().query(AUDIO_URI, new String[]{"_id"}, null, null, null);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "query audio uri failed: " + e.getMessage());
        }
        if (audioCursor != null) {
            count = audioCursor.getCount();
            audioCursor.close();
        }
        return count;
    }

    private int getVideoNum() {
        int count = 0;
        if (this.mContext == null) {
            return 0;
        }
        Cursor videoCursor = null;
        try {
            videoCursor = this.mContext.getContentResolver().query(VIDEO_URI, new String[]{"_id"}, null, null, null);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "query video uri failed: " + e.getMessage());
        }
        if (videoCursor != null) {
            count = videoCursor.getCount();
            videoCursor.close();
        }
        return count;
    }

    private int getImageNum() {
        int count = 0;
        if (this.mContext == null) {
            return 0;
        }
        Cursor imageCursor = null;
        try {
            imageCursor = this.mContext.getContentResolver().query(IMAGE_URI, new String[]{"_id"}, "_data LIKE '%DCIM/Camera%'", null, null);
        } catch (RuntimeException e) {
            OPCollectLog.e(TAG, "query image uri failed: " + e.getMessage());
        }
        if (imageCursor != null) {
            count = imageCursor.getCount();
            imageCursor.close();
        }
        return count;
    }

    private Cursor queryDataFromGallery2(Uri uri) {
        if (uri == null || this.mContext == null) {
            return null;
        }
        boolean z = false;
        try {
            return this.mContext.getContentResolver().query(uri, null, null, null, null);
        } catch (Exception e) {
            OPCollectLog.e(TAG, "queryDataFromGallery2: " + e.getMessage());
            return z;
        }
    }

    private String analyseAudioYearList() {
        if (this.mContext == null) {
            OPCollectLog.e(TAG, "context is null");
            return "";
        }
        JSONObject object = new JSONObject();
        String[] audioProjection = {"year"};
        int countSeventy = 0;
        int countEighty = 0;
        Cursor audioCursorSeventy = null;
        Cursor audioCursorEighty = null;
        Cursor audioCursorNinety = null;
        Cursor audioCursorNewCentury = null;
        try {
            Cursor audioCursorSeventy2 = this.mContext.getContentResolver().query(AUDIO_URI, audioProjection, "year <= 1979", null, null);
            if (audioCursorSeventy2 != null) {
                countSeventy = audioCursorSeventy2.getCount();
                object.put(SEVENTY_SONGS, countSeventy);
            }
            Cursor audioCursorEighty2 = this.mContext.getContentResolver().query(AUDIO_URI, audioProjection, "year <= 1989", null, null);
            if (audioCursorEighty2 != null) {
                countEighty = audioCursorEighty2.getCount() - countSeventy;
                object.put(EIGHTY_SONGS, countEighty);
            }
            Cursor audioCursorNinety2 = this.mContext.getContentResolver().query(AUDIO_URI, audioProjection, "year <= 1999", null, null);
            if (audioCursorNinety2 != null) {
                object.put(NINETY_SONGS, (audioCursorNinety2.getCount() - countEighty) - countSeventy);
            }
            Cursor audioCursorNewCentury2 = this.mContext.getContentResolver().query(AUDIO_URI, audioProjection, "year > 1999", null, null);
            if (audioCursorNewCentury2 != null) {
                object.put(NEW_CENTURY_SONGS, audioCursorNewCentury2.getCount());
            }
            if (audioCursorSeventy2 != null) {
                audioCursorSeventy2.close();
            }
            if (audioCursorEighty2 != null) {
                audioCursorEighty2.close();
            }
            if (audioCursorNinety2 != null) {
                audioCursorNinety2.close();
            }
            if (audioCursorNewCentury2 != null) {
                audioCursorNewCentury2.close();
            }
        } catch (JSONException e) {
            OPCollectLog.e(TAG, "json exception: " + e.getMessage());
            if (audioCursorSeventy != null) {
                audioCursorSeventy.close();
            }
            if (audioCursorEighty != null) {
                audioCursorEighty.close();
            }
            if (audioCursorNinety != null) {
                audioCursorNinety.close();
            }
            if (audioCursorNewCentury != null) {
                audioCursorNewCentury.close();
            }
        } catch (RuntimeException e2) {
            OPCollectLog.e(TAG, "runtime exception: " + e2.getMessage());
            if (audioCursorSeventy != null) {
                audioCursorSeventy.close();
            }
            if (audioCursorEighty != null) {
                audioCursorEighty.close();
            }
            if (audioCursorNinety != null) {
                audioCursorNinety.close();
            }
            if (audioCursorNewCentury != null) {
                audioCursorNewCentury.close();
            }
        } catch (Throwable th) {
            if (audioCursorSeventy != null) {
                audioCursorSeventy.close();
            }
            if (audioCursorEighty != null) {
                audioCursorEighty.close();
            }
            if (audioCursorNinety != null) {
                audioCursorNinety.close();
            }
            if (audioCursorNewCentury != null) {
                audioCursorNewCentury.close();
            }
            throw th;
        }
        OPCollectLog.d(TAG, "Years of music:" + object.toString());
        return object.toString();
    }
}
