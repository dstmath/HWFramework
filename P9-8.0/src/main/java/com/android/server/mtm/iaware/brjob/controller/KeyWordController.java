package com.android.server.mtm.iaware.brjob.controller;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.Bundle;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;

public class KeyWordController extends AwareStateController {
    private static final String CONDITION_EXTRA = "Extra";
    private static final String CONDITION_KEYWORD = "KeyWord";
    private static final String EXTRA_BOOLEAN = "boolean";
    private static final String EXTRA_CHAR = "char";
    private static final String EXTRA_DOUBLE = "double";
    private static final String EXTRA_FLOAT = "float";
    private static final String EXTRA_INT = "int";
    private static final String EXTRA_LONG = "long";
    private static final String EXTRA_OBJECT = "Object";
    private static final String EXTRA_SHORT = "short";
    private static final int EXTRA_SPLIT_LENGTH = 3;
    private static final String EXTRA_STRING = "String";
    private static final String KEYWORD_HOST = "host";
    private static final String KEYWORD_MIME = "mimeType";
    private static final String KEYWORD_PACKAGE_NAME = "packageName";
    private static final String KEYWORD_PATH = "path";
    private static final String KEYWORD_PATH_PATTERN = "pathPattern";
    private static final String KEYWORD_PATH_PREFIX = "pathPrefix";
    private static final String KEYWORD_PORT = "port";
    private static final String KEYWORD_SCHEME = "scheme";
    private static final int KEYWORD_SPLIT_LENGTH = 2;
    private static final int KEYWORD_SPLIT_PACKAGE_LENGTH = 3;
    private static final int KEYWORD_SPLIT_PACKAGE_VALUE_INDEX = 2;
    private static final String KEYWORD_SSP = "ssp";
    private static final String KEYWORD_SSP_PATTERN = "sspPattern";
    private static final String KEYWORD_SSP_PREFIX = "sspPrefix";
    private static final String TAG = "KeyWordController";
    private static KeyWordController mSingleton;
    private static Object sCreationLock = new Object();

    public static KeyWordController get(AwareJobSchedulerService jms) {
        KeyWordController keyWordController;
        synchronized (sCreationLock) {
            if (mSingleton == null) {
                mSingleton = new KeyWordController(jms, jms.getContext(), jms.getLock());
            }
            keyWordController = mSingleton;
        }
        return keyWordController;
    }

