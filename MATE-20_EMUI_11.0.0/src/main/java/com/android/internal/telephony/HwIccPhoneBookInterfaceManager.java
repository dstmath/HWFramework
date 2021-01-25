package com.android.internal.telephony;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.android.internal.telephony.gsm.IUsimPhoneBookManagerInner;
import com.android.internal.telephony.uicc.IAdnRecordCacheInner;
import com.android.internal.telephony.uicc.IIccFileHandlerInner;
import com.android.internal.telephony.uicc.IIccRecordsInner;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.uicc.AdnRecordExt;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
import java.util.Arrays;

public class HwIccPhoneBookInterfaceManager extends Handler implements IHwPhoneBookInterfaceManagerEx {
    private static final String ACTION_READ_SIM_CONTACTS_DONE = "com.huawei.intent.action.READ_SIM_CONTACTS_DONE";
    private static final boolean DBG = false;
    private static final String LOG_TAG = "HwIccPhoneBookInterfaceManager";
    private volatile boolean isGettingAdnRecordsSize = false;
    private IAdnRecordCacheInner mAdnCache;
    private IIccPhoneBookInterfaceManagerInner mIIccPhoneBookInterfaceManagerInner;
    private final Object mLock2;
    private PhoneExt mPhone;
    private IUsimPhoneBookManagerInner mUsimPhoneBookManager;

    public HwIccPhoneBookInterfaceManager(IIccPhoneBookInterfaceManagerInner iIccPhoneBookInterfaceManagerInner, PhoneExt phone, Object lock) {
        this.mIIccPhoneBookInterfaceManagerInner = iIccPhoneBookInterfaceManagerInner;
        this.mPhone = phone;
        this.mLock2 = lock;
    }

    private static void log(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }

