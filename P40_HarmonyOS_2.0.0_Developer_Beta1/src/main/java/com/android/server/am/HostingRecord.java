package com.android.server.am;

import android.content.ComponentName;

public final class HostingRecord {
    private static final int APP_ZYGOTE = 2;
    private static final int REGULAR_ZYGOTE = 0;
    private static final int WEBVIEW_ZYGOTE = 1;
    private final String mDefiningPackageName;
    private final int mDefiningUid;
    private final String mHostingName;
    private final String mHostingType;
    private final int mHostingZygote;

    public HostingRecord(String hostingType) {
        this(hostingType, null, 0, null, -1);
    }

    public HostingRecord(String hostingType, ComponentName hostingName) {
        this(hostingType, hostingName, 0);
    }

    public HostingRecord(String hostingType, String hostingName) {
        this(hostingType, hostingName, 0);
    }

    private HostingRecord(String hostingType, ComponentName hostingName, int hostingZygote) {
        this(hostingType, hostingName.toShortString(), hostingZygote);
    }

    private HostingRecord(String hostingType, String hostingName, int hostingZygote) {
        this(hostingType, hostingName, hostingZygote, null, -1);
    }

    private HostingRecord(String hostingType, String hostingName, int hostingZygote, String definingPackageName, int definingUid) {
        this.mHostingType = hostingType;
        this.mHostingName = hostingName;
        this.mHostingZygote = hostingZygote;
        this.mDefiningPackageName = definingPackageName;
        this.mDefiningUid = definingUid;
    }

    public String getType() {
        return this.mHostingType;
    }

    public String getName() {
        return this.mHostingName;
    }

    public int getDefiningUid() {
        return this.mDefiningUid;
    }

    public String getDefiningPackageName() {
        return this.mDefiningPackageName;
    }

    public static HostingRecord byWebviewZygote(ComponentName hostingName) {
        return new HostingRecord("", hostingName.toShortString(), 1);
    }

    public static HostingRecord byAppZygote(ComponentName hostingName, String definingPackageName, int definingUid) {
        return new HostingRecord("", hostingName.toShortString(), 2, definingPackageName, definingUid);
    }

    public boolean usesAppZygote() {
        return this.mHostingZygote == 2;
    }

    public boolean usesWebviewZygote() {
        return this.mHostingZygote == 1;
    }
}
