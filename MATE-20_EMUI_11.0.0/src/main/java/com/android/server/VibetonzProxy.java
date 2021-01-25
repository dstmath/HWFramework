package com.android.server;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import dalvik.system.DexClassLoader;
import java.lang.reflect.InvocationTargetException;

public class VibetonzProxy {
    private static String TAG = "VibetonzProxy";
    private static String apkPath = "/system/framework/immersion.jar";
    private static String dexOutputDir = "/data/data/com.immersion/";
    private static boolean mloadedVibetonz;
    private static DexClassLoader sClassLoader = null;
    private IVibetonzImpl mVibetonzImpl = null;

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

    public static void initVibetonzImpl() {
        String str = TAG;
        Log.w(str, "Never mkdir " + dexOutputDir + " before the storage is encrypted,it cause storage encryption failure!");
        boolean z = false;
        try {
            sClassLoader = new DexClassLoader(apkPath, dexOutputDir, null, ClassLoader.getSystemClassLoader());
            Class<?> clazz = null;
            try {
                clazz = sClassLoader.loadClass("com.immersion.Device");
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "initVibetonzImpl com.immersion.Device is not found");
            } catch (Exception e2) {
                String str2 = TAG;
                Log.e(str2, "initVibetonzImpl Exception : " + e2.getClass());
            }
            if (clazz != null) {
                z = true;
            }
            mloadedVibetonz = z;
        } catch (IllegalArgumentException e3) {
            String str3 = TAG;
            Log.e(str3, "initVibetonzImpl IllegalArgumentException : " + e3.getMessage());
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

    public static class VibetonzStub implements IVibetonzImpl {
        private VibetonzStub() {
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public void playIvtEffect(String effectName) {
            Log.e(VibetonzProxy.TAG, "playIvtEffect called while not implement!");
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public void stopPlayEffect() {
            Log.e(VibetonzProxy.TAG, "stopPlayEffect called while not implement!");
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public void pausePlayEffect(String effectName) {
            Log.e(VibetonzProxy.TAG, "pausePlayEffect called while not implement!");
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public void resumePausedEffect(String effectName) {
            Log.e(VibetonzProxy.TAG, "resumePausedEffect called while not implement!");
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public boolean isPlaying(String effectName) {
            Log.e(VibetonzProxy.TAG, "isPlaying called while not implement!");
            return false;
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public boolean startHaptic(Context context, int callerId, int ringtoneType, Uri uri) {
            Log.e(VibetonzProxy.TAG, "startHaptic called while not implement!");
            return false;
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public boolean hasHaptic(Context context, Uri uri) {
            Log.e(VibetonzProxy.TAG, "hasHaptic called while not implement!");
            return false;
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public void stopHaptic() {
            Log.e(VibetonzProxy.TAG, "stopHaptic called while not implement!");
        }
    }

    public static class VibetonzReflactCall implements IVibetonzImpl {
        private Class<?> mClazz_RingtoneVibetonzImpl;
        private Class<?> mClazz_vibetonzImpl;
        private Object mObject_RingtoneVibetonzImpl;
        private Object mObject_vibetonzImpl;

        private VibetonzReflactCall() {
            try {
                this.mClazz_vibetonzImpl = VibetonzProxy.sClassLoader.loadClass("com.immersion.VibetonzImpl");
                this.mObject_vibetonzImpl = this.mClazz_vibetonzImpl.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                this.mClazz_RingtoneVibetonzImpl = VibetonzProxy.sClassLoader.loadClass("com.immersion.RingtoneVibetonzImpl");
                this.mObject_RingtoneVibetonzImpl = this.mClazz_RingtoneVibetonzImpl.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            } catch (ClassNotFoundException e) {
                Log.e(VibetonzProxy.TAG, "VibetonzReflactCall com.immersion.RingtoneVibetonzImpl is not found");
            } catch (NoSuchMethodException e2) {
                Log.e(VibetonzProxy.TAG, "VibetonzReflactCall getInstance method is not found");
            } catch (IllegalAccessException e3) {
                String str = VibetonzProxy.TAG;
                Log.e(str, "VibetonzReflactCall IllegalAccessException : " + e3.getMessage());
            } catch (IllegalArgumentException e4) {
                String str2 = VibetonzProxy.TAG;
                Log.e(str2, "VibetonzReflactCall IllegalArgumentException : " + e4.getMessage());
            } catch (InvocationTargetException e5) {
                String str3 = VibetonzProxy.TAG;
                Log.e(str3, "VibetonzReflactCall InvocationTargetException : " + e5.getMessage());
            } catch (RuntimeException e6) {
                String str4 = VibetonzProxy.TAG;
                Log.e(str4, "VibetonzReflactCall RuntimeException : " + e6.getMessage());
            } catch (Exception e7) {
                String str5 = VibetonzProxy.TAG;
                Log.e(str5, "VibetonzReflactCall Exception : " + e7.getClass());
            }
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public void playIvtEffect(String effectName) {
            if (this.mClazz_vibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "playIvtEffect can not found the class!");
                return;
            }
            Log.v(VibetonzProxy.TAG, "playIvtEffect===================");
            try {
                this.mClazz_vibetonzImpl.getMethod("playIvtEffect", String.class).invoke(this.mObject_vibetonzImpl, effectName);
            } catch (NoSuchMethodException e) {
                Log.e(VibetonzProxy.TAG, "playIvtEffect method is not found");
            } catch (IllegalAccessException e2) {
                String str = VibetonzProxy.TAG;
                Log.e(str, "playIvtEffect IllegalAccessException : " + e2.getMessage());
            } catch (IllegalArgumentException e3) {
                String str2 = VibetonzProxy.TAG;
                Log.e(str2, "playIvtEffect IllegalArgumentException : " + e3.getMessage());
            } catch (InvocationTargetException e4) {
                String str3 = VibetonzProxy.TAG;
                Log.e(str3, "playIvtEffect InvocationTargetException : " + e4.getMessage());
            } catch (RuntimeException e5) {
                String str4 = VibetonzProxy.TAG;
                Log.e(str4, "playIvtEffect RuntimeException : " + e5.getMessage());
            } catch (Exception e6) {
                String str5 = VibetonzProxy.TAG;
                Log.e(str5, "playIvtEffect Exception : " + e6.getClass());
            }
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public void stopPlayEffect() {
            if (this.mClazz_vibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "stopPlayEffect can not found the class!");
                return;
            }
            Log.v(VibetonzProxy.TAG, "stopPlayEffect===================");
            try {
                this.mClazz_vibetonzImpl.getMethod("stopPlayEffect", new Class[0]).invoke(this.mObject_vibetonzImpl, new Object[0]);
            } catch (NoSuchMethodException e) {
                Log.e(VibetonzProxy.TAG, "stopPlayEffect method is not found");
            } catch (IllegalAccessException e2) {
                String str = VibetonzProxy.TAG;
                Log.e(str, "stopPlayEffect IllegalAccessException : " + e2.getMessage());
            } catch (IllegalArgumentException e3) {
                String str2 = VibetonzProxy.TAG;
                Log.e(str2, "stopPlayEffect IllegalArgumentException : " + e3.getMessage());
            } catch (InvocationTargetException e4) {
                String str3 = VibetonzProxy.TAG;
                Log.e(str3, "stopPlayEffect InvocationTargetException : " + e4.getMessage());
            } catch (RuntimeException e5) {
                String str4 = VibetonzProxy.TAG;
                Log.e(str4, "stopPlayEffect RuntimeException : " + e5.getMessage());
            } catch (Exception e6) {
                String str5 = VibetonzProxy.TAG;
                Log.e(str5, "stopPlayEffect RuntimeException : " + e6.getClass());
            }
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public void pausePlayEffect(String effectName) {
            if (this.mClazz_vibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "pausePlayEffect can not found the class!");
                return;
            }
            Log.v(VibetonzProxy.TAG, "pausePlayEffect===================");
            try {
                this.mClazz_vibetonzImpl.getMethod("pausePlayEffect", String.class).invoke(this.mObject_vibetonzImpl, effectName);
            } catch (NoSuchMethodException e) {
                Log.e(VibetonzProxy.TAG, "pausePlayEffect method is not found");
            } catch (IllegalAccessException e2) {
                String str = VibetonzProxy.TAG;
                Log.e(str, "pausePlayEffect IllegalAccessException : " + e2.getMessage());
            } catch (IllegalArgumentException e3) {
                String str2 = VibetonzProxy.TAG;
                Log.e(str2, "pausePlayEffect IllegalArgumentException : " + e3.getMessage());
            } catch (InvocationTargetException e4) {
                String str3 = VibetonzProxy.TAG;
                Log.e(str3, "pausePlayEffect InvocationTargetException : " + e4.getMessage());
            } catch (RuntimeException e5) {
                String str4 = VibetonzProxy.TAG;
                Log.e(str4, "pausePlayEffect RuntimeException : " + e5.getMessage());
            } catch (Exception e6) {
                String str5 = VibetonzProxy.TAG;
                Log.e(str5, "pausePlayEffect Exception : " + e6.getClass());
            }
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public void resumePausedEffect(String effectName) {
            if (this.mClazz_vibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "resumePausedEffect can not found the class!");
                return;
            }
            Log.v(VibetonzProxy.TAG, "resumePausedEffect===================");
            try {
                this.mClazz_vibetonzImpl.getMethod("resumePausedEffect", String.class).invoke(this.mObject_vibetonzImpl, effectName);
            } catch (NoSuchMethodException e) {
                Log.e(VibetonzProxy.TAG, "resumePausedEffect method is not found");
            } catch (IllegalAccessException e2) {
                String str = VibetonzProxy.TAG;
                Log.e(str, "resumePausedEffect IllegalAccessException : " + e2.getMessage());
            } catch (IllegalArgumentException e3) {
                String str2 = VibetonzProxy.TAG;
                Log.e(str2, "resumePausedEffect IllegalArgumentException : " + e3.getMessage());
            } catch (InvocationTargetException e4) {
                String str3 = VibetonzProxy.TAG;
                Log.e(str3, "resumePausedEffect InvocationTargetException : " + e4.getMessage());
            } catch (RuntimeException e5) {
                String str4 = VibetonzProxy.TAG;
                Log.e(str4, "resumePausedEffect RuntimeException : " + e5.getMessage());
            } catch (Exception e6) {
                String str5 = VibetonzProxy.TAG;
                Log.e(str5, "resumePausedEffect Exception : " + e6.getClass());
            }
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public boolean isPlaying(String effectName) {
            if (this.mClazz_vibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "isPlaying can not found the class!");
                return false;
            }
            Log.v(VibetonzProxy.TAG, "isPlaying===================");
            try {
                return Boolean.parseBoolean(this.mClazz_vibetonzImpl.getMethod("isPlaying", String.class).invoke(this.mObject_vibetonzImpl, effectName).toString());
            } catch (NoSuchMethodException e) {
                Log.e(VibetonzProxy.TAG, "isPlaying method is not found");
                return false;
            } catch (IllegalAccessException e2) {
                String str = VibetonzProxy.TAG;
                Log.e(str, "isPlaying IllegalAccessException : " + e2.getMessage());
                return false;
            } catch (IllegalArgumentException e3) {
                String str2 = VibetonzProxy.TAG;
                Log.e(str2, "isPlaying IllegalArgumentException : " + e3.getMessage());
                return false;
            } catch (InvocationTargetException e4) {
                String str3 = VibetonzProxy.TAG;
                Log.e(str3, "isPlaying InvocationTargetException : " + e4.getMessage());
                return false;
            } catch (RuntimeException e5) {
                String str4 = VibetonzProxy.TAG;
                Log.e(str4, "isPlaying RuntimeException : " + e5.getMessage());
                return false;
            } catch (Exception e6) {
                String str5 = VibetonzProxy.TAG;
                Log.e(str5, "isPlaying Exception : " + e6.getClass());
                return false;
            }
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public boolean startHaptic(Context context, int callerId, int ringtoneType, Uri uri) {
            if (this.mClazz_RingtoneVibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "startHaptic can not found the class!");
                return false;
            }
            Log.v(VibetonzProxy.TAG, "startHaptic===================");
            try {
                return Boolean.parseBoolean(this.mClazz_RingtoneVibetonzImpl.getMethod("startHaptic", Context.class, Integer.TYPE, Integer.TYPE, Uri.class).invoke(this.mObject_RingtoneVibetonzImpl, context, Integer.valueOf(callerId), Integer.valueOf(ringtoneType), uri).toString());
            } catch (NoSuchMethodException e) {
                Log.e(VibetonzProxy.TAG, "startHaptic method is not found");
                return false;
            } catch (IllegalAccessException e2) {
                String str = VibetonzProxy.TAG;
                Log.e(str, "isPlaying IllegalAccessException : " + e2.getMessage());
                return false;
            } catch (IllegalArgumentException e3) {
                String str2 = VibetonzProxy.TAG;
                Log.e(str2, "isPlaying IllegalArgumentException : " + e3.getMessage());
                return false;
            } catch (InvocationTargetException e4) {
                String str3 = VibetonzProxy.TAG;
                Log.e(str3, "isPlaying InvocationTargetException : " + e4.getMessage());
                return false;
            } catch (RuntimeException e5) {
                String str4 = VibetonzProxy.TAG;
                Log.e(str4, "isPlaying RuntimeException : " + e5.getMessage());
                return false;
            } catch (Exception e6) {
                String str5 = VibetonzProxy.TAG;
                Log.e(str5, "isPlaying Exception : " + e6.getClass());
                return false;
            }
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public boolean hasHaptic(Context context, Uri uri) {
            if (this.mClazz_RingtoneVibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "hasHaptic can not found the class!");
                return false;
            }
            Log.v(VibetonzProxy.TAG, "hasHaptic===================");
            try {
                return Boolean.parseBoolean(this.mClazz_RingtoneVibetonzImpl.getMethod("hasHaptic", Context.class, Uri.class).invoke(this.mObject_RingtoneVibetonzImpl, context, uri).toString());
            } catch (NoSuchMethodException e) {
                Log.e(VibetonzProxy.TAG, "hasHaptic method is not found");
                return false;
            } catch (IllegalAccessException e2) {
                String str = VibetonzProxy.TAG;
                Log.e(str, "isPlaying IllegalAccessException : " + e2.getMessage());
                return false;
            } catch (IllegalArgumentException e3) {
                String str2 = VibetonzProxy.TAG;
                Log.e(str2, "isPlaying IllegalArgumentException : " + e3.getMessage());
                return false;
            } catch (InvocationTargetException e4) {
                String str3 = VibetonzProxy.TAG;
                Log.e(str3, "isPlaying InvocationTargetException : " + e4.getMessage());
                return false;
            } catch (RuntimeException e5) {
                String str4 = VibetonzProxy.TAG;
                Log.e(str4, "isPlaying RuntimeException : " + e5.getMessage());
                return false;
            } catch (Exception e6) {
                String str5 = VibetonzProxy.TAG;
                Log.e(str5, "isPlaying Exception : " + e6.getClass());
                return false;
            }
        }

        @Override // com.android.server.VibetonzProxy.IVibetonzImpl
        public void stopHaptic() {
            if (this.mClazz_RingtoneVibetonzImpl == null) {
                Log.e(VibetonzProxy.TAG, "stopHaptic can not found the class!");
                return;
            }
            Log.v(VibetonzProxy.TAG, "stopHaptic===================");
            try {
                this.mClazz_RingtoneVibetonzImpl.getMethod("stopHaptic", new Class[0]).invoke(this.mObject_RingtoneVibetonzImpl, new Object[0]);
            } catch (NoSuchMethodException e) {
                Log.e(VibetonzProxy.TAG, "stopHaptic method is not found");
            } catch (IllegalAccessException e2) {
                String str = VibetonzProxy.TAG;
                Log.e(str, "isPlaying IllegalAccessException : " + e2.getMessage());
            } catch (IllegalArgumentException e3) {
                String str2 = VibetonzProxy.TAG;
                Log.e(str2, "isPlaying IllegalArgumentException : " + e3.getMessage());
            } catch (InvocationTargetException e4) {
                String str3 = VibetonzProxy.TAG;
                Log.e(str3, "isPlaying InvocationTargetException : " + e4.getMessage());
            } catch (RuntimeException e5) {
                String str4 = VibetonzProxy.TAG;
                Log.e(str4, "isPlaying RuntimeException : " + e5.getMessage());
            } catch (Exception e6) {
                String str5 = VibetonzProxy.TAG;
                Log.e(str5, "isPlaying Exception : " + e6.getClass());
            }
        }
    }
}
