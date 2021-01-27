package com.android.internal.telephony.dataconnection;

import android.util.Pair;
import android.util.Xml;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.huawei.android.internal.util.XmlUtilsEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwDataRetryManager {
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String CUST_FILE_NAME = "data_retry_strategy.xml";
    private static final String CUST_FILE_PATH = "/data/cust/xml/";
    private static final String DATA_RETRY_STRATEGY_DOCUMENT = "dataRetryStrategyList";
    private static final int DEFAULT_BACKOFF_TIME = 120000;
    private static final int DEFAULT_RETRY_STRATEGY_INTERVAL = 5000;
    private static final int DEFAULT_RETRY_STRATEGY_TIMEIND = 0;
    private static final int DEFAULT_RETRY_STRATEGY_TIMES = 3;
    private static final int EXPONENTIAL_BACK_OFF = 1;
    private static final String HW_CFG_POLICY_PATH = "hwCfgPolicyPath";
    private static final int IMMEDIATE_REEST_INTERVAL = 50;
    private static final int INVALID_RETRY_ID = -1;
    private static final int MAXIMUM_RETRY_INTERVAL = 720000;
    private static final String NODE_CAUSE_LIST = "causeList";
    private static final String NODE_RETRY_POLICY = "retryPolicy";
    private static final String NODE_RETRY_STRATEGY = "retryStrategy";
    private static final long NO_RETRY = -1;
    private static final long NO_SUGGESTED_RETRY_DELAY = -2;
    private static final int RETRY_ACTION_CONTINUE = 1;
    private static final int RETRY_ACTION_CYCLE = 2;
    private static final int RETRY_ACTION_DISABLE_NR = 3;
    private static final int RETRY_ACTION_IMMEDIATE_REEST = 4;
    private static final int RETRY_ACTION_NO_ACTION = 0;
    private static final String SYSTEM_FILE_PATH = "/system/etc/xml/";
    private static final int TIME_RANDOMIZED = 2;
    private static final int TIME_UNIT_SECOND_TO_MS = 1000;
    private String LOG_TAG = "HwDataRetryManager";
    private final ArrayList<Pair<Integer, Integer>> mCauseStrategyPair = new ArrayList<>();
    private DcTrackerEx mDcTrackerBase;
    private int mDefaultRetryId = -1;
    private PhoneExt mPhone;
    private final HashMap<Integer, ArrayList<RetryRec>> mRetryStrategies = new HashMap<>();
    private final HashMap<String, RetryStrategy> mRetryStrategyByApnType = new HashMap<>();
    private Random mRng = new Random();

    public HwDataRetryManager(DcTrackerEx dcTrackerBase, PhoneExt phoneExt) {
        this.mDcTrackerBase = dcTrackerBase;
        this.mPhone = phoneExt;
    }

    private Pair<Boolean, Integer> parseNonNegativeInt(String name, String stringValue) {
        Pair<Boolean, Integer> retVal;
        try {
            int value = Integer.parseInt(stringValue);
            retVal = new Pair<>(Boolean.valueOf(validateNonNegativeInt(name, value)), Integer.valueOf(value));
        } catch (NumberFormatException e) {
            loge(name + " bad value: " + stringValue, e);
            retVal = new Pair<>(false, 0);
        }
        logd("parseNonNetativeInt: " + name + ", " + stringValue + ", " + retVal.first + ", " + retVal.second);
        return retVal;
    }

    private boolean validateNonNegativeInt(String name, int value) {
        boolean retVal;
        if (value < 0) {
            loge(name + " bad value: is < 0");
            retVal = false;
        } else {
            retVal = true;
        }
        logd("validateNonNegative: " + name + ", " + value + ", " + retVal);
        return retVal;
    }

    private void resetRetryStrategy(String apnType) {
        RetryStrategy retryStrategy = this.mRetryStrategyByApnType.get(apnType);
        if (retryStrategy != null) {
            retryStrategy.mRetryCount = 0;
            retryStrategy.mLastRetryId = -1;
            retryStrategy.mNextAction = 0;
            retryStrategy.mBackOffTime = 0;
        }
    }

    public void resetAllRetryStrategy() {
        for (RetryStrategy retryStrategy : this.mRetryStrategyByApnType.values()) {
            retryStrategy.mRetryCount = 0;
            retryStrategy.mLastRetryId = -1;
            retryStrategy.mNextAction = 0;
            retryStrategy.mBackOffTime = 0;
        }
    }

    private int getFailCauseRetryId(int failCause) {
        Iterator<Pair<Integer, Integer>> it = this.mCauseStrategyPair.iterator();
        while (it.hasNext()) {
            Pair<Integer, Integer> pair = it.next();
            if (failCause == ((Integer) pair.first).intValue()) {
                return ((Integer) pair.second).intValue();
            }
        }
        logd("getFailCauseRetryId: no retry strategy for failcause " + failCause + ", use default retry strategy: " + this.mDefaultRetryId);
        return this.mDefaultRetryId;
    }

    private void calcRetryInterval(RetryStrategy retryStrategy, RetryRec retryRec, int nextAction, int retryCount) {
        if (retryStrategy.mBackOffTime == 0) {
            retryStrategy.mBackOffTime = (long) retryRec.mBackOffTime;
        }
        if (nextAction == 3) {
            retryStrategy.mRetryInterval = retryStrategy.mBackOffTime;
        } else if (nextAction == 4) {
            retryStrategy.mRetryInterval = 50;
        } else {
            retryStrategy.mRetryInterval = (long) retryRec.mDelayTime;
            int i = retryRec.mAdditionalTimeInd;
            if (i == 1) {
                for (int i2 = 0; i2 < retryCount - 1; i2++) {
                    retryStrategy.mRetryInterval *= 2;
                }
            } else if (i == 2) {
                retryStrategy.mRetryInterval = this.mRng.nextLong() * retryStrategy.mRetryInterval;
            } else if (i == 3) {
                for (int i3 = 0; i3 < retryCount - 1; i3++) {
                    retryStrategy.mRetryInterval *= 2;
                }
                retryStrategy.mRetryInterval = this.mRng.nextLong() * retryStrategy.mRetryInterval;
            }
        }
    }

    private RetryStrategy initializeRetryStrategy(ApnContextEx apnContext) {
        if (apnContext == null) {
            return null;
        }
        String apnType = apnContext.getApnType();
        RetryStrategy retryStrategy = this.mRetryStrategyByApnType.get(apnType);
        if (retryStrategy == null) {
            retryStrategy = new RetryStrategy(0, 0, -1, 0, 0);
            this.mRetryStrategyByApnType.put(apnType, retryStrategy);
            logd("InitializeRetryStrategy: create retry strategy for " + apnContext);
        }
        long modemSuggestedDelay = apnContext.getModemSuggestedDelay();
        apnContext.setModemSuggestedDelay((long) NO_SUGGESTED_RETRY_DELAY);
        int currRetryId = getFailCauseRetryId(apnContext.getFailCause());
        if (currRetryId != retryStrategy.mLastRetryId) {
            resetRetryStrategy(apnType);
            retryStrategy.mLastRetryId = currRetryId;
        }
        retryStrategy.mRetryCount++;
        retryStrategy.mBackOffTime = getModemSuggestedDelay(modemSuggestedDelay);
        logd("InitializeRetryStrategy: retryCount = " + retryStrategy.mRetryCount + " nextAction = " + retryStrategy.mNextAction + " interval = " + retryStrategy.mRetryInterval + " backoff time = " + retryStrategy.mBackOffTime + " retryId = " + retryStrategy.mLastRetryId);
        return retryStrategy;
    }

    private long getModemSuggestedDelay(long modemSuggestedDelay) {
        if (modemSuggestedDelay <= 0) {
            return 0;
        }
        long backOffTime = 1000 * modemSuggestedDelay;
        if (backOffTime < 120000) {
            return 120000;
        }
        if (backOffTime > 720000) {
            return 720000;
        }
        return backOffTime;
    }

    private void setRetryAction(RetryStrategy retryStrategy, int nextAction) {
        retryStrategy.mNextAction = nextAction;
        if (nextAction != 3) {
            return;
        }
        if (this.mDcTrackerBase.getDataRat() != 20 || !HuaweiTelephonyConfigs.isHisiPlatform()) {
            logd("setDataRetryAction:action=disableNr, changed to continue");
            retryStrategy.mNextAction = 1;
        }
    }

    private void updateRetryCount(RetryStrategy retryStrategy) {
        if (retryStrategy.mNextAction == 2 || retryStrategy.mNextAction == 4) {
            logd("updateRetryCount: orig retry count = " + retryStrategy.mRetryCount + " mNextAction = " + retryStrategy.mNextAction);
            retryStrategy.mRetryCount = 0;
        }
    }

    public void updateDataRetryStategy(ApnContextEx apnContext) {
        RetryStrategy retryStrategy = initializeRetryStrategy(apnContext);
        if (retryStrategy == null || retryStrategy.mLastRetryId == -1) {
            logd("updateDataRetryStategy: retryStrategy is null or retry id is invalid");
            return;
        }
        ArrayList<RetryRec> retryRecList = this.mRetryStrategies.get(Integer.valueOf(retryStrategy.mLastRetryId));
        int accRetryTimes = 0;
        int size = retryRecList != null ? retryRecList.size() : 0;
        int i = 0;
        while (true) {
            if (i >= size) {
                break;
            }
            RetryRec retryRec = retryRecList.get(i);
            accRetryTimes += retryRec.mRetryTimes;
            if (retryStrategy.mRetryCount < accRetryTimes) {
                setRetryAction(retryStrategy, 1);
                calcRetryInterval(retryStrategy, retryRec, 1, retryStrategy.mRetryCount - accRetryTimes);
                break;
            } else if (retryStrategy.mRetryCount == accRetryTimes) {
                setRetryAction(retryStrategy, retryRec.mNextAction);
                calcRetryInterval(retryStrategy, retryRec, retryRec.mNextAction, retryRec.mRetryTimes);
                break;
            } else if (i == size - 1) {
                setRetryAction(retryStrategy, retryRec.mNextAction);
                calcRetryInterval(retryStrategy, retryRec, retryRec.mNextAction, retryRec.mRetryTimes);
                break;
            } else {
                i++;
            }
        }
        updateRetryCount(retryStrategy);
        logd("updateDataRetryStategy: retryCount=" + retryStrategy.mRetryCount + " nextAction=" + retryStrategy.mNextAction + " interval=" + retryStrategy.mRetryInterval + " backoff time=" + retryStrategy.mBackOffTime + " retryId=" + retryStrategy.mLastRetryId);
    }

    public int getDataRetryAction(ApnContextEx apnContext) {
        if (apnContext == null) {
            loge("getDataRetryAction with null apnContext");
            return 0;
        }
        RetryStrategy retryStrategy = this.mRetryStrategyByApnType.get(apnContext.getApnType());
        if (retryStrategy != null) {
            logd("getDataRetryAction: retry action = " + retryStrategy.mNextAction);
            return retryStrategy.mNextAction;
        }
        loge("getDataRetryAction with null retryStrategy");
        return 0;
    }

    public long getDataRetryDelay(ApnContextEx apnContext) {
        if (apnContext == null) {
            loge("getDataRetryDelay with null apnContext");
            return NO_RETRY;
        }
        RetryStrategy retryStrategy = this.mRetryStrategyByApnType.get(apnContext.getApnType());
        if (retryStrategy != null) {
            logd("getDataRetryDelay: retry interval = " + retryStrategy.mRetryInterval);
            return retryStrategy.mRetryInterval;
        }
        loge("getDataRetryDelay with null retryStrategy");
        return 50;
    }

    private void parseCauseList(String causeStr, int retryId) {
        logd("parseCauseList: retryId=" + retryId + " causeList=" + causeStr);
        if (causeStr != null) {
            String[] splitStr = causeStr.split(",");
            for (int j = 0; j < splitStr.length; j++) {
                splitStr[j] = splitStr[j].trim();
                Pair<Boolean, Integer> value = parseNonNegativeInt("Cause", splitStr[j]);
                if (((Boolean) value.first).booleanValue() && this.mDcTrackerBase.isFailCauseValid(((Integer) value.second).intValue())) {
                    logd("parseCauseList: valid cause");
                    this.mCauseStrategyPair.add(new Pair<>(value.second, Integer.valueOf(retryId)));
                }
            }
        }
    }

    private void parseRetryPolicy(XmlPullParser xmlParser, ArrayList<RetryRec> retryRecArr) {
        if (xmlParser == null || retryRecArr == null) {
            loge("parseRetryPolicy: invalid parameter.");
            return;
        }
        RetryRec rec = new RetryRec(0, 0, 0, 1, 0);
        Pair<Boolean, Integer> value = parseNonNegativeInt("delayTime", xmlParser.getAttributeValue(null, "interval"));
        rec.mDelayTime = ((Boolean) value.first).booleanValue() ? ((Integer) value.second).intValue() * TIME_UNIT_SECOND_TO_MS : DEFAULT_RETRY_STRATEGY_INTERVAL;
        int i = rec.mDelayTime;
        int i2 = MAXIMUM_RETRY_INTERVAL;
        if (i <= MAXIMUM_RETRY_INTERVAL) {
            i2 = rec.mDelayTime;
        }
        rec.mDelayTime = i2;
        Pair<Boolean, Integer> value2 = parseNonNegativeInt("intervalInd", xmlParser.getAttributeValue(null, "intervalInd"));
        rec.mAdditionalTimeInd = ((Boolean) value2.first).booleanValue() ? ((Integer) value2.second).intValue() : 0;
        Pair<Boolean, Integer> value3 = parseNonNegativeInt("retryTimes", xmlParser.getAttributeValue(null, "times"));
        rec.mRetryTimes = ((Boolean) value3.first).booleanValue() ? ((Integer) value3.second).intValue() : 3;
        Pair<Boolean, Integer> value4 = parseNonNegativeInt("nextAction", xmlParser.getAttributeValue(null, "action"));
        rec.mNextAction = ((Boolean) value4.first).booleanValue() ? ((Integer) value4.second).intValue() : 1;
        if (rec.mNextAction == 3) {
            Pair<Boolean, Integer> value5 = parseNonNegativeInt("backOffTime", xmlParser.getAttributeValue(null, "backOffTime"));
            rec.mBackOffTime = ((Boolean) value5.first).booleanValue() ? ((Integer) value5.second).intValue() * TIME_UNIT_SECOND_TO_MS : DEFAULT_BACKOFF_TIME;
        }
        retryRecArr.add(rec);
        logd("parseRetryPolicy: delayTime=" + rec.mDelayTime + " intervalInd=" + rec.mAdditionalTimeInd + " retryTimes=" + rec.mRetryTimes + " nextAction=" + rec.mNextAction + "back-off time=" + rec.mBackOffTime);
    }

    private void setDefaultRetryId(String retryStrategyType, int retryStrategyId) {
        if (retryStrategyType.equals("default")) {
            logd("setDefaultRetryId mDefaultRetryId = " + this.mDefaultRetryId);
            this.mDefaultRetryId = retryStrategyId;
        }
    }

    private void parseDataRetryStrategy(XmlPullParser confparser) throws XmlPullParserException, IOException, NumberFormatException {
        if (confparser != null) {
            XmlUtilsEx.beginDocument(confparser, DATA_RETRY_STRATEGY_DOCUMENT);
            int retryStrategyId = 0;
            ArrayList<RetryRec> retryRecArr = null;
            int eventType = confparser.next();
            while (eventType != 1) {
                if (eventType == 2) {
                    String tagName = confparser.getName();
                    if (tagName.equals(NODE_RETRY_STRATEGY)) {
                        try {
                            retryStrategyId = Integer.parseInt(confparser.getAttributeValue(null, ATTRIBUTE_ID));
                        } catch (NumberFormatException e) {
                            loge("parseDataRetryStrategy: NumberFormatException");
                        }
                        String retryStrategyType = confparser.getAttributeValue(null, ATTRIBUTE_TYPE);
                        retryRecArr = new ArrayList<>();
                        setDefaultRetryId(retryStrategyType, retryStrategyId);
                        logd("parseDataRetryStrategy: retryId: " + retryStrategyId + " retryType:" + retryStrategyType);
                    }
                    if (tagName.equals(NODE_CAUSE_LIST)) {
                        parseCauseList(confparser.nextText(), retryStrategyId);
                    }
                    if (tagName.equals(NODE_RETRY_POLICY)) {
                        parseRetryPolicy(confparser, retryRecArr);
                    }
                } else if (eventType == 3 && confparser.getName().equals(NODE_RETRY_STRATEGY) && retryRecArr != null) {
                    this.mRetryStrategies.put(Integer.valueOf(retryStrategyId), retryRecArr);
                    retryRecArr = null;
                }
                eventType = confparser.next();
            }
        }
    }

    private boolean loadDataRetryStrategy(String filePath) {
        File confFile;
        if (HW_CFG_POLICY_PATH.equals(filePath)) {
            try {
                File cfg = HwCfgFilePolicy.getCfgFile("xml/data_retry_strategy.xml", 0, this.mPhone.getPhoneId());
                if (cfg == null) {
                    return false;
                }
                confFile = cfg;
            } catch (NoClassDefFoundError e) {
                logw("NoClassDefFoundError : HwCfgFilePolicy ");
                return false;
            }
        } else {
            confFile = new File(filePath, CUST_FILE_NAME);
        }
        FileInputStream fin = null;
        try {
            XmlPullParser confparser = Xml.newPullParser();
            if (confparser != null) {
                fin = new FileInputStream(confFile);
                confparser.setInput(fin, "UTF-8");
                parseDataRetryStrategy(confparser);
            }
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e2) {
                    return false;
                }
            }
            logd("DataRetryStrategy file is successfully loaded from filePath:" + filePath);
            return true;
        } catch (FileNotFoundException e3) {
            logd("File not found");
            if (0 != 0) {
                try {
                    fin.close();
                } catch (IOException e4) {
                    return false;
                }
            }
            return false;
        } catch (IOException | NumberFormatException | XmlPullParserException e5) {
            loge("loadDataRetryStrategy: fail to load config file ", e5);
            if (0 != 0) {
                try {
                    fin.close();
                } catch (IOException e6) {
                    return false;
                }
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fin.close();
                } catch (IOException e7) {
                    return false;
                }
            }
            throw th;
        }
    }

    public void configDataRetryStrategy() {
        resetAllRetryStrategy();
        if (loadDataRetryStrategy(HW_CFG_POLICY_PATH)) {
            logd("loadDataRetryStrategy from hwCfgPolicyPath success");
        } else if (loadDataRetryStrategy(CUST_FILE_PATH)) {
            logd("loadDataRetryStrategy from cust success");
        } else if (loadDataRetryStrategy(SYSTEM_FILE_PATH)) {
            logd("loadDataRetryStrategy from system/etc success");
        } else {
            logd("can't find data_retry_strategy.xml, load failed!");
        }
    }

    public void setLogTagSuffix(String tagSuffix) {
        this.LOG_TAG = "HwDataRetryManager" + tagSuffix;
    }

    private void logd(String msg) {
        RlogEx.i(this.LOG_TAG, msg);
    }

    private void logw(String msg) {
        RlogEx.w(this.LOG_TAG, msg);
    }

    private void loge(String msg) {
        RlogEx.e(this.LOG_TAG, msg);
    }

    private void loge(String msg, Throwable tr) {
        RlogEx.e(this.LOG_TAG, msg, tr);
    }

    public static class RetryRec {
        int mAdditionalTimeInd;
        int mBackOffTime;
        int mDelayTime;
        int mNextAction;
        int mRetryTimes;

        RetryRec(int delayTime, int additionalTimeInd, int retryTimes, int nextAction, int backOffTime) {
            this.mDelayTime = delayTime;
            this.mAdditionalTimeInd = additionalTimeInd;
            this.mRetryTimes = retryTimes;
            this.mNextAction = nextAction;
            this.mBackOffTime = backOffTime;
        }
    }

    /* access modifiers changed from: private */
    public static class RetryStrategy {
        long mBackOffTime;
        int mLastRetryId;
        int mNextAction;
        int mRetryCount;
        long mRetryInterval;

        RetryStrategy(int nextAction, int retryCount, int lastRetryId, long retryInterval, long backOffTime) {
            this.mNextAction = nextAction;
            this.mRetryCount = retryCount;
            this.mLastRetryId = lastRetryId;
            this.mRetryInterval = retryInterval;
            this.mBackOffTime = backOffTime;
        }
    }
}
