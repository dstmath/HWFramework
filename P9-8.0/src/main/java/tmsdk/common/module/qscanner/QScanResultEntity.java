package tmsdk.common.module.qscanner;

import java.util.ArrayList;

public class QScanResultEntity {
    public String packageName;
    public String path;
    public ArrayList<QScanResultPluginEntity> plugins;
    public int scanResult;
    public String softName;
    public String version;
    public int versionCode;
    public String virusDiscription;
    public String virusName;
    public String virusUrl;
}
