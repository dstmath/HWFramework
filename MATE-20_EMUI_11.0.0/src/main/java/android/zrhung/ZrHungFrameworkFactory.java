package android.zrhung;

import android.os.BlockMonitor;
import android.os.IBlockMonitor;
import android.zrhung.appeye.AppEyeFwkBlock;
import android.zrhung.appeye.AppEyeUiProbe;
import com.huawei.dfr.DefaultZrHungFrameworkFactory;
import com.huawei.dfr.zrhung.DefaultZrHungImpl;

public class ZrHungFrameworkFactory extends DefaultZrHungFrameworkFactory {
    private static final String TAG = "ZrHungFrameworkFactory";

    public IAppEyeUiProbe getAppEyeUiProbe() {
        return AppEyeUiProbe.get();
    }

    public IZrHung getIZrHung(String wpName) {
        return ZrHungImpl.getZrHung(wpName);
    }

    public IBlockMonitor getBlockMonitor() {
        return BlockMonitor.getInstance();
    }

    public DefaultZrHungImpl getAppEyeFwkBlock() {
        return AppEyeFwkBlock.getInstance();
    }
}
