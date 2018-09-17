package android.support.v4.media;

import android.media.AudioAttributes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import java.lang.reflect.Method;

@RequiresApi(21)
class AudioAttributesCompatApi21 {
    private static final String TAG = "AudioAttributesCompat";
    private static Method sAudioAttributesToLegacyStreamType;

    static final class Wrapper {
        private AudioAttributes mWrapped;

        private Wrapper(AudioAttributes obj) {
            this.mWrapped = obj;
        }

        public static Wrapper wrap(@NonNull AudioAttributes obj) {
            if (obj != null) {
                return new Wrapper(obj);
            }
            throw new IllegalArgumentException("AudioAttributesApi21.Wrapper cannot wrap null");
        }

        public AudioAttributes unwrap() {
            return this.mWrapped;
        }
    }

    AudioAttributesCompatApi21() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x002f A:{Splitter: B:1:0x0004, ExcHandler: java.lang.NoSuchMethodException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x002f A:{Splitter: B:1:0x0004, ExcHandler: java.lang.NoSuchMethodException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x002f A:{Splitter: B:1:0x0004, ExcHandler: java.lang.NoSuchMethodException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:7:0x002f, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:8:0x0030, code:
            android.util.Log.w(TAG, "getLegacyStreamType() failed on API21+", r1);
     */
    /* JADX WARNING: Missing block: B:9:0x003a, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int toLegacyStreamType(Wrapper aaWrap) {
        AudioAttributes aaObject = aaWrap.unwrap();
        try {
            if (sAudioAttributesToLegacyStreamType == null) {
                sAudioAttributesToLegacyStreamType = AudioAttributes.class.getMethod("toLegacyStreamType", new Class[]{AudioAttributes.class});
            }
            return ((Integer) sAudioAttributesToLegacyStreamType.invoke(null, new Object[]{aaObject})).intValue();
        } catch (Exception e) {
        }
    }
}
