package com.android.server.mtm.iaware.brjob.controller;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;

public class KeyWordController extends AwareStateController {
    private static final String CONDITION_EXTRA = "Extra";
    private static final String CONDITION_KEYWORD = "KeyWord";
    private static final Object CREATE_LOCK = new Object();
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
    private static KeyWordController sSingleton;

    private KeyWordController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
    }

    public static KeyWordController get(AwareJobSchedulerService jms) {
        KeyWordController keyWordController;
        synchronized (CREATE_LOCK) {
            if (sSingleton == null) {
                sSingleton = new KeyWordController(jms, jms.getContext(), jms.getLock());
            }
            keyWordController = sSingleton;
        }
        return keyWordController;
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        Object obj;
        if (job != null) {
            if (job.hasConstraint("KeyWord") || job.hasConstraint("Extra")) {
                Intent intent = job.getIntent();
                if (this.debug) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("iaware_brjob awarejobstatus, intent: ");
                    sb.append(intent);
                    sb.append(", extras: ");
                    if (intent == null) {
                        obj = "null";
                    } else {
                        obj = intent.getExtras();
                    }
                    sb.append(obj);
                    AwareLog.i(TAG, sb.toString());
                }
                if (job.hasConstraint("KeyWord")) {
                    checkKeyword(job);
                } else if (job.hasConstraint("Extra")) {
                    checkExtra(job);
                }
            }
        }
    }

    private boolean checkJobFilterAndIntent(AwareJobStatus job) {
        String filterValue = job.getActionFilterValue("KeyWord");
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob checkKeyword: " + filterValue);
        }
        if (TextUtils.isEmpty(filterValue)) {
            AwareLog.w(TAG, "iaware_brjob keyword config error!");
            job.setSatisfied("KeyWord", false);
            return false;
        } else if (job.getIntent() != null) {
            return true;
        } else {
            AwareLog.w(TAG, "iaware_brjob intent is null.");
            job.setSatisfied("KeyWord", false);
            return false;
        }
    }

    private boolean updateFilter(String[] keyAndValue, String[] hostAndPort, IntentFilter filter) {
        String key = keyAndValue[0];
        String value = keyAndValue[1];
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return false;
        }
        if (KEYWORD_SCHEME.equals(key)) {
            filter.addDataScheme(value);
        } else if (KEYWORD_HOST.equals(key)) {
            hostAndPort[0] = value;
        } else if (KEYWORD_PORT.equals(key)) {
            hostAndPort[1] = value;
        } else if (KEYWORD_MIME.equals(key)) {
            try {
                filter.addDataType(value);
            } catch (IntentFilter.MalformedMimeTypeException e) {
                AwareLog.e(TAG, "iaware_brjob invalid mimeType!");
            }
        } else if (KEYWORD_PATH.equals(key)) {
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
        return true;
    }

    private void checkKeyword(AwareJobStatus job) {
        if (checkJobFilterAndIntent(job)) {
            Intent intent = job.getIntent();
            String[] keywords = job.getActionFilterValue("KeyWord").split("[\\[\\]]");
            IntentFilter filter = new IntentFilter(job.getAction());
            String[] hostAndPort = new String[2];
            boolean specialFormat = false;
            for (int i = 0; i < keywords.length; i++) {
                if (!(keywords[i] == null || keywords[i].trim().length() == 0 || ":".equals(keywords[i].trim()))) {
                    String[] values = keywords[i].split("[:]");
                    String[] pkgValues = keywords[i].split("[:@]");
                    AwareLog.d(TAG, "iaware_brjob config keyword: " + keywords[i]);
                    if (pkgValues.length == 3) {
                        if (!"packageName".equals(pkgValues[0]) || !checkPackgeKeyword(job, pkgValues, intent)) {
                            specialFormat = true;
                        } else {
                            return;
                        }
                    } else if (values.length != 2) {
                        AwareLog.e(TAG, "iaware_brjob KeyWord value format is wrong: " + job.getComponentName());
                        job.setSatisfied("KeyWord", false);
                        return;
                    } else if (!updateFilter(values, hostAndPort, filter)) {
                    }
                }
            }
            if (!specialFormat) {
                if (hostAndPort[0] != null) {
                    filter.addDataAuthority(hostAndPort[0], hostAndPort[1]);
                }
                int match = filter.match(intent.getAction(), job.getHwBroadcastRecord().getResolvedType(), intent.getScheme(), intent.getData(), null, TAG);
                if (this.debug) {
                    AwareLog.i(TAG, "iaware_brjob filter match: " + intent.getAction() + ", " + job.getHwBroadcastRecord().getResolvedType() + ", " + intent.getScheme() + ", " + intent.getData() + ", result: " + match);
                }
                job.setSatisfied("KeyWord", match >= 0);
            }
        }
    }

    private boolean checkPackgeKeyword(AwareJobStatus job, String[] pkgValues, Intent intent) {
        boolean result;
        if (intent.getData() == null) {
            AwareLog.e(TAG, "intent data is null.");
            job.setSatisfied("KeyWord", false);
            return false;
        }
        String pkgName = pkgValues[2];
        String ssp = intent.getData().getSchemeSpecificPart();
        if (this.debug) {
            StringBuilder sb = new StringBuilder();
            sb.append("iaware_brjob checkPackgeKeyword: ");
            sb.append(Arrays.toString(pkgValues));
            sb.append(", ssp: ");
            sb.append(ssp == null ? "null" : ssp);
            AwareLog.i(TAG, sb.toString());
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
            if (this.debug) {
                AwareLog.w(TAG, "iaware_brjob extra config error!");
            }
            job.setSatisfied("Extra", false);
            return;
        }
        if (this.debug) {
            AwareLog.w(TAG, "iaware_brjob checkExtra: " + filterValue);
        }
        String[] extras = filterValue.split("[\\[\\]]");
        boolean hasMatch = false;
        int i = 0;
        while (true) {
            if (i >= extras.length) {
                break;
            }
            if (!(extras[i] == null || extras[i].trim().length() == 0 || ":".equals(extras[i].trim()))) {
                if (this.debug) {
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
        job.setSatisfied("Extra", hasMatch);
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStopTrackingJobLocked(AwareJobStatus jobStatus) {
        if (jobStatus != null && this.debug) {
            AwareLog.i(TAG, "iaware_brjob no tracked jobStatus.");
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    KeyWordController iaware_brjob nothing to dump.");
        }
    }

    public static boolean matchReg(String key, String type, String value, Intent intent) {
        KeyWordController keyWordController;
        if (key == null || type == null || value == null || intent == null || (keyWordController = sSingleton) == null) {
            return false;
        }
        return keyWordController.match(key, type, value, intent);
    }

    private boolean match(String key, String type, String value, Intent intent) {
        if (EXTRA_BOOLEAN.equals(type)) {
            return matchBoolean(key, value, intent);
        }
        if (EXTRA_INT.equals(type)) {
            return matchInt(key, value, intent);
        }
        if (EXTRA_STRING.equals(type)) {
            return matchString(key, value, intent);
        }
        if (EXTRA_CHAR.equals(type)) {
            return matchChar(key, value, intent);
        }
        if (EXTRA_SHORT.equals(type)) {
            return matchShort(key, value, intent);
        }
        if (EXTRA_LONG.equals(type)) {
            return matchLong(key, value, intent);
        }
        if (EXTRA_DOUBLE.equals(type)) {
            return matchDouble(key, value, intent);
        }
        if (EXTRA_FLOAT.equals(type)) {
            return matchFloat(key, value, intent);
        }
        if (EXTRA_OBJECT.equals(type)) {
            return matchObject(key, value, intent);
        }
        AwareLog.e(TAG, "iaware_brjob type is error");
        return false;
    }

    private boolean matchBoolean(String key, String value, Intent intent) {
        if ("true".equals(value)) {
            return intent.getBooleanExtra(key, false);
        }
        if ("false".equals(value)) {
            return true ^ intent.getBooleanExtra(key, true);
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
