package android.hwgallerycache;

import android.common.HwFrameworkFactory;
import android.graphics.Bitmap;
import android.graphics.Bitmap.GalleryCacheInfo;
import android.graphics.BitmapFactory.Options;
import android.os.Process;
import android.util.Log;
import android.widget.ImageView;
import java.io.InputStream;

public class HwGalleryCacheManager {
    private static final String TAG = "HwGalleryCacheManager";
    private static IHwGalleryCacheManager sInstance;

    public interface IHwGalleryCacheManager {
        Bitmap getGalleryCachedImage(InputStream inputStream, Options options);

        boolean isGalleryCacheEffect();

        void recycleCacheInfo(GalleryCacheInfo galleryCacheInfo);

        boolean revertWechatThumb(ImageView imageView, Bitmap bitmap);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hwgallerycache.HwGalleryCacheManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hwgallerycache.HwGalleryCacheManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hwgallerycache.HwGalleryCacheManager.<clinit>():void");
    }

    private static IHwGalleryCacheManager getImplObject() {
        if (sInstance != null) {
            return sInstance;
        }
        if (Process.myPpid() == 1) {
            return null;
        }
        IHwGalleryCacheManager instance = null;
        IHwGalleryCacheManagerFactory obj = HwFrameworkFactory.getHwGalleryCacheManagerFactory();
        if (obj != null) {
            instance = obj.getGalleryCacheManagerInstance();
        }
        if (instance != null) {
            sInstance = instance;
        } else {
            Log.w(TAG, "can't get impl object from vendor, use default implemention");
            sInstance = new HwGalleryCacheManagerDummy();
        }
        return sInstance;
    }

    public static Bitmap getGalleryCachedImage(InputStream is, Options opts) {
        if (getImplObject() != null) {
            return getImplObject().getGalleryCachedImage(is, opts);
        }
        return null;
    }

    public static boolean isGalleryCacheEffect() {
        if (getImplObject() != null) {
            return getImplObject().isGalleryCacheEffect();
        }
        return false;
    }

    public static void recycleCacheInfo(GalleryCacheInfo cache) {
        if (getImplObject() != null) {
            getImplObject().recycleCacheInfo(cache);
        }
    }

    public static boolean revertWechatThumb(ImageView view, Bitmap bm) {
        if (getImplObject() != null) {
            return getImplObject().revertWechatThumb(view, bm);
        }
        return false;
    }
}
