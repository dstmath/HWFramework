package huawei.android.hwgallerycache;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Bitmap.GalleryCacheInfo;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hwgallerycache.HwGalleryCacheManager.IHwGalleryCacheManager;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import android.widget.ImageView;
import com.huawei.kvdb.HwKVDatabase;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FilterFD;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwGalleryCacheManagerImpl implements IHwGalleryCacheManager {
    private static final String ATTR_NAME = "name";
    private static String CONFIG_FILEPATH = "/data/app_acc/app_config.xml";
    private static final boolean Debug = true;
    private static String SWITCH_FILEPATH = "/data/app_acc/app_switch.xml";
    private static final String TAG = "HwGalleryCacheManagerImpl";
    private static final String TEXT_NAME = "CacheList";
    private static final int TYPE_MICROTHUMBNAIL = 2;
    private static final int TYPE_MICROTHUMBNAIL_SCALE = 1;
    private static final String XML_TAG_APPNAME = "packageName";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_ITEM = "item";
    private static final String XML_TAG_SWITCH = "switch";
    private static final String XML_TAG_THREADNAME = "threadName";
    private static final String XML_TAG_VERSION = "supportVersion";
    private static Object mDecoderLock = new Object();
    private AppData mAppData = null;
    private final Object mCacheLock = new Object();
    private GalleryCacheInfo mCacheTail = null;
    private Context mContext = null;
    private String mCurrentPackageName = null;
    private int mGalleryLazyWorking = 0;
    private HwGalleryCacheNative mHwGalleryCacheNative = null;
    private boolean mIsEffect = false;
    private HwKVDatabase mKVvDatabase = null;
    private DecoderThread mLastThread = null;
    private DecoderThread mNextThread = null;
    private String mPath = null;

    private static class AppData {
        public String mAppName;
        public String mSupportVersion;
        public String mThreadName;

        public AppData(String name, String supportVersion, String threadName) {
            this.mAppName = name;
            this.mSupportVersion = supportVersion;
            this.mThreadName = threadName;
        }
    }

    private static class DecoderThread extends Thread {
        private static final int MSG_STOP = 1;
        private static final int MSG_TASK = 0;
        private Handler mHandler = null;
        private Object mLock = new Object();
        public boolean mStopped = false;

        public DecoderThread(String threadName) {
            super(threadName);
        }

        public void run() {
            Looper.prepare();
            synchronized (this.mLock) {
                this.mHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        if (msg.what == 0) {
                            GalleryCacheInfo cache = msg.obj;
                            if (HwGalleryCacheManagerImpl.needDecode(cache)) {
                                synchronized (cache) {
                                    Bitmap wechatThumb;
                                    cache.setIsDecoding(true);
                                    Bitmap bm = BitmapFactory.decodeFile(cache.getPath(), cache.getOptions());
                                    if (bm == null || cache.getMatrix() == null) {
                                        wechatThumb = bm;
                                    } else {
                                        wechatThumb = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), cache.getMatrix(), cache.getFilter());
                                    }
                                    if (wechatThumb != null) {
                                        cache.setWechatThumb(wechatThumb);
                                    }
                                    cache.setIsDecoding(false);
                                    cache.notifyAll();
                                }
                            }
                        } else if (1 == msg.what) {
                            synchronized (HwGalleryCacheManagerImpl.mDecoderLock) {
                                if (DecoderThread.this.mHandler.hasMessages(0)) {
                                    DecoderThread.this.resetTimer();
                                } else {
                                    DecoderThread.this.mStopped = true;
                                    Looper.myLooper().quit();
                                }
                            }
                        }
                    }
                };
                this.mLock.notifyAll();
            }
            Looper.loop();
        }

        private void decodeAsync(GalleryCacheInfo cache) {
            synchronized (this.mLock) {
                while (this.mHandler == null) {
                    try {
                        this.mLock.wait(1000);
                    } catch (InterruptedException e) {
                        Log.e(HwGalleryCacheManagerImpl.TAG, "Interrupted while waiting for decode response");
                    }
                }
            }
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = cache;
            this.mHandler.sendMessageDelayed(msg, 50);
        }

        private void resetTimer() {
            synchronized (this.mLock) {
                while (this.mHandler == null) {
                    try {
                        this.mLock.wait(1000);
                    } catch (InterruptedException e) {
                        Log.e(HwGalleryCacheManagerImpl.TAG, "Interrupted while waiting for decode response");
                    }
                }
            }
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, 5000);
        }
    }

    private static class ThumbThread extends Thread {
        private GalleryCacheInfo mCache = null;
        private Handler mHandler = null;
        private ImageView mImageView = null;

        ThumbThread(Handler handler, GalleryCacheInfo cache, ImageView view) {
            this.mHandler = handler;
            this.mCache = cache;
            this.mImageView = view;
        }

        public void run() {
            if (this.mCache != null) {
                if (HwGalleryCacheManagerImpl.needDecode(this.mCache)) {
                    synchronized (this.mCache) {
                        Bitmap wechatThumb;
                        this.mCache.setIsDecoding(true);
                        Bitmap bm = BitmapFactory.decodeFile(this.mCache.getPath(), this.mCache.getOptions());
                        if (bm == null || this.mCache.getMatrix() == null) {
                            wechatThumb = bm;
                        } else {
                            wechatThumb = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), this.mCache.getMatrix(), this.mCache.getFilter());
                        }
                        if (wechatThumb != null) {
                            this.mCache.setWechatThumb(wechatThumb);
                        }
                        this.mCache.setIsDecoding(false);
                        this.mCache.notifyAll();
                    }
                } else {
                    synchronized (this.mCache) {
                        while (this.mCache.getIsDecoding()) {
                            try {
                                this.mCache.wait();
                            } catch (InterruptedException e) {
                                Log.e(HwGalleryCacheManagerImpl.TAG, "Interrupted while waiting for decode response");
                            }
                        }
                    }
                }
                if (this.mCache.getWechatThumb() != null) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            if (1 == ThumbThread.this.mImageView.mInBigView) {
                                ThumbThread.this.mImageView.setImageBitmap(ThumbThread.this.mCache.getWechatThumb());
                            }
                        }
                    });
                }
            }
        }
    }

    public HwGalleryCacheManagerImpl() {
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false)) {
            this.mContext = ActivityThread.currentApplication();
            this.mCurrentPackageName = ActivityThread.currentPackageName();
            if (this.mContext != null && this.mCurrentPackageName != null && !this.mCurrentPackageName.isEmpty()) {
                if ("com.tencent.mm".equals(this.mCurrentPackageName) && isSwitchEnabled() && loadConfigFile()) {
                    this.mHwGalleryCacheNative = new HwGalleryCacheNative();
                    this.mKVvDatabase = HwKVDatabase.getInstance(this.mContext);
                    this.mIsEffect = true;
                }
                Log.d(TAG, "mIsEffect:" + this.mIsEffect);
            }
        }
    }

    private File getFile(String fileName) {
        return new File(fileName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:140:0x01a0  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ab A:{SYNTHETIC, Splitter: B:53:0x00ab} */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0143 A:{SYNTHETIC, Splitter: B:105:0x0143} */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0149 A:{SYNTHETIC, Splitter: B:109:0x0149} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0111 A:{SYNTHETIC, Splitter: B:88:0x0111} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0117 A:{SYNTHETIC, Splitter: B:92:0x0117} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x00df A:{SYNTHETIC, Splitter: B:71:0x00df} */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x00e5 A:{SYNTHETIC, Splitter: B:75:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x016c A:{SYNTHETIC, Splitter: B:120:0x016c} */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x0172 A:{SYNTHETIC, Splitter: B:124:0x0172} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSwitchEnabled() {
        Throwable th;
        File file = getFile(SWITCH_FILEPATH);
        if (!file.exists()) {
            return false;
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(is2, StandardCharsets.UTF_8.name());
                int outerDepth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        if (is2 == null) {
                            try {
                                is2.close();
                            } catch (IOException e) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                        }
                    } else if (!(type == 3 || type == 4)) {
                        if (XML_TAG_SWITCH.equals(parser.getName())) {
                            if (Integer.parseInt(parser.nextText()) == 1) {
                                if (is2 != null) {
                                    try {
                                        is2.close();
                                    } catch (IOException e2) {
                                        Log.e(TAG, "close file input stream fail!");
                                    }
                                }
                                if (parser != null) {
                                    try {
                                        ((KXmlParser) parser).close();
                                    } catch (Exception e3) {
                                        Log.e(TAG, "parser close error");
                                    }
                                }
                                return true;
                            }
                            if (is2 != null) {
                                try {
                                    is2.close();
                                } catch (IOException e4) {
                                    Log.e(TAG, "close file input stream fail!");
                                    is = is2;
                                }
                            }
                            if (parser != null) {
                                try {
                                    ((KXmlParser) parser).close();
                                } catch (Exception e5) {
                                    Log.e(TAG, "parser close error");
                                }
                            }
                            return false;
                        }
                    }
                }
                if (is2 == null) {
                }
                if (parser != null) {
                    try {
                        ((KXmlParser) parser).close();
                    } catch (Exception e6) {
                        Log.e(TAG, "parser close error");
                    }
                }
                return false;
            } catch (XmlPullParserException e7) {
                is = is2;
                Log.e(TAG, "failed parsing config file parser error");
                if (is != null) {
                }
                if (null != null) {
                }
                return false;
            } catch (IOException e8) {
                is = is2;
                Log.e(TAG, "failed parsing config file IO error ");
                if (is != null) {
                }
                if (null != null) {
                }
                return false;
            } catch (NumberFormatException e9) {
                is = is2;
                try {
                    Log.e(TAG, "switch number format error");
                    if (is != null) {
                    }
                    if (null != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e10) {
                            Log.e(TAG, "close file input stream fail!");
                        }
                    }
                    if (null != null) {
                        try {
                            ((KXmlParser) null).close();
                        } catch (Exception e11) {
                            Log.e(TAG, "parser close error");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th22) {
                th = th22;
                is = is2;
                if (is != null) {
                }
                if (null != null) {
                }
                throw th;
            }
        } catch (XmlPullParserException e12) {
            Log.e(TAG, "failed parsing config file parser error");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e13) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (null != null) {
                try {
                    ((KXmlParser) null).close();
                } catch (Exception e14) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        } catch (IOException e15) {
            Log.e(TAG, "failed parsing config file IO error ");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e16) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (null != null) {
                try {
                    ((KXmlParser) null).close();
                } catch (Exception e17) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        } catch (NumberFormatException e18) {
            Log.e(TAG, "switch number format error");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e19) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (null != null) {
                try {
                    ((KXmlParser) null).close();
                } catch (Exception e20) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:127:0x0199  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0094 A:{SYNTHETIC, Splitter: B:40:0x0094} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0134 A:{SYNTHETIC, Splitter: B:92:0x0134} */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x013a A:{SYNTHETIC, Splitter: B:96:0x013a} */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x00fe A:{SYNTHETIC, Splitter: B:75:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0104 A:{SYNTHETIC, Splitter: B:79:0x0104} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00c8 A:{SYNTHETIC, Splitter: B:58:0x00c8} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00ce A:{SYNTHETIC, Splitter: B:62:0x00ce} */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0161 A:{SYNTHETIC, Splitter: B:107:0x0161} */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0167 A:{SYNTHETIC, Splitter: B:111:0x0167} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean loadConfigFile() {
        Throwable th;
        File file = getFile(CONFIG_FILEPATH);
        if (!file.exists()) {
            return false;
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(is2, StandardCharsets.UTF_8.name());
                int outerDepth = parser.getDepth();
                while (true) {
                    int type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        if (is2 == null) {
                            try {
                                is2.close();
                            } catch (IOException e) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                        }
                    } else if (!(type == 3 || type == 4)) {
                        if (XML_TAG_CONFIG.equals(parser.getName())) {
                            if (TEXT_NAME.equals(parser.getAttributeValue(null, ATTR_NAME))) {
                                boolean appOptimized = false;
                                if (checkAppListFromXml(parser)) {
                                    appOptimized = true;
                                }
                                if (is2 != null) {
                                    try {
                                        is2.close();
                                    } catch (IOException e2) {
                                        Log.e(TAG, "close file input stream fail!");
                                    }
                                }
                                if (parser != null) {
                                    try {
                                        ((KXmlParser) parser).close();
                                    } catch (Exception e3) {
                                        Log.e(TAG, "parser close error");
                                    }
                                }
                                return appOptimized;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                if (is2 == null) {
                }
                if (parser != null) {
                    try {
                        ((KXmlParser) parser).close();
                    } catch (Exception e4) {
                        Log.e(TAG, "parser close error");
                    }
                }
                return false;
            } catch (XmlPullParserException e5) {
                is = is2;
                Log.e(TAG, "failed parsing config file parser error");
                if (is != null) {
                }
                if (null != null) {
                }
                return false;
            } catch (IOException e6) {
                is = is2;
                Log.e(TAG, "failed parsing config file IO error ");
                if (is != null) {
                }
                if (null != null) {
                }
                return false;
            } catch (NumberFormatException e7) {
                is = is2;
                try {
                    Log.e(TAG, "config number format error");
                    if (is != null) {
                    }
                    if (null != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (is != null) {
                    }
                    if (null != null) {
                    }
                    throw th;
                }
            } catch (Throwable th22) {
                th = th22;
                is = is2;
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e8) {
                        Log.e(TAG, "close file input stream fail!");
                    }
                }
                if (null != null) {
                    try {
                        ((KXmlParser) null).close();
                    } catch (Exception e9) {
                        Log.e(TAG, "parser close error");
                    }
                }
                throw th;
            }
        } catch (XmlPullParserException e10) {
            Log.e(TAG, "failed parsing config file parser error");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e11) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (null != null) {
                try {
                    ((KXmlParser) null).close();
                } catch (Exception e12) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        } catch (IOException e13) {
            Log.e(TAG, "failed parsing config file IO error ");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e14) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (null != null) {
                try {
                    ((KXmlParser) null).close();
                } catch (Exception e15) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        } catch (NumberFormatException e16) {
            Log.e(TAG, "config number format error");
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e17) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            if (null != null) {
                try {
                    ((KXmlParser) null).close();
                } catch (Exception e18) {
                    Log.e(TAG, "parser close error");
                }
            }
            return false;
        }
    }

    private boolean checkAppListFromXml(XmlPullParser parser) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return false;
            }
            if (!(type == 3 || type == 4)) {
                if (XML_TAG_ITEM.equals(parser.getName())) {
                    this.mAppData = new AppData();
                    readAppDataFromXml(parser, this.mAppData);
                    if (!(this.mAppData.mAppName == null || this.mAppData.mSupportVersion == null || !this.mAppData.mAppName.equals(this.mCurrentPackageName))) {
                        return isWechatVersionSupport(this.mAppData.mAppName, this.mAppData.mSupportVersion);
                    }
                } else {
                    continue;
                }
            }
        }
        return false;
    }

    private void readAppDataFromXml(XmlPullParser parser, AppData appdata) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                String tag = parser.getName();
                if ("packageName".equals(tag)) {
                    appdata.mAppName = parser.nextText();
                } else if (XML_TAG_VERSION.equals(tag)) {
                    appdata.mSupportVersion = parser.nextText();
                } else if (XML_TAG_THREADNAME.equals(tag)) {
                    appdata.mThreadName = parser.nextText();
                } else {
                    Log.w(TAG, "Unknown  tag: " + tag);
                }
            }
        }
    }

    private boolean isWechatVersionSupport(String appName, String supportVersion) {
        try {
            int currentVersionCode = this.mContext.getPackageManager().getPackageInfo(appName, 0).versionCode;
            Log.d(TAG, "isWechatVersionSupport currentVersionCode:" + currentVersionCode);
            return Utils.versionInRange(currentVersionCode, supportVersion);
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private String getFilePath(FileDescriptor fd) {
        if (this.mHwGalleryCacheNative == null) {
            return null;
        }
        return this.mHwGalleryCacheNative.getFilePath(fd);
    }

    private String getFileID(FileDescriptor fd) {
        if (this.mHwGalleryCacheNative == null) {
            return null;
        }
        return this.mHwGalleryCacheNative.getFileID(fd);
    }

    private Bitmap getImageCache(FileDescriptor fd, Options options) {
        if (fd == null || this.mKVvDatabase == null) {
            return null;
        }
        String path = getFilePath(fd);
        if (path == null || path.length() < 1) {
            Log.e(TAG, "Can't get path from fd!");
            return null;
        }
        String id = getFileID(fd);
        if (id == null || id.length() < 1) {
            Log.e(TAG, "Can't get id from fd!");
            return null;
        }
        try {
            int rowId = Integer.parseInt(id);
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            long timeModified = file.lastModified() / 1000;
            if (timeModified <= 0) {
                return null;
            }
            this.mPath = path;
            HwKVDatabase hwKVDatabase = this.mKVvDatabase;
            return this.mKVvDatabase.getBitmap(HwKVDatabase.generateKey(rowId, timeModified, 1, 2), options);
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException!");
            return null;
        }
    }

    private Bitmap getImageCache(InputStream is, Options options) {
        FileDescriptor fd = null;
        if (is instanceof FilterInputStream) {
            fd = new FilterFD((FilterInputStream) is).getFD();
        } else if (is instanceof FileInputStream) {
            try {
                fd = ((FileInputStream) is).getFD();
            } catch (IOException e) {
                return null;
            }
        }
        return getImageCache(fd, options);
    }

    private boolean isGalleryThread() {
        String currentThreadName = Thread.currentThread().getName();
        if (this.mAppData == null || !currentThreadName.equals(this.mAppData.mThreadName)) {
            return false;
        }
        return true;
    }

    private boolean isGalleryLazyThread() {
        if (Thread.currentThread().getName().contains("album-image-gallery-lazy-loader")) {
            return true;
        }
        return false;
    }

    private Bitmap resizeToWechat(InputStream is, Bitmap bm, int sampleSize) {
        int srcWidth = bm.getWidth();
        int srcHeight = bm.getHeight();
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, opts);
        if (opts.outWidth <= 0 || opts.outHeight <= 0) {
            return bm;
        }
        int dstWidth = opts.outWidth;
        int dstHeight = opts.outHeight;
        Log.d(TAG, "Resize Cache(" + srcWidth + "x" + srcHeight + ") to (" + dstWidth + "x" + dstHeight + ")==> " + sampleSize);
        if (sampleSize > 0) {
            dstWidth /= sampleSize;
            dstHeight /= sampleSize;
        }
        Bitmap newBm = Bitmap.createBitmap(dstWidth, dstHeight, Config.ARGB_8888);
        if (newBm != null) {
            Rect dst;
            Canvas canvas = new Canvas(newBm);
            Rect src = new Rect(0, 0, srcWidth, srcHeight);
            int diff;
            if (dstWidth > dstHeight) {
                diff = (dstWidth - dstHeight) / 2;
                dst = new Rect(diff, 0, dstWidth - diff, dstHeight);
            } else {
                diff = (dstHeight - dstWidth) / 2;
                dst = new Rect(0, diff, dstWidth, dstHeight - diff);
            }
            canvas.drawBitmap(bm, src, dst, null);
            canvas.save();
            canvas.restore();
            bm.recycle();
            bm = newBm;
            Log.d(TAG, "Resize " + src + " to " + dst);
        }
        return bm;
    }

    private Bitmap getExifThumbnail(InputStream is) {
        try {
            byte[] thumbData = new ExifInterface(is).getThumbnail();
            is.reset();
            if (thumbData != null) {
                Options options = new Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, options);
                if (options.outWidth < 256 || options.outHeight < 256) {
                    return null;
                }
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, options);
                if (bitmap != null) {
                    return bitmap;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "getExifThumbnail fail!");
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            if (r9.inSampleSize > 2) goto L_0x000e;
     */
    /* JADX WARNING: Missing block: B:26:0x007b, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized Bitmap getGalleryCachedImageInner(InputStream is, Options opts) {
        int i = 0;
        synchronized (this) {
            if (opts != null) {
                if (!opts.inJustDecodeBounds) {
                }
            }
            Options myOptions = null;
            if (opts != null) {
                myOptions = new Options();
                myOptions.inSampleSize = opts.inSampleSize;
                opts.inSampleSize = 0;
            }
            Bitmap bm = getImageCache(is, opts);
            if (bm != null) {
                Log.d(TAG, "Thumb from gallery: " + bm.getWidth() + " x " + bm.getHeight());
                if (myOptions != null) {
                    i = myOptions.inSampleSize;
                }
                bm = resizeToWechat(is, bm, i);
                bm.mGalleryCached = true;
                GalleryCacheInfo cache = new GalleryCacheInfo();
                cache.setOptions(myOptions);
                cache.setPath(this.mPath);
                bm.mCacheInfo = cache;
                addCacheToTail(cache);
                return bm;
            }
            bm = getExifThumbnail(is);
            if (bm != null) {
                bm.mGalleryCached = true;
                return bm;
            }
        }
    }

    private void addCacheToTail(GalleryCacheInfo cache) {
        if (cache != null) {
            synchronized (this.mCacheLock) {
                if (this.mCacheTail != null) {
                    this.mCacheTail.setNext(cache);
                    cache.setLast(this.mCacheTail);
                }
                this.mCacheTail = cache;
            }
        }
    }

    private void removeCache(GalleryCacheInfo cache) {
        if (cache != null) {
            synchronized (this.mCacheLock) {
                if (cache.getLast() != null) {
                    cache.getLast().setNext(cache.getNext());
                }
                if (cache.getNext() != null) {
                    cache.getNext().setLast(cache.getLast());
                } else {
                    this.mCacheTail = cache.getLast();
                }
                if (cache.getWechatThumb() != null) {
                    cache.getWechatThumb().recycle();
                }
                cache.setNext(null);
                cache.setLast(null);
                cache.setWechatThumb(null);
            }
        }
    }

    private static boolean needDecode(GalleryCacheInfo cache) {
        if (cache == null || cache.getPath() == null || cache.getIsDecoding() || cache.getWechatThumb() != null) {
            return false;
        }
        return true;
    }

    private void decodeInLastThread(GalleryCacheInfo cache) {
        if (needDecode(cache)) {
            synchronized (mDecoderLock) {
                if (this.mLastThread == null || this.mLastThread.mStopped) {
                    this.mLastThread = new DecoderThread("LastDecoderThread");
                    this.mLastThread.start();
                }
                if (this.mLastThread != null) {
                    this.mLastThread.resetTimer();
                    this.mLastThread.decodeAsync(cache);
                }
            }
        }
    }

    private void decodeInNextThread(GalleryCacheInfo cache) {
        if (needDecode(cache)) {
            synchronized (mDecoderLock) {
                if (this.mNextThread == null || this.mNextThread.mStopped) {
                    this.mNextThread = new DecoderThread("NextDecoderThread");
                    this.mNextThread.start();
                }
                if (this.mNextThread != null) {
                    this.mNextThread.resetTimer();
                    this.mNextThread.decodeAsync(cache);
                }
            }
        }
    }

    public boolean isGalleryCacheEffect() {
        return this.mIsEffect;
    }

    public Bitmap getGalleryCachedImage(InputStream is, Options opts) {
        if (is == null) {
            return null;
        }
        if (isGalleryLazyThread()) {
            this.mGalleryLazyWorking = 6;
        } else if (isGalleryThread()) {
            if (this.mGalleryLazyWorking <= 0) {
                return getGalleryCachedImageInner(is, opts);
            }
            this.mGalleryLazyWorking--;
        }
        return null;
    }

    public void recycleCacheInfo(GalleryCacheInfo cache) {
        removeCache(cache);
    }

    private boolean isNeedRevert(ImageView view, Bitmap bm) {
        if (bm == null || view == null) {
            return false;
        }
        if (!bm.mGalleryCached || (bm.mCacheInfo == null && 1 == view.mInBigView)) {
            view.mInBigView = 2;
            return false;
        }
        if (2 == view.mInBigView) {
            view.mInBigView = 1;
        }
        if (view.mInBigView < 0) {
            String parent = view.getContext().getClass().getCanonicalName();
            if (parent == null || !parent.contains("com.tencent.mm.plugin.gallery.ui.ImagePreviewUI")) {
                view.mInBigView = 0;
            } else {
                view.mInBigView = 1;
            }
        } else if (view.mInBigView == 0) {
            return false;
        }
        return true;
    }

    public boolean revertWechatThumb(ImageView view, Bitmap bm) {
        if (!isNeedRevert(view, bm)) {
            return false;
        }
        GalleryCacheInfo cache = bm.mCacheInfo;
        if (!(cache.getNext() == null || cache.getNext().getNext() == null)) {
            decodeInNextThread(cache.getNext().getNext());
        }
        if (!(cache.getLast() == null || cache.getLast().getLast() == null)) {
            decodeInLastThread(cache.getLast().getLast());
        }
        if (cache.getWechatThumb() != null) {
            Log.d(TAG, "Wechat thumb is ready, replace with this one!");
            view.setImageBitmap(cache.getWechatThumb());
        } else {
            new ThumbThread(new Handler(), cache, view).start();
        }
        return true;
    }

    public Bitmap getGalleryCachedVideo(int rowid, long timeModified, Options opts) {
        if (this.mKVvDatabase == null || timeModified <= 0) {
            return null;
        }
        HwKVDatabase hwKVDatabase = this.mKVvDatabase;
        long key = HwKVDatabase.generateKey(rowid, timeModified, 3, 1);
        if (this.mKVvDatabase.hasKey(key)) {
            return this.mKVvDatabase.getBitmap(key, opts);
        }
        Log.w(TAG, "wechatopti getGalleryCachedVideo no key in kvdb!");
        return null;
    }
}