    private KeyWordController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
    }

    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null) {
            if (job.hasConstraint("KeyWord") || job.hasConstraint("Extra")) {
                Intent intent = job.getIntent();
                if (this.DEBUG) {
                    AwareLog.i(TAG, "iaware_brjob awarejobstatus, intent: " + intent + ", extras: " + (intent == null ? "null" : intent.getExtras()));
                }
                if (job.hasConstraint("KeyWord")) {
                    checkKeyword(job);
                } else if (job.hasConstraint("Extra")) {
                    checkExtra(job);
                }
            }
        }
    }

    private void checkKeyword(AwareJobStatus job) {
        String filterValue = job.getActionFilterValue("KeyWord");
        if (this.DEBUG) {
            AwareLog.i(TAG, "iaware_brjob checkKeyword: " + filterValue);
        }
        if (TextUtils.isEmpty(filterValue)) {
            if (this.DEBUG) {
                AwareLog.w(TAG, "iaware_brjob keyword config error!");
            }
            job.setSatisfied("KeyWord", false);
            return;
        }
        Intent intent = job.getIntent();
        if (intent == null) {
            AwareLog.w(TAG, "iaware_brjob intent is null.");
            job.setSatisfied("KeyWord", false);
            return;
        }
        String[] keywords = filterValue.split("[\\[\\]]");
        IntentFilter filter = new IntentFilter(job.getAction());
        String host = null;
        String port = null;
        boolean specialFormat = false;
        int i = 0;
        while (i < keywords.length) {
            if (!(keywords[i] == null || keywords[i].trim().length() == 0 || ":".equals(keywords[i].trim()))) {
                String[] values = keywords[i].split("[:]");
                String[] pkgValues = keywords[i].split("[:@]");
                if (this.DEBUG) {
                    AwareLog.i(TAG, "iaware_brjob config keyword: " + keywords[i]);
                }
                if (pkgValues.length == 3) {
                    specialFormat = true;
                    if (!"packageName".equals(pkgValues[0])) {
                        AwareLog.e(TAG, "iaware_brjob KeyWord value format is wrong: " + job.getComponentName());
                    } else if (checkPackgeKeyword(job, pkgValues, intent)) {
                        return;
                    }
                } else if (values.length != 2) {
                    AwareLog.e(TAG, "iaware_brjob KeyWord value format is wrong: " + job.getComponentName());
                    job.setSatisfied("KeyWord", false);
                    return;
                } else {
                    String key = values[0];
                    String value = values[1];
                    if (!(TextUtils.isEmpty(key) || TextUtils.isEmpty(value))) {
                        if (KEYWORD_SCHEME.equals(key)) {
                            filter.addDataScheme(value);
                        } else if (KEYWORD_HOST.equals(key)) {
                            host = value;
                        } else if (KEYWORD_PORT.equals(key)) {
                            port = value;
                        } else if (KEYWORD_MIME.equals(key)) {
                            try {
                                filter.addDataType(value);
                            } catch (MalformedMimeTypeException e) {
                                AwareLog.e(TAG, "iaware_brjob invalid mimeType!");
                            }
                        } else if ("path".equals(key)) {
                            filter.addDataPath(value, 0);
                        } else if (KEYWORD_PATH_PREFIX.equals(key)) {
                            filter.addDataPath(value, 1);
                        } else if (KEYWORD_PATH_PATTERN.equals(key)) {
                            filter.addDataPath(value, 2);
                        } else if (KEYWORD_SSP.equals(key)) {
                            filter.addDataSchemeSpecificPart(value, 0);
                        } else if (KEYWORD_SSP_PREFIX.equals(key)) {
                            filter.addDataSchemeSpecificPart(value, 1);
                        } else if (KEYWORD_SSP_PATTERN.equals(key)) {
                            filter.addDataSchemeSpecificPart(value, 2);
                        } else {
                            AwareLog.e(TAG, "iaware_brjob invalid key: " + key);
                        }
                    }
                }
            }
            i++;
        }
        if (!specialFormat) {
            if (host != null) {
                filter.addDataAuthority(host, port);
            }
            int match = filter.match(intent.getAction(), job.getHwBroadcastRecord().getResolvedType(), intent.getScheme(), intent.getData(), null, TAG);
            if (this.DEBUG) {
                AwareLog.i(TAG, "iaware_brjob filter match: " + intent.getAction() + ", " + job.getHwBroadcastRecord().getResolvedType() + ", " + intent.getScheme() + ", " + intent.getData() + ", result: " + match);
            }
            if (match >= 0) {
                job.setSatisfied("KeyWord", true);
            } else {
                job.setSatisfied("KeyWord", false);
            }
        }
    }

    private boolean checkPackgeKeyword(AwareJobStatus job, String[] pkgValues, Intent intent) {
        if (intent.getData() == null) {
            AwareLog.e(TAG, "intent data is null.");
            job.setSatisfied("KeyWord", false);
            return false;
        }
        boolean result;
        String pkgName = pkgValues[2];
        String ssp = intent.getData().getSchemeSpecificPart();
        if (this.DEBUG) {
            String str;
            String str2 = TAG;
            StringBuilder append = new StringBuilder().append("iaware_brjob checkPackgeKeyword: ").append(Arrays.toString(pkgValues)).append(", ssp: ");
            if (ssp == null) {
                str = "null";
            } else {
                str = ssp;
            }
            AwareLog.i(str2, append.append(str).toString());
        }
        if (ssp == null || pkgName == null || !ssp.contains(pkgName)) {
            result = false;
        } else {
            result = true;
        }
        job.setSatisfied("KeyWord", result);
        return result;
    }

    private void checkExtra(AwareJobStatus job) {
        Intent intent = job.getIntent();
        if (intent == null) {
            AwareLog.e(TAG, "iaware_brjob intent is null.");
            job.setSatisfied("Extra", false);
            return;
        }
        String filterValue = job.getActionFilterValue("Extra");
        if (TextUtils.isEmpty(filterValue)) {
            if (this.DEBUG) {
                AwareLog.w(TAG, "iaware_brjob extra config error!");
            }
            job.setSatisfied("Extra", false);
            return;
        }
        if (this.DEBUG) {
            AwareLog.w(TAG, "iaware_brjob checkExtra: " + filterValue);
        }
        String[] extras = filterValue.split("[\\[\\]]");
        boolean hasMatch = false;
        int i = 0;
        while (i < extras.length) {
            if (!(extras[i] == null || extras[i].trim().length() == 0 || ":".equals(extras[i].trim()))) {
                if (this.DEBUG) {
                    AwareLog.i(TAG, "iaware_brjob compare extra: " + extras[i]);
                }
                String[] values = extras[i].split("[:@]");
                if (values.length != 3) {
                    AwareLog.e(TAG, "iaware_brjob extra value length is wrong.");
                    job.setSatisfied("Extra", false);
                    return;
                } else if (match(values[0], values[1], values[2], intent)) {
                    hasMatch = true;
                    break;
                }
            }
            i++;
        }
        if (hasMatch) {
            job.setSatisfied("Extra", true);
        } else {
            job.setSatisfied("Extra", false);
        }
    }

    public void maybeStopTrackingJobLocked(AwareJobStatus jobStatus) {
        if (jobStatus != null && this.DEBUG) {
            AwareLog.i(TAG, "iaware_brjob no tracked jobStatus.");
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    KeyWordController iaware_brjob nothing to dump.");
        }
    }

    private boolean match(String key, String type, String value, Intent intent) {
        if (type.equals(EXTRA_BOOLEAN)) {
            return matchBoolean(key, value, intent);
        }
        if (type.equals(EXTRA_INT)) {
            return matchInt(key, value, intent);
        }
        if (type.equals(EXTRA_STRING)) {
            return matchString(key, value, intent);
        }
        if (type.equals(EXTRA_CHAR)) {
            return matchChar(key, value, intent);
        }
        if (type.equals(EXTRA_SHORT)) {
            return matchShort(key, value, intent);
        }
        if (type.equals(EXTRA_LONG)) {
            return matchLong(key, value, intent);
        }
        if (type.equals(EXTRA_DOUBLE)) {
            return matchDouble(key, value, intent);
        }
        if (type.equals(EXTRA_FLOAT)) {
            return matchFloat(key, value, intent);
        }
        if (type.equals(EXTRA_OBJECT)) {
            return matchObject(key, value, intent);
        }
        AwareLog.e(TAG, "iaware_brjob type is error");
        return false;
    }

    private boolean matchBoolean(String key, String value, Intent intent) {
        if (value.equals(StorageUtils.SDCARD_ROMOUNTED_STATE)) {
            return intent.getBooleanExtra(key, false);
        }
        if (value.equals(StorageUtils.SDCARD_RWMOUNTED_STATE)) {
            return intent.getBooleanExtra(key, true) ^ 1;
        }
        return false;
    }

    private boolean matchInt(String key, String value, Intent intent) {
        try {
            int temp = Integer.parseInt(value);
            if (intent.getIntExtra(key, temp - 1) == temp) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "iaware_brjob value format error");
            return false;
        }
    }

    private boolean matchString(String key, String value, Intent intent) {
        if (value.equals(intent.getStringExtra(key))) {
            return true;
        }
        return false;
    }

    private boolean matchChar(String key, String value, Intent intent) {
        char[] chs = value.toCharArray();
        if (chs.length != 1) {
            return false;
        }
        char temp = chs[0];
        Bundle bundle = intent.getExtras();
        return bundle != null && bundle.getChar(key) == temp;
    }

    private boolean matchFloat(String key, String value, Intent intent) {
        try {
            float temp = Float.parseFloat(value);
            if (new BigDecimal((double) intent.getFloatExtra(key, temp - 1.0f)).equals(new BigDecimal((double) temp))) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "iaware_brjob value format error");
            return false;
        }
    }

    private boolean matchDouble(String key, String value, Intent intent) {
        try {
            double temp = Double.parseDouble(value);
            if (new BigDecimal(intent.getDoubleExtra(key, temp - 1.0d)).equals(new BigDecimal(temp))) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "iaware_brjob value format error");
            return false;
        }
    }

    private boolean matchShort(String key, String value, Intent intent) {
        try {
            short temp = Short.parseShort(value);
            if (intent.getShortExtra(key, (short) (temp - 1)) == temp) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "iaware_brjob value format error");
            return false;
        }
    }

    private boolean matchLong(String key, String value, Intent intent) {
        try {
            long temp = Long.parseLong(value);
            if (intent.getLongExtra(key, temp - 1) == temp) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "iaware_brjob value format error");
            return false;
        }
    }

    private boolean matchObject(String key, String value, Intent intent) {
        Bundle extra = intent.getExtras();
        return (extra == null ? null : extra.get(key)) != null;
    }
}