    public boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) {
        Throwable th;
        synchronized (this.mLock2) {
            try {
                if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") != 0) {
                    throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
                } else if (values == null) {
                    loge("input values is null.");
                    return false;
                } else {
                    String oldTag = values.getAsString("tag");
                    String newTag = values.getAsString("newTag");
                    String oldPhoneNumber = values.getAsString("number");
                    String newPhoneNumber = values.getAsString("newNumber");
                    String oldEmail = values.getAsString("emails");
                    String newEmail = values.getAsString("newEmails");
                    String oldAnr = values.getAsString("anrs");
                    String newAnr = values.getAsString("newAnrs");
                    String[] oldEmailArray = TextUtils.isEmpty(oldEmail) ? null : new String[]{oldEmail};
                    String[] newEmailArray = TextUtils.isEmpty(newEmail) ? null : new String[]{newEmail};
                    String[] oldAnrArray = TextUtils.isEmpty(oldAnr) ? null : new String[]{oldAnr};
                    String[] newAnrArray = TextUtils.isEmpty(newAnr) ? null : new String[]{newAnr};
                    try {
                        int efid2 = this.mIIccPhoneBookInterfaceManagerInner.updateEfForIccTypeHw(efid);
                        try {
                            log("updateAdnRecordsWithContentValuesInEfBySearch: efid=" + efid2 + ", pin2=xxxx");
                            this.mIIccPhoneBookInterfaceManagerInner.checkThreadHw();
                            Object updateRequest = this.mIIccPhoneBookInterfaceManagerInner.getRequest();
                            synchronized (updateRequest) {
                                Message response = obtainMessage(IIccPhoneBookInterfaceManagerInner.getEventIdUpdateDoneHw(), updateRequest);
                                AdnRecordExt oldAdn = new AdnRecordExt(oldTag, oldPhoneNumber, oldEmailArray, oldAnrArray);
                                AdnRecordExt newAdn = new AdnRecordExt(newTag, newPhoneNumber, newEmailArray, newAnrArray);
                                if (this.mAdnCache != null) {
                                    this.mAdnCache.updateAdnBySearch(efid2, oldAdn, newAdn, pin2, response);
                                    this.mIIccPhoneBookInterfaceManagerInner.waitForResultHw(updateRequest);
                                } else {
                                    loge("Failure while trying to update by search due to uninitialised adncache");
                                }
                            }
                            Object result = this.mIIccPhoneBookInterfaceManagerInner.getRequestResult(updateRequest);
                            return result == null ? false : ((Boolean) result).booleanValue();
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
    }

    public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) {
        Throwable th;
        Throwable th2;
        synchronized (this.mLock2) {
            try {
                if (this.mPhone.getContext().checkCallingOrSelfPermission("android.permission.WRITE_CONTACTS") == 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("updateUsimAdnRecordsInEfByIndexHW: efid=");
                    sb.append(efid);
                    sb.append(" sEf_id=");
                    try {
                        sb.append(sEf_id);
                        sb.append(" Index=");
                    } catch (Throwable th3) {
                        th = th3;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        }
                        throw th;
                    }
                    try {
                        sb.append(index);
                        sb.append(" pin2=xxxx");
                        log(sb.toString());
                        this.mIIccPhoneBookInterfaceManagerInner.checkThreadHw();
                        Object updateRequest = this.mIIccPhoneBookInterfaceManagerInner.getRequest();
                        synchronized (updateRequest) {
                            try {
                                Message response = obtainMessage(IIccPhoneBookInterfaceManagerInner.getEventIdUpdateDoneHw(), updateRequest);
                                try {
                                    AdnRecordExt newAdn = new AdnRecordExt(newTag, newPhoneNumber, newEmails, newAnrNumbers);
                                    int efid2 = this.mIIccPhoneBookInterfaceManagerInner.updateEfForIccTypeHw(efid);
                                    try {
                                        if (this.mAdnCache != null) {
                                            this.mAdnCache.updateUsimAdnByIndexHw(efid2, newAdn, sEf_id, index, pin2, response);
                                            this.mIIccPhoneBookInterfaceManagerInner.waitForResultHw(updateRequest);
                                        } else {
                                            loge("Failure while trying to update by index due to uninitialised adncache");
                                        }
                                    } catch (Throwable th5) {
                                        th2 = th5;
                                        throw th2;
                                    }
                                } catch (Throwable th6) {
                                    th2 = th6;
                                    throw th2;
                                }
                            } catch (Throwable th7) {
                                th2 = th7;
                                throw th2;
                            }
                        }
                        try {
                            Object result = this.mIIccPhoneBookInterfaceManagerInner.getRequestResult(updateRequest);
                            return result == null ? false : ((Boolean) result).booleanValue();
                        } catch (Throwable th8) {
                            th = th8;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } catch (Throwable th9) {
                        th = th9;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } else {
                    try {
                        throw new SecurityException("Requires android.permission.WRITE_CONTACTS permission");
                    } catch (Throwable th10) {
                        th = th10;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } catch (Throwable th11) {
                th = th11;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    public int getAdnCountHw() {
        if (this.mAdnCache != null) {
            PhoneExt phoneExt = this.mPhone;
            if (phoneExt == null || (phoneExt.getCurrentUiccAppType() != IccCardApplicationStatusEx.AppTypeEx.APPTYPE_USIM && this.mPhone.getCurrentUiccAppType() != IccCardApplicationStatusEx.AppTypeEx.APPTYPE_CSIM && this.mPhone.getCurrentUiccAppType() != IccCardApplicationStatusEx.AppTypeEx.APPTYPE_ISIM)) {
                return this.mAdnCache.getAdnCountHw();
            }
            return this.mAdnCache.getUsimAdnCountHw();
        }
        loge("mAdnCache is NULL when getAdnCountHw.");
        return 0;
    }

    public int getAnrCountHw() {
        IAdnRecordCacheInner iAdnRecordCacheInner = this.mAdnCache;
        if (iAdnRecordCacheInner != null) {
            return iAdnRecordCacheInner.getAnrCountHw();
        }
        loge("mAdnCache is NULL when getAnrCountHW.");
        return 0;
    }

    public int getEmailCountHw() {
        IAdnRecordCacheInner iAdnRecordCacheInner = this.mAdnCache;
        if (iAdnRecordCacheInner != null) {
            return iAdnRecordCacheInner.getEmailCountHw();
        }
        loge("mAdnCache is NULL when getEmailCountHW.");
        return 0;
    }

    public int getSpareAnrCountHw() {
        IAdnRecordCacheInner iAdnRecordCacheInner = this.mAdnCache;
        if (iAdnRecordCacheInner != null) {
            return iAdnRecordCacheInner.getSpareAnrCountHw();
        }
        loge("mAdnCache is NULL when getSpareAnrCountHW.");
        return 0;
    }

    public int getSpareEmailCountHw() {
        IAdnRecordCacheInner iAdnRecordCacheInner = this.mAdnCache;
        if (iAdnRecordCacheInner != null) {
            return iAdnRecordCacheInner.getSpareEmailCountHw();
        }
        loge("mAdnCache is NULL when getSpareEmailCountHW.");
        return 0;
    }

    public int[] getRecordsSizeHw() {
        if (!IccRecordsEx.getEmailAnrSupport()) {
            loge("getRecordsSize return null as prop not open.");
            return null;
        }
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt != null && phoneExt.getIccRecordsInner() != null && !this.mPhone.getIccRecordsInner().isGetPBRDone()) {
            loge("getRecordsSize(): is not get PBR done, please wait!");
            return null;
        } else if (this.isGettingAdnRecordsSize) {
            loge("getRecordsSize(): is running already, please wait!");
            return null;
        } else {
            synchronized (this.mLock2) {
                if (getAdnCountHw() == 0) {
                    loge("getRecordsSize: adn is not ever read!");
                    this.isGettingAdnRecordsSize = true;
                    try {
                        this.mIIccPhoneBookInterfaceManagerInner.getAdnRecordsInEfForEx(28474);
                    } catch (SecurityException e) {
                        this.isGettingAdnRecordsSize = false;
                        log("getRecordsSize():catch exception.");
                        return null;
                    }
                }
                log("getRecordsSize: adn all loaded! GettingAdnRecordsSize = " + this.isGettingAdnRecordsSize);
                if (this.isGettingAdnRecordsSize && this.mPhone != null) {
                    Intent intent = new Intent(ACTION_READ_SIM_CONTACTS_DONE);
                    intent.setPackage("com.huawei.contacts");
                    intent.addFlags(268435456);
                    this.mPhone.getContext().sendBroadcast(intent);
                }
                this.isGettingAdnRecordsSize = false;
                log("read sim card done");
                this.mIIccPhoneBookInterfaceManagerInner.checkThreadHw();
                if (this.mAdnCache != null) {
                    return this.mAdnCache.getRecordsSizeHw();
                }
                loge("mAdnCache is NULL when getRecordsSizeHW.");
                return null;
            }
        }
    }

    public int updateEfFor3gCardType(int efid) {
        IIccRecordsInner iccRecords;
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt == null || (iccRecords = phoneExt.getIccRecordsInner()) == null) {
            log("Translate EF_ADN to EF_PBR");
            return 20272;
        } else if (iccRecords.has3Gphonebook()) {
            log("Translate EF_ADN to EF_PBR");
            return 20272;
        } else {
            log("updateEfForIccType use EF_ADN");
            return efid;
        }
    }

    public int[] getAdnRecordsSizeHw(int efid) {
        int[] recordSize = new int[3];
        IIccFileHandlerInner fh = null;
        PhoneExt phoneExt = this.mPhone;
        if (phoneExt != null) {
            fh = phoneExt.getIccFileHandler();
        }
        if (fh != null) {
            this.mAdnCache = this.mPhone.getAdnCacheInner();
            IAdnRecordCacheInner iAdnRecordCacheInner = this.mAdnCache;
            if (iAdnRecordCacheInner != null) {
                this.mUsimPhoneBookManager = iAdnRecordCacheInner.getUsimPhoneBookManager();
                IUsimPhoneBookManagerInner iUsimPhoneBookManagerInner = this.mUsimPhoneBookManager;
                if (iUsimPhoneBookManagerInner != null) {
                    iUsimPhoneBookManagerInner.setIccFileHandler(fh);
                    recordSize = this.mUsimPhoneBookManager.getAdnRecordsSizeFromEF();
                }
                if (recordSize == null) {
                    loge("null == recordSize");
                    return new int[3];
                }
            } else {
                loge("Failure while trying to load from SIM due to uninitialised adncache");
                return null;
            }
        }
        return Arrays.copyOf(recordSize, 3);
    }

    public int getSpareExt1CountHw() {
        IAdnRecordCacheInner iAdnRecordCacheInner = this.mAdnCache;
        if (iAdnRecordCacheInner != null) {
            return iAdnRecordCacheInner.getSpareExt1CountHw();
        }
        loge("mAdnCache is NULL when getSpareExt1CountHW.");
        return -1;
    }

    public void updateAdnRecordCache(IAdnRecordCacheInner iAdnRecordCacheInner) {
        this.mAdnCache = iAdnRecordCacheInner;
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        this.mIIccPhoneBookInterfaceManagerInner.handleMessageForEx(msg);
    }
}
