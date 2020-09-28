package com.android.internal.telephony.emergency;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.emergency.EmergencyNumber;
import android.text.TextUtils;
import android.util.LocalLog;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.LocaleTracker;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.PlmnActRecord;
import com.android.internal.util.IndentingPrintWriter;
import com.android.phone.ecc.nano.ProtobufEccData;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import libcore.io.IoUtils;

public class EmergencyNumberTracker extends Handler {
    public static final int ADD_EMERGENCY_NUMBER_TEST_MODE = 1;
    public static boolean DBG = false;
    private static final String EMERGENCY_NUMBER_DB_ASSETS_FILE = "eccdata";
    private static final int EVENT_UNSOL_EMERGENCY_NUMBER_LIST = 1;
    private static final int EVENT_UPDATE_DB_COUNTRY_ISO_CHANGED = 2;
    private static final int EVENT_UPDATE_EMERGENCY_NUMBER_PREFIX = 4;
    private static final int EVENT_UPDATE_EMERGENCY_NUMBER_TEST_MODE = 3;
    private static final int LOCAL_LOG_SIZE = 5;
    public static final int REMOVE_EMERGENCY_NUMBER_TEST_MODE = 2;
    public static final int RESET_EMERGENCY_NUMBER_TEST_MODE = 3;
    private static final String TAG = EmergencyNumberTracker.class.getSimpleName();
    private final CommandsInterface mCi;
    private String mCountryIso;
    private List<EmergencyNumber> mEmergencyNumberList = new ArrayList();
    private final LocalLog mEmergencyNumberListDatabaseLocalLog = new LocalLog(5);
    private List<EmergencyNumber> mEmergencyNumberListFromDatabase = new ArrayList();
    private List<EmergencyNumber> mEmergencyNumberListFromFakeEcc = new ArrayList();
    private List<EmergencyNumber> mEmergencyNumberListFromRadio = new ArrayList();
    private List<EmergencyNumber> mEmergencyNumberListFromTestMode = new ArrayList();
    private final LocalLog mEmergencyNumberListLocalLog = new LocalLog(5);
    private final LocalLog mEmergencyNumberListPrefixLocalLog = new LocalLog(5);
    private final LocalLog mEmergencyNumberListRadioLocalLog = new LocalLog(5);
    private final LocalLog mEmergencyNumberListTestModeLocalLog = new LocalLog(5);
    private List<EmergencyNumber> mEmergencyNumberListWithPrefix = new ArrayList();
    private String[] mEmergencyNumberPrefix = new String[0];
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.emergency.EmergencyNumberTracker.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            int phoneId;
            if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                EmergencyNumberTracker.this.onCarrierConfigChanged();
            } else if (intent.getAction().equals("android.telephony.action.NETWORK_COUNTRY_CHANGED") && (phoneId = intent.getIntExtra("phone", -1)) == EmergencyNumberTracker.this.mPhone.getPhoneId()) {
                String countryIso = intent.getStringExtra("android.telephony.extra.NETWORK_COUNTRY");
                EmergencyNumberTracker.logd("ACTION_NETWORK_COUNTRY_CHANGED: PhoneId: " + phoneId + " CountryIso: " + countryIso);
                if (!TextUtils.isEmpty(countryIso)) {
                    EmergencyNumberTracker.this.updateEmergencyNumberDatabaseCountryChange(countryIso);
                }
            }
        }
    };
    private final Phone mPhone;

    public EmergencyNumberTracker(Phone phone, CommandsInterface ci) {
        this.mPhone = phone;
        this.mCi = ci;
        Phone phone2 = this.mPhone;
        if (phone2 != null) {
            CarrierConfigManager configMgr = (CarrierConfigManager) phone2.getContext().getSystemService("carrier_config");
            if (configMgr != null) {
                PersistableBundle b = configMgr.getConfigForSubId(this.mPhone.getSubId());
                if (b != null) {
                    this.mEmergencyNumberPrefix = b.getStringArray("emergency_number_prefix_string_array");
                }
            } else {
                loge("CarrierConfigManager is null.");
            }
            IntentFilter filter = new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED");
            filter.addAction("android.telephony.action.NETWORK_COUNTRY_CHANGED");
            this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter);
        } else {
            loge("mPhone is null.");
        }
        initializeDatabaseEmergencyNumberList();
        this.mCi.registerForEmergencyNumberList(this, 1, null);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.result == null) {
                loge("EVENT_UNSOL_EMERGENCY_NUMBER_LIST: Result from RIL is null.");
            } else if (ar.result == null || ar.exception != null) {
                loge("EVENT_UNSOL_EMERGENCY_NUMBER_LIST: Exception from RIL : " + ar.exception);
            } else {
                updateRadioEmergencyNumberListAndNotify((List) ar.result);
            }
        } else if (i != 2) {
            if (i != 3) {
                if (i == 4) {
                    if (msg.obj == null) {
                        loge("EVENT_UPDATE_EMERGENCY_NUMBER_PREFIX: Result from onCarrierConfigChanged is null.");
                    } else {
                        updateEmergencyNumberPrefixAndNotify((String[]) msg.obj);
                    }
                }
            } else if (msg.obj == null) {
                loge("EVENT_UPDATE_EMERGENCY_NUMBER_TEST_MODE: Result from executeEmergencyNumberTestModeCommand is null.");
            } else {
                updateEmergencyNumberListTestModeAndNotify(msg.arg1, (EmergencyNumber) msg.obj);
            }
        } else if (msg.obj == null) {
            loge("EVENT_UPDATE_DB_COUNTRY_ISO_CHANGED: Result from UpdateCountryIso is null.");
        } else {
            updateEmergencyNumberListDatabaseAndNotify((String) msg.obj);
        }
    }

    private void initializeDatabaseEmergencyNumberList() {
        if (this.mCountryIso == null) {
            this.mCountryIso = getInitialCountryIso().toLowerCase();
            cacheEmergencyDatabaseByCountry(this.mCountryIso);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCarrierConfigChanged() {
        PersistableBundle b;
        Phone phone = this.mPhone;
        if (phone != null) {
            CarrierConfigManager configMgr = (CarrierConfigManager) phone.getContext().getSystemService("carrier_config");
            if (configMgr != null && (b = configMgr.getConfigForSubId(this.mPhone.getSubId())) != null) {
                String[] emergencyNumberPrefix = b.getStringArray("emergency_number_prefix_string_array");
                if (!this.mEmergencyNumberPrefix.equals(emergencyNumberPrefix)) {
                    obtainMessage(4, emergencyNumberPrefix).sendToTarget();
                    return;
                }
                return;
            }
            return;
        }
        loge("onCarrierConfigChanged mPhone is null.");
    }

    private String getInitialCountryIso() {
        LocaleTracker lt;
        Phone phone = this.mPhone;
        if (phone != null) {
            ServiceStateTracker sst = phone.getServiceStateTracker();
            if (sst == null || (lt = sst.getLocaleTracker()) == null) {
                return PhoneConfigurationManager.SSSS;
            }
            return lt.getCurrentCountry();
        }
        loge("getInitialCountryIso mPhone is null.");
        return PhoneConfigurationManager.SSSS;
    }

    public void updateEmergencyNumberDatabaseCountryChange(String countryIso) {
        obtainMessage(2, countryIso).sendToTarget();
    }

    private EmergencyNumber convertEmergencyNumberFromEccInfo(ProtobufEccData.EccInfo eccInfo, String countryIso) {
        int i;
        String phoneNumber = eccInfo.phoneNumber.trim();
        if (phoneNumber.isEmpty()) {
            loge("EccInfo has empty phone number.");
            return null;
        }
        int[] iArr = eccInfo.types;
        int emergencyServiceCategoryBitmask = 0;
        for (int typeData : iArr) {
            int i2 = 1;
            if (typeData != 1) {
                int i3 = 2;
                if (typeData == 2) {
                    if (emergencyServiceCategoryBitmask != 0) {
                        i3 = emergencyServiceCategoryBitmask | 2;
                    }
                    emergencyServiceCategoryBitmask = i3;
                } else if (typeData == 3) {
                    if (emergencyServiceCategoryBitmask == 0) {
                        i = 4;
                    } else {
                        i = emergencyServiceCategoryBitmask | 4;
                    }
                    emergencyServiceCategoryBitmask = i;
                }
            } else {
                if (emergencyServiceCategoryBitmask != 0) {
                    i2 = emergencyServiceCategoryBitmask | 1;
                }
                emergencyServiceCategoryBitmask = i2;
            }
        }
        return new EmergencyNumber(phoneNumber, countryIso, PhoneConfigurationManager.SSSS, emergencyServiceCategoryBitmask, new ArrayList(), 16, 0);
    }

    private void cacheEmergencyDatabaseByCountry(String countryIso) {
        if (SystemProperties.getBoolean("ril.ecc.ignore_eccdata", true)) {
            logd("cacheEmergencyDatabaseByCountry, ignore ecc number from EMERGENCY_NUMBER_DB_ASSETS_FILE");
            return;
        }
        BufferedInputStream inputStream = null;
        List<EmergencyNumber> updatedEmergencyNumberList = new ArrayList<>();
        try {
            inputStream = new BufferedInputStream(this.mPhone.getContext().getAssets().open(EMERGENCY_NUMBER_DB_ASSETS_FILE));
            ProtobufEccData.AllInfo allEccMessages = ProtobufEccData.AllInfo.parseFrom(readInputStreamToByteArray(new GZIPInputStream(inputStream)));
            logd(countryIso + " emergency database is loaded. ");
            ProtobufEccData.CountryInfo[] countryInfoArr = allEccMessages.countries;
            int length = countryInfoArr.length;
            for (int i = 0; i < length; i++) {
                ProtobufEccData.CountryInfo countryEccInfo = countryInfoArr[i];
                if (countryEccInfo.isoCode.equals(countryIso.toUpperCase())) {
                    for (ProtobufEccData.EccInfo eccInfo : countryEccInfo.eccs) {
                        updatedEmergencyNumberList.add(convertEmergencyNumberFromEccInfo(eccInfo, countryIso));
                    }
                }
            }
            EmergencyNumber.mergeSameNumbersInEmergencyNumberList(updatedEmergencyNumberList);
            this.mEmergencyNumberListFromDatabase = updatedEmergencyNumberList;
        } catch (IOException ex) {
            loge("Cache emergency database failure: " + ex);
        } catch (Throwable th) {
            IoUtils.closeQuietly((AutoCloseable) null);
            throw th;
        }
        IoUtils.closeQuietly(inputStream);
    }

    private static byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[PlmnActRecord.ACCESS_TECH_EUTRAN];
        while (true) {
            int nRead = inputStream.read(data, 0, data.length);
            if (nRead != -1) {
                buffer.write(data, 0, nRead);
            } else {
                buffer.flush();
                return buffer.toByteArray();
            }
        }
    }

    private void updateRadioEmergencyNumberListAndNotify(List<EmergencyNumber> emergencyNumberListRadio) {
        Collections.sort(emergencyNumberListRadio);
        logd("updateRadioEmergencyNumberListAndNotify(): receiving " + emergencyNumberListRadio);
        if (!emergencyNumberListRadio.equals(this.mEmergencyNumberListFromRadio)) {
            try {
                EmergencyNumber.mergeSameNumbersInEmergencyNumberList(emergencyNumberListRadio);
                writeUpdatedEmergencyNumberListMetrics(emergencyNumberListRadio);
                this.mEmergencyNumberListFromRadio = emergencyNumberListRadio;
                if (!DBG) {
                    LocalLog localLog = this.mEmergencyNumberListRadioLocalLog;
                    localLog.log("updateRadioEmergencyNumberList:" + emergencyNumberListRadio);
                }
                updateEmergencyNumberList();
                if (!DBG) {
                    LocalLog localLog2 = this.mEmergencyNumberListLocalLog;
                    localLog2.log("updateRadioEmergencyNumberListAndNotify:" + this.mEmergencyNumberList);
                }
                notifyEmergencyNumberList();
            } catch (NullPointerException ex) {
                loge("updateRadioEmergencyNumberListAndNotify() Phone already destroyed: " + ex + " EmergencyNumberList not notified");
            }
        }
    }

    private void updateEmergencyNumberListDatabaseAndNotify(String countryIso) {
        logd("updateEmergencyNumberListDatabaseAndNotify(): receiving countryIso: " + countryIso);
        this.mCountryIso = countryIso.toLowerCase();
        cacheEmergencyDatabaseByCountry(countryIso);
        writeUpdatedEmergencyNumberListMetrics(this.mEmergencyNumberListFromDatabase);
        if (!DBG) {
            LocalLog localLog = this.mEmergencyNumberListDatabaseLocalLog;
            localLog.log("updateEmergencyNumberListDatabaseAndNotify:" + this.mEmergencyNumberListFromDatabase);
        }
        updateEmergencyNumberList();
        if (!DBG) {
            LocalLog localLog2 = this.mEmergencyNumberListLocalLog;
            localLog2.log("updateEmergencyNumberListDatabaseAndNotify:" + this.mEmergencyNumberList);
        }
        notifyEmergencyNumberList();
    }

    private void updateEmergencyNumberPrefixAndNotify(String[] emergencyNumberPrefix) {
        logd("updateEmergencyNumberPrefixAndNotify(): receiving emergencyNumberPrefix: " + emergencyNumberPrefix.toString());
        this.mEmergencyNumberPrefix = emergencyNumberPrefix;
        updateEmergencyNumberList();
        if (!DBG) {
            LocalLog localLog = this.mEmergencyNumberListLocalLog;
            localLog.log("updateEmergencyNumberPrefixAndNotify:" + this.mEmergencyNumberList);
        }
        notifyEmergencyNumberList();
    }

    private void notifyEmergencyNumberList() {
        try {
            if (getEmergencyNumberList() != null) {
                this.mPhone.notifyEmergencyNumberList();
                logd("notifyEmergencyNumberList(): notified");
            }
        } catch (NullPointerException ex) {
            loge("notifyEmergencyNumberList(): failure: Phone already destroyed: " + ex);
        }
    }

    private void updateEmergencyNumberList() {
        List<EmergencyNumber> mergedEmergencyNumberList = new ArrayList<>(this.mEmergencyNumberListFromDatabase);
        mergedEmergencyNumberList.addAll(this.mEmergencyNumberListFromRadio);
        this.mEmergencyNumberListWithPrefix.clear();
        if (this.mEmergencyNumberPrefix.length != 0) {
            this.mEmergencyNumberListWithPrefix.addAll(getEmergencyNumberListWithPrefix(this.mEmergencyNumberListFromRadio));
            this.mEmergencyNumberListWithPrefix.addAll(getEmergencyNumberListWithPrefix(this.mEmergencyNumberListFromDatabase));
        }
        if (!DBG) {
            LocalLog localLog = this.mEmergencyNumberListPrefixLocalLog;
            localLog.log("updateEmergencyNumberList:" + this.mEmergencyNumberListWithPrefix);
        }
        mergedEmergencyNumberList.addAll(this.mEmergencyNumberListWithPrefix);
        mergedEmergencyNumberList.addAll(this.mEmergencyNumberListFromTestMode);
        mergedEmergencyNumberList.addAll(this.mEmergencyNumberListFromFakeEcc);
        EmergencyNumber.mergeSameNumbersInEmergencyNumberList(mergedEmergencyNumberList);
        this.mEmergencyNumberList = mergedEmergencyNumberList;
    }

    public List<EmergencyNumber> getEmergencyNumberList() {
        if (!this.mEmergencyNumberListFromRadio.isEmpty()) {
            return Collections.unmodifiableList(this.mEmergencyNumberList);
        }
        return getEmergencyNumberListFromEccListAndTest();
    }

    public boolean isEmergencyNumber(String number, boolean exactMatch) {
        if (number == null) {
            return false;
        }
        String number2 = PhoneNumberUtils.stripSeparators(number);
        if (!this.mEmergencyNumberListFromRadio.isEmpty()) {
            for (EmergencyNumber num : this.mEmergencyNumberList) {
                if (this.mCountryIso.equals("br") || this.mCountryIso.equals("cl") || this.mCountryIso.equals("ni")) {
                    exactMatch = true;
                }
                if (exactMatch) {
                    if (num.getNumber().equals(number2)) {
                        return true;
                    }
                } else if (number2.startsWith(num.getNumber())) {
                    return true;
                }
            }
            return false;
        } else if (isEmergencyNumberFromEccList(number2, exactMatch) || isEmergencyNumberForTest(number2)) {
            return true;
        } else {
            return false;
        }
    }

    public EmergencyNumber getEmergencyNumber(String emergencyNumber) {
        String emergencyNumber2 = PhoneNumberUtils.stripSeparators(emergencyNumber);
        for (EmergencyNumber num : getEmergencyNumberList()) {
            if (num.getNumber().equals(emergencyNumber2)) {
                return num;
            }
        }
        return null;
    }

    public int getEmergencyServiceCategories(String emergencyNumber) {
        String emergencyNumber2 = PhoneNumberUtils.stripSeparators(emergencyNumber);
        for (EmergencyNumber num : getEmergencyNumberList()) {
            if (num.getNumber().equals(emergencyNumber2) && (num.isFromSources(1) || num.isFromSources(2))) {
                return num.getEmergencyServiceCategoryBitmask();
            }
        }
        return 0;
    }

    public int getEmergencyCallRouting(String emergencyNumber) {
        String emergencyNumber2 = PhoneNumberUtils.stripSeparators(emergencyNumber);
        for (EmergencyNumber num : getEmergencyNumberList()) {
            if (num.getNumber().equals(emergencyNumber2) && num.isFromSources(16)) {
                return num.getEmergencyCallRouting();
            }
        }
        return 0;
    }

    private List<EmergencyNumber> getEmergencyNumberListFromEccList() {
        List<EmergencyNumber> emergencyNumberList = new ArrayList<>();
        int slotId = SubscriptionController.getInstance().getSlotIndex(this.mPhone.getSubId());
        String ecclist = "ril.ecclist";
        if (slotId > 0) {
            ecclist = ecclist + slotId;
        }
        HwEmergencyNumberTrackerMgr eccTrackerMgr = HwTelephonyFactory.getHwEmergencyNumberTrackerMgr();
        if (eccTrackerMgr != null) {
            ecclist = eccTrackerMgr.changeEcclistToHwEcclist(slotId);
        }
        String emergencyNumbers = SystemProperties.get(ecclist, PhoneConfigurationManager.SSSS);
        if (TextUtils.isEmpty(emergencyNumbers)) {
            emergencyNumbers = SystemProperties.get("ro.ril.ecclist");
        }
        if (!TextUtils.isEmpty(emergencyNumbers)) {
            String[] split = emergencyNumbers.split(",");
            for (String emergencyNum : split) {
                if (eccTrackerMgr != null) {
                    emergencyNumberList.add(eccTrackerMgr.getCustLabeledEmergencyNumberForEcclist(this.mEmergencyNumberListFromDatabase, emergencyNum, this.mCountryIso));
                } else {
                    emergencyNumberList.add(getLabeledEmergencyNumberForEcclist(emergencyNum));
                }
            }
        }
        for (String emergencyNum2 : (slotId < 0 ? "112,911,000,08,110,118,119,999" : "112,911").split(",")) {
            emergencyNumberList.add(getLabeledEmergencyNumberForEcclist(emergencyNum2));
        }
        if (this.mEmergencyNumberPrefix.length != 0) {
            emergencyNumberList.addAll(getEmergencyNumberListWithPrefix(emergencyNumberList));
        }
        EmergencyNumber.mergeSameNumbersInEmergencyNumberList(emergencyNumberList);
        return emergencyNumberList;
    }

    private List<EmergencyNumber> getEmergencyNumberListWithPrefix(List<EmergencyNumber> emergencyNumberList) {
        List<EmergencyNumber> emergencyNumberListWithPrefix = new ArrayList<>();
        for (EmergencyNumber num : emergencyNumberList) {
            String[] strArr = this.mEmergencyNumberPrefix;
            for (String prefix : strArr) {
                if (!num.getNumber().startsWith(prefix)) {
                    emergencyNumberListWithPrefix.add(new EmergencyNumber(prefix + num.getNumber(), num.getCountryIso(), num.getMnc(), num.getEmergencyServiceCategoryBitmask(), num.getEmergencyUrns(), num.getEmergencyNumberSourceBitmask(), num.getEmergencyCallRouting()));
                }
            }
        }
        return emergencyNumberListWithPrefix;
    }

    private boolean isEmergencyNumberForTest(String number) {
        String number2 = PhoneNumberUtils.stripSeparators(number);
        for (EmergencyNumber num : this.mEmergencyNumberListFromTestMode) {
            if (num.getNumber().equals(number2)) {
                return true;
            }
        }
        return false;
    }

    private EmergencyNumber getLabeledEmergencyNumberForEcclist(String number) {
        String number2 = PhoneNumberUtils.stripSeparators(number);
        for (EmergencyNumber num : this.mEmergencyNumberListFromDatabase) {
            if (num.getNumber().equals(number2)) {
                return new EmergencyNumber(number2, this.mCountryIso.toLowerCase(), PhoneConfigurationManager.SSSS, num.getEmergencyServiceCategoryBitmask(), new ArrayList(), 16, 0);
            }
        }
        return new EmergencyNumber(number2, PhoneConfigurationManager.SSSS, PhoneConfigurationManager.SSSS, 0, new ArrayList(), 0, 0);
    }

    private boolean isEmergencyNumberFromEccList(String number, boolean useExactMatch) {
        int i = 0;
        if (number == null || PhoneNumberUtils.isUriNumber(number)) {
            return false;
        }
        String number2 = PhoneNumberUtils.extractNetworkPortionAlt(number);
        int slotId = SubscriptionController.getInstance().getSlotIndex(this.mPhone.getSubId());
        String ecclist = "ril.ecclist";
        if (slotId > 0) {
            ecclist = ecclist + slotId;
        }
        HwEmergencyNumberTrackerMgr eccTrackerMgr = HwTelephonyFactory.getHwEmergencyNumberTrackerMgr();
        if (eccTrackerMgr != null) {
            ecclist = eccTrackerMgr.changeEcclistToHwEcclist(slotId);
        }
        String emergencyNumbers = SystemProperties.get(ecclist, PhoneConfigurationManager.SSSS);
        logd("slotId:" + slotId + " country:" + this.mCountryIso + " emergencyNumbers: " + emergencyNumbers);
        if (TextUtils.isEmpty(emergencyNumbers)) {
            emergencyNumbers = SystemProperties.get("ro.ril.ecclist");
        }
        String emergencyNumbers2 = "112,911,000,08,110,118,119,999";
        String emergencyNumbers3 = HwFrameworkFactory.getHwInnerTelephonyManager().custExtraEmergencyNumbers((long) this.mPhone.getSubId(), emergencyNumbers) + "," + (slotId < 0 ? emergencyNumbers2 : "112,911");
        boolean z = true;
        if (!TextUtils.isEmpty(emergencyNumbers3)) {
            String[] split = emergencyNumbers3.split(",");
            int length = split.length;
            int i2 = 0;
            while (i2 < length) {
                String emergencyNum = split[i2];
                if (eccTrackerMgr != null) {
                    emergencyNum = eccTrackerMgr.splitEmergencyNum(emergencyNum);
                }
                if (useExactMatch || this.mCountryIso.equals("br") || this.mCountryIso.equals("cl") || this.mCountryIso.equals("ni")) {
                    if (number2.equals(emergencyNum)) {
                        return true;
                    }
                    for (String prefix : this.mEmergencyNumberPrefix) {
                        if (number2.equals(prefix + emergencyNum)) {
                            return true;
                        }
                    }
                    continue;
                } else if (number2.startsWith(emergencyNum)) {
                    return z;
                } else {
                    String[] strArr = this.mEmergencyNumberPrefix;
                    int length2 = strArr.length;
                    for (int i3 = i; i3 < length2; i3++) {
                        if (number2.startsWith(strArr[i3] + emergencyNum)) {
                            return true;
                        }
                    }
                    continue;
                }
                i2++;
                i = 0;
                z = true;
            }
            return false;
        } else if (HwFrameworkFactory.getHwInnerTelephonyManager().skipHardcodeEmergencyNumbers()) {
            return false;
        } else {
            logd("System property doesn't provide any emergency numbers. Use embedded logic for determining ones.");
            if (slotId >= 0) {
                emergencyNumbers2 = "112,911";
            }
            String[] split2 = emergencyNumbers2.split(",");
            for (String emergencyNum2 : split2) {
                if (useExactMatch) {
                    if (number2.equals(emergencyNum2)) {
                        return true;
                    }
                    for (String prefix2 : this.mEmergencyNumberPrefix) {
                        if (number2.equals(prefix2 + emergencyNum2)) {
                            return true;
                        }
                    }
                    continue;
                } else if (number2.startsWith(emergencyNum2)) {
                    return true;
                } else {
                    for (String prefix3 : this.mEmergencyNumberPrefix) {
                        if (number2.equals(prefix3 + emergencyNum2)) {
                            return true;
                        }
                    }
                    continue;
                }
            }
            if (this.mCountryIso == null) {
                return false;
            }
            ShortNumberInfo info = ShortNumberInfo.getInstance();
            if (useExactMatch) {
                if (info.isEmergencyNumber(number2, this.mCountryIso.toUpperCase())) {
                    return true;
                }
                for (String prefix4 : this.mEmergencyNumberPrefix) {
                    if (info.isEmergencyNumber(prefix4 + number2, this.mCountryIso.toUpperCase())) {
                        return true;
                    }
                }
                return false;
            } else if (info.connectsToEmergencyNumber(number2, this.mCountryIso.toUpperCase())) {
                return true;
            } else {
                for (String prefix5 : this.mEmergencyNumberPrefix) {
                    if (info.connectsToEmergencyNumber(prefix5 + number2, this.mCountryIso.toUpperCase())) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public void executeEmergencyNumberTestModeCommand(int action, EmergencyNumber num) {
        obtainMessage(3, action, 0, num).sendToTarget();
    }

    private void updateEmergencyNumberListTestModeAndNotify(int action, EmergencyNumber num) {
        if (action == 1) {
            if (!isEmergencyNumber(num.getNumber(), true)) {
                this.mEmergencyNumberListFromTestMode.add(num);
            }
        } else if (action == 3) {
            this.mEmergencyNumberListFromTestMode.clear();
        } else if (action == 2) {
            this.mEmergencyNumberListFromTestMode.remove(num);
        } else {
            loge("updateEmergencyNumberListTestModeAndNotify: Unexpected action in test mode.");
            return;
        }
        if (!DBG) {
            LocalLog localLog = this.mEmergencyNumberListTestModeLocalLog;
            localLog.log("updateEmergencyNumberListTestModeAndNotify:" + this.mEmergencyNumberListFromTestMode);
        }
        updateEmergencyNumberList();
        if (!DBG) {
            LocalLog localLog2 = this.mEmergencyNumberListLocalLog;
            localLog2.log("updateEmergencyNumberListTestModeAndNotify:" + this.mEmergencyNumberList);
        }
        notifyEmergencyNumberList();
    }

    private List<EmergencyNumber> getEmergencyNumberListFromEccListAndTest() {
        List<EmergencyNumber> mergedEmergencyNumberList = getEmergencyNumberListFromEccList();
        mergedEmergencyNumberList.addAll(getEmergencyNumberListTestMode());
        return mergedEmergencyNumberList;
    }

    public List<EmergencyNumber> getEmergencyNumberListTestMode() {
        return Collections.unmodifiableList(this.mEmergencyNumberListFromTestMode);
    }

    @VisibleForTesting
    public List<EmergencyNumber> getRadioEmergencyNumberList() {
        return new ArrayList(this.mEmergencyNumberListFromRadio);
    }

    /* access modifiers changed from: private */
    public static void logd(String str) {
        Rlog.i(TAG, str);
    }

    private static void loge(String str) {
        Rlog.e(TAG, str);
    }

    private void writeUpdatedEmergencyNumberListMetrics(List<EmergencyNumber> updatedEmergencyNumberList) {
        if (updatedEmergencyNumberList != null) {
            for (EmergencyNumber num : updatedEmergencyNumberList) {
                TelephonyMetrics.getInstance().writeEmergencyNumberUpdateEvent(this.mPhone.getPhoneId(), num);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        ipw.println(" Hal Version:" + this.mPhone.getHalVersion());
        ipw.println(" ========================================= ");
        ipw.println("mEmergencyNumberListDatabaseLocalLog:");
        ipw.increaseIndent();
        this.mEmergencyNumberListDatabaseLocalLog.dump(fd, pw, args);
        ipw.decreaseIndent();
        ipw.println(" ========================================= ");
        ipw.println("mEmergencyNumberListRadioLocalLog:");
        ipw.increaseIndent();
        this.mEmergencyNumberListRadioLocalLog.dump(fd, pw, args);
        ipw.decreaseIndent();
        ipw.println(" ========================================= ");
        ipw.println("mEmergencyNumberListPrefixLocalLog:");
        ipw.increaseIndent();
        this.mEmergencyNumberListPrefixLocalLog.dump(fd, pw, args);
        ipw.decreaseIndent();
        ipw.println(" ========================================= ");
        ipw.println("mEmergencyNumberListTestModeLocalLog:");
        ipw.increaseIndent();
        this.mEmergencyNumberListTestModeLocalLog.dump(fd, pw, args);
        ipw.decreaseIndent();
        ipw.println(" ========================================= ");
        ipw.println("mEmergencyNumberListLocalLog (valid >= 1.4 HAL):");
        ipw.increaseIndent();
        this.mEmergencyNumberListLocalLog.dump(fd, pw, args);
        ipw.decreaseIndent();
        ipw.println(" ========================================= ");
        int slotId = SubscriptionController.getInstance().getSlotIndex(this.mPhone.getSubId());
        String ecclist = "ril.ecclist";
        if (slotId > 0) {
            ecclist = ecclist + slotId;
        }
        ipw.println(" ril.ecclist: " + SystemProperties.get(ecclist, PhoneConfigurationManager.SSSS));
        ipw.println(" ========================================= ");
        ipw.println("Emergency Number List for Phone(" + this.mPhone.getPhoneId() + ")");
        ipw.increaseIndent();
        ipw.println(getEmergencyNumberList());
        ipw.decreaseIndent();
        ipw.println(" ========================================= ");
        ipw.flush();
    }

    public void updateFakeEccEmergencyNumberListAndNotify(List<EmergencyNumber> emergencyNumberListFromFakeEcc) {
        if (emergencyNumberListFromFakeEcc == null) {
            loge("emergencyNumberListFromFakeEcc is null, EmergencyNumberList not notified");
            return;
        }
        Collections.sort(emergencyNumberListFromFakeEcc);
        logd("updateFakeEccEmergencyNumberListAndNotify(): receiving eccnum size" + emergencyNumberListFromFakeEcc.size());
        if (!emergencyNumberListFromFakeEcc.equals(this.mEmergencyNumberListFromFakeEcc)) {
            EmergencyNumber.mergeSameNumbersInEmergencyNumberList(emergencyNumberListFromFakeEcc);
            this.mEmergencyNumberListFromFakeEcc = emergencyNumberListFromFakeEcc;
            updateEmergencyNumberList();
            notifyEmergencyNumberList();
        }
    }
}
