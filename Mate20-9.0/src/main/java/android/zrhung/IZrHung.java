package android.zrhung;

public interface IZrHung {
    public static final String APPEYE_ANR_NAME = "appeye_anr";
    public static final String APPEYE_BF_NAME = "appeye_bootfail";
    public static final String APPEYE_BK_NAME = "appeye_backkey";
    public static final String APPEYE_CANR_NAME = "appeye_coreappanr";
    public static final String APPEYE_CLA_NAME = "appeye_clearall";
    public static final String APPEYE_CL_NAME = "appeye_clear";
    public static final String APPEYE_FWB_NAME = "appeye_frameworkblock";
    public static final String APPEYE_HK_NAME = "appeye_homekey";
    public static final String APPEYE_NFW_NAME = "appeye_nofocuswindow";
    public static final String APPEYE_OBS_NAME = "appeye_observer";
    public static final String APPEYE_RCV_NAME = "appeye_receiver";
    public static final String APPEYE_SBF_NAME = "appeye_ssbinderfull";
    public static final String APPEYE_TWIN_NAME = "appeye_transparentwindow";
    public static final String APPEYE_UIP_NAME = "appeye_uiprobe";
    public static final String APPEYE_XCOLLIE_NAME = "appeye_xcollie";
    public static final int CONFIG_OK = 0;
    public static final int NOT_READY = 1;
    public static final int NOT_SUPPORT = -1;
    public static final int NO_CONFIG = -2;
    public static final String ZRHUNG_WP_SCRNONFWK_NAME = "zrhung_wp_screenon_framework";
    public static final String ZRHUNG_WP_VMWTG_NAME = "zrhung_wp_vm_watchdog";

    boolean addInfo(ZrHungData zrHungData);

    boolean cancelCheck(ZrHungData zrHungData);

    boolean check(ZrHungData zrHungData);

    int init(ZrHungData zrHungData);

    ZrHungData query();

    boolean sendEvent(ZrHungData zrHungData);

    boolean start(ZrHungData zrHungData);

    boolean stop(ZrHungData zrHungData);
}
