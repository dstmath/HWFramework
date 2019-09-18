package com.android.server.mtm.iaware.brjob.controller;

import android.content.Context;
import android.content.Intent;
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
                    StringBuilder sb = new StringBuilder();
                    sb.append("iaware_brjob awarejobstatus, intent: ");
                    sb.append(intent);
                    sb.append(", extras: ");
                    sb.append(intent == null ? "null" : intent.getExtras());
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

    /* JADX WARNING: type inference failed for: r4v0 */
    /* JADX WARNING: type inference failed for: r4v1, types: [boolean] */
    /* JADX WARNING: type inference failed for: r4v4 */
    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    private void checkKeyword(com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus r20) {
        /*
            r19 = this;
            r1 = r19
            r2 = r20
            java.lang.String r0 = "KeyWord"
            java.lang.String r3 = r2.getActionFilterValue(r0)
            boolean r0 = r1.DEBUG
            if (r0 == 0) goto L_0x0024
            java.lang.String r0 = "KeyWordController"
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "iaware_brjob checkKeyword: "
            r4.append(r5)
            r4.append(r3)
            java.lang.String r4 = r4.toString()
            android.rms.iaware.AwareLog.i(r0, r4)
        L_0x0024:
            boolean r0 = android.text.TextUtils.isEmpty(r3)
            r4 = 0
            if (r0 == 0) goto L_0x003c
            boolean r0 = r1.DEBUG
            if (r0 == 0) goto L_0x0036
            java.lang.String r0 = "KeyWordController"
            java.lang.String r5 = "iaware_brjob keyword config error!"
            android.rms.iaware.AwareLog.w(r0, r5)
        L_0x0036:
            java.lang.String r0 = "KeyWord"
            r2.setSatisfied(r0, r4)
            return
        L_0x003c:
            android.content.Intent r5 = r20.getIntent()
            if (r5 != 0) goto L_0x004f
            java.lang.String r0 = "KeyWordController"
            java.lang.String r6 = "iaware_brjob intent is null."
            android.rms.iaware.AwareLog.w(r0, r6)
            java.lang.String r0 = "KeyWord"
            r2.setSatisfied(r0, r4)
            return
        L_0x004f:
            java.lang.String r0 = "[\\[\\]]"
            java.lang.String[] r6 = r3.split(r0)
            android.content.IntentFilter r0 = new android.content.IntentFilter
            java.lang.String r7 = r20.getAction()
            r0.<init>(r7)
            r7 = r0
            r0 = 0
            r8 = 0
            r9 = 0
            r14 = r0
            r0 = r4
            r13 = r8
            r15 = r9
        L_0x0066:
            r8 = r0
            int r0 = r6.length
            if (r8 >= r0) goto L_0x01db
            r0 = r6[r8]
            if (r0 == 0) goto L_0x01d2
            r0 = r6[r8]
            java.lang.String r0 = r0.trim()
            int r0 = r0.length()
            if (r0 == 0) goto L_0x01d2
            java.lang.String r0 = ":"
            r9 = r6[r8]
            java.lang.String r9 = r9.trim()
            boolean r0 = r0.equals(r9)
            if (r0 == 0) goto L_0x0089
            goto L_0x00e8
        L_0x0089:
            r0 = r6[r8]
            java.lang.String r9 = "[:]"
            java.lang.String[] r9 = r0.split(r9)
            r0 = r6[r8]
            java.lang.String r10 = "[:@]"
            java.lang.String[] r10 = r0.split(r10)
            boolean r0 = r1.DEBUG
            if (r0 == 0) goto L_0x00b5
            java.lang.String r0 = "KeyWordController"
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "iaware_brjob config keyword: "
            r11.append(r12)
            r12 = r6[r8]
            r11.append(r12)
            java.lang.String r11 = r11.toString()
            android.rms.iaware.AwareLog.i(r0, r11)
        L_0x00b5:
            int r0 = r10.length
            r11 = 3
            if (r0 != r11) goto L_0x00ec
            r0 = 1
            java.lang.String r11 = "packageName"
            r12 = r10[r4]
            boolean r11 = r11.equals(r12)
            if (r11 == 0) goto L_0x00cc
            boolean r11 = r1.checkPackgeKeyword(r2, r10, r5)
            if (r11 == 0) goto L_0x00e7
            return
        L_0x00cc:
            java.lang.String r11 = "KeyWordController"
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r15 = "iaware_brjob KeyWord value format is wrong: "
            r12.append(r15)
            java.lang.String r15 = r20.getComponentName()
            r12.append(r15)
            java.lang.String r12 = r12.toString()
            android.rms.iaware.AwareLog.e(r11, r12)
        L_0x00e7:
            r15 = r0
        L_0x00e8:
            r18 = r3
            goto L_0x01d4
        L_0x00ec:
            int r0 = r9.length
            r11 = 2
            if (r0 == r11) goto L_0x0110
            java.lang.String r0 = "KeyWordController"
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "iaware_brjob KeyWord value format is wrong: "
            r11.append(r12)
            java.lang.String r12 = r20.getComponentName()
            r11.append(r12)
            java.lang.String r11 = r11.toString()
            android.rms.iaware.AwareLog.e(r0, r11)
            java.lang.String r0 = "KeyWord"
            r2.setSatisfied(r0, r4)
            return
        L_0x0110:
            r12 = r9[r4]
            r0 = 1
            r11 = r9[r0]
            boolean r0 = android.text.TextUtils.isEmpty(r12)
            if (r0 != 0) goto L_0x01d2
            boolean r0 = android.text.TextUtils.isEmpty(r11)
            if (r0 == 0) goto L_0x0122
            goto L_0x00e8
        L_0x0122:
            java.lang.String r0 = "scheme"
            boolean r0 = r0.equals(r12)
            if (r0 == 0) goto L_0x012f
            r7.addDataScheme(r11)
            goto L_0x00e8
        L_0x012f:
            java.lang.String r0 = "host"
            boolean r0 = r0.equals(r12)
            if (r0 == 0) goto L_0x013a
            r0 = r11
            r14 = r0
            goto L_0x00e8
        L_0x013a:
            java.lang.String r0 = "port"
            boolean r0 = r0.equals(r12)
            if (r0 == 0) goto L_0x0146
            r0 = r11
            r13 = r0
            goto L_0x00e8
        L_0x0146:
            java.lang.String r0 = "mimeType"
            boolean r0 = r0.equals(r12)
            if (r0 == 0) goto L_0x015f
            r7.addDataType(r11)     // Catch:{ MalformedMimeTypeException -> 0x0152 }
        L_0x0151:
            goto L_0x00e8
        L_0x0152:
            r0 = move-exception
            r16 = r0
            java.lang.String r4 = "KeyWordController"
            r17 = r0
            java.lang.String r0 = "iaware_brjob invalid mimeType!"
            android.rms.iaware.AwareLog.e(r4, r0)
            goto L_0x0151
        L_0x015f:
            java.lang.String r0 = "path"
            boolean r0 = r0.equals(r12)
            if (r0 == 0) goto L_0x016e
            r4 = 0
            r7.addDataPath(r11, r4)
            goto L_0x00e8
        L_0x016e:
            java.lang.String r0 = "pathPrefix"
            boolean r0 = r0.equals(r12)
            if (r0 == 0) goto L_0x017d
            r0 = 1
            r7.addDataPath(r11, r0)
            goto L_0x00e8
        L_0x017d:
            java.lang.String r0 = "pathPattern"
            boolean r0 = r0.equals(r12)
            if (r0 == 0) goto L_0x018c
            r0 = 2
            r7.addDataPath(r11, r0)
            goto L_0x00e8
        L_0x018c:
            java.lang.String r0 = "ssp"
            boolean r0 = r0.equals(r12)
            if (r0 == 0) goto L_0x019b
            r4 = 0
            r7.addDataSchemeSpecificPart(r11, r4)
            goto L_0x00e8
        L_0x019b:
            java.lang.String r0 = "sspPrefix"
            boolean r0 = r0.equals(r12)
            if (r0 == 0) goto L_0x01aa
            r0 = 1
            r7.addDataSchemeSpecificPart(r11, r0)
            goto L_0x00e8
        L_0x01aa:
            java.lang.String r0 = "sspPattern"
            boolean r0 = r0.equals(r12)
            if (r0 == 0) goto L_0x01b9
            r0 = 2
            r7.addDataSchemeSpecificPart(r11, r0)
            goto L_0x00e8
        L_0x01b9:
            java.lang.String r0 = "KeyWordController"
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r18 = r3
            java.lang.String r3 = "iaware_brjob invalid key: "
            r4.append(r3)
            r4.append(r12)
            java.lang.String r3 = r4.toString()
            android.rms.iaware.AwareLog.e(r0, r3)
            goto L_0x01d4
        L_0x01d2:
            r18 = r3
        L_0x01d4:
            int r0 = r8 + 1
            r3 = r18
            r4 = 0
            goto L_0x0066
        L_0x01db:
            r18 = r3
            r0 = 1
            if (r15 != 0) goto L_0x0263
            if (r14 == 0) goto L_0x01e5
            r7.addDataAuthority(r14, r13)
        L_0x01e5:
            java.lang.String r9 = r5.getAction()
            com.android.server.am.HwBroadcastRecord r3 = r20.getHwBroadcastRecord()
            java.lang.String r10 = r3.getResolvedType()
            java.lang.String r11 = r5.getScheme()
            android.net.Uri r12 = r5.getData()
            r3 = 0
            java.lang.String r4 = "KeyWordController"
            r8 = r7
            r16 = r13
            r13 = r3
            r3 = r14
            r14 = r4
            int r4 = r8.match(r9, r10, r11, r12, r13, r14)
            boolean r8 = r1.DEBUG
            if (r8 == 0) goto L_0x0254
            java.lang.String r8 = "KeyWordController"
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "iaware_brjob filter match: "
            r9.append(r10)
            java.lang.String r10 = r5.getAction()
            r9.append(r10)
            java.lang.String r10 = ", "
            r9.append(r10)
            com.android.server.am.HwBroadcastRecord r10 = r20.getHwBroadcastRecord()
            java.lang.String r10 = r10.getResolvedType()
            r9.append(r10)
            java.lang.String r10 = ", "
            r9.append(r10)
            java.lang.String r10 = r5.getScheme()
            r9.append(r10)
            java.lang.String r10 = ", "
            r9.append(r10)
            android.net.Uri r10 = r5.getData()
            r9.append(r10)
            java.lang.String r10 = ", result: "
            r9.append(r10)
            r9.append(r4)
            java.lang.String r9 = r9.toString()
            android.rms.iaware.AwareLog.i(r8, r9)
        L_0x0254:
            if (r4 < 0) goto L_0x025c
            java.lang.String r8 = "KeyWord"
            r2.setSatisfied(r8, r0)
            goto L_0x0266
        L_0x025c:
            java.lang.String r0 = "KeyWord"
            r8 = 0
            r2.setSatisfied(r0, r8)
            goto L_0x0266
        L_0x0263:
            r16 = r13
            r3 = r14
        L_0x0266:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.iaware.brjob.controller.KeyWordController.checkKeyword(com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus):void");
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
        if (this.DEBUG) {
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
        while (true) {
            if (i >= extras.length) {
                break;
            }
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

    public static boolean matchReg(String key, String type, String value, Intent intent) {
        if (key == null || type == null || value == null || intent == null || mSingleton == null) {
            return false;
        }
        return mSingleton.match(key, type, value, intent);
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
        if (value.equals("true")) {
            return intent.getBooleanExtra(key, false);
        }
        if (value.equals("false")) {
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
