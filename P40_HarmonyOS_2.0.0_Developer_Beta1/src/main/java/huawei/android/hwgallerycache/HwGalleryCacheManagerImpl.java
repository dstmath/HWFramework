package huawei.android.hwgallerycache;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hwgallerycache.HwGalleryCacheManagerDummy;
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

public class HwGalleryCacheManagerImpl extends HwGalleryCacheManagerDummy {
    private static final String ATTR_NAME = "name";
    private static final String CONFIG_FILE_PATH = "/data/app_acc/app_config.xml";
    private static final boolean DEBUG = true;
    private static final Object DECODER_LOCK = new Object();
    private static final String SWITCH_FILE_PATH = "/data/app_acc/app_switch.xml";
    private static final String TAG = "HwGalleryCacheManagerImpl";
    private static final String TEXT_NAME = "CacheList";
    private static final int TYPE_MICRO_THUMBNAIL = 2;
    private static final int TYPE_MICRO_THUMBNAIL_SCALE = 1;
    private static final String XML_TAG_APP_NAME = "packageName";
    private static final String XML_TAG_CONFIG = "config";
    private static final String XML_TAG_ITEM = "item";
    private static final String XML_TAG_SWITCH = "switch";
    private static final String XML_TAG_THREAD_NAME = "threadName";
    private static final String XML_TAG_VERSION = "supportVersion";
    private AppData mAppData = null;
    private final Object mCacheLock = new Object();
    private Bitmap.GalleryCacheInfo mCacheTail = null;
    private String mCurrentPackageName = null;
    private int mGalleryLazyWorking = 0;
    private final Object mGalleryLock = new Object();
    private HwGalleryCacheNative mHwGalleryCacheNative = null;
    private boolean mIsEffect = false;
    private HwKVDatabase mKvDatabase = null;
    private DecoderThread mLastThread = null;
    private DecoderThread mNextThread = null;
    private XmlPullParser mParser = null;
    private String mPath = null;

    /* access modifiers changed from: private */
    public static class AppData {
        String mAppName;
        String mSupportVersion;
        String mThreadName;

        AppData() {
        }
    }

