package com.android.internal.telephony.cat;

import android.os.SystemProperties;
import com.android.internal.telephony.cat.AbstractCommandParamsFactory;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.List;

public class HwCommandParamsFactoryReference implements AbstractCommandParamsFactory.CommandParamsFactoryReference {
    private static final boolean SUPPORT_SIM_REFRESH = SystemProperties.getBoolean("ro.config.hw_sim_file_refresh", false);
    private static CommandParamsFactoryUtils commandParamsFactoryUtils = EasyInvokeFactory.getInvokeUtils(CommandParamsFactoryUtils.class);
    private CommandParamsFactory mCommandParamsFactory;

    public HwCommandParamsFactoryReference(CommandParamsFactory commandParamsFactory) {
        CatLog.d(this, "construct HwCommandParamsFactoryReference ");
        this.mCommandParamsFactory = commandParamsFactory;
    }

    public boolean processLanguageNotification(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) throws ResultException {
        CatLog.d(this, "processLanguageNotification");
        String Language = null;
        if (cmdDet.commandQualifier != 1) {
            CatLog.d(this, "commandQualifier 0x00");
        } else {
            CatLog.d(this, "commandQualifier 0x01");
            ComprehensionTlv ctlv = commandParamsFactoryUtils.searchForTag(this.mCommandParamsFactory, ComprehensionTlvTag.LANGUAGE, ctlvs);
            if (ctlv != null) {
                Language = ValueParser.retrieveTextString(ctlv);
            } else {
                throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
            }
        }
        commandParamsFactoryUtils.setCmdParams(this.mCommandParamsFactory, new HwCommandParams(cmdDet, Language));
        return false;
    }

    public void processFileChangeNotification(CommandDetails cmdDet, List<ComprehensionTlv> ctlvs) {
        if (SUPPORT_SIM_REFRESH) {
            CatLog.d(this, "processFileChangeNotification");
            ComprehensionTlv ctlv = commandParamsFactoryUtils.searchForTag(this.mCommandParamsFactory, ComprehensionTlvTag.FILE_LIST, ctlvs);
            if (ctlv != null) {
                try {
                    byte[] rawValue = ctlv.getRawValue();
                    int[] fileList = new int[ctlv.getLength()];
                    for (int i = 0; i < fileList.length; i++) {
                        fileList[i] = rawValue[ctlv.getValueIndex() + i] & 255;
                    }
                    commandParamsFactoryUtils.setCmdParams(this.mCommandParamsFactory, new FileChangeNotificationParams(cmdDet, fileList));
                } catch (Exception e) {
                    CatLog.e(this, "processFileChangeNotification:exception");
                }
            } else {
                CatLog.e(this, "processFileChangeNotification: ctlv is missing!");
            }
        }
    }
}
