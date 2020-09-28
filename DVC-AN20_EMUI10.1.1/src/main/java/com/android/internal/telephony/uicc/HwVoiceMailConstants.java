package com.android.internal.telephony.uicc;

import android.os.Environment;
import android.telephony.Rlog;
import com.huawei.android.telephony.RlogEx;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class HwVoiceMailConstants {
    static final String LOG_TAG = "VoiceMailConstants";
    static final String PARTNER_VOICEMAIL_PATH = "etc/voicemail-conf.xml";

    public static FileReader getVoiceMailFileReader() {
        File confFile = new File("/system/etc", "voicemail-conf.xml");
        File vmFileCust = new File("/data/cust/", "xml/voicemail-conf.xml");
        File vmFile = new File(Environment.getRootDirectory(), PARTNER_VOICEMAIL_PATH);
        try {
            String[] fileInfo = HwCfgFilePolicy.getDownloadCfgFile("/global", "global/voicemail-conf.xml");
            File cota_cfg = fileInfo == null ? null : new File(fileInfo[0]);
            if (cota_cfg == null || !cota_cfg.exists()) {
                File cfg = HwCfgFilePolicy.getCfgFile("voicemail-conf.xml", 0);
                if (cfg != null) {
                    confFile = cfg;
                    log("load voicemail-conf.xml from HwCfgFilePolicy folder");
                } else if (vmFileCust.exists()) {
                    confFile = vmFileCust;
                    log("load voicemail-conf.xml from cust folder");
                } else {
                    confFile = vmFile;
                    log("load voicemail-conf.xml from etc folder");
                }
                try {
                    return new FileReader(confFile);
                } catch (FileNotFoundException e) {
                    Rlog.w(LOG_TAG, "Can't open " + Environment.getRootDirectory() + "/" + PARTNER_VOICEMAIL_PATH);
                    return null;
                }
            } else {
                confFile = cota_cfg;
                log("load voicemail-conf.xml from HwCfgFilePolicy DownloadFile folder");
                return new FileReader(confFile);
            }
        } catch (NoClassDefFoundError e2) {
            Rlog.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
        }
    }

    private static void log(String string) {
        RlogEx.i(LOG_TAG, string);
    }
}
