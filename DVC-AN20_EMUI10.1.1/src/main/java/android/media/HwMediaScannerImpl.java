package android.media;

import android.common.HwMediaScannerManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScanner;
import android.media.hwmnote.HwMnoteInterface;
import android.media.hwmnote.HwMnoteInterfaceUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.ExternalStorageFileImpl;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.uikit.effect.BuildConfig;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.android.provider.HwSettings;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.Character;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import libcore.io.IoUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class HwMediaScannerImpl implements HwMediaScannerManager {
    private static final String ALARMS = "alarms";
    private static final String ALARMS_PATH = "/system/media/audio/alarms/";
    private static final String AUDIO_FORMAT = ".ogg";
    private static final int CONSTANT_VALUE = 1000;
    private static final long CONST_TEN = 10;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final int DEFAULT_THRESHOLD_MAX_BYTES = 524288000;
    private static final String DEL_AUDIO_LIST_FILE = "del_audio_list.xml";
    private static final Uri EXTERNAL_AUDIO_URI = MediaStore.Audio.Media.getContentUri("external");
    private static final Uri EXTERNAL_IMAGE_URI = MediaStore.Images.Media.getContentUri("external");
    private static final Uri EXTERNAL_VIDEO_URI = MediaStore.Video.Media.getContentUri("external");
    private static final int FLAG_ALBUM = 1;
    private static final int FLAG_ARTIST = 2;
    private static final int FLAG_TITLE = 3;
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
    private static final int HW_LIVE_PHOTO_IMAGE_TYPE = 50;
    private static final String HW_LIVE_TAG = "LIVE_";
    private static final String HW_MAKER_NOTE = "HwMakerNote";
    private static final String HW_MNOTE_ISO = "ISO-8859-1";
    private static final int HW_PANORAMA_3D_COMBINED_IMAGE_TYPE = 20;
    private static final String HW_PANORAMA_3D_COMBINED_TAG = "#FYUSEv3";
    private static final String HW_RECTIFY_IMAGE_COLUMN = "hw_rectify_offset";
    private static final String HW_RECTIFY_IMAGE_TAG = "RECTIFY_";
    private static final int HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_LEN = 7;
    private static final String HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_TAG = "Refocus";
    private static final String HW_SPECIAL_FILE_OFFSET_IMAGE_COLUMN = "special_file_offset";
    private static final String HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN = "special_file_type";
    private static final String HW_VOICE_IMAGE_COLUMN = "hw_voice_offset";
    private static final String HW_VOICE_TAG = "HWVOICE_";
    private static final String INVALID_UTF8_TOKEN = "??";
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
    private static final String KEY_TAG_SCENE_NIGHT_CONF = "209";
    private static final String KEY_TAG_SCENE_SNOW_CONF = "206";
    private static final String KEY_TAG_SCENE_STAGE_CONF = "202";
    private static final String KEY_TAG_SCENE_SUNSET_CONF = "207";
    private static final String KEY_TAG_SCENE_TEXT_CONF = "210";
    private static final String KEY_TAG_SCENE_VERSION = "200";
    private static final int MAX_HW_CUSTOM_IMAGE_TAG_LEN = 20;
    private static final long MAX_NOMEDIA_SIZE = 1024;
    private static final int MEDIA_BUFFER_SIZE = 100;
    private static final String NOTIFICATIONS = "notifications";
    private static final String NOTIFICATIONS_PATH = "/system/media/audio/notifications/";
    private static final String[] NO_MEDIA_FILE_PATH = {"/.nomedia", "/DCIM/.nomedia", "/DCIM/Camera/.nomedia", "/Pictures/.nomedia", "/Pictures/Screenshots/.nomedia", "/tencent/.nomedia", "/tencent/MicroMsg/.nomedia", "/tencent/MicroMsg/Weixin/.nomedia", "/tencent/QQ_Images/.nomedia"};
    private static final String RINGTONES = "ringtones";
    private static final String RINGTONES_PATH = "/system/media/audio/ringtones/";
    private static final int SPLIT_LENGTH_MIN = 2;
    private static final String TAG = "HwMediaScannerImpl";
    private static final String UI = "ui";
    private static final String UI_PATH = "/system/media/audio/ui/";
    private static HwMediaScannerManager mHwMediaScannerManager = new HwMediaScannerImpl();
    private static HashMap<Integer, DataFlagMap> mTagsDataFlagMap = new LinkedHashMap();
    private static MediaScannerUtils utils = ((MediaScannerUtils) EasyInvokeFactory.getInvokeUtils(MediaScannerUtils.class));
    private CustomImageInfo[] mCustomImageInfos;
    private String mDefaultRingtoneFilename2;
    private boolean mDefaultRingtoneSet2;
    private HashSet<String> mDelRingtonesList = new HashSet<>();
    private final String mExternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private boolean mIsAudioFilterLoad = false;
    private MediaInserter mMediaInserter;

    static {
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_CAPTURE_MODE, KEY_TAG_CAPTURE_MODE, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_BURST_NUMBER, KEY_TAG_BURST_NUMBER, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_FRONT_CAMERA, KEY_TAG_FRONT_CAMERA, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_VERSION, KEY_TAG_SCENE_VERSION, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_FOOD_CONF, KEY_TAG_SCENE_FOOD_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_STAGE_CONF, KEY_TAG_SCENE_STAGE_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_BLUESKY_CONF, KEY_TAG_SCENE_BLUESKY_CONF, 0);
        addTagsType(HwMnoteInterfaceUtils.HW_MNOTE_TAG_SCENE_GREENPLANT_CONF, KEY_TAG_SCENE_GREENPLANT_CONF, 0);
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

    /* access modifiers changed from: private */
    public static class DataFlagMap {
        private final String tagData;
        private final int tagFlag;

        DataFlagMap(String tagData2, int tagFlag2) {
            this.tagData = tagData2;
            this.tagFlag = tagFlag2;
        }

        public String getTagData() {
            return this.tagData;
        }

        public int getTagFlag() {
            return this.tagFlag;
        }
    }

    private HwMediaScannerImpl() {
        initCustomImageInfos();
    }

    private static void addTagsType(int tag, String tagData, int tagFlag) {
        mTagsDataFlagMap.put(Integer.valueOf(tag), new DataFlagMap(tagData, tagFlag));
    }

    public static HwMediaScannerManager getDefault() {
        return mHwMediaScannerManager;
    }

    public boolean loadAudioFilterConfig(Context context) {
        boolean z;
        synchronized (this) {
            z = true;
            if (!this.mIsAudioFilterLoad) {
                loadAudioFilterConfigFromCust();
                loadAudioFilterConfigFromCache(context);
                this.mIsAudioFilterLoad = true;
            }
            if (this.mDelRingtonesList.size() == 0) {
                z = false;
            }
        }
        return z;
    }

    private void loadAudioFilterConfigFromCust() {
        ArrayList<File> files = HwCfgFilePolicy.getCfgFileList("xml/del_audio_list.xml", 0);
        int filesLen = files.size();
        for (int i = 0; i < filesLen; i++) {
            File file = files.get(i);
            if (file != null && file.exists()) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput(in, null);
                    int eventType = xpp.getEventType();
                    while (eventType != 1) {
                        if (eventType != 2) {
                            eventType = xpp.next();
                        } else {
                            if (ALARMS.equals(xpp.getName())) {
                                HashSet<String> hashSet = this.mDelRingtonesList;
                                hashSet.add(ALARMS_PATH + xpp.nextText() + AUDIO_FORMAT);
                            } else if (NOTIFICATIONS.equals(xpp.getName())) {
                                HashSet<String> hashSet2 = this.mDelRingtonesList;
                                hashSet2.add(NOTIFICATIONS_PATH + xpp.nextText() + AUDIO_FORMAT);
                            } else if (RINGTONES.equals(xpp.getName())) {
                                HashSet<String> hashSet3 = this.mDelRingtonesList;
                                hashSet3.add(RINGTONES_PATH + xpp.nextText() + AUDIO_FORMAT);
                            } else if (UI.equals(xpp.getName())) {
                                HashSet<String> hashSet4 = this.mDelRingtonesList;
                                hashSet4.add(UI_PATH + xpp.nextText() + AUDIO_FORMAT);
                            } else if (DEBUG) {
                                Log.w(TAG, "No event type could be met.");
                            }
                            eventType = xpp.next();
                        }
                    }
                } catch (XmlPullParserException e) {
                    Log.w(TAG, "failed to load audio filter config from cust, parser exception");
                } catch (IOException e2) {
                    Log.w(TAG, "failed to load audio filter config from cust, io exception");
                } catch (Throwable th) {
                    IoUtils.closeQuietly((AutoCloseable) null);
                    throw th;
                }
                IoUtils.closeQuietly(in);
            }
        }
    }

    private void loadAudioFilterConfigFromCache(Context context) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.openFileInput(DEL_AUDIO_LIST_FILE), "UTF-8"));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                this.mDelRingtonesList.add(line);
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG, "failed to load audio filter config from cache, file not found exception");
        } catch (IOException e2) {
            Log.w(TAG, "failed to load audio filter config from cache, io exception");
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtils.closeQuietly(reader);
    }

    public boolean isAudioFilterFile(String path) {
        boolean contains;
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        synchronized (this) {
            contains = this.mDelRingtonesList.contains(path);
        }
        return contains;
    }

    private int getSkipCustomDirectory(String[] whiteList, String[] blackList, StringBuffer sb) {
        int size = 0;
        for (String dir : whiteList) {
            if (!dir.isEmpty()) {
                sb.append(dir);
                sb.append(",");
                size++;
            }
        }
        for (String dir2 : blackList) {
            if (!dir2.isEmpty()) {
                sb.append(dir2);
                sb.append(",");
                size++;
            }
        }
        return size;
    }

    public void setMediaInserter(MediaInserter mediaInserter) {
        this.mMediaInserter = mediaInserter;
    }

    public void scanCustomDirectories(MediaScanner scanner, MediaScanner.MyMediaScannerClient mClient, String[] directories, String[] whiteList, String[] blackList) {
        try {
            utils.prescan(scanner, null, scanner.getIsPrescanFiles());
            for (String item : whiteList) {
                MediaScanner.sCurScanDIR = item;
                scanner.setStorageIdForCurScanDIR(item);
                insertDirectory(mClient, item);
                scanner.setExteLen(scanner.getRootDirLength(item));
                utils.processDirectory(scanner, item, mClient);
            }
            if (this.mMediaInserter != null) {
                this.mMediaInserter.flushAll();
            }
            StringBuffer sb = new StringBuffer();
            scanner.addSkipCustomDirectory(sb.toString(), getSkipCustomDirectory(whiteList, blackList, sb));
            for (String item2 : directories) {
                MediaScanner.sCurScanDIR = item2;
                scanner.setStorageIdForCurScanDIR(item2);
                if (!shouldSkipDirectory(item2, whiteList, blackList)) {
                    insertDirectory(mClient, item2);
                }
                scanner.setExteLen(scanner.getRootDirLength(item2));
                utils.processDirectory(scanner, item2, mClient);
            }
            scanner.clearSkipCustomDirectory();
            if (this.mMediaInserter != null) {
                this.mMediaInserter.flushAll();
            }
            for (String item3 : blackList) {
                MediaScanner.sCurScanDIR = item3;
                scanner.setStorageIdForCurScanDIR(item3);
                insertDirectory(mClient, item3);
                scanner.setExteLen(scanner.getRootDirLength(item3));
                utils.processDirectory(scanner, item3, mClient);
            }
            if (this.mMediaInserter != null) {
                this.mMediaInserter.flushAll();
            }
            scanner.postscan(directories);
        } catch (UnsupportedOperationException e) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan() error");
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in MediaScanner.scan() error");
        }
    }

    private static boolean shouldSkipDirectory(String path, String[] whiteList, String[] blackList) {
        for (String dir : whiteList) {
            if (dir.equals(path)) {
                return true;
            }
        }
        for (String dir2 : blackList) {
            if (dir2.equals(path)) {
                return true;
            }
        }
        return false;
    }

    private void insertDirectory(MediaScannerClient client, String path) {
        String filePath;
        File file = new File(path);
        if (file.exists()) {
            long lastModifiedSeconds = file.lastModified() / 1000;
            if (path.length() <= 1 || path.charAt(path.length() - 1) != '/') {
                filePath = path;
            } else {
                filePath = path.substring(0, path.length() - 1);
            }
            client.scanFile(filePath, lastModifiedSeconds, 0, true, false);
        }
    }

    public int getBufferSize(Uri tableUri, int bufferSizePerUri) {
        boolean isImage = EXTERNAL_IMAGE_URI.equals(tableUri);
        boolean isVideo = EXTERNAL_VIDEO_URI.equals(tableUri);
        boolean isAudio = EXTERNAL_AUDIO_URI.equals(tableUri);
        if (isImage || isVideo || isAudio) {
            return 100;
        }
        return bufferSizePerUri;
    }

    public void setHwDefaultRingtoneFileNames() {
        if (isMultiSimEnabled()) {
            this.mDefaultRingtoneFilename2 = SystemProperties.get("ro.config.ringtone2");
        }
    }

    public boolean hwNeedSetSettings(String path) {
        return isMultiSimEnabled() && !this.mDefaultRingtoneSet2 && (TextUtils.isEmpty(this.mDefaultRingtoneFilename2) || doesPathHaveFilename(path, this.mDefaultRingtoneFilename2));
    }

    private boolean doesPathHaveFilename(String path, String filename) {
        if (path == null || filename == null) {
            return false;
        }
        int pathFilenameStart = path.lastIndexOf(File.separatorChar) + 1;
        int filenameLength = filename.length();
        boolean pathLengthEnough = pathFilenameStart + filenameLength == path.length();
        if (!path.regionMatches(pathFilenameStart, filename, 0, filenameLength) || !pathLengthEnough) {
            return false;
        }
        return true;
    }

    public void hwSetRingtone2Settings(boolean needToSetSettings2, boolean ringtones, Uri tableUri, long rowId, Context context) {
        if (isMultiSimEnabled() && needToSetSettings2 && ringtones) {
            setSettingIfNotSet(HwSettings.System.RINGTONE2, tableUri, rowId, context);
            this.mDefaultRingtoneSet2 = true;
        }
    }

    private String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    private boolean wasRingtoneAlreadySet(ContentResolver cr, String name) {
        try {
            return Settings.System.getInt(cr, settingSetIndicatorName(name)) != 0;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    private void setSettingIfNotSet(String settingName, Uri uri, long rowId, Context context) {
        ContentResolver cr = context.getContentResolver();
        if (!wasRingtoneAlreadySet(cr, settingName)) {
            if (TextUtils.isEmpty(Settings.System.getString(cr, settingName))) {
                Uri settingUri = Settings.System.getUriFor(settingName);
                RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.getDefaultType(settingUri), ContentUris.withAppendedId(uri, rowId));
            }
            Settings.System.putInt(cr, settingSetIndicatorName(settingName), 1);
        }
    }

    public String getExtSdcardVolumePath(Context context) {
        StorageVolume[] storageVolumes;
        if (context == null || (storageVolumes = ((StorageManager) context.getSystemService("storage")).getVolumeList()) == null) {
            return null;
        }
        for (StorageVolume storageVolume : storageVolumes) {
            if (storageVolume.isRemovable() && !storageVolume.getPath().contains("usb")) {
                return storageVolume.getPath();
            }
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x003b, code lost:
        if (r10 != null) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003d, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004c, code lost:
        if (0 == 0) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004f, code lost:
        if (r1 != 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0051, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        return false;
     */
    public boolean isSkipExtSdcard(ContentProviderClient mMediaProvider, String mExtStroagePath, String mPackageName, Uri mFilesUriNoNotify) {
        if (mExtStroagePath == null) {
            return false;
        }
        int externelNum = -1;
        String[] projectionIn = {"COUNT(*)"};
        Cursor cursor = null;
        try {
            cursor = mMediaProvider.query(mFilesUriNoNotify, projectionIn, "_data LIKE '" + mExtStroagePath + "%'", null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                externelNum = cursor.getInt(0);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "isSkipExtSdcard query error");
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
    }

    public boolean isBitmapSizeTooLarge(String path) {
        File imageFile = new File(path);
        long limitSize = SystemProperties.getLong("ro.config.hw_pic_limit_size", 0);
        long newSize = limitSize * MAX_NOMEDIA_SIZE * MAX_NOMEDIA_SIZE;
        if (limitSize <= 0 || imageFile.length() <= newSize) {
            return false;
        }
        return true;
    }

    public void initializeHwVoiceAndFocus(String path, ContentValues values) {
        byte[] fileEndBytes = readFileEndBytes(path);
        CustomImageInfo[] customImageInfoArr = this.mCustomImageInfos;
        int length = customImageInfoArr.length;
        int i = 0;
        while (i < length && !customImageInfoArr[i].checkTag(fileEndBytes, values)) {
            i++;
        }
    }

    private byte[] readFileEndBytes(String path) {
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

    public void pruneDeadThumbnailsFolder() {
        boolean isDelete = false;
        try {
            StatFs sdcardFileStats = new StatFs(this.mExternalStoragePath);
            long freeMem = ((long) sdcardFileStats.getAvailableBlocks()) * ((long) sdcardFileStats.getBlockSize());
            long totalMem = (((long) sdcardFileStats.getBlockCount()) * ((long) sdcardFileStats.getBlockSize())) / CONST_TEN;
            long thresholdMem = 524288000;
            if (totalMem <= 524288000) {
                thresholdMem = totalMem;
            }
            isDelete = freeMem <= thresholdMem;
            Log.v(TAG, "freeMem[" + freeMem + "] 10%totalMem[" + totalMem + "] under " + this.mExternalStoragePath);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException in pruneDeadThumbnailsFolder error");
        }
        if (isDelete) {
            File thumbFolder = new File(this.mExternalStoragePath + "/DCIM/.thumbnails");
            if (!thumbFolder.exists()) {
                Log.e(TAG, ".thumbnails folder not exists. ");
                return;
            }
            File[] files = thumbFolder.listFiles();
            if (files != null) {
                Log.v(TAG, "delete .thumbnails");
                for (File file : files) {
                    if (!file.delete()) {
                        Log.e(TAG, "Failed to delete file!");
                    }
                }
            }
        }
    }

    private static boolean isMultiSimEnabled() {
        return TelephonyManager.getDefault().isMultiSimEnabled();
    }

    private boolean isMessyCharacter(char input) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(input);
        return isMessyCharacter1(unicodeBlock) || isMessyCharacter2(unicodeBlock) || isMessyCharacter3(unicodeBlock);
    }

    private boolean isMessyCharacter1(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.LATIN_1_SUPPLEMENT || unicodeBlock == Character.UnicodeBlock.SPECIALS || unicodeBlock == Character.UnicodeBlock.HEBREW || unicodeBlock == Character.UnicodeBlock.GREEK;
    }

    private boolean isMessyCharacter2(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY || unicodeBlock == Character.UnicodeBlock.LATIN_EXTENDED_A || unicodeBlock == Character.UnicodeBlock.LATIN_EXTENDED_B || unicodeBlock == Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS;
    }

    private boolean isMessyCharacter3(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.PRIVATE_USE_AREA || unicodeBlock == Character.UnicodeBlock.ARMENIAN;
    }

    private boolean isMessyCharacterOrigin(char input) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(input);
        return isMessyCharacterOrigin1(unicodeBlock) || isMessyCharacterOrigin2(unicodeBlock) || isMessyCharacterOrigin3(input);
    }

    private boolean isMessyCharacterOrigin1(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.SPECIALS || unicodeBlock == Character.UnicodeBlock.GREEK || unicodeBlock == Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY || unicodeBlock == Character.UnicodeBlock.LATIN_EXTENDED_A;
    }

    private boolean isMessyCharacterOrigin2(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.LATIN_EXTENDED_B || unicodeBlock == Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS || unicodeBlock == Character.UnicodeBlock.PRIVATE_USE_AREA;
    }

    private boolean isMessyCharacterOrigin3(char input) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(input);
        return (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS && !CharacterTables.isFrequentHan(input)) || unicodeBlock == Character.UnicodeBlock.BOX_DRAWING || unicodeBlock == Character.UnicodeBlock.HANGUL_SYLLABLES || unicodeBlock == Character.UnicodeBlock.ARMENIAN;
    }

    private boolean isAcceptableCharacter(char input) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(input);
        return isCJKCharacter(unicodeBlock) || unicodeBlock == Character.UnicodeBlock.BASIC_LATIN || unicodeBlock == Character.UnicodeBlock.GENERAL_PUNCTUATION || unicodeBlock == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    private boolean isCJKCharacter(Character.UnicodeBlock unicodeBlock) {
        return unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || unicodeBlock == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || unicodeBlock == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION;
    }

    private String trimIncorrectPunctuation(String input) {
        return Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]").matcher(Pattern.compile("\\s*|\t*|\r*|\n*").matcher(input).replaceAll(BuildConfig.FLAVOR).replaceAll("\\p{P}", BuildConfig.FLAVOR)).replaceAll(BuildConfig.FLAVOR);
    }

    private boolean isAcceptableString(String input) {
        for (char item : trimIncorrectPunctuation(input).trim().toCharArray()) {
            if (!isAcceptableCharacter(item)) {
                return false;
            }
        }
        return true;
    }

    private boolean isStringMessy(String input) {
        for (char intent : trimIncorrectPunctuation(input).trim().toCharArray()) {
            if (isMessyCharacter(intent)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStringMessyOrigin(String input) {
        for (char intent : trimIncorrectPunctuation(input).trim().toCharArray()) {
            if (isMessyCharacterOrigin(intent)) {
                return true;
            }
        }
        return false;
    }

    private String getCorrectEncodedString(String input) {
        if (isStringMessy(input)) {
            try {
                String utf8 = new String(input.getBytes(HW_MNOTE_ISO), "UTF-8");
                if (isAcceptableString(utf8)) {
                    return utf8;
                }
                String gbk = new String(input.getBytes(HW_MNOTE_ISO), "GBK");
                if (isAcceptableString(gbk)) {
                    return gbk;
                }
                String big5 = new String(input.getBytes(HW_MNOTE_ISO), "BIG5");
                if (isAcceptableString(big5)) {
                    return big5;
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "unsupported encoding error");
            }
        }
        return input;
    }

    private boolean isInvalidUtf8(String input) {
        return input != null && input.contains(INVALID_UTF8_TOKEN);
    }

    private boolean isInvalidString(String input) {
        return TextUtils.isEmpty(input) || isInvalidUtf8(input) || isStringMessy(input);
    }

    private String finalCheck(String value, String path, int flag) {
        if (!isInvalidString(value)) {
            return value;
        }
        if (flag == 1 || flag == 2) {
            return "<unknown>";
        }
        return getDisplayName(path);
    }

    private String getDisplayName(String path) {
        int lastdotIndex = path.lastIndexOf(".");
        int lastSlashIndex = path.lastIndexOf("/");
        if ((lastdotIndex <= 0 || lastSlashIndex <= 0) || lastSlashIndex > lastdotIndex) {
            return BuildConfig.FLAVOR;
        }
        return path.substring(lastSlashIndex + 1, lastdotIndex);
    }

    public boolean useMessyOptimize() {
        String debug = SystemProperties.get("ro.product.locale.region", BuildConfig.FLAVOR);
        return debug != null && "CN".equals(debug);
    }

    public boolean isMp3(String mimetype) {
        if (mimetype == null) {
            return false;
        }
        if ("audio/mpeg".equalsIgnoreCase(mimetype) || "audio/x-mp3".equalsIgnoreCase(mimetype) || "audio/x-mpeg".equalsIgnoreCase(mimetype) || "audio/mp3".equalsIgnoreCase(mimetype)) {
            return true;
        }
        return false;
    }

    public boolean preHandleStringTag(String value, String mimetype) {
        if (!useMessyOptimize() || !isMp3(mimetype) || TextUtils.isEmpty(value) || !isStringMessyOrigin(value)) {
            return false;
        }
        Log.w(TAG, "value: " + value);
        return true;
    }

    public void initializeSniffer(String path) {
    }

    public void resetSniffer() {
    }

    public String postHandleStringTag(String value, String path, int flag) {
        if (flag == 1) {
            return finalCheck(getCorrectEncodedString(value), path, flag);
        }
        if (flag == 2) {
            return finalCheck(getCorrectEncodedString(value), path, flag);
        }
        if (flag != 3) {
            return value;
        }
        return finalCheck(getCorrectEncodedString(value), path, flag);
    }

    private void initCustomImageInfos() {
        this.mCustomImageInfos = new CustomImageInfo[]{new HwVoiceOrRectifyImageInfo(HW_VOICE_TAG, 20, HW_MNOTE_ISO, HW_VOICE_IMAGE_COLUMN), new HwVoiceOrRectifyImageInfo(HW_RECTIFY_IMAGE_TAG, 20, HW_MNOTE_ISO, HW_RECTIFY_IMAGE_COLUMN), new SpecialOffsetImageInfo(HW_LIVE_TAG, 20, "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, 50), new FixedEndTagCustomImageInfo(HW_SINGLE_CAMERA_ALLFOCUS_IMAGE_TAG, 7, "UTF-8", HW_ALLFOCUS_IMAGE_COLUMN, 1), new FixedEndTagCustomImageInfo(HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG, 8, "UTF-8", HW_ALLFOCUS_IMAGE_COLUMN, 2), new FixedEndTagCustomImageInfo(HW_DUAL_CAMERA_ALLFOCUS_IMAGE_TAG_WESTALGO, 8, "UTF-8", HW_ALLFOCUS_IMAGE_COLUMN, 8), new FixedEndTagCustomImageInfo(HW_PANORAMA_3D_COMBINED_TAG, HW_PANORAMA_3D_COMBINED_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, 20), new FixedEndTagCustomImageInfo(HW_3D_MODEL_IMAGE_TAG, HW_3D_MODEL_IMAGE_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, 16), new FixedEndTagCustomImageInfo(HW_AUTO_BEAUTY_FRONT_IMAGE_TAG, HW_AUTO_BEAUTY_FRONT_IMAGE_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, 51), new FixedEndTagCustomImageInfo(HW_AUTO_BEAUTY_BACK_IMAGE_TAG, HW_AUTO_BEAUTY_FRONT_IMAGE_TAG.length(), "UTF-8", HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, 51)};
    }

    /* access modifiers changed from: private */
    public static abstract class CustomImageInfo {
        protected String customImageTag;
        protected String databaseColumn;
        protected int databaseType;
        protected String tagCharsetName;
        protected int tagLength;

        /* access modifiers changed from: protected */
        public abstract boolean checkTag(byte[] bArr, ContentValues contentValues);

        protected CustomImageInfo(String customImageTag2, int tagLength2, String tagCharsetName2, String databaseColumn2, int databaseType2) {
            this.customImageTag = customImageTag2;
            this.tagLength = tagLength2;
            this.tagCharsetName = tagCharsetName2;
            this.databaseColumn = databaseColumn2;
            this.databaseType = databaseType2;
        }

        public String getCustomImageTag() {
            return this.customImageTag;
        }

        public void setCustomImageTag(String customImageTag2) {
            this.customImageTag = customImageTag2;
        }

        public int getTagLength() {
            return this.tagLength;
        }

        public void setTagLength(int tagLength2) {
            this.tagLength = tagLength2;
        }

        public String getTagCharsetName() {
            return this.tagCharsetName;
        }

        public void setTagCharsetName(String tagCharsetName2) {
            this.tagCharsetName = tagCharsetName2;
        }

        public String getDatabaseColumn() {
            return this.databaseColumn;
        }

        public void setDatabaseColumn(String databaseColumn2) {
            this.databaseColumn = databaseColumn2;
        }

        public int getDatabaseType() {
            return this.databaseType;
        }

        public void setDatabaseType(int databaseType2) {
            this.databaseType = databaseType2;
        }
    }

    /* access modifiers changed from: private */
    public static class FixedEndTagCustomImageInfo extends CustomImageInfo {
        protected FixedEndTagCustomImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn, int databaseType) {
            super(customImageTag, tagLength, tagCharsetName, databaseColumn, databaseType);
        }

        /* access modifiers changed from: protected */
        @Override // android.media.HwMediaScannerImpl.CustomImageInfo
        public boolean checkTag(byte[] fileEndBytes, ContentValues values) {
            if (fileEndBytes == null || fileEndBytes.length < this.tagLength || !Arrays.equals(Arrays.copyOfRange(fileEndBytes, fileEndBytes.length - this.tagLength, fileEndBytes.length), this.customImageTag.getBytes(Charset.forName(this.tagCharsetName)))) {
                return false;
            }
            values.put(this.databaseColumn, Integer.valueOf(this.databaseType));
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class HwVoiceOrRectifyImageInfo extends CustomImageInfo {
        protected HwVoiceOrRectifyImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn) {
            super(customImageTag, tagLength, tagCharsetName, databaseColumn, 0);
        }

        /* access modifiers changed from: protected */
        @Override // android.media.HwMediaScannerImpl.CustomImageInfo
        public boolean checkTag(byte[] fileEndBytes, ContentValues values) {
            if (fileEndBytes == null || fileEndBytes.length < this.tagLength) {
                return false;
            }
            try {
                String tag = new String(Arrays.copyOfRange(fileEndBytes, fileEndBytes.length - this.tagLength, fileEndBytes.length), this.tagCharsetName).trim();
                if (tag.startsWith(this.customImageTag)) {
                    String[] split = tag.split("_");
                    if (split.length < 2) {
                        return false;
                    }
                    values.put(this.databaseColumn, Long.valueOf(split[1]));
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                Log.w(HwMediaScannerImpl.TAG, "fail to check custom image tag, throws UnsupportedEncodingException");
            } catch (NumberFormatException e2) {
                Log.w(HwMediaScannerImpl.TAG, "fail to check custom image tag, throws NumberFormatException");
            } catch (UnsupportedCharsetException e3) {
                Log.w(HwMediaScannerImpl.TAG, "fail to check custom image tag, throws UnsupportedCharsetException");
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class SpecialOffsetImageInfo extends CustomImageInfo {
        protected SpecialOffsetImageInfo(String customImageTag, int tagLength, String tagCharsetName, String databaseColumn, int databaseType) {
            super(customImageTag, tagLength, tagCharsetName, databaseColumn, databaseType);
        }

        /* access modifiers changed from: protected */
        @Override // android.media.HwMediaScannerImpl.CustomImageInfo
        public boolean checkTag(byte[] fileEndBytes, ContentValues values) {
            if (fileEndBytes == null || fileEndBytes.length < this.tagLength) {
                return false;
            }
            try {
                String tag = new String(Arrays.copyOfRange(fileEndBytes, fileEndBytes.length - this.tagLength, fileEndBytes.length), this.tagCharsetName).trim();
                if (tag.startsWith(this.customImageTag)) {
                    String[] split = tag.split("_");
                    if (split.length < 2) {
                        return false;
                    }
                    values.put(this.databaseColumn, Integer.valueOf(this.databaseType));
                    values.put(HwMediaScannerImpl.HW_SPECIAL_FILE_OFFSET_IMAGE_COLUMN, Long.valueOf(split[1]));
                    Log.d(HwMediaScannerImpl.TAG, "find a live tag. " + tag);
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                Log.w(HwMediaScannerImpl.TAG, "fail tddo check custom image tag, throws UnsupportedEncodingException " + this.databaseType);
            } catch (NumberFormatException e2) {
                Log.w(HwMediaScannerImpl.TAG, "fail to Long.valueOf");
            }
            return false;
        }
    }

    public void deleteNomediaFile(StorageVolume[] volumes) {
        for (StorageVolume storageVolume : volumes) {
            String rootPath = storageVolume.getPath();
            for (String nomedia : NO_MEDIA_FILE_PATH) {
                String nomediaPath = rootPath + nomedia;
                ExternalStorageFileImpl nomediaFile = new ExternalStorageFileImpl(nomediaPath);
                try {
                    if (nomediaFile.exists()) {
                        if (nomediaFile.isFile() && nomediaFile.length() > MAX_NOMEDIA_SIZE) {
                            printLog(nomediaPath, nomediaFile.length());
                        } else if (deleteFile(nomediaFile)) {
                            printDeleteLog("Success", nomediaPath);
                        } else {
                            printDeleteLog("Fail", nomediaPath);
                        }
                    }
                } catch (IOException e) {
                    printDeleteLog("ex", nomediaPath);
                }
            }
        }
    }

    private void printDeleteLog(String result, String tag) {
        if (DEBUG) {
            Log.w(TAG, "delete nomedia file " + result + "[" + tag + "]");
        }
    }

    private void printLog(String tag, long size) {
        if (DEBUG) {
            Log.w(TAG, "skip nomedia file [" + tag + "]  size:" + size);
        }
    }

    private boolean deleteFile(File file) throws IOException {
        boolean result = true;
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            if (!file.delete()) {
                return false;
            }
            return true;
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File item : files) {
                    if (!deleteFile(item)) {
                        result = false;
                    }
                }
            }
            if (!file.delete()) {
                return false;
            }
            return result;
        } else if (!DEBUG) {
            return true;
        } else {
            Log.w(TAG, "the file is neither file or directory, delete fail");
            return true;
        }
    }

    public void scanHwMakerNote(ContentValues values, ExifInterface exif) {
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
        for (Map.Entry<Integer, DataFlagMap> entry : mTagsDataFlagMap.entrySet()) {
            int key = entry.getKey().intValue();
            DataFlagMap dataFlagMap = entry.getValue();
            int i = dataFlagMap.tagFlag;
            if (i == 0) {
                try {
                    jsonObject.put(dataFlagMap.tagData, hwMnoteInterface.getTagLongValue(key));
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
                            jsonObject.put(dataFlagMap.tagData, tmpStr);
                        }
                    }
                } catch (JSONException e2) {
                    Log.w(TAG, "HwMediaScannerImpl getJsonDatas FUNCTION_BYTE jsonObject.put has JSONException");
                }
            }
        }
        return jsonObject;
    }
}
