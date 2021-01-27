package com.huawei.nb.utils.logger.diagnoselogger;

import android.text.TextUtils;
import com.huawei.nb.utils.logger.DSLog;
import com.huawei.nb.utils.logger.diagnoselogger.DiagnoseLogTitle;
import java.util.HashMap;
import java.util.Map;

public class DiagnoselLog {
    private static String ACTION_HINT_TAG = "ACTION HINT: ";
    private static String CAUSE_DETAIL_TAG = "CAUSE DETAIL: ";
    private static String DLOG_TAG = "DLOG: ";
    private static String ELOG_TAG = "ELOG: ";
    private static String ILOG_TAG = "ILOG: ";
    private static String WLOG_TAG = "WLOG: ";
    private static final Map<DiagnoseLogTitle.Type, String> mTitles = new HashMap();

    static {
        mTitles.put(DiagnoseLogTitle.Type.PROCESS_EXCEPTION, "Process stop with AimodelDownloadException happened.");
        mTitles.put(DiagnoseLogTitle.Type.DUPLICATE_STOPPING, "Quit with failure about duplicate stopping.");
        mTitles.put(DiagnoseLogTitle.Type.PROCESS_FAIL_TO_STOP_OTHERS, "Process fail to stop on-going processes.");
        mTitles.put(DiagnoseLogTitle.Type.SYNC_POLICY_NULL, "Fail to start uploading ai models with a corresponding policy.");
        mTitles.put(DiagnoseLogTitle.Type.REQUEST_FAIL_WITH_ENV, "Do not request by wifi-down or screen-on or battery-none-charge ");
        mTitles.put(DiagnoseLogTitle.Type.PUSH_MSG_NOT_ADAPT_LOCAL, "Pushed resource packages and local rules do not match");
        mTitles.put(DiagnoseLogTitle.Type.QUERY_RESPONSE_HAS_NO_RESID, "Aimodel response with no resid.");
        mTitles.put(DiagnoseLogTitle.Type.QUERY_RESPONSE_JSON_SYNTAXEXCEPTION, "Fail to analyze response msg from cloud.");
        mTitles.put(DiagnoseLogTitle.Type.JSON_ARRAY_LONG_ANALYSE_FAIL, "Fail to analyze long type array.");
        mTitles.put(DiagnoseLogTitle.Type.REQUEST_BODY_NULL, "Request body is null.");
        mTitles.put(DiagnoseLogTitle.Type.REQUEST_URL_FAIL_FROM_CLOUD, "onFailure callback is called after request from cloud.");
        mTitles.put(DiagnoseLogTitle.Type.REQUEST_URL_NOTHING_CAN_UPDATE, "Cloud response said all ai model resources are the latest, nothing to sync.");
        mTitles.put(DiagnoseLogTitle.Type.XCHANNEL_PUSH_TOO_MANY_TIMES, "There are too many push msg from xchannel.");
        mTitles.put(DiagnoseLogTitle.Type.XCHANNEL_PUSH_AN_ABSENT_RESID, "Receive a resource information which is not registered.");
        mTitles.put(DiagnoseLogTitle.Type.XCHANNEL_PUSH_NULL_MSG, "Receive a null msg.");
        mTitles.put(DiagnoseLogTitle.Type.XCHANNEL_PUSH_WRONG_ACTION, "Receive a wrong action.");
        mTitles.put(DiagnoseLogTitle.Type.XCHANNEL_PUSH_WRONG_JSON, "Json string from xchannel is not valid.");
        mTitles.put(DiagnoseLogTitle.Type.XCHANNEL_PUSH_RES_HAS_NO_PERMISSION, "Receive a res with no permission.");
        mTitles.put(DiagnoseLogTitle.Type.HIACTION_REGISTER_FAIL, "Fail to register to hiAction push callback!");
        mTitles.put(DiagnoseLogTitle.Type.HIACTION_UNREGISTER_FAIL, "Fail to unregister to hiAction push callback!");
        mTitles.put(DiagnoseLogTitle.Type.HIACTION_FIRST_REGISTER, "Firstly registering hiaction.");
        mTitles.put(DiagnoseLogTitle.Type.LAZY_UPDATE_CALLBACK_NULL, "UpdatePackage callback is null.");
        mTitles.put(DiagnoseLogTitle.Type.LAZY_UPDATE_PARAM_ERROR, "UpdatePackage input param error!");
        mTitles.put(DiagnoseLogTitle.Type.LAZY_DELETE_PARAM_ERROR, "DeletePackage input param error!");
        mTitles.put(DiagnoseLogTitle.Type.LAZY_REQUESTER_LONG_PARSE_ERROR, "Param convert to long failed when get resources' info.");
        mTitles.put(DiagnoseLogTitle.Type.LAZY_REQUESTER_INPUT_LIST_EMPTY, "Fail to update package check without valid list of resources.");
        mTitles.put(DiagnoseLogTitle.Type.LAZY_REQUESTER_INPUT_ATTRIBUTE_INVALID, "Fail to update package check because of some attribute is invalid,");
        mTitles.put(DiagnoseLogTitle.Type.REMOTEEXCEPTION_HAPPEN, "Fail to callback because of a remote exception.");
        mTitles.put(DiagnoseLogTitle.Type.DELETE_AN_ABSENT_REOSOURCE, "The resource to delete is not exist.");
        mTitles.put(DiagnoseLogTitle.Type.DELETE_RESOURCE_RESULT_FAIL, "Failed to delete resource files.");
        mTitles.put(DiagnoseLogTitle.Type.GETIMMEDIATE_FAIL_NULL_RES, "Fail to update package without valid list of resources.");
        mTitles.put(DiagnoseLogTitle.Type.GETIMMEDIATE_FAIL_NOT_PRESET_RES, "Fail to update package because of resid is not preset.");
        mTitles.put(DiagnoseLogTitle.Type.GETIMMEDIATE_FAIL_ATTRIBUTE_EMPTY, "Fail to update package because of some attribute is empty.");
        mTitles.put(DiagnoseLogTitle.Type.DOWNLOAD_INTERRUP_BY_ENV, "Cloud side download interrupt by screen-on or battery-none-charge ");
        mTitles.put(DiagnoseLogTitle.Type.DOWNLOAD_OVERFLOW_WHEN_NO_WIFI, "NO-WIFI situation, data is overflow.");
        mTitles.put(DiagnoseLogTitle.Type.DOWNLOAD_STILL_DOWNLOAD, "Resource package download incomplete");
        mTitles.put(DiagnoseLogTitle.Type.DOWNLOAD_VERIFY_FAILED, "Verify failed.");
        mTitles.put(DiagnoseLogTitle.Type.DOWNLOAD_EXCEPTION_HAPPEN, "AiModelDownloadException happen when downloading.");
        mTitles.put(DiagnoseLogTitle.Type.DOWNLOAD_LOOSE_RESOURCE, "Try to download a resource not preset locally.");
        mTitles.put(DiagnoseLogTitle.Type.DOWNLOAD_NEED_NOT_SYNC, "Resource packages do not need to be downloaded.");
        mTitles.put(DiagnoseLogTitle.Type.DOWNLOAD_PERMISSION_CLOSED, "Resource stop download for closed permission.");
        mTitles.put(DiagnoseLogTitle.Type.DOWNLOAD_RESULT_ONFAILURE, "Downloading the resource package failed.");
        mTitles.put(DiagnoseLogTitle.Type.DOWNLOAD_FAIL_TO_COMPLETE, "Fail to complete a successful download.");
        mTitles.put(DiagnoseLogTitle.Type.POSETHANDLE_UNZIP_AIMODEL_FAIL, "Fail to unzip model files.");
        mTitles.put(DiagnoseLogTitle.Type.POSETHANDLE_ENCRYPT_FAIL, "Encrypt file failed.");
        mTitles.put(DiagnoseLogTitle.Type.POSETHANDLE_DECRYPT_FAIL, "Decrypt file failed.");
        mTitles.put(DiagnoseLogTitle.Type.POSETHANDLE_GET_WORKKEY_FAIL, "Get workkey failed.");
        mTitles.put(DiagnoseLogTitle.Type.POSETHANDLE_METADATA_FAIL, "Get aimodel metadata failed.");
        mTitles.put(DiagnoseLogTitle.Type.AIMODEL_PERIOD_REQUEST_FAILED, "Periodic request for all resources failed.");
        mTitles.put(DiagnoseLogTitle.Type.AIMODEL_JOB_FRESH_DATA, "All fresh aimodel resources are handle recently.");
        mTitles.put(DiagnoseLogTitle.Type.AIMODEL_DOWNLOAD_TASK_FAIL, "Download all files task interrupt and to be failed.");
        mTitles.put(DiagnoseLogTitle.Type.POSETHANDLE_UNZIP_INNER_ZIP_FAIL, "Fail to unzip resource file.");
        mTitles.put(DiagnoseLogTitle.Type.POSETHANDLE_UPDATE_METADATA_FAIL, "Update aimodel metadata failed.");
        mTitles.put(DiagnoseLogTitle.Type.DELETE_RESOURCE_RECOVER_FAIL, "Failed to recover preset resource after delete.");
    }

