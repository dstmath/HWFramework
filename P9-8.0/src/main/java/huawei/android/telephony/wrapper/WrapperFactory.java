package huawei.android.telephony.wrapper;

import android.telephony.Rlog;

public class WrapperFactory {
    private static Factory mInstance;

    public interface Factory {
        HuaweiTelephonyManagerWrapper getHuaweiTelephonyManagerWrapper();

        MSimTelephonyManagerWrapper getMSimTelephonyManagerWrapper();
    }

    static {
        try {
            mInstance = (Factory) Class.forName("huawei.android.telephony.wrapper.WrapperFactoryImpl").getConstructor(new Class[0]).newInstance(new Object[0]);
            Rlog.d("WrapperFactory", "got success! mInstance = " + mInstance);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        if (mInstance == null) {
            mInstance = new DummyWrapperFactory();
        }
    }

    public static HuaweiTelephonyManagerWrapper getHuaweiTelephonyManagerWrapper() {
        return mInstance.getHuaweiTelephonyManagerWrapper();
    }

    public static MSimTelephonyManagerWrapper getMSimTelephonyManagerWrapper() {
        return mInstance.getMSimTelephonyManagerWrapper();
    }
}
