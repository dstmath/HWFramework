package ohos.global.config;

/* compiled from: ConfigManagerImpl */
enum SysProp {
    BRIDGE_ON_5GHZ("ro.config.disable_5G_bridge", "false", true),
    COUNTRY_REGION("ro.hw.country;ro.config.hw.region", null),
    FACE_CHANGING("ro.camera.cos_face_changing", "true"),
    VOICE_CFG("ro.config.voice_cfg", null);
    
    private String def;
    private String key;
    private boolean opposite;

    private SysProp(String str, String str2) {
        this(str, str2, false);
    }

    private SysProp(String str, String str2, boolean z) {
        this.key = str;
        this.def = str2;
        this.opposite = z;
    }

    public String getKey() {
        return this.key;
    }

    public String getDef() {
        return this.def;
    }

    public Boolean getOpposite() {
        return Boolean.valueOf(this.opposite);
    }
}
