package tmsdk.bg.module.aresengine;

import tmsdk.common.TMSDKContext;
import tmsdk.common.module.aresengine.AbsSysDao;
import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.ContactEntity;
import tmsdk.common.module.aresengine.DefaultSysDao;
import tmsdk.common.module.aresengine.ICallLogDao;
import tmsdk.common.module.aresengine.IContactDao;
import tmsdk.common.module.aresengine.IEntityConverter;
import tmsdk.common.module.aresengine.IKeyWordDao;
import tmsdk.common.module.aresengine.ILastCallLogDao;
import tmsdk.common.module.aresengine.ISmsDao;
import tmsdk.common.module.aresengine.SmsEntity;

public abstract class AresEngineFactor {
    public abstract IContactDao<? extends ContactEntity> getBlackListDao();

    public abstract ICallLogDao<? extends CallLogEntity> getCallLogDao();

    public abstract IEntityConverter getEntityConverter();

    public abstract IKeyWordDao getKeyWordDao();

    public abstract ILastCallLogDao getLastCallLogDao();

    public ISmsDao<? extends SmsEntity> getPaySmsDao() {
        return null;
    }

    public PhoneDeviceController getPhoneDeviceController() {
        return DefaultPhoneDeviceController.getInstance();
    }

    public abstract ICallLogDao<? extends CallLogEntity> getPrivateCallLogDao();

    public abstract IContactDao<? extends ContactEntity> getPrivateListDao();

    public abstract ISmsDao<? extends SmsEntity> getPrivateSmsDao();

    public abstract ISmsDao<? extends SmsEntity> getSmsDao();

    public AbsSysDao getSysDao() {
        return DefaultSysDao.getInstance(TMSDKContext.getApplicaionContext());
    }

    public abstract IContactDao<? extends ContactEntity> getWhiteListDao();
}
