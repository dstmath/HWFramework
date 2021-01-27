package com.android.internal.telephony.cat;

import android.os.SystemProperties;
import com.android.internal.telephony.cat.AppInterface;

public class HwCustCatCmdMessageImpl extends HwCustCatCmdMessage {
    private static final int REFRESH_FILE_CHANGE_NOTIFICATION = 1;
    private static final boolean SUPPORT_DCM_UIM_LOCK = SystemProperties.getBoolean("ro.config.hw_support_dcm_ulock", false);
    private static final boolean SUPPORT_SIM_REFRESH = SystemProperties.getBoolean("ro.config.hw_sim_file_refresh", false);

    public boolean supportDcmSimFileRefresh(CommandParams cmdParams) {
        if (!SUPPORT_SIM_REFRESH || !SUPPORT_DCM_UIM_LOCK || cmdParams == null) {
            return false;
        }
        CommandDetails cmdDet = cmdParams.mCmdDet;
        AppInterface.CommandType cmdType = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
        if (cmdDet.commandQualifier == 1 && cmdType == AppInterface.CommandType.REFRESH) {
            return true;
        }
        return false;
    }
}