    public static class Detail {
        String log;

        public Detail(String str, Object... objArr) {
            if (!(objArr == null || objArr.length == 0)) {
                str = String.format(str, objArr);
            }
            this.log = str;
        }

        public Detail() {
            this.log = "null.";
        }
    }

    public static class Hint {
        String log;

        public Hint(String str, Object... objArr) {
            if (!(objArr == null || objArr.length == 0)) {
                str = String.format(str, objArr);
            }
            this.log = str;
        }

        public Hint() {
            this.log = "null.";
        }
    }

    private static String logString(String str, DiagnoseLogTitle.Type type, Detail detail, Hint hint) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append(str);
        sb.append(mTitles.get(type));
        if (detail != null && !TextUtils.isEmpty(detail.log)) {
            sb.append(System.lineSeparator());
            sb.append(CAUSE_DETAIL_TAG);
            sb.append(detail.log);
        }
        if (hint != null && !TextUtils.isEmpty(hint.log)) {
            sb.append(System.lineSeparator());
            sb.append(ACTION_HINT_TAG);
            sb.append(hint.log);
        }
        return sb.toString();
    }

    public static void e(DiagnoseLogTitle.Type type, Detail detail, Hint hint) {
        if (!type.equals(DiagnoseLogTitle.Type.ODMF_INNER)) {
            DSLog.e(logString(ELOG_TAG, type, detail, hint), new Object[0]);
        } else if (detail != null) {
            DSLog.e(detail.log, new Object[0]);
        }
    }

    public static void w(DiagnoseLogTitle.Type type, Detail detail, Hint hint) {
        if (!type.equals(DiagnoseLogTitle.Type.ODMF_INNER)) {
            DSLog.w(logString(WLOG_TAG, type, detail, hint), new Object[0]);
        } else if (detail != null) {
            DSLog.w(detail.log, new Object[0]);
        }
    }

    public static void i(DiagnoseLogTitle.Type type, Detail detail, Hint hint) {
        if (!type.equals(DiagnoseLogTitle.Type.ODMF_INNER)) {
            DSLog.i(logString(ILOG_TAG, type, detail, hint), new Object[0]);
        } else if (detail != null) {
            DSLog.i(detail.log, new Object[0]);
        }
    }

    public static void d(DiagnoseLogTitle.Type type, Detail detail, Hint hint) {
        if (!type.equals(DiagnoseLogTitle.Type.ODMF_INNER)) {
            DSLog.d(logString(DLOG_TAG, type, detail, hint), new Object[0]);
        } else if (detail != null) {
            DSLog.d(detail.log, new Object[0]);
        }
    }
}
