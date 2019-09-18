package com.msic.qarth;

import android.util.Log;
import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QarthReportUtil {
    private static final String RECORD_STATUS_DIR = "/data/hotpatch/fwkpatchdir/";
    private static final String TAG = QarthReportUtil.class.getSimpleName();
    private static QarthReportUtil mUtilInstance = null;
    private final String HOOK_DOWNLOADED = "downloaded";
    private final String HOOK_FAILED = "failed";
    private final String HOOK_LOADING = "loading";
    private final String HOOK_UNSTARTED = "unstarted";
    private final String HOTA_PATCH_FAILED = "HOTA_ERR_STACK";
    private final String HOTA_PATCH_NOFILE = "HOTA_NO_PATCH";
    private final String HOTA_PATCH_SUCCESS = "HOTA_SUCCESS";
    private final String HOTA_PATCH_UNSTART = "HOTA_NO_UNSTART";
    private final String INVOKE_DISABLE = "disable";
    private final String INVOKE_SUCCESS = "success";
    private final String INVOKE_UNEXECUTED = "unexecuted";
    private final String mPatchdDir = Constants.FWK_HOT_PATCH_PATH;

    private static class QarthReport implements Serializable {
        private int mHookErrorCode;
        private String mHookStatus;
        private String mInvokeStatus;
        private String mPatchName;

        private QarthReport() {
        }

        /* access modifiers changed from: private */
        public String getPatchName() {
            return this.mPatchName;
        }

        /* access modifiers changed from: private */
        public void setPatchName(String patchName) {
            this.mPatchName = patchName;
        }

        /* access modifiers changed from: private */
        public String getHookStatus() {
            return this.mHookStatus;
        }

        /* access modifiers changed from: private */
        public void setHookStatus(String hookStatus) {
            this.mHookStatus = hookStatus;
        }

        /* access modifiers changed from: private */
        public int getHookErrorCode() {
            return this.mHookErrorCode;
        }

        /* access modifiers changed from: private */
        public void setHookErrorCode(int hookErrorCode) {
            this.mHookErrorCode = hookErrorCode;
        }

        /* access modifiers changed from: private */
        public String getInvokeStatus() {
            return this.mInvokeStatus;
        }

        /* access modifiers changed from: private */
        public void setInvokeStatus(String invokeStatus) {
            this.mInvokeStatus = invokeStatus;
        }

        public String toString() {
            return "PatchStatus:patchName:" + this.mPatchName + ", hookStatus:" + this.mHookStatus + ", hookErrorCode:" + String.valueOf(this.mHookErrorCode) + ", invokeStatus:" + this.mInvokeStatus;
        }
    }

    private class QarthReportFilter implements FilenameFilter {
        private final String mHookRegex = "([a-zA-Z]+)_([0-9])_(.*)_([0-9]).log$";
        private final String mInvokeRegex = "(.*)\\.(success|disable)$";
        private List<QarthReport> mQarthReports = new ArrayList();
        private String[] mRegulars = {"([a-zA-Z]+)_([0-9])_(.*)_([0-9]).log$", "(.*)\\.(success|disable)$"};

        QarthReportFilter() {
        }

        public boolean accept(File dir, String name) {
            for (int i = 0; i < this.mRegulars.length; i++) {
                Matcher matcher = Pattern.compile(this.mRegulars[i]).matcher(name);
                if (matcher.find()) {
                    switch (i) {
                        case 0:
                            QarthReport qarthReport = new QarthReport();
                            qarthReport.setHookStatus(matcher.group(1));
                            qarthReport.setHookErrorCode(Integer.parseInt(matcher.group(2)));
                            qarthReport.setPatchName(matcher.group(3));
                            this.mQarthReports.add(qarthReport);
                            break;
                        case 1:
                            QarthReport qarthReport2 = new QarthReport();
                            qarthReport2.setPatchName(matcher.group(1));
                            qarthReport2.setInvokeStatus(matcher.group(2));
                            this.mQarthReports.add(qarthReport2);
                            break;
                    }
                    return matcher.matches();
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public List<QarthReport> getQarthReports() {
            return this.mQarthReports;
        }
    }

    public static QarthReportUtil getQarthReportUtil() {
        if (mUtilInstance == null) {
            mUtilInstance = new QarthReportUtil();
        }
        return mUtilInstance;
    }

    private List<QarthReport> getQarthReportListFromRecordFile(File path, List<QarthReport> qarthReports) {
        File[] files = path.listFiles();
        if (files == null) {
            return qarthReports;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                Log.i(TAG, "getQarthReportListFromRecordFile file name : " + f.getName());
                QarthReportFilter qarthReportFilter = new QarthReportFilter();
                f.listFiles(qarthReportFilter);
                qarthReports.addAll(qarthReportFilter.getQarthReports());
                getQarthReportListFromRecordFile(f, qarthReports);
            }
        }
        return qarthReports;
    }

    private List<QarthReport> groupByQarthReport(List<QarthReport> sourcelist) {
        List<QarthReport> targetList = new ArrayList<>();
        Set<String> sets = new HashSet<>();
        if (sourcelist == null || sourcelist.size() == 0) {
            return targetList;
        }
        for (QarthReport s : sourcelist) {
            sets.add(s.getPatchName());
        }
        for (String set : sets) {
            QarthReport qarthReport = new QarthReport();
            for (QarthReport s2 : sourcelist) {
                if (set.equals(s2.getPatchName())) {
                    qarthReport.setPatchName(s2.getPatchName());
                    if (s2.getHookStatus() != null) {
                        qarthReport.setHookStatus(s2.getHookStatus());
                        qarthReport.setHookErrorCode(s2.getHookErrorCode());
                    }
                    if (s2.getInvokeStatus() != null) {
                        if (s2.getInvokeStatus().equals("disable")) {
                            qarthReport.setInvokeStatus("disable");
                        }
                        if (qarthReport.getInvokeStatus() == null && s2.getInvokeStatus().equals("success")) {
                            qarthReport.setInvokeStatus("success");
                        }
                    }
                }
            }
            if (qarthReport.getInvokeStatus() == null) {
                qarthReport.setInvokeStatus("unexecuted");
            }
            targetList.add(qarthReport);
        }
        return targetList;
    }

    private String organizeDataToString(List<QarthReport> qarthReports) {
        String qarthReportsString;
        List<QarthReport> list = new ArrayList<>();
        List<String> hookState = new ArrayList<>();
        for (QarthReport qarthReport : qarthReports) {
            hookState.add(qarthReport.getHookStatus());
            list.add(qarthReport);
        }
        if (hookState.contains("failed")) {
            qarthReportsString = "HOTA_ERR_STACK";
        } else if (hookState.contains("downloaded") || hookState.contains("loading") || hookState.contains("unstarted")) {
            qarthReportsString = "HOTA_NO_UNSTART";
        } else {
            qarthReportsString = "HOTA_SUCCESS";
        }
        if (list.size() <= 0) {
            return qarthReportsString;
        }
        return qarthReportsString + ":" + list.toString();
    }

    private List<String> getPatchFileNameList() {
        List<String> patchFilesName = new ArrayList<>();
        File file = new File(this.mPatchdDir);
        if (!file.exists()) {
            return patchFilesName;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return patchFilesName;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                File[] fs = f.listFiles($$Lambda$QarthReportUtil$FFI9mmniF15hw8nnZsg0aQPwmfU.INSTANCE);
                if (fs == null) {
                    break;
                }
                for (File patchFile : fs) {
                    patchFilesName.add(patchFile.getName());
                }
            }
        }
        return patchFilesName;
    }

    public String getFwkHotPatchResult() {
        List<QarthReport> qarthReports = new ArrayList<>();
        File file = new File(RECORD_STATUS_DIR);
        if (file.exists()) {
            qarthReports.addAll(getQarthReportListFromRecordFile(file, qarthReports));
        }
        List<QarthReport> overageReports = groupByQarthReport(qarthReports);
        String str = TAG;
        Log.i(str, "the fwkhotpatch status file count is : " + overageReports.size());
        List<String> patchFiles = getPatchFileNameList();
        if (patchFiles == null || patchFiles.size() == 0) {
            Log.i(TAG, "the fwkhotpatch qarth file not exist");
            return "HOTA_NO_PATCH";
        }
        List<QarthReport> reports = new ArrayList<>();
        for (String s : patchFiles) {
            boolean isFind = false;
            Iterator<QarthReport> it = overageReports.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                QarthReport q = it.next();
                if (s.equals(q.getPatchName())) {
                    reports.add(q);
                    isFind = true;
                    break;
                }
            }
            if (!isFind) {
                QarthReport qarthReport = new QarthReport();
                qarthReport.setPatchName(s);
                String str2 = TAG;
                Log.i(str2, "fwkhotpatch status file not find : " + s + " set HOOK_UNSTARTED");
                qarthReport.setHookStatus("unstarted");
                qarthReport.setHookErrorCode(0);
                qarthReport.setInvokeStatus("unexecuted");
                reports.add(qarthReport);
            }
        }
        return organizeDataToString(reports);
    }

    public static String getFwkHotPatchResultEx() {
        return getQarthReportUtil().getFwkHotPatchResult();
    }
}
