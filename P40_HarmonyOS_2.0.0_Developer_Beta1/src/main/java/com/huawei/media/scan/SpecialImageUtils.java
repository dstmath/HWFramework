package com.huawei.media.scan;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.media.BuildConfig;
import android.media.ExifInterface;
import android.media.hwmnote.HwMnoteInterface;
import android.media.hwmnote.HwMnoteInterfaceUtils;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class SpecialImageUtils {
    private static final String[] FILES_PRESCAN_PROJECTION_MEDIA = {"_id", "_data"};
    private static final int FUNCTION_BYTE = 1;
    private static final int FUNCTION_LONG = 0;
    private static final String HW_3D_MODEL_IMAGE_TAG = "H W 3 D ";
    private static final int HW_3D_MODEL_IMAGE_TYPE = 16;
    private static final String HW_ALLFOCUS_IMAGE_COLUMN = "hw_image_refocus";
    private static final int HW_ALLFOCUS_IMAGE_TYPE_DUAL_CAMERA = 2;
    private static final int HW_ALLFOCUS_IMAGE_TYPE_DUAL_CAMERA_WESTALGO = 8;
    private static final int HW_ALLFOCUS_IMAGE_TYPE_SINGLE_CAMERA = 1;
    private static final String HW_AUTO_BEAUTY_BACK_IMAGE_TAG = "sbcb\u0000\u0000\u0000\u0000";
    private static final String HW_AUTO_BEAUTY_FRONT_IMAGE_TAG = "sbc\u0000\u0000\u0000\u0000\u0000";
    private static final int HW_AUTO_BEAUTY_IMAGE = 51;
    private static final int HW_CUSTOM_IMAGE_TAG_LEN = 20;
    private static final int HW_DUAL_CAMERA_ALLFOCUS_IMAGE_LEN = 8;
    private static final String HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG = "DepthEn\u0000";
    private static final String HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG_WESTALGO = "DepthWn\u0000";
    private static final int HW_FOV_ORG_IMAGE = 53;
    private static final String HW_FOV_ORG_TAG = "FOV_ORG";
    private static final int HW_FOV_WIDE_IMAGE = 54;
    private static final String HW_FOV_WIDE_TAG = "FOV_WIDE";
    private static final int HW_LIVE_PHOTO_IMAGE_TYPE = 50;
    private static final String HW_LIVE_TAG = "LIVE_";
    private static final String HW_MAKER_NOTE = "HwMakerNote";
    private static final String HW_MNOTE_ISO = "ISO-8859-1";
    private static final int HW_PANORAMA_3D_COMBINED_IMAGE_TYPE = 20;
    private static final String HW_PANORAMA_3D_COMBINED_TAG = "#FYUSEv3";
    private static final String HW_RECTIFY_IMAGE_COLUMN = "hw_rectify_offset";
    private static final String HW_RECTIFY_IMAGE_TAG = "RECTIFY_";
    private static final int HW_REFOCUS_IMAGE_TYPE_PORTRAIT = 9;
    private static final String HW_REFOCUS_PORTRAIT_IMAGE_TAG = "DepthP\u0000\u0000";
    private static final int HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_LEN = 7;
    private static final String HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_TAG = "Refocus";
    private static final String HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN = "special_file_type";
    private static final String HW_VOICE_IMAGE_COLUMN = "hw_voice_offset";
    private static final String HW_VOICE_TAG = "HWVOICE_";
    private static final String IMAGE_TYPE_BEAUTY_FRONT = "fbt";
    private static final String IMAGE_TYPE_BEAUTY_REAR = "rbt";
    private static final String IMAGE_TYPE_HDR = "hdr";
    private static final String IMAGE_TYPE_JHDR = "jhdr";
    private static final int IMAGE_TYPE_LENGTH = 3;
    private static final String IMAGE_TYPE_PORTRAIT_FRONT = "fpt";
    private static final String IMAGE_TYPE_PORTRAIT_REAR = "rpt";
    private static final int IMAGE_TYPE_VALUE_BEAUTY_FRONT = 40;
    private static final int IMAGE_TYPE_VALUE_BEAUTY_REAR = 41;
    private static final int IMAGE_TYPE_VALUE_DEFAULT = 0;
    private static final int IMAGE_TYPE_VALUE_HDR = 1;
    private static final int IMAGE_TYPE_VALUE_JHDR = 2;
    private static final int IMAGE_TYPE_VALUE_PORTRAIT_FRONT = 30;
    private static final int IMAGE_TYPE_VALUE_PORTRAIT_REAR = 31;
    private static final String IS_HDR = "is_hdr";
    private static final String KEY_TAG_BURST_NUMBER = "101";
    private static final String KEY_TAG_CAPTURE_MODE = "100";
    private static final String KEY_TAG_FACE_CONF = "302";
    private static final String KEY_TAG_FACE_COUNT = "301";
    private static final String KEY_TAG_FACE_LEYE_CENTER = "305";
    private static final String KEY_TAG_FACE_MOUTH_CENTER = "307";
    private static final String KEY_TAG_FACE_RECT = "304";
    private static final String KEY_TAG_FACE_REYE_CENTER = "306";
    private static final String KEY_TAG_FACE_SMILE_SCORE = "303";
    private static final String KEY_TAG_FACE_VERSION = "300";
    private static final String KEY_TAG_FRONT_CAMERA = "102";
    private static final String KEY_TAG_SCENE_BEACH_CONF = "205";
    private static final String KEY_TAG_SCENE_BLUESKY_CONF = "203";
    private static final String KEY_TAG_SCENE_FLOWERS_CONF = "208";
    private static final String KEY_TAG_SCENE_FOOD_CONF = "201";
    private static final String KEY_TAG_SCENE_GREENPLANT_CONF = "204";
    private static final String KEY_TAG_SCENE_MASTER_AI_MODE = "211";
    private static final String KEY_TAG_SCENE_NIGHT_CONF = "209";
    private static final String KEY_TAG_SCENE_SNOW_CONF = "206";
    private static final String KEY_TAG_SCENE_STAGE_CONF = "202";
    private static final String KEY_TAG_SCENE_SUNSET_CONF = "207";
    private static final String KEY_TAG_SCENE_TEXT_CONF = "210";
    private static final String KEY_TAG_SCENE_VERSION = "200";
    private static final int MAX_HW_CUSTOM_IMAGE_TAG_LEN = 20;
    private static final int SQL_QUERY_COUNT = 100;
    private static final String SQL_QUERY_LIMIT = "1000";
    private static final String SQL_VALUE_EXIF_FLAG = "1";
    private static final String TAG = "SpecialImageUtils";
    private static CustomImageInfo[] sCustomImageInfos;
    private static CustomImageInfo sPanorama3DImageInfo;
    private static boolean sPowerConnectState;
    private final Uri mImagesUri;
    private final ContentProviderClient mMediaProvider;
    private HashMap<Integer, DataFlagMap> mTagsDataFlagMap = new LinkedHashMap();

    private void addTagsType(int tag, String tagData, int tagFlag) {
        this.mTagsDataFlagMap.put(Integer.valueOf(tag), new DataFlagMap(tagData, tagFlag));
    }

    private void initHwMnote() {
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_CAPTURE_MODE, KEY_TAG_CAPTURE_MODE, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_BURST_NUMBER, KEY_TAG_BURST_NUMBER, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FRONT_CAMERA, KEY_TAG_FRONT_CAMERA, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_VERSION, KEY_TAG_SCENE_VERSION, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_FOOD_CONF, KEY_TAG_SCENE_FOOD_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_STAGE_CONF, KEY_TAG_SCENE_STAGE_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_BLUESKY_CONF, KEY_TAG_SCENE_BLUESKY_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_GREENPLANT_CONF, KEY_TAG_SCENE_GREENPLANT_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_MASTER_AI_MODE, KEY_TAG_SCENE_MASTER_AI_MODE, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_BEACH_CONF, KEY_TAG_SCENE_BEACH_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_SNOW_CONF, KEY_TAG_SCENE_SNOW_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_SUNSET_CONF, KEY_TAG_SCENE_SUNSET_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_FLOWERS_CONF, KEY_TAG_SCENE_FLOWERS_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_NIGHT_CONF, KEY_TAG_SCENE_NIGHT_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_TEXT_CONF, KEY_TAG_SCENE_TEXT_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_VERSION, KEY_TAG_FACE_VERSION, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_COUNT, KEY_TAG_FACE_COUNT, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_CONF, KEY_TAG_FACE_CONF, 1);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_SMILE_SCORE, KEY_TAG_FACE_SMILE_SCORE, 1);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_RECT, KEY_TAG_FACE_RECT, 1);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_LEYE_CENTER, KEY_TAG_FACE_LEYE_CENTER, 1);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_REYE_CENTER, KEY_TAG_FACE_REYE_CENTER, 1);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FACE_MOUTH_CENTER, KEY_TAG_FACE_MOUTH_CENTER, 1);
    }

    public SpecialImageUtils(Context context) {
        if (context != null) {
            this.mMediaProvider = context.getContentResolver().acquireContentProviderClient("media");
        } else {
            this.mMediaProvider = null;
        }
        this.mImagesUri = MediaStore.Images.Media.getContentUri("external");
        initHwMnote();
        initCustomImageInfos();
    }

    public static void setPowerConnectState(boolean state) {
        sPowerConnectState = state;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b9, code lost:
        r5 = r16;
     */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00f2 A[Catch:{ SQLException -> 0x00f3, RemoteException -> 0x00e9, all -> 0x00e5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0100  */
    /* JADX WARNING: Removed duplicated region for block: B:76:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:77:? A[RETURN, SYNTHETIC] */
    public void updateExifFile() {
        Throwable th;
        Cursor cursor;
        int i;
        Log.i(TAG, "updateExifFile");
        if (this.mMediaProvider != null) {
            String[] selectionArgs = {"0"};
            Uri limitUri = this.mImagesUri.buildUpon().appendQueryParameter("limit", SQL_QUERY_LIMIT).build();
            int count = 0;
            long lastId = Long.MIN_VALUE;
            Cursor cursor2 = null;
            while (true) {
                try {
                } catch (SQLException e) {
                    Log.e(TAG, "updateExifFile SQLException !");
                    if (cursor2 == null) {
                    }
                    cursor2.close();
                } catch (RemoteException e2) {
                    Log.e(TAG, "updateExifFile RemoteException !");
                    if (cursor2 != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (cursor2 != null) {
                    }
                    throw th;
                }
                if (!sPowerConnectState || count >= 100) {
                    break;
                }
                int count2 = count + 1;
                try {
                    selectionArgs[0] = BuildConfig.FLAVOR + lastId;
                    if (cursor2 != null) {
                        cursor2.close();
                        cursor = null;
                    } else {
                        cursor = cursor2;
                    }
                } catch (SQLException e3) {
                    Log.e(TAG, "updateExifFile SQLException !");
                    if (cursor2 == null) {
                    }
                    cursor2.close();
                } catch (RemoteException e4) {
                    count = count2;
                    Log.e(TAG, "updateExifFile RemoteException !");
                    if (cursor2 != null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor2 != null) {
                    }
                    throw th;
                }
                try {
                    i = 0;
                    cursor2 = this.mMediaProvider.query(limitUri, FILES_PRESCAN_PROJECTION_MEDIA, "_id>? and cam_exif_flag is null", selectionArgs, "_id", null);
                } catch (SQLException e5) {
                    cursor2 = cursor;
                    Log.e(TAG, "updateExifFile SQLException !");
                    if (cursor2 == null) {
                        return;
                    }
                    cursor2.close();
                } catch (RemoteException e6) {
                    count = count2;
                    cursor2 = cursor;
                    Log.e(TAG, "updateExifFile RemoteException !");
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    return;
                } catch (Throwable th4) {
                    th = th4;
                    cursor2 = cursor;
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    throw th;
                }
                if (cursor2 == null || cursor2.getCount() == 0) {
                    break;
                }
                while (sPowerConnectState && cursor2.moveToNext()) {
                    long rowId = cursor2.getLong(i);
                    lastId = rowId;
                    ContentValues values = new ContentValues();
                    values.put("cam_exif_flag", SQL_VALUE_EXIF_FLAG);
                    Uri updateRowId = ContentUris.withAppendedId(this.mImagesUri, rowId);
                    try {
                        scanHwMakerNote(values, new ExifInterface(cursor2.getString(1)));
                        this.mMediaProvider.update(updateRowId, values, null, null);
                    } catch (IOException e7) {
                        Log.e(TAG, "new ExifInterface Exception !");
                    }
                    i = 0;
                }
                count = count2;
            }
            if (count == 100) {
                Log.i(TAG, "SQL query exceed the limit 10 !");
            }
            if (cursor2 == null) {
                return;
            }
            cursor2.close();
        }
    }

    private void scanHwMakerNote(ContentValues values, ExifInterface exif) {
        if (exif == null || values == null) {
            Log.e(TAG, "HwMediaScannerImpl scanhwMnote arguments error !");
            return;
        }
        String hwMakerNoteStr = exif.getAttribute(HW_MAKER_NOTE);
        if (hwMakerNoteStr != null) {
            byte[] hwMakerNote = hwMakerNoteStr.getBytes(Charset.forName(HW_MNOTE_ISO));
            HwMnoteInterface hwMnoteInterface = new HwMnoteInterface();
            try {
                hwMnoteInterface.readHwMnote(hwMakerNote);
                JSONObject jsonObject = getJsonDatas(hwMnoteInterface);
                if (jsonObject == null) {
                    Log.e(TAG, "HwMediaScannerImpl scanhwMnote jsonObject == null !");
                } else {
                    values.put("cam_perception", jsonObject.toString());
                }
            } catch (IOException e) {
                Log.e(TAG, "HwMediaScannerImpl scanHwMnote readHwMnote() failed !!!");
            }
        }
    }

    private JSONObject getJsonDatas(HwMnoteInterface hwMnoteInterface) {
        if (hwMnoteInterface == null) {
            Log.w(TAG, "HwMediaScannerImpl getJsonDatas parameter hwMnoteInterface == null !");
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Integer, DataFlagMap> entry : this.mTagsDataFlagMap.entrySet()) {
            int key = entry.getKey().intValue();
            DataFlagMap dataFlagMap = entry.getValue();
            int i = dataFlagMap.mTagFlag;
            if (i == 0) {
                try {
                    jsonObject.put(dataFlagMap.mTagData, hwMnoteInterface.getTagLongValue(key));
                } catch (JSONException e) {
                    Log.w(TAG, "HwMediaScannerImpl getJsonDatas FUNCTION_LONG jsonObject.put has JSONException !");
                }
            } else if (i != 1) {
                Log.i(TAG, "Other Function Type !");
            } else {
                try {
                    byte[] bytes = hwMnoteInterface.getTagByteValues(key);
                    if (bytes != null) {
                        String tmpStr = new String(bytes, Charset.forName(HW_MNOTE_ISO));
                        if (tmpStr.length() > 0) {
                            jsonObject.put(dataFlagMap.mTagData, tmpStr);
                        }
                    }
                } catch (JSONException e2) {
                    Log.w(TAG, "HwMediaScannerImpl getJsonDatas FUNCTION_BYTE jsonObject.put has JSONException");
                }
            }
        }
        return jsonObject;
    }

    /* access modifiers changed from: private */
    public static class DataFlagMap {
        private final String mTagData;
        private final int mTagFlag;

        DataFlagMap(String tagData, int tagFlag) {
            this.mTagData = tagData;
            this.mTagFlag = tagFlag;
        }

        public String getTagData() {
            return this.mTagData;
        }

        public int getTagFlag() {
            return this.mTagFlag;
        }
    }

    public static void scannerSpecialImageType(ContentValues values, String exifDescription) {
        int hdrType;
        if (values != null) {
            if (IMAGE_TYPE_HDR.equals(exifDescription)) {
                hdrType = 1;
            } else if (IMAGE_TYPE_JHDR.equals(exifDescription)) {
                hdrType = 2;
            } else {
                hdrType = 0;
            }
            values.put(IS_HDR, Integer.valueOf(hdrType));
            if (exifDescription != null && exifDescription.length() >= 3) {
                String subString = exifDescription.substring(0, 3);
                if (IMAGE_TYPE_PORTRAIT_FRONT.equals(subString)) {
                    values.put(HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, Integer.valueOf((int) IMAGE_TYPE_VALUE_PORTRAIT_FRONT));
                } else if (IMAGE_TYPE_PORTRAIT_REAR.equals(subString)) {
                    values.put(HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, Integer.valueOf((int) IMAGE_TYPE_VALUE_PORTRAIT_REAR));
                } else if (IMAGE_TYPE_BEAUTY_FRONT.equals(subString)) {
                    values.put(HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, Integer.valueOf((int) IMAGE_TYPE_VALUE_BEAUTY_FRONT));
                } else if (IMAGE_TYPE_BEAUTY_REAR.equals(subString)) {
                    values.put(HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, Integer.valueOf((int) IMAGE_TYPE_VALUE_BEAUTY_REAR));
                } else {
                    Log.v(TAG, "scannerSpecialImageType, subString = " + subString);
                }
            }
        }
    }

    private static void initCustomImageInfos() {
        sCustomImageInfos = new CustomImageInfo[]{new HwVoiceOrRectifyImageInfo(HW_VOICE_TAG, 20, HW_MNOTE_ISO, HW_VOICE_IMAGE_COLUMN), new HwVoiceOrRectifyImageInfo(HW_RECTIFY_IMAGE_TAG, 20, HW_MNOTE_ISO, HW_RECTIFY_IMAGE_COLUMN), new SpecialOffsetImageInfo(HW_LIVE_TAG, 20, "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, HW_LIVE_PHOTO_IMAGE_TYPE), new FixedEndTagCustomImageInfo(HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_TAG, 7, "UTF-8", HW_ALLFOCUS_IMAGE_COLUMN, 1), new FixedEndTagCustomImageInfo(HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG, 8, "UTF-8", HW_ALLFOCUS_IMAGE_COLUMN, 2), new FixedEndTagCustomImageInfo(HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG_WESTALGO, 8, "UTF-8", HW_ALLFOCUS_IMAGE_COLUMN, 8), new FixedEndTagCustomImageInfo(HW_3D_MODEL_IMAGE_TAG, HW_3D_MODEL_IMAGE_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, 16), new FixedEndTagCustomImageInfo(HW_AUTO_BEAUTY_FRONT_IMAGE_TAG, HW_AUTO_BEAUTY_FRONT_IMAGE_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, HW_AUTO_BEAUTY_IMAGE), new FixedEndTagCustomImageInfo(HW_AUTO_BEAUTY_BACK_IMAGE_TAG, HW_AUTO_BEAUTY_FRONT_IMAGE_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, HW_AUTO_BEAUTY_IMAGE), new FixedEndTagCustomImageInfo(HW_REFOCUS_PORTRAIT_IMAGE_TAG, HW_REFOCUS_PORTRAIT_IMAGE_TAG.length(), "UTF-8", HW_ALLFOCUS_IMAGE_COLUMN, 9), new FOVCustomImageInfo(HW_FOV_ORG_TAG, HW_FOV_ORG_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, HW_FOV_ORG_IMAGE), new FOVCustomImageInfo(HW_FOV_WIDE_TAG, HW_FOV_WIDE_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, HW_FOV_WIDE_IMAGE)};
        if (SystemPropertiesEx.getBoolean("hw_mc.media_scanner.support_panorama_3d", true)) {
            sPanorama3DImageInfo = new FixedEndTagCustomImageInfo(HW_PANORAMA_3D_COMBINED_TAG, HW_PANORAMA_3D_COMBINED_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, 20);
        }
    }

    /* access modifiers changed from: private */
    public static abstract class CustomImageInfo {
        protected String mCustomImageTag;
        protected String mDatabaseColumn;
        protected int mDatabaseType;
        protected String mTagCharsetName;
        protected int mTagLength;

        /* access modifiers changed from: protected */
        public abstract boolean checkTag(byte[] bArr, ContentValues contentValues);

        protected CustomImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn, int databaseType) {
            this.mCustomImageTag = customImageTag;
            this.mTagLength = tagLength;
            this.mTagCharsetName = tagCharsetName;
            this.mDatabaseColumn = databaseColumn;
            this.mDatabaseType = databaseType;
        }

        public String getCustomImageTag() {
            return this.mCustomImageTag;
        }

        public void setCustomImageTag(String customImageTag) {
            this.mCustomImageTag = customImageTag;
        }

        public int getTagLength() {
            return this.mTagLength;
        }

        public void setTagLength(int tagLength) {
            this.mTagLength = tagLength;
        }

        public String getTagCharsetName() {
            return this.mTagCharsetName;
        }

        public void setTagCharsetName(String tagCharsetName) {
            this.mTagCharsetName = tagCharsetName;
        }

        public String getDatabaseColumn() {
            return this.mDatabaseColumn;
        }

        public void setDatabaseColumn(String databaseColumn) {
            this.mDatabaseColumn = databaseColumn;
        }

        public int getDatabaseType() {
            return this.mDatabaseType;
        }

        public void setDatabaseType(int databaseType) {
            this.mDatabaseType = databaseType;
        }
    }

    /* access modifiers changed from: private */
    public static class FixedEndTagCustomImageInfo extends CustomImageInfo {
        protected FixedEndTagCustomImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn, int databaseType) {
            super(customImageTag, tagLength, tagCharsetName, databaseColumn, databaseType);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.media.scan.SpecialImageUtils.CustomImageInfo
        public boolean checkTag(byte[] fileEndBytes, ContentValues values) {
            if (fileEndBytes == null || fileEndBytes.length < this.mTagLength || values == null || !Arrays.equals(Arrays.copyOfRange(fileEndBytes, fileEndBytes.length - this.mTagLength, fileEndBytes.length), this.mCustomImageTag.getBytes(Charset.forName(this.mTagCharsetName)))) {
                return false;
            }
            values.put(this.mDatabaseColumn, Integer.valueOf(this.mDatabaseType));
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class HwVoiceOrRectifyImageInfo extends CustomImageInfo {
        private static final int SPLIT_LENGTH_MIN = 2;

        protected HwVoiceOrRectifyImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn) {
            super(customImageTag, tagLength, tagCharsetName, databaseColumn, 0);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.media.scan.SpecialImageUtils.CustomImageInfo
        public boolean checkTag(byte[] fileEndBytes, ContentValues values) {
            if (fileEndBytes == null || fileEndBytes.length < this.mTagLength || values == null) {
                return false;
            }
            try {
                String tag = new String(Arrays.copyOfRange(fileEndBytes, fileEndBytes.length - this.mTagLength, fileEndBytes.length), this.mTagCharsetName).trim();
                if (tag.startsWith(this.mCustomImageTag)) {
                    String[] split = tag.split("_");
                    if (split.length < 2) {
                        return false;
                    }
                    values.put(this.mDatabaseColumn, Long.valueOf(split[1]));
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                Log.w(SpecialImageUtils.TAG, "fail to check custom image tag, throws UnsupportedEncodingException");
            } catch (NumberFormatException e2) {
                Log.w(SpecialImageUtils.TAG, "fail to check custom image tag, throws NumberFormatException");
            } catch (UnsupportedCharsetException e3) {
                Log.w(SpecialImageUtils.TAG, "fail to check custom image tag, throws UnsupportedCharsetException");
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class SpecialOffsetImageInfo extends CustomImageInfo {
        private static final String HW_SPECIAL_FILE_OFFSET_IMAGE_COLUMN = "special_file_offset";
        private static final int SPLIT_LENGTH_MIN = 2;

        protected SpecialOffsetImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn, int databaseType) {
            super(customImageTag, tagLength, tagCharsetName, databaseColumn, databaseType);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.media.scan.SpecialImageUtils.CustomImageInfo
        public boolean checkTag(byte[] fileEndBytes, ContentValues values) {
            if (fileEndBytes == null || fileEndBytes.length < this.mTagLength || values == null) {
                return false;
            }
            try {
                String tag = new String(Arrays.copyOfRange(fileEndBytes, fileEndBytes.length - this.mTagLength, fileEndBytes.length), this.mTagCharsetName).trim();
                if (tag.startsWith(this.mCustomImageTag)) {
                    String[] split = tag.split("_");
                    if (split.length < 2) {
                        return false;
                    }
                    values.put(this.mDatabaseColumn, Integer.valueOf(this.mDatabaseType));
                    values.put(HW_SPECIAL_FILE_OFFSET_IMAGE_COLUMN, Long.valueOf(split[1]));
                    Log.i(SpecialImageUtils.TAG, "find a live tag. " + tag);
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                Log.w(SpecialImageUtils.TAG, "fail tddo check custom image tag, throws UnsupportedEncodingException " + this.mDatabaseType);
            } catch (NumberFormatException e2) {
                Log.w(SpecialImageUtils.TAG, "fail to Long.valueOf");
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class FOVCustomImageInfo extends CustomImageInfo {
        protected FOVCustomImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn, int databaseType) {
            super(customImageTag, tagLength, tagCharsetName, databaseColumn, databaseType);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.media.scan.SpecialImageUtils.CustomImageInfo
        public boolean checkTag(byte[] fileEndBytes, ContentValues values) {
            if (fileEndBytes == null || fileEndBytes.length < this.mTagLength || values == null || !Arrays.equals(Arrays.copyOfRange(fileEndBytes, 0, this.mTagLength), this.mCustomImageTag.getBytes(Charset.forName(this.mTagCharsetName)))) {
                return false;
            }
            Log.i(SpecialImageUtils.TAG, "find a FOV tag. " + this.mCustomImageTag);
            values.put(this.mDatabaseColumn, Integer.valueOf(this.mDatabaseType));
            return true;
        }
    }

    public static void initializeHwVoiceAndFocus(String path, ContentValues values) {
        byte[] fileEndBytes = readFileEndBytes(path);
        CustomImageInfo[] customImageInfoArr = sCustomImageInfos;
        int length = customImageInfoArr.length;
        int i = 0;
        while (i < length && !customImageInfoArr[i].checkTag(fileEndBytes, values)) {
            i++;
        }
        CustomImageInfo customImageInfo = sPanorama3DImageInfo;
        if (customImageInfo != null) {
            customImageInfo.checkTag(fileEndBytes, values);
        }
    }

    private static byte[] readFileEndBytes(String path) {
        RandomAccessFile randomFile = null;
        byte[] buffer = new byte[0];
        if (path == null) {
            return buffer;
        }
        try {
            randomFile = new RandomAccessFile(path, "r");
            long fileLength = randomFile.length();
            if (fileLength < 20) {
                try {
                    randomFile.close();
                } catch (IOException e) {
                    Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                }
                return buffer;
            }
            byte[] tmp = new byte[20];
            randomFile.seek(fileLength - 20);
            if (randomFile.read(tmp) != 20) {
                try {
                    randomFile.close();
                } catch (IOException e2) {
                    Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                }
                return buffer;
            }
            buffer = tmp;
            try {
                randomFile.close();
            } catch (IOException e3) {
                Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
            }
            return buffer;
        } catch (IOException e4) {
            Log.w(TAG, "fail to process custom image, readFileEndBytes throws IOException");
            if (randomFile != null) {
                randomFile.close();
            }
        } catch (SecurityException e5) {
            Log.w(TAG, "fail to process custom image, readFileEndBytes throws SecurityException");
            if (randomFile != null) {
                randomFile.close();
            }
        } catch (IllegalArgumentException e6) {
            Log.w(TAG, "fail to process custom image, readFileEndBytes throws IllegalArgumentException");
            if (randomFile != null) {
                randomFile.close();
            }
        } catch (Throwable th) {
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e7) {
                    Log.w(TAG, "fail to process custom image, readFileEndBytes close file fail");
                }
            }
            throw th;
        }
    }
}
