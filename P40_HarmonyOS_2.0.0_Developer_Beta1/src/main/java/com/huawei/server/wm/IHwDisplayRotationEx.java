package com.huawei.server.wm;

import android.os.Message;

public interface IHwDisplayRotationEx {
    public static final int DARK_LIGHT_ACTION = -3;
    public static final int DIRECTION_DOWN_ACTION = 2;
    public static final int DIRECTION_LEFT_ACTION = 3;
    public static final int DIRECTION_RIGHT_ACTION = 1;
    public static final int DIRECTION_UNKNOWN_ACTION = -1;
    public static final int DIRECTION_UP_ACTION = 0;
    public static final int FACE_AWAY_ACTION = -2;
    public static final int SENSOR_ROTATION = 1;
    public static final int SWING_ROTAITON = 2;
    public static final int UNKNOWN_ROTAITON = 0;

    int getRotationFromSensorOrFace(int i);

    int getRotationType();

    int getSwingRotation(int i, int i2);

    void handleReportLog(Message message);

    boolean isIntelliServiceEnabled(int i);

    void reportRotation(int i, int i2, int i3, String str);

    void setRotation(int i);

    void setRotationType(int i);

    void setSensorRotation(int i);

    void setSwingRotation(int i);

    void startIntelliService();

    void startIntelliService(int i);
}
