package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import com.huawei.airsharing.util.HwLog;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayInfo implements Parcelable {
    public static final Parcelable.Creator<PlayInfo> CREATOR = new Parcelable.Creator<PlayInfo>() {
        /* class com.huawei.airsharing.api.PlayInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PlayInfo[] newArray(int size) {
            return new PlayInfo[size];
        }

        @Override // android.os.Parcelable.Creator
        public PlayInfo createFromParcel(Parcel source) {
            PlayInfo castPlayInfo = new PlayInfo(EHwMediaInfoType.UNKNOWN);
            castPlayInfo.setPlayMediaType(EHwMediaInfoType.valueOf(source.readString()));
            String mCastPlayInfo = source.readString();
            String mMediaMetadataArray = source.readString();
            try {
                castPlayInfo.setCastPlayInfo(new JSONObject(mCastPlayInfo));
                castPlayInfo.setMediaMetadataArray(new JSONArray(mMediaMetadataArray));
            } catch (JSONException e) {
                PlayInfo.sLog.e(PlayInfo.TAG, "createFromParcel throw JSONException");
            }
            return castPlayInfo;
        }
    };
    private static final String KEY_PLAY_APP_PID = "PLAY_APP_PID";
    private static final String KEY_PLAY_CREATE_TIME = "PLAY_CREATE_TIME";
    public static final String KEY_PLAY_FASTFORWARD = "PLAY_FASTFORWARD";
    private static final String KEY_PLAY_MEDIA_TYPE = "PLAY_MEDIA_TYPE";
    public static final String KEY_PLAY_RATE = "PLAY_RATE";
    public static final String KEY_PLAY_REPEAT_MODE = "PLAY_REPEAT_MODE";
    public static final String KEY_PLAY_REWIND = "PLAY_REWIND";
    public static final String KEY_PLAY_SEEK_POSITION = "PLAY_SEEK_POSITION";
    public static final String KEY_PLAY_START_INDEX = "PLAY_START_INDEX";
    private static final String TAG = "PlayInfo";
    private static HwLog sLog = HwLog.getInstance();
    private JSONObject mCastPlayInfo = null;
    private ERepeatMode mCastRepeatMode = ERepeatMode.PLAY_IN_ORDER;
    private JSONArray mMediaMetadataArray = null;
    private EHwMediaInfoType mPlayMediaType = null;

    public PlayInfo(EHwMediaInfoType playMediaType) {
        this.mPlayMediaType = playMediaType;
        this.mCastPlayInfo = getJsonObjectOfPlayInfo();
        this.mMediaMetadataArray = getJsonArrayOfPlayInfo();
    }

    public PlayInfo(EHwMediaInfoType playMediaType, ERepeatMode castRepeatMode) {
        this.mPlayMediaType = playMediaType;
        this.mCastRepeatMode = castRepeatMode;
        this.mCastPlayInfo = getJsonObjectOfPlayInfo();
        this.mMediaMetadataArray = getJsonArrayOfPlayInfo();
    }

    public EHwMediaInfoType getPlayMediaType() {
        return this.mPlayMediaType;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setPlayMediaType(EHwMediaInfoType playMediaType) {
        this.mPlayMediaType = playMediaType;
    }

    public JSONObject getCastPlayInfo() {
        return this.mCastPlayInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCastPlayInfo(JSONObject castPlayInfo) {
        this.mCastPlayInfo = castPlayInfo;
    }

    public JSONArray getMediaMetadataArray() {
        return this.mMediaMetadataArray;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMediaMetadataArray(JSONArray mediaMetadataArray) {
        this.mMediaMetadataArray = mediaMetadataArray;
    }

    private JSONObject getJsonObjectOfPlayInfo() {
        if (this.mCastPlayInfo == null) {
            this.mCastPlayInfo = new JSONObject();
        }
        try {
            this.mCastPlayInfo.put(KEY_PLAY_CREATE_TIME, SystemClock.uptimeMillis());
            this.mCastPlayInfo.put(KEY_PLAY_MEDIA_TYPE, this.mPlayMediaType.toString());
            this.mCastPlayInfo.put(KEY_PLAY_REPEAT_MODE, this.mCastRepeatMode.toString());
        } catch (JSONException e) {
            sLog.e(TAG, "getJsonObjectOfPlayInfo failed");
        }
        return this.mCastPlayInfo;
    }

    private JSONArray getJsonArrayOfPlayInfo() {
        if (this.mMediaMetadataArray == null) {
            this.mMediaMetadataArray = new JSONArray();
        }
        return this.mMediaMetadataArray;
    }

    public boolean putLong(String key, long value) {
        if (!isValidPlayInfoKey(key)) {
            HwLog hwLog = sLog;
            hwLog.e(TAG, "key(" + key + ") is invalid.");
            return false;
        }
        try {
            HwLog hwLog2 = sLog;
            hwLog2.i(TAG, "putLong, key: " + key + ", value: " + value);
            getJsonObjectOfPlayInfo().putOpt(key, Long.valueOf(value));
            return false;
        } catch (JSONException e) {
            HwLog hwLog3 = sLog;
            hwLog3.e(TAG, "putLong throw JSONException, key: " + key);
            return false;
        }
    }

    public boolean putDouble(String key, double value) {
        if (!isValidPlayInfoKey(key)) {
            HwLog hwLog = sLog;
            hwLog.e(TAG, "key(" + key + ") is invalid.");
            return false;
        }
        try {
            HwLog hwLog2 = sLog;
            hwLog2.i(TAG, "putDouble, key: " + key + ", value: " + value);
            getJsonObjectOfPlayInfo().putOpt(key, Double.valueOf(value));
            return false;
        } catch (JSONException e) {
            HwLog hwLog3 = sLog;
            hwLog3.e(TAG, "putDouble throw JSONException, key: " + key);
            return false;
        }
    }

    public boolean putInt(String key, int value) {
        if (!isValidPlayInfoKey(key)) {
            HwLog hwLog = sLog;
            hwLog.e(TAG, "key(" + key + ") is invalid.");
            return false;
        }
        try {
            HwLog hwLog2 = sLog;
            hwLog2.i(TAG, "putInt, key: " + key + ", value: " + value);
            getJsonObjectOfPlayInfo().putOpt(key, Integer.valueOf(value));
            return true;
        } catch (JSONException e) {
            HwLog hwLog3 = sLog;
            hwLog3.e(TAG, "putInt throw JSONException, key: " + key);
            return false;
        }
    }

    public boolean putString(String key, String value) {
        if (!isValidPlayInfoKey(key)) {
            HwLog hwLog = sLog;
            hwLog.e(TAG, "key(" + key + ") is invalid.");
            return false;
        }
        try {
            HwLog hwLog2 = sLog;
            hwLog2.i(TAG, "putString, key: " + key + ", value: " + value);
            getJsonObjectOfPlayInfo().putOpt(key, value);
            return true;
        } catch (JSONException e) {
            HwLog hwLog3 = sLog;
            hwLog3.e(TAG, "putString throw JSONException, key: " + key);
            return false;
        }
    }

    public boolean putMediaMetadata(MediaMetadata value) {
        if (value == null) {
            sLog.e(TAG, "put MediaMetadata is null.");
            return false;
        }
        HwLog hwLog = sLog;
        hwLog.i(TAG, "putMediaMetadata: " + value.getMediaMetadata().toString());
        getJsonArrayOfPlayInfo().put(value.getMediaMetadata());
        return true;
    }

    public boolean addMediaMetadataList(List<MediaMetadata> list) {
        sLog.i(TAG, "addMediaMetadataList in");
        if (list == null || list.isEmpty()) {
            sLog.e(TAG, "list is null or empty.");
            return false;
        }
        for (MediaMetadata object : list) {
            this.mMediaMetadataArray.put(object.getMediaMetadata());
        }
        return true;
    }

    public boolean putMediaMetadata(int index, MediaMetadata value) {
        if (index < 0) {
            sLog.e(TAG, "putMediaMetadata index is invalid.");
            return false;
        } else if (value == null) {
            sLog.e(TAG, "put MediaMetadata is null.");
            return false;
        } else {
            try {
                HwLog hwLog = sLog;
                hwLog.i(TAG, "putMediaMetadata: " + value.getMediaMetadata().toString() + ", index: " + index);
                getJsonArrayOfPlayInfo().put(index, value);
                return true;
            } catch (JSONException e) {
                HwLog hwLog2 = sLog;
                hwLog2.e(TAG, "putMediaMetadata throw JSONException, index: " + index);
                return false;
            }
        }
    }

    public long getLong(String key) {
        HwLog hwLog = sLog;
        hwLog.i(TAG, "getLong, key: " + key);
        return getJsonObjectOfPlayInfo().optLong(key);
    }

    public double getDouble(String key) {
        HwLog hwLog = sLog;
        hwLog.i(TAG, "getDouble, key: " + key);
        return getJsonObjectOfPlayInfo().optDouble(key, 0.0d);
    }

    public int getInt(String key) {
        HwLog hwLog = sLog;
        hwLog.i(TAG, "getInt, key: " + key);
        return getJsonObjectOfPlayInfo().optInt(key);
    }

    public String getString(String key) {
        HwLog hwLog = sLog;
        hwLog.i(TAG, "getString, key: " + key);
        return getJsonObjectOfPlayInfo().optString(key);
    }

    public MediaMetadata getMediaMetadata(int index) {
        HwLog hwLog = sLog;
        hwLog.i(TAG, "getMediaMetadata, index: " + index);
        Object object = getJsonArrayOfPlayInfo().opt(index);
        if (object instanceof MediaMetadata) {
            return (MediaMetadata) object;
        }
        return null;
    }

    private boolean isValidPlayInfoKey(String key) {
        if (key == null) {
            sLog.e(TAG, "isValidPlayInfoKey failed because key is null");
            return false;
        }
        char c = 65535;
        switch (key.hashCode()) {
            case -1508681238:
                if (key.equals(KEY_PLAY_START_INDEX)) {
                    c = 6;
                    break;
                }
                break;
            case -1242222558:
                if (key.equals(KEY_PLAY_APP_PID)) {
                    c = 5;
                    break;
                }
                break;
            case 21010470:
                if (key.equals(KEY_PLAY_REWIND)) {
                    c = 3;
                    break;
                }
                break;
            case 734938428:
                if (key.equals(KEY_PLAY_REPEAT_MODE)) {
                    c = 7;
                    break;
                }
                break;
            case 801531205:
                if (key.equals(KEY_PLAY_SEEK_POSITION)) {
                    c = 4;
                    break;
                }
                break;
            case 832277696:
                if (key.equals(KEY_PLAY_MEDIA_TYPE)) {
                    c = 1;
                    break;
                }
                break;
            case 842211902:
                if (key.equals(KEY_PLAY_FASTFORWARD)) {
                    c = 0;
                    break;
                }
                break;
            case 938564363:
                if (key.equals(KEY_PLAY_RATE)) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                return true;
            default:
                return false;
        }
    }

    @Override // java.lang.Object
    public String toString() {
        if (this.mCastPlayInfo != null) {
            return "{PLAY_MEDIA_TYPE:" + this.mPlayMediaType.toString() + ", PLAY_REPEAT_MODE:" + this.mCastRepeatMode.toString() + "}, " + this.mCastPlayInfo.toString();
        }
        return "{PLAY_MEDIA_TYPE:" + this.mPlayMediaType.toString() + ", PLAY_REPEAT_MODE:" + this.mCastRepeatMode.toString() + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPlayMediaType.toString());
        dest.writeString(this.mCastPlayInfo.toString());
        dest.writeString(this.mMediaMetadataArray.toString());
    }

    public PlayInfo copy() {
        PlayInfo castPlayInfo = new PlayInfo(EHwMediaInfoType.UNKNOWN);
        castPlayInfo.setPlayMediaType(this.mPlayMediaType);
        castPlayInfo.setCastPlayInfo(this.mCastPlayInfo);
        castPlayInfo.setMediaMetadataArray(this.mMediaMetadataArray);
        return castPlayInfo;
    }

    public class MediaMetadata {
        public static final String KEY_MEDIA_APP_ICON_URL = "MEDIA_APP_ICON_URL";
        public static final String KEY_MEDIA_APP_NAME = "MEDIA_APP_NAME";
        public static final String KEY_MEDIA_ARTIST = "MEDIA_ARTIST";
        public static final String KEY_MEDIA_CREATION_DATE = "MEDIA_CREATION_DATE";
        public static final String KEY_MEDIA_CUSTOM = "MEDIA_CUSTOM";
        public static final String KEY_MEDIA_DRM_LICENSE_URL = "MEDIA_DRM_LICENSE_URL";
        public static final String KEY_MEDIA_DRM_SCHEME = "MEDIA_DRM_SCHEME";
        public static final String KEY_MEDIA_FORMAT = "MEDIA_FORMAT";
        public static final String KEY_MEDIA_ICON_URL = "MEDIA_ICON_URL";
        public static final String KEY_MEDIA_METADATA_CREATE_TIME = "MEDIA_METADATA_CREATE_TIME";
        public static final String KEY_MEDIA_NAME = "MEDIA_NAME";
        public static final String KEY_MEDIA_SIZE = "MEDIA_SIZE";
        public static final String KEY_MEDIA_SUBTITLE_LANGUAGE = "MEDIA_SUBTITLE_LANGUAGE";
        public static final String KEY_MEDIA_SUBTITLE_MIME_TYPE = "MEDIA_SUBTITLE_MIME_TYPE";
        public static final String KEY_MEDIA_SUBTITLE_URL = "MEDIA_SUBTITLE_URL";
        public static final String KEY_MEDIA_TITLE = "MEDIA_TITLE";
        public static final String KEY_MEDIA_URL = "MEDIA_URL";
        private static final String TAG = "MediaMetadata";
        private JSONObject mMediaMetadata = null;

        public MediaMetadata() {
        }

        private JSONObject getJsonObjectOfMediaMetadata() {
            if (this.mMediaMetadata == null) {
                this.mMediaMetadata = new JSONObject();
                try {
                    this.mMediaMetadata.put(KEY_MEDIA_METADATA_CREATE_TIME, SystemClock.uptimeMillis());
                } catch (JSONException e) {
                    PlayInfo.sLog.e(TAG, "getJsonObjectOfMediaMetadata throw JSONException");
                }
            }
            return this.mMediaMetadata;
        }

        private boolean isValidMediaMetadataKey(String key) {
            if (key == null) {
                PlayInfo.sLog.e(TAG, "isValidMediaMetadataKey failed because key is null");
                return false;
            }
            int i = AnonymousClass2.$SwitchMap$com$huawei$airsharing$api$EHwMediaInfoType[PlayInfo.this.mPlayMediaType.ordinal()];
            if (i == 1) {
                return isValidKeyOfGenericPhoto(key);
            }
            if (i == 2) {
                return isValidKeyOfGenericAudio(key);
            }
            if (i == 3) {
                return isValidKeyOfGenericVideo(key);
            }
            if (i == 4) {
                return isValidKeyOfUser(key);
            }
            HwLog hwLog = PlayInfo.sLog;
            hwLog.e(TAG, "unsupported playMediaType: " + PlayInfo.this.mPlayMediaType.toString());
            return false;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        private boolean isValidKeyOfGenericPhoto(String key) {
            char c;
            switch (key.hashCode()) {
                case -1840648666:
                    if (key.equals(KEY_MEDIA_NAME)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1840491620:
                    if (key.equals(KEY_MEDIA_SIZE)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -1219747331:
                    if (key.equals(KEY_MEDIA_TITLE)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -935985500:
                    if (key.equals(KEY_MEDIA_ICON_URL)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 306893858:
                    if (key.equals(KEY_MEDIA_ARTIST)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 633368148:
                    if (key.equals(KEY_MEDIA_URL)) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1743984595:
                    if (key.equals(KEY_MEDIA_CREATION_DATE)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    return true;
                default:
                    return false;
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        private boolean isValidKeyOfGenericAudio(String key) {
            char c;
            switch (key.hashCode()) {
                case -1840648666:
                    if (key.equals(KEY_MEDIA_NAME)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1840491620:
                    if (key.equals(KEY_MEDIA_SIZE)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1219747331:
                    if (key.equals(KEY_MEDIA_TITLE)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -935985500:
                    if (key.equals(KEY_MEDIA_ICON_URL)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 306893858:
                    if (key.equals(KEY_MEDIA_ARTIST)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 633368148:
                    if (key.equals(KEY_MEDIA_URL)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            return c == 0 || c == 1 || c == 2 || c == 3 || c == 4 || c == 5;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        private boolean isValidKeyOfGenericVideo(String key) {
            char c;
            switch (key.hashCode()) {
                case -1840648666:
                    if (key.equals(KEY_MEDIA_NAME)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1840491620:
                    if (key.equals(KEY_MEDIA_SIZE)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1219747331:
                    if (key.equals(KEY_MEDIA_TITLE)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -935985500:
                    if (key.equals(KEY_MEDIA_ICON_URL)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 306893858:
                    if (key.equals(KEY_MEDIA_ARTIST)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 633368148:
                    if (key.equals(KEY_MEDIA_URL)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            return c == 0 || c == 1 || c == 2 || c == 3 || c == 4 || c == 5;
        }

        private boolean isValidKeyOfUser(String key) {
            if (((key.hashCode() == 366903372 && key.equals(KEY_MEDIA_CUSTOM)) ? (char) 0 : 65535) != 0) {
                return false;
            }
            return true;
        }

        public boolean putString(String key, String value) {
            if (!isValidMediaMetadataKey(key)) {
                HwLog hwLog = PlayInfo.sLog;
                hwLog.e(TAG, "key don't match the type, type: " + PlayInfo.this.mPlayMediaType.toString() + ", key: " + key);
                return false;
            }
            try {
                HwLog hwLog2 = PlayInfo.sLog;
                hwLog2.i(TAG, "putString, key: " + key + ", value: " + value);
                getJsonObjectOfMediaMetadata().putOpt(key, value);
                return true;
            } catch (JSONException e) {
                HwLog hwLog3 = PlayInfo.sLog;
                hwLog3.e(TAG, "MediaMetadata putString throw JSONException, key: " + key);
                return false;
            }
        }

        public boolean putInt(String key, int value) {
            if (!isValidMediaMetadataKey(key)) {
                HwLog hwLog = PlayInfo.sLog;
                hwLog.e(TAG, "key don't match the type, type: " + PlayInfo.this.mPlayMediaType.toString() + ", key: " + key);
                return false;
            }
            try {
                HwLog hwLog2 = PlayInfo.sLog;
                hwLog2.i(TAG, "putInt, key: " + key + ", value: " + value);
                getJsonObjectOfMediaMetadata().putOpt(key, Integer.valueOf(value));
                return true;
            } catch (JSONException e) {
                HwLog hwLog3 = PlayInfo.sLog;
                hwLog3.e(TAG, "MediaMetadata putInt throw JSONException, key: " + key);
                return false;
            }
        }

        public boolean putLong(String key, long value) {
            if (!isValidMediaMetadataKey(key)) {
                HwLog hwLog = PlayInfo.sLog;
                hwLog.e(TAG, "key don't match the type, type: " + PlayInfo.this.mPlayMediaType.toString() + ", key: " + key);
                return false;
            }
            try {
                HwLog hwLog2 = PlayInfo.sLog;
                hwLog2.i(TAG, "putLong, key: " + key + ", value: " + value);
                getJsonObjectOfMediaMetadata().putOpt(key, Long.valueOf(value));
                return true;
            } catch (JSONException e) {
                HwLog hwLog3 = PlayInfo.sLog;
                hwLog3.e(TAG, "MediaMetadata putLong throw JSONException, key: " + key);
                return false;
            }
        }

        public int getInt(String key) {
            HwLog hwLog = PlayInfo.sLog;
            hwLog.i(TAG, "getInt, key: " + key);
            return getJsonObjectOfMediaMetadata().optInt(key);
        }

        public String getString(String key) {
            HwLog hwLog = PlayInfo.sLog;
            hwLog.i(TAG, "getString, key: " + key);
            return getJsonObjectOfMediaMetadata().optString(key);
        }

        public long getLong(String key) {
            HwLog hwLog = PlayInfo.sLog;
            hwLog.i(TAG, "getLong, key: " + key);
            return getJsonObjectOfMediaMetadata().optLong(key);
        }

        public JSONObject getMediaMetadata() {
            return this.mMediaMetadata;
        }

        public void setMediaMetadata(JSONObject mediaMetadata) {
            this.mMediaMetadata = mediaMetadata;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.airsharing.api.PlayInfo$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$airsharing$api$EHwMediaInfoType = new int[EHwMediaInfoType.values().length];

        static {
            try {
                $SwitchMap$com$huawei$airsharing$api$EHwMediaInfoType[EHwMediaInfoType.IMAGE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$airsharing$api$EHwMediaInfoType[EHwMediaInfoType.AUDIO.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$airsharing$api$EHwMediaInfoType[EHwMediaInfoType.VIDEO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$airsharing$api$EHwMediaInfoType[EHwMediaInfoType.CUSTOM.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }
}
