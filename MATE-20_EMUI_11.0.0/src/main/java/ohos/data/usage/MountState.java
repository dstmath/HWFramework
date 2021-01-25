package ohos.data.usage;

public enum MountState {
    DISK_MOUNTED(0, "mounted"),
    DISK_UNMOUNTED(1, "unmounted"),
    DISK_REMOVED(2, "removed"),
    DISK_UNKNOWN(3, "unknown");
    
    private int code;
    private String description;

    private MountState(int i, String str) {
        this.code = i;
        this.description = str;
    }

    public String getDescription() {
        return this.description;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public static MountState getStatus(String str) {
        char c;
        switch (str.hashCode()) {
            case -1792139919:
                if (str.equals("ejecting")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1340233281:
                if (str.equals("unmounted")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1091836000:
                if (str.equals("removed")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1203725746:
                if (str.equals("bad_removal")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1242932856:
                if (str.equals("mounted")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1536898522:
                if (str.equals("checking")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return DISK_MOUNTED;
        }
        if (c == 1 || c == 2 || c == 3 || c == 4) {
            return DISK_UNMOUNTED;
        }
        if (c != 5) {
            return DISK_UNKNOWN;
        }
        return DISK_REMOVED;
    }
}
