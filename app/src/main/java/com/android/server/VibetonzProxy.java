package com.android.server;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.android.server.wifipro.WifiProCHRManager;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class VibetonzProxy {
    private static String TAG;
    private static String apkPath;
    private static String dexOutputDir;
    private static DexClassLoader mClassLoader;
    private static boolean mloadedVibetonz;
    private IVibetonzImpl mVibetonzImpl;

    public interface IVibetonzImpl {
        boolean hasHaptic(Context context, Uri uri);

        boolean isPlaying(String str);

        void pausePlayEffect(String str);

        void playIvtEffect(String str);

        void resumePausedEffect(String str);

        boolean startHaptic(Context context, int i, int i2, Uri uri);

        void stopHaptic();

        void stopPlayEffect();
    }

    public static class VibetonzReflactCall implements IVibetonzImpl {
        private Class<?> mClazz_RingtoneVibetonzImpl;
        private Class<?> mClazz_vibetonzImpl;
        private Object mObject_RingtoneVibetonzImpl;
        private Object mObject_vibetonzImpl;

        private VibetonzReflactCall() {
            try {
                this.mClazz_vibetonzImpl = VibetonzProxy.mClassLoader.loadClass("com.immersion.VibetonzImpl");
                this.mObject_vibetonzImpl = this.mClazz_vibetonzImpl.getMethod(WifiProCHRManager.LOG_GET_INSTANCE_API_NAME, new Class[0]).invoke(null, new Object[0]);
                this.mClazz_RingtoneVibetonzImpl = VibetonzProxy.mClassLoader.loadClass("com.immersion.RingtoneVibetonzImpl");
                this.mObject_RingtoneVibetonzImpl = this.mClazz_RingtoneVibetonzImpl.getMethod(WifiProCHRManager.LOG_GET_INSTANCE_API_NAME, new Class[0]).invoke(null, new Object[0]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
            } catch (IllegalArgumentException e4) {
                e4.printStackTrace();
            } catch (InvocationTargetException e5) {
                e5.printStackTrace();
            } catch (RuntimeException e6) {
                e6.printStackTrace();
            } catch (Exception e7) {
                e7.printStackTrace();
            }
        }

        public void playIvtEffect(String effectName) {
            if (this.mClazz_vibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "playIvtEffect can not found the class!");
                return;
            }
            Log.v(VibetonzProxy.TAG, "playIvtEffect===================");
            try {
                this.mClazz_vibetonzImpl.getMethod("playIvtEffect", new Class[]{String.class}).invoke(this.mObject_vibetonzImpl, new Object[]{effectName});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            } catch (RuntimeException e5) {
                e5.printStackTrace();
            } catch (Exception e6) {
                e6.printStackTrace();
            }
        }

        public void stopPlayEffect() {
            if (this.mClazz_vibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "stopPlayEffect can not found the class!");
                return;
            }
            Log.v(VibetonzProxy.TAG, "stopPlayEffect===================");
            try {
                this.mClazz_vibetonzImpl.getMethod("stopPlayEffect", new Class[0]).invoke(this.mObject_vibetonzImpl, new Object[0]);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            } catch (RuntimeException e5) {
                e5.printStackTrace();
            } catch (Exception e6) {
                e6.printStackTrace();
            }
        }

        public void pausePlayEffect(String effectName) {
            if (this.mClazz_vibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "pausePlayEffect can not found the class!");
                return;
            }
            Log.v(VibetonzProxy.TAG, "pausePlayEffect===================");
            try {
                this.mClazz_vibetonzImpl.getMethod("pausePlayEffect", new Class[]{String.class}).invoke(this.mObject_vibetonzImpl, new Object[]{effectName});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            } catch (RuntimeException e5) {
                e5.printStackTrace();
            } catch (Exception e6) {
                e6.printStackTrace();
            }
        }

        public void resumePausedEffect(String effectName) {
            if (this.mClazz_vibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "resumePausedEffect can not found the class!");
                return;
            }
            Log.v(VibetonzProxy.TAG, "resumePausedEffect===================");
            try {
                this.mClazz_vibetonzImpl.getMethod("resumePausedEffect", new Class[]{String.class}).invoke(this.mObject_vibetonzImpl, new Object[]{effectName});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            } catch (RuntimeException e5) {
                e5.printStackTrace();
            } catch (Exception e6) {
                e6.printStackTrace();
            }
        }

        public boolean isPlaying(String effectName) {
            if (this.mClazz_vibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "isPlaying can not found the class!");
                return false;
            }
            Log.v(VibetonzProxy.TAG, "isPlaying===================");
            try {
                return Boolean.parseBoolean(this.mClazz_vibetonzImpl.getMethod("isPlaying", new Class[]{String.class}).invoke(this.mObject_vibetonzImpl, new Object[]{effectName}).toString());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
                return false;
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
                return false;
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
                return false;
            } catch (RuntimeException e5) {
                e5.printStackTrace();
                return false;
            } catch (Exception e6) {
                e6.printStackTrace();
                return false;
            }
        }

        public boolean startHaptic(Context mContext, int callerID, int ringtoneType, Uri uri) {
            if (this.mClazz_RingtoneVibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "startHaptic can not found the class!");
                return false;
            }
            Log.v(VibetonzProxy.TAG, "startHaptic===================");
            try {
                return Boolean.parseBoolean(this.mClazz_RingtoneVibetonzImpl.getMethod("startHaptic", new Class[]{Context.class, Integer.TYPE, Integer.TYPE, Uri.class}).invoke(this.mObject_RingtoneVibetonzImpl, new Object[]{mContext, Integer.valueOf(callerID), Integer.valueOf(ringtoneType), uri}).toString());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
                return false;
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
                return false;
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
                return false;
            } catch (RuntimeException e5) {
                e5.printStackTrace();
                return false;
            } catch (Exception e6) {
                e6.printStackTrace();
                return false;
            }
        }

        public boolean hasHaptic(Context mContext, Uri uri) {
            if (this.mClazz_RingtoneVibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "hasHaptic can not found the class!");
                return false;
            }
            Log.v(VibetonzProxy.TAG, "hasHaptic===================");
            try {
                return Boolean.parseBoolean(this.mClazz_RingtoneVibetonzImpl.getMethod("hasHaptic", new Class[]{Context.class, Uri.class}).invoke(this.mObject_RingtoneVibetonzImpl, new Object[]{mContext, uri}).toString());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return false;
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
                return false;
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
                return false;
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
                return false;
            } catch (RuntimeException e5) {
                e5.printStackTrace();
                return false;
            } catch (Exception e6) {
                e6.printStackTrace();
                return false;
            }
        }

        public void stopHaptic() {
            if (this.mClazz_RingtoneVibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "stopHaptic can not found the class!");
                return;
            }
            Log.v(VibetonzProxy.TAG, "stopHaptic===================");
            try {
                this.mClazz_RingtoneVibetonzImpl.getMethod("stopHaptic", new Class[0]).invoke(this.mObject_RingtoneVibetonzImpl, new Object[0]);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            } catch (RuntimeException e5) {
                e5.printStackTrace();
            } catch (Exception e6) {
                e6.printStackTrace();
            }
        }
    }

    public static class VibetonzStub implements IVibetonzImpl {
        private VibetonzStub() {
        }

        public void playIvtEffect(String effectName) {
            Log.e(VibetonzProxy.TAG, "playIvtEffect called while not implement!");
        }

        public void stopPlayEffect() {
            Log.e(VibetonzProxy.TAG, "stopPlayEffect called while not implement!");
        }

        public void pausePlayEffect(String effectName) {
            Log.e(VibetonzProxy.TAG, "pausePlayEffect called while not implement!");
        }

        public void resumePausedEffect(String effectName) {
            Log.e(VibetonzProxy.TAG, "resumePausedEffect called while not implement!");
        }

        public boolean isPlaying(String effectName) {
            Log.e(VibetonzProxy.TAG, "isPlaying called while not implement!");
            return false;
        }

        public boolean startHaptic(Context mContext, int callerID, int ringtoneType, Uri uri) {
            Log.e(VibetonzProxy.TAG, "startHaptic called while not implement!");
            return false;
        }

        public boolean hasHaptic(Context mContext, Uri uri) {
            Log.e(VibetonzProxy.TAG, "hasHaptic called while not implement!");
            return false;
        }

        public void stopHaptic() {
            Log.e(VibetonzProxy.TAG, "stopHaptic called while not implement!");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.VibetonzProxy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.VibetonzProxy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.VibetonzProxy.<clinit>():void");
    }

    public VibetonzProxy() {
        this.mVibetonzImpl = null;
    }

    public static void initVibetonzImpl() {
        boolean z = false;
        File dir = new File(dexOutputDir);
        if (!(dir.exists() || dir.mkdirs())) {
            Log.e(TAG, " DIR NOT EXISTS====create dir fail====");
        }
        try {
            mClassLoader = new DexClassLoader(apkPath, dexOutputDir, null, ClassLoader.getSystemClassLoader());
            Class<?> clazz = null;
            try {
                clazz = mClassLoader.loadClass("com.immersion.Device");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (clazz != null) {
                z = true;
            }
            mloadedVibetonz = z;
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
            mloadedVibetonz = false;
        }
    }

    private static boolean isVibetonzAvailable() {
        return mloadedVibetonz;
    }

    public IVibetonzImpl getInstance() {
        if (this.mVibetonzImpl == null) {
            initVibetonzImpl();
            if (isVibetonzAvailable()) {
                Log.d(TAG, "will create VibetonzReflactCall");
                this.mVibetonzImpl = new VibetonzReflactCall();
            } else {
                Log.d(TAG, "will create VibetonzStub");
                this.mVibetonzImpl = new VibetonzStub();
            }
        }
        return this.mVibetonzImpl;
    }
}
