package huawei.android.telephony.wrapper;

import android.os.RemoteException;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.List;

public interface IIccPhoneBookMSimWrapper {
    List<AdnRecord> getAdnRecordsInEf(int i, int i2) throws RemoteException;

    int[] getAdnRecordsSize(int i, int i2) throws RemoteException;

    boolean updateAdnRecordsInEfByIndex(int i, String str, String str2, int i2, String str3, int i3) throws RemoteException;

    boolean updateAdnRecordsInEfBySearch(int i, String str, String str2, String str3, String str4, String str5, int i2) throws RemoteException;
}
