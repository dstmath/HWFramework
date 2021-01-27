package ohos.aafwk.ability;

public class FormException extends Exception {
    private static final int CODE_FORM_OFFSET = 8585216;
    private static final int ERR_ADD_INVALID_PARAM = 8585223;
    private static final int ERR_BIND_SUPPLIER_FAILED = 8585226;
    private static final int ERR_CFG_NOT_MATCH_ID = 8585224;
    private static final int ERR_CODE_COMMON = 8585217;
    private static final int ERR_DEL_FORM_NOT_SELF = 8585229;
    private static final int ERR_FORM_DUPLICATE_ADDED = 8585247;
    private static final int ERR_GET_BMS_BINDER = 8585249;
    private static final int ERR_GET_BUNDLE_FAILED = 8585221;
    private static final int ERR_GET_FMS_BINDER = 8585246;
    private static final int ERR_GET_INFO_FAILED = 8585220;
    private static final int ERR_GET_LAYOUT_FAILED = 8585222;
    private static final int ERR_INTENT_PARCEL = 8585219;
    private static final int ERR_MAX_INSTANCES_PER_FORM = 8585228;
    private static final int ERR_MAX_SYSTEM_FORMS = 8585227;
    private static final int ERR_NOT_EXIST_ID = 8585225;
    private static final int ERR_PERMISSION_DENY = 8585218;
    private static final int ERR_SEND_BMS_MSG = 8585250;
    private static final int ERR_SEND_FMS_MSG = 8585248;
    private static final int ERR_START_ABILITY = 8585251;
    private static final int ERR_SUPPLIER_DEL_FAIL = 8585230;
    private String errCode;

    /* access modifiers changed from: package-private */
    public enum FormError {
        INPUT_PARAM_INVALID(FormException.ERR_ADD_INVALID_PARAM, "invalid params received on acquire form"),
        FMS_BINDER_ERROR(FormException.ERR_GET_FMS_BINDER, "get fms binder failed"),
        BMS_BINDER_ERROR(FormException.ERR_GET_BMS_BINDER, "get bms binder failed"),
        SEND_FMS_MSG_ERROR(FormException.ERR_SEND_FMS_MSG, "send request to fms failed"),
        SEND_BMS_MSG_ERROR(FormException.ERR_SEND_BMS_MSG, "send request to bms failed"),
        FMS_INTENT_PARCEL_FAIL(FormException.ERR_INTENT_PARCEL, "fms unmarshal intent failed"),
        PERMISSION_DENY(FormException.ERR_PERMISSION_DENY, "check permission deny, need to request ohos.permission.REQUIRE_FORM"),
        FORM_INFO_NOT_FOUND(FormException.ERR_GET_INFO_FAILED, "can't get form info with requested bundle, ability, module or form"),
        GET_BUNDLE_FAILED(FormException.ERR_GET_BUNDLE_FAILED, "the requested bundle name does not exist"),
        INIT_LAYOUT_FAILED(FormException.ERR_GET_LAYOUT_FAILED, "can't get layout with requested dimension and orientation"),
        BIND_SUPPLIER_FAILED(FormException.ERR_BIND_SUPPLIER_FAILED, "fms bind supplier failed"),
        FORM_DUPLICATE_ADDED(FormException.ERR_FORM_DUPLICATE_ADDED, "form do not support acquire same id twice"),
        FORM_CFG_NOT_MATCH_ID(FormException.ERR_CFG_NOT_MATCH_ID, "the form id and form config are not matched"),
        FORM_ID_NOT_EXIST(FormException.ERR_NOT_EXIST_ID, "the requested form id is not existed on fms"),
        EXCEED_MAX_SYSTEM_FORMS(FormException.ERR_MAX_SYSTEM_FORMS, "exceed max forms in system, current limit is 500"),
        EXCEED_MAX_FORMS_PER_INSTANCE(FormException.ERR_MAX_INSTANCES_PER_FORM, "exceed max instances per form, limit is 32"),
        FORM_NOT_SELF_OWNED(FormException.ERR_DEL_FORM_NOT_SELF, "the form to be deleted is not self-owned or has been deleted already"),
        SUPPLIER_DELETE_FAIL(FormException.ERR_SUPPLIER_DEL_FAIL, "fms notify supplier to delete failed"),
        INTERNAL_ERROR(FormException.ERR_CODE_COMMON, "some internal server occurs");
        
        private int errCode;
        private String errMsg;

        private FormError(int i, String str) {
            this.errCode = i;
            this.errMsg = str;
        }

        /* access modifiers changed from: package-private */
        public int getErrCode() {
            return this.errCode;
        }

        /* access modifiers changed from: package-private */
        public String getErrMsg() {
            return this.errMsg;
        }

        static FormError fromErrCode(int i) {
            FormError[] values = values();
            for (FormError formError : values) {
                if (formError.getErrCode() == i) {
                    return formError;
                }
            }
            return null;
        }
    }

    FormException(FormError formError) {
        this(formError.toString(), formError.getErrMsg());
    }

    FormException(FormError formError, String str) {
        this(formError.toString(), str);
    }

    public FormException(String str, String str2) {
        super(str2);
        this.errCode = "";
        this.errCode = str;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return this.errCode + ": " + super.getMessage();
    }
}
