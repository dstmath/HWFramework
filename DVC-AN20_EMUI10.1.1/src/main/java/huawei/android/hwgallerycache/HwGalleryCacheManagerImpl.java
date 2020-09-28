package huawei.android.hwgallerycache;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hwgallerycache.HwGalleryCacheManager;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwGalleryCacheManagerImpl implements HwGalleryCacheManager.IHwGalleryCacheManager {
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
    private static final Object mDecoderLock = new Object();
    private AppData mAppData = null;
    private final Object mCacheLock = new Object();
    private Bitmap.GalleryCacheInfo mCacheTail = null;
    private Context mContext = null;
    private String mCurrentPackageName = null;
    private int mGalleryLazyWorking = 0;
    private HwGalleryCacheNative mHwGalleryCacheNative = null;
    private boolean mIsEffect = false;
    private HwKVDatabase mKVvDatabase = null;
    private DecoderThread mLastThread = null;
    private DecoderThread mNextThread = null;
    private XmlPullParser mParser = null;
    private String mPath = null;

    /* access modifiers changed from: private */
    public static class AppData {
        public String mAppName;
        public String mSupportVersion;
        public String mThreadName;

        public AppData() {
        }

        public AppData(String name, String supportVersion, String threadName) {
            this.mAppName = name;
            this.mSupportVersion = supportVersion;
            this.mThreadName = threadName;
        }
    }

    public HwGalleryCacheManagerImpl() {
        String str;
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false)) {
            this.mContext = ActivityThread.currentApplication();
            this.mCurrentPackageName = ActivityThread.currentPackageName();
            if (this.mContext != null && (str = this.mCurrentPackageName) != null && !str.isEmpty()) {
                if ("com.tencent.mm".equals(this.mCurrentPackageName)) {
                    this.mParser = Xml.newPullParser();
                    if (isSwitchEnabled() && loadConfigFile()) {
                        this.mHwGalleryCacheNative = new HwGalleryCacheNative();
                        this.mKVvDatabase = HwKVDatabase.getInstance(this.mContext);
                        this.mIsEffect = true;
                    }
                }
                Log.d(TAG, "mIsEffect:" + this.mIsEffect);
            }
        }
    }

    private File getFile(String fileName) {
        return new File(fileName);
    }

    private boolean isSwitchEnabled() {
        File file = getFile(SWITCH_FILEPATH);
        if (!file.exists() || this.mParser == null) {
            return false;
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(file);
            this.mParser.setInput(is2, StandardCharsets.UTF_8.name());
            int outerDepth = this.mParser.getDepth();
            while (true) {
                int type = this.mParser.next();
                if (type == 1 || (type == 3 && this.mParser.getDepth() <= outerDepth)) {
                    try {
                        is2.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close file input stream fail!");
                    }
                    return false;
                } else if (type != 3) {
                    if (type != 4) {
                        if (XML_TAG_SWITCH.equals(this.mParser.getName())) {
                            if (Integer.parseInt(this.mParser.nextText()) == 1) {
                                try {
                                    is2.close();
                                } catch (IOException e2) {
                                    Log.e(TAG, "close file input stream fail!");
                                }
                                return true;
                            }
                            try {
                                is2.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                            return false;
                        }
                    }
                }
            }
        } catch (XmlPullParserException e4) {
            Log.e(TAG, "failed parsing config file parser error");
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e5) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            return false;
        } catch (IOException e6) {
            Log.e(TAG, "failed parsing config file IO error ");
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e7) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            return false;
        } catch (NumberFormatException e8) {
            Log.e(TAG, "switch number format error");
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e9) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e10) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            throw th;
        }
    }

    private boolean loadConfigFile() {
        File file = getFile(CONFIG_FILEPATH);
        if (!file.exists() || this.mParser == null) {
            return false;
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(file);
            this.mParser.setInput(is2, StandardCharsets.UTF_8.name());
            int outerDepth = this.mParser.getDepth();
            while (true) {
                int type = this.mParser.next();
                if (type == 1 || (type == 3 && this.mParser.getDepth() <= outerDepth)) {
                    try {
                        is2.close();
                    } catch (IOException e) {
                        Log.e(TAG, "close file input stream fail!");
                    }
                    return false;
                } else if (type != 3) {
                    if (type != 4) {
                        if (XML_TAG_CONFIG.equals(this.mParser.getName()) && TEXT_NAME.equals(this.mParser.getAttributeValue(null, ATTR_NAME))) {
                            boolean appOptimized = false;
                            if (checkAppListFromXml(this.mParser)) {
                                appOptimized = true;
                            }
                            try {
                                is2.close();
                            } catch (IOException e2) {
                                Log.e(TAG, "close file input stream fail!");
                            }
                            return appOptimized;
                        }
                    }
                }
            }
        } catch (XmlPullParserException e3) {
            Log.e(TAG, "failed parsing config file parser error");
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e4) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            return false;
        } catch (IOException e5) {
            Log.e(TAG, "failed parsing config file IO error ");
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e6) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            return false;
        } catch (NumberFormatException e7) {
            Log.e(TAG, "config number format error");
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e8) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e9) {
                    Log.e(TAG, "close file input stream fail!");
                }
            }
            throw th;
        }
    }

    private boolean checkAppListFromXml(XmlPullParser parser) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return false;
            }
            if (!(type == 3 || type == 4 || !XML_TAG_ITEM.equals(parser.getName()))) {
                this.mAppData = new AppData();
                readAppDataFromXml(parser, this.mAppData);
                if (!(this.mAppData.mAppName == null || this.mAppData.mSupportVersion == null || !this.mAppData.mAppName.equals(this.mCurrentPackageName))) {
                    return isWechatVersionSupport(this.mAppData.mAppName, this.mAppData.mSupportVersion);
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
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private String getFilePath(FileDescriptor fd) {
        HwGalleryCacheNative hwGalleryCacheNative = this.mHwGalleryCacheNative;
        if (hwGalleryCacheNative == null) {
            return null;
        }
        return hwGalleryCacheNative.getFilePath(fd);
    }

    private String getFileID(FileDescriptor fd) {
        HwGalleryCacheNative hwGalleryCacheNative = this.mHwGalleryCacheNative;
        if (hwGalleryCacheNative == null) {
            return null;
        }
        return hwGalleryCacheNative.getFileID(fd);
    }

    private Bitmap getImageCache(FileDescriptor fd, BitmapFactory.Options options) {
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

    private Bitmap getImageCache(InputStream is, BitmapFactory.Options options) {
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
        AppData appData = this.mAppData;
        if (appData == null || !currentThreadName.equals(appData.mThreadName)) {
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
        Bitmap newBm;
        Rect dst;
        int srcWidth = bm.getWidth();
        int srcHeight = bm.getHeight();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, opts);
        int dstWidth = opts.outWidth;
        int dstHeight = opts.outHeight;
        Log.d(TAG, "Resize Cache(" + srcWidth + "x" + srcHeight + ") to (" + dstWidth + "x" + dstHeight + ")==> " + sampleSize);
        if (sampleSize > 0) {
            dstWidth /= sampleSize;
            dstHeight /= sampleSize;
        }
        if (dstWidth <= 0 || dstHeight <= 0 || (newBm = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)) == null) {
            return bm;
        }
        Canvas canvas = new Canvas(newBm);
        Rect src = new Rect(0, 0, srcWidth, srcHeight);
        if (dstWidth > dstHeight) {
            int diff = (dstWidth - dstHeight) / 2;
            dst = new Rect(diff, 0, dstWidth - diff, dstHeight);
        } else {
            int diff2 = (dstHeight - dstWidth) / 2;
            dst = new Rect(0, diff2, dstWidth, dstHeight - diff2);
        }
        canvas.drawBitmap(bm, src, dst, (Paint) null);
        canvas.save();
        canvas.restore();
        bm.recycle();
        Log.d(TAG, "Resize " + src + " to " + dst);
        return newBm;
    }

    private Bitmap getExifThumbnail(InputStream is) {
        try {
            byte[] thumbData = new ExifInterface(is).getThumbnail();
            is.reset();
            if (thumbData != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, options);
                if (options.outWidth >= 256) {
                    if (options.outHeight >= 256) {
                        options.inJustDecodeBounds = false;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, options);
                        if (bitmap != null) {
                            return bitmap;
                        }
                    }
                }
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "getExifThumbnail fail!");
        }
        return null;
    }

    private synchronized Bitmap getGalleryCachedImageInner(InputStream is, BitmapFactory.Options opts) {
        if (opts != null) {
            try {
                if (!opts.inJustDecodeBounds && opts.inSampleSize > 2) {
                }
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        BitmapFactory.Options myOptions = null;
        int i = 0;
        if (opts != null) {
            myOptions = new BitmapFactory.Options();
            myOptions.inSampleSize = opts.inSampleSize;
            opts.inSampleSize = 0;
        }
        Bitmap bm = getImageCache(is, opts);
        if (bm != null) {
            Log.d(TAG, "Thumb from gallery: " + bm.getWidth() + " x " + bm.getHeight());
            if (myOptions != null) {
                i = myOptions.inSampleSize;
            }
            Bitmap bm2 = resizeToWechat(is, bm, i);
            bm2.mGalleryCached = true;
            Bitmap.GalleryCacheInfo cache = new Bitmap.GalleryCacheInfo();
            cache.setOptions(myOptions);
            cache.setPath(this.mPath);
            bm2.mCacheInfo = cache;
            addCacheToTail(cache);
            return bm2;
        }
        Bitmap bm3 = getExifThumbnail(is);
        if (bm3 != null) {
            bm3.mGalleryCached = true;
            return bm3;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static class DecoderThread extends Thread {
        private static final int MSG_STOP = 1;
        private static final int MSG_TASK = 0;
        private Handler mHandler = null;
        private final Object mLock = new Object();
        public boolean mStopped = false;

        public DecoderThread(String threadName) {
            super(threadName);
        }

        public void run() {
            Looper.prepare();
            synchronized (this.mLock) {
                this.mHandler = new Handler() {
                    /* class huawei.android.hwgallerycache.HwGalleryCacheManagerImpl.DecoderThread.AnonymousClass1 */

                    public void handleMessage(Message msg) {
                        Bitmap wechatThumb;
                        if (msg.what == 0) {
                            Bitmap.GalleryCacheInfo cache = (Bitmap.GalleryCacheInfo) msg.obj;
                            if (HwGalleryCacheManagerImpl.needDecode(cache)) {
                                synchronized (cache) {
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

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void decodeAsync(Bitmap.GalleryCacheInfo cache) {
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

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
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

    private void addCacheToTail(Bitmap.GalleryCacheInfo cache) {
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

    private void removeCache(Bitmap.GalleryCacheInfo cache) {
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
                cache.setNext((Bitmap.GalleryCacheInfo) null);
                cache.setLast((Bitmap.GalleryCacheInfo) null);
                cache.setWechatThumb((Bitmap) null);
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean needDecode(Bitmap.GalleryCacheInfo cache) {
        if (cache == null || cache.getPath() == null || cache.getIsDecoding() || cache.getWechatThumb() != null) {
            return false;
        }
        return true;
    }

    private void decodeInLastThread(Bitmap.GalleryCacheInfo cache) {
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

    private void decodeInNextThread(Bitmap.GalleryCacheInfo cache) {
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

    private static class ThumbThread extends Thread {
        private final Bitmap.GalleryCacheInfo mCache;
        private Handler mHandler = null;
        private ImageView mImageView = null;

        ThumbThread(Handler handler, Bitmap.GalleryCacheInfo cache, ImageView view) {
            this.mHandler = handler;
            this.mCache = cache;
            this.mImageView = view;
        }

        public void run() {
            Bitmap wechatThumb;
            Bitmap.GalleryCacheInfo galleryCacheInfo = this.mCache;
            if (galleryCacheInfo != null) {
                if (HwGalleryCacheManagerImpl.needDecode(galleryCacheInfo)) {
                    synchronized (this.mCache) {
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
                        /* class huawei.android.hwgallerycache.HwGalleryCacheManagerImpl.ThumbThread.AnonymousClass1 */

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

    public boolean isGalleryCacheEffect() {
        return this.mIsEffect;
    }

    public Bitmap getGalleryCachedImage(InputStream is, BitmapFactory.Options opts) {
        if (is == null) {
            return null;
        }
        if (isGalleryLazyThread()) {
            this.mGalleryLazyWorking = 6;
        } else if (isGalleryThread()) {
            int i = this.mGalleryLazyWorking;
            if (i <= 0) {
                return getGalleryCachedImageInner(is, opts);
            }
            this.mGalleryLazyWorking = i - 1;
        }
        return null;
    }

    public void recycleCacheInfo(Bitmap.GalleryCacheInfo cache) {
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
        Bitmap.GalleryCacheInfo cache = bm.mCacheInfo;
        if (!(cache.getNext() == null || cache.getNext().getNext() == null)) {
            decodeInNextThread(cache.getNext().getNext());
        }
        if (!(cache.getLast() == null || cache.getLast().getLast() == null)) {
            decodeInLastThread(cache.getLast().getLast());
        }
        if (cache.getWechatThumb() != null) {
            Log.d(TAG, "Wechat thumb is ready, replace with this one!");
            view.setImageBitmap(cache.getWechatThumb());
            return true;
        }
        new ThumbThread(new Handler(), cache, view).start();
        return true;
    }

    public Bitmap getGalleryCachedVideo(int rowid, long timeModified, BitmapFactory.Options opts) {
        if (this.mKVvDatabase == null || timeModified <= 0) {
            return null;
        }
        long key = HwKVDatabase.generateKey(rowid, timeModified, 3, 1);
        if (this.mKVvDatabase.hasKey(key)) {
            return this.mKVvDatabase.getBitmap(key, opts);
        }
        Log.w(TAG, "wechatopti getGalleryCachedVideo no key in kvdb!");
        return null;
    }
}
