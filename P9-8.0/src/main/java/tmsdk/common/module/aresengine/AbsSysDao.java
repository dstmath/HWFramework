package tmsdk.common.module.aresengine;

import android.net.Uri;
import java.util.List;

public abstract class AbsSysDao {
    public abstract boolean contains(String str);

    public abstract List<CallLogEntity> getAllCallLog();

    public abstract List<ContactEntity> getAllContact();

    public abstract CallLogEntity getLastCallLog();

    public abstract SmsEntity getLastInBoxSms(int i, int i2);

    public abstract SmsEntity getLastOutBoxSms(int i);

    public abstract SmsEntity getLastSentSms(int i);

    public abstract List<ContactEntity> getSimContact();

    public abstract Uri insert(SmsEntity smsEntity);

    public abstract Uri insert(SmsEntity smsEntity, boolean z);

    public abstract boolean remove(CallLogEntity callLogEntity);

    public abstract boolean remove(SmsEntity smsEntity);

    public abstract boolean supportThisPhone();
}
