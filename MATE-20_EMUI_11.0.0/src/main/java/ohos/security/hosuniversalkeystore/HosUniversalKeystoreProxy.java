package ohos.security.hosuniversalkeystore;

import android.content.Context;
import com.huawei.security.keystore.HwUniversalKeyStoreProvider;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.X509Certificate;
import ohos.aafwk.ability.Ability;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;

public class HosUniversalKeystoreProxy implements IHosUniversalKeystore {
    private static final HiLogLabel LABEL = new HiLogLabel(3, SUB_DOMAIN_SECURITY_HUKS, "HosUniversalKeystoreProxy");
    private static final int SUB_DOMAIN_SECURITY_HUKS = 218115844;
    private final IRemoteObject mRemote;

    public HosUniversalKeystoreProxy(IRemoteObject iRemoteObject) {
        this.mRemote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemote;
    }

    @Override // ohos.security.hosuniversalkeystore.IHosUniversalKeystore
    public void install() {
        HwUniversalKeyStoreProvider.install();
    }

    @Override // ohos.security.hosuniversalkeystore.IHosUniversalKeystore
    public X509Certificate[] attestDeviceIds(Ability ability, int[] iArr, byte[] bArr) {
        if (ability == null) {
            return null;
        }
        Object hostContext = ability.getHostContext();
        if (!(hostContext instanceof Context)) {
            return null;
        }
        try {
            return (X509Certificate[]) Class.forName("com.huawei.security.keystore.HwAttestationUtils").getMethod("attestDeviceIds", Context.class, int[].class, byte[].class).invoke(null, (Context) hostContext, iArr, bArr);
        } catch (ClassNotFoundException e) {
            HiLog.error(LABEL, "getCertificateChain ClassNotFoundException %{public}s", e.getMessage());
            return null;
        } catch (NoSuchMethodException e2) {
            HiLog.error(LABEL, "getCertificateChain NoSuchMethodException %{public}s", e2.getMessage());
            return null;
        } catch (IllegalAccessException e3) {
            HiLog.error(LABEL, "getCertificateChain IllegalAccessException %{public}s", e3.getMessage());
            return null;
        } catch (InvocationTargetException e4) {
            HiLog.error(LABEL, "getCertificateChain InvocationTargetException %{public}s", e4.getMessage());
            return null;
        }
    }
}
