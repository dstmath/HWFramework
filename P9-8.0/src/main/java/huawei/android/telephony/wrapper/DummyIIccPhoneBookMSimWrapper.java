package huawei.android.telephony.wrapper;

import android.os.RemoteException;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.List;

public class DummyIIccPhoneBookMSimWrapper implements IIccPhoneBookMSimWrapper {
    private static DummyIIccPhoneBookMSimWrapper mInstance = new DummyIIccPhoneBookMSimWrapper();

    public static IIccPhoneBookMSimWrapper getInstance() {
        return mInstance;
    }

    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2, int subscription) throws RemoteException {
        return false;
    }

    public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2, int subscription) throws RemoteException {
        return false;
    }

    public int[] getAdnRecordsSize(int efid, int subscription) throws RemoteException {
        return new int[]{0};
    }

    public List<AdnRecord> getAdnRecordsInEf(int efid, int subscription) throws RemoteException {
        return null;
    }
}
