package android.service.dreams;

import android.os.IBinder;
import android.view.WindowManager.LayoutParams;

public class HwCustDreamService {
    public HwCustDreamService(DreamService service) {
    }

    public boolean isChargingAlbumEnabled() {
        return false;
    }

    public void enableChargingAlbum() {
    }

    public void setAlbumLayoutParams(LayoutParams lp, IBinder windowToken) {
    }
}
