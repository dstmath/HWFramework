package android.service.dreams;

import android.os.IBinder;
import android.os.ServiceManager;
import android.service.dreams.IDreamManager.Stub;
import android.util.Slog;
import android.view.WindowManager.LayoutParams;

public class HwCustDreamServiceImpl extends HwCustDreamService {
    private static final String TAG = "HwCustDreamServiceImpl";
    private static final boolean mChargingAlbumSupported = false;
    private boolean mAlbumMode;
    private DreamService mFather;
    private final IDreamManager mSandman;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.dreams.HwCustDreamServiceImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.service.dreams.HwCustDreamServiceImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.dreams.HwCustDreamServiceImpl.<clinit>():void");
    }

    public HwCustDreamServiceImpl(DreamService service) {
        super(service);
        this.mAlbumMode = false;
        Slog.w(TAG, TAG);
        this.mSandman = Stub.asInterface(ServiceManager.getService("dreams"));
        this.mFather = service;
    }

    public boolean isChargingAlbumEnabled() {
        if (mChargingAlbumSupported) {
            return this.mAlbumMode;
        }
        return super.isChargingAlbumEnabled();
    }

    public void enableChargingAlbum() {
        if (mChargingAlbumSupported) {
            try {
                if (this.mSandman == null) {
                    this.mAlbumMode = false;
                    Slog.w(TAG, "No dream manager found");
                } else if (this.mSandman.isChargingAlbumEnabled()) {
                    this.mAlbumMode = true;
                } else {
                    this.mAlbumMode = false;
                }
            } catch (Throwable t) {
                this.mAlbumMode = false;
                Slog.w(TAG, "Crashed in isChargingAlbumEnabled()", t);
            }
        }
    }

    public void setAlbumLayoutParams(LayoutParams lp, IBinder windowToken) {
        int i = 0;
        if (mChargingAlbumSupported) {
            int i2;
            Slog.v(TAG, String.format("Attaching window token: %s to window of type %s", new Object[]{windowToken, Integer.valueOf(2102)}));
            lp.type = 2102;
            lp.token = windowToken;
            lp.windowAnimations = 16974584;
            int i3 = lp.flags;
            if (this.mFather.isFullscreen()) {
                i2 = 1024;
            } else {
                i2 = 0;
            }
            i2 |= -2146893567;
            if (this.mFather.isScreenBright()) {
                i = 128;
            }
            lp.flags = (i2 | i) | i3;
        }
    }
}
