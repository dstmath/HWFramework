package android.service.dreams;

import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.service.dreams.IDreamManager.Stub;
import android.util.Slog;
import android.view.WindowManager.LayoutParams;

public class HwCustDreamServiceImpl extends HwCustDreamService {
    private static final String TAG = "HwCustDreamServiceImpl";
    private static final boolean mChargingAlbumSupported = SystemProperties.getBoolean("ro.config.ChargingAlbum", false);
    private boolean mAlbumMode = false;
    private DreamService mFather;
    private final IDreamManager mSandman;

    public HwCustDreamServiceImpl(DreamService service) {
        super(service);
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
            lp.windowAnimations = 16974572;
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