    public HwGalleryCacheManagerImpl() {
        String str;
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false)) {
            Context context = ActivityThread.currentApplication();
            this.mCurrentPackageName = ActivityThread.currentPackageName();
            if (context != null && (str = this.mCurrentPackageName) != null && !str.isEmpty()) {
                if ("com.tencent.mm".equals(this.mCurrentPackageName)) {
                    this.mParser = Xml.newPullParser();
                    if (isSwitchEnabled() && loadConfigFile()) {
                        this.mHwGalleryCacheNative = new HwGalleryCacheNative();
                        this.mKvDatabase = HwKVDatabase.getInstance(context);
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

    private boolean hasNext(int type, int outerDepth) {
        if (type == 1 || (type == 3 && this.mParser.getDepth() <= outerDepth)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        android.util.Log.e(huawei.android.hwgallerycache.HwGalleryCacheManagerImpl.TAG, "failed parsing config file");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007d, code lost:
        closeStream(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0081, code lost:
        return false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0075 A[ExcHandler: IOException | NumberFormatException | XmlPullParserException (e java.lang.Throwable), Splitter:B:18:0x005e] */
    private boolean isSwitchEnabled() {
        File file = getFile(SWITCH_FILE_PATH);
        boolean z = false;
        if (!file.exists() || this.mParser == null) {
            return false;
        }
        InputStream is = new FileInputStream(file);
        this.mParser.setInput(is, StandardCharsets.UTF_8.name());
        int outerDepth = this.mParser.getDepth();
        int type = this.mParser.next();
        while (hasNext(type, outerDepth)) {
            if (type == 3 || type == 4) {
                type = this.mParser.next();
            } else if (XML_TAG_SWITCH.equals(this.mParser.getName())) {
                if (Integer.parseInt(this.mParser.nextText()) == 1) {
                    z = true;
                }
                closeStream(is);
                return z;
            } else {
                try {
                    type = this.mParser.next();
                } catch (IOException | NumberFormatException | XmlPullParserException e) {
                } catch (Throwable th) {
                    closeStream(is);
                    throw th;
                }
            }
        }
        closeStream(is);
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        android.util.Log.e(huawei.android.hwgallerycache.HwGalleryCacheManagerImpl.TAG, "failed parsing config file parser error");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0086, code lost:
        closeStream(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x008a, code lost:
        return false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x007e A[ExcHandler: IOException | NumberFormatException | XmlPullParserException (e java.lang.Throwable), Splitter:B:17:0x0067] */
    private boolean loadConfigFile() {
        File file = getFile(CONFIG_FILE_PATH);
        if (!file.exists() || this.mParser == null) {
            return false;
        }
        InputStream is = new FileInputStream(file);
        this.mParser.setInput(is, StandardCharsets.UTF_8.name());
        int outerDepth = this.mParser.getDepth();
        int type = this.mParser.next();
        while (hasNext(type, outerDepth)) {
            if (type == 3 || type == 4) {
                type = this.mParser.next();
            } else if (!XML_TAG_CONFIG.equals(this.mParser.getName()) || !TEXT_NAME.equals(this.mParser.getAttributeValue(null, ATTR_NAME))) {
                try {
                    type = this.mParser.next();
                } catch (IOException | NumberFormatException | XmlPullParserException e) {
                } catch (Throwable th) {
                    closeStream(is);
                    throw th;
                }
            } else {
                boolean checkAppListFromXml = checkAppListFromXml(this.mParser);
                closeStream(is);
                return checkAppListFromXml;
            }
        }
        closeStream(is);
        return false;
    }

    private void closeStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "close file input stream fail!");
            }
        }
    }

    private boolean checkAppListFromXml(XmlPullParser parser) throws XmlPullParserException, IOException, NumberFormatException {
        int outerDepth = parser.getDepth();
        int type = parser.next();
        while (hasNext(type, outerDepth)) {
            if (type == 3 || type == 4) {
                type = parser.next();
            } else {
                if (XML_TAG_ITEM.equals(parser.getName())) {
                    this.mAppData = new AppData();
                    readAppDataFromXml(parser, this.mAppData);
                    if (this.mAppData.mAppName == null || this.mAppData.mSupportVersion == null) {
                        type = parser.next();
                    } else if (this.mAppData.mAppName.equals(this.mCurrentPackageName)) {
                        return isWeChatVersionSupport(this.mAppData.mAppName, this.mAppData.mSupportVersion);
                    }
                }
                type = parser.next();
            }
        }
        return false;
    }

    private void readAppDataFromXml(XmlPullParser parser, AppData appData) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type = parser.next();
        while (hasNext(type, outerDepth)) {
            if (type == 3 || type == 4) {
                type = parser.next();
            } else {
                String tag = parser.getName();
                if (XML_TAG_APP_NAME.equals(tag)) {
                    appData.mAppName = parser.nextText();
                } else if (XML_TAG_VERSION.equals(tag)) {
                    appData.mSupportVersion = parser.nextText();
                } else if (XML_TAG_THREAD_NAME.equals(tag)) {
                    appData.mThreadName = parser.nextText();
                } else {
                    Log.w(TAG, "Unknown tag:" + tag);
                }
                type = parser.next();
            }
        }
    }

    private boolean isWeChatVersionSupport(String appName, String supportVersion) {
        Context context = ActivityThread.currentApplication();
        if (context == null) {
            return false;
        }
        try {
            int currentVersionCode = context.getPackageManager().getPackageInfo(appName, 0).versionCode;
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

    private String getFileId(FileDescriptor fd) {
        HwGalleryCacheNative hwGalleryCacheNative = this.mHwGalleryCacheNative;
        if (hwGalleryCacheNative == null) {
            return null;
        }
        return hwGalleryCacheNative.getFileId(fd);
    }

    private Bitmap getImageCache(FileDescriptor fd, BitmapFactory.Options options) {
        if (fd == null || this.mKvDatabase == null) {
            return null;
        }
        String path = getFilePath(fd);
        if (path == null || path.length() < 1) {
            Log.e(TAG, "Can't get path from fd!");
            return null;
        }
        String id = getFileId(fd);
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
            return this.mKvDatabase.getBitmap(HwKVDatabase.generateKey(rowId, timeModified, 1, 2), options);
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
        String currentThreadName = Thread.currentThread().getName();
        if (currentThreadName == null || !currentThreadName.contains("album-image-gallery-lazy-loader")) {
            return false;
        }
        return true;
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

    private Bitmap getGalleryCachedImageInner(InputStream is, BitmapFactory.Options opts) {
        synchronized (this.mGalleryLock) {
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
    }

    /* access modifiers changed from: private */
    public static class DecoderThread extends Thread {
        private static final int MSG_STOP = 1;
        private static final int MSG_TASK = 0;
        private boolean mCanStopped = false;
        private Handler mHandler = null;
        private final Object mLock = new Object();

        DecoderThread(String threadName) {
            super(threadName);
        }

        /* access modifiers changed from: package-private */
        public boolean isStopped() {
            return this.mCanStopped;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Looper.prepare();
            synchronized (this.mLock) {
                initHandler();
                this.mLock.notifyAll();
            }
            Looper.loop();
        }

        private void initHandler() {
            this.mHandler = new Handler() {
                /* class huawei.android.hwgallerycache.HwGalleryCacheManagerImpl.DecoderThread.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    int i = msg.what;
                    if (i != 0) {
                        if (i == 1) {
                            DecoderThread.this.handleMsgStop();
                        }
                    } else if (msg.obj instanceof Bitmap.GalleryCacheInfo) {
                        DecoderThread.this.handleMsgTask(msg);
                    }
                }
            };
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleMsgTask(Message msg) {
            Bitmap weChatThumb;
            Bitmap.GalleryCacheInfo cache = (Bitmap.GalleryCacheInfo) msg.obj;
            if (HwGalleryCacheManagerImpl.needDecode(cache)) {
                synchronized (cache) {
                    cache.setIsDecoding(true);
                    Bitmap bm = BitmapFactory.decodeFile(cache.getPath(), cache.getOptions());
                    if (bm == null || cache.getMatrix() == null) {
                        weChatThumb = bm;
                    } else {
                        weChatThumb = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), cache.getMatrix(), cache.getFilter());
                    }
                    if (weChatThumb != null) {
                        cache.setWechatThumb(weChatThumb);
                    }
                    cache.setIsDecoding(false);
                    cache.notifyAll();
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleMsgStop() {
            synchronized (HwGalleryCacheManagerImpl.DECODER_LOCK) {
                if (this.mHandler.hasMessages(0)) {
                    resetTimer();
                } else {
                    this.mCanStopped = true;
                    Looper.myLooper().quit();
                }
            }
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
            synchronized (DECODER_LOCK) {
                if (this.mLastThread == null || this.mLastThread.isStopped()) {
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
            synchronized (DECODER_LOCK) {
                if (this.mNextThread == null || this.mNextThread.isStopped()) {
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

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Bitmap.GalleryCacheInfo galleryCacheInfo = this.mCache;
            if (galleryCacheInfo != null) {
                if (HwGalleryCacheManagerImpl.needDecode(galleryCacheInfo)) {
                    decodeBitmap();
                } else {
                    waitDecodeBitmap();
                }
                updateImageBitmap();
            }
        }

        private void decodeBitmap() {
            Bitmap weChatThumb;
            synchronized (this.mCache) {
                this.mCache.setIsDecoding(true);
                Bitmap bm = BitmapFactory.decodeFile(this.mCache.getPath(), this.mCache.getOptions());
                if (bm == null || this.mCache.getMatrix() == null) {
                    weChatThumb = bm;
                } else {
                    weChatThumb = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), this.mCache.getMatrix(), this.mCache.getFilter());
                }
                if (weChatThumb != null) {
                    this.mCache.setWechatThumb(weChatThumb);
                }
                this.mCache.setIsDecoding(false);
                this.mCache.notifyAll();
            }
        }

        private void waitDecodeBitmap() {
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

        private void updateImageBitmap() {
            if (this.mCache.getWechatThumb() != null) {
                this.mHandler.post(new Runnable() {
                    /* class huawei.android.hwgallerycache.HwGalleryCacheManagerImpl.ThumbThread.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        if (ThumbThread.this.mImageView.mInBigView == 1) {
                            ThumbThread.this.mImageView.setImageBitmap(ThumbThread.this.mCache.getWechatThumb());
                        }
                    }
                });
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
        if (!bm.mGalleryCached || (bm.mCacheInfo == null && view.mInBigView == 1)) {
            view.mInBigView = 2;
            return false;
        }
        if (view.mInBigView == 2) {
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
        Bitmap.GalleryCacheInfo cache;
        if (!isNeedRevert(view, bm) || (cache = bm.mCacheInfo) == null) {
            return false;
        }
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

    public Bitmap getGalleryCachedVideo(int rowId, long timeModified, BitmapFactory.Options opts) {
        if (this.mKvDatabase == null || timeModified <= 0) {
            return null;
        }
        long key = HwKVDatabase.generateKey(rowId, timeModified, 3, 1);
        if (this.mKvDatabase.hasKey(key)) {
            return this.mKvDatabase.getBitmap(key, opts);
        }
        Log.w(TAG, "wechatopti getGalleryCachedVideo no key in kvdb!");
        return null;
    }
}
