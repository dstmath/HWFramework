package com.android.server.display;

import android.view.Display;

public class DisplayDeviceInfoEx {
    public static final int FLAG_DEFAULT_DISPLAY = 1;
    public static final int FLAG_DESTROY_CONTENT_ON_REMOVAL = 1024;
    public static final int FLAG_PRESENTATION = 64;
    public static final int FLAG_SECURE = 4;
    public static final int TOUCH_VIRTUAL = 3;
    public static final int TYPE_OVERLAY = 4;
    private DisplayDeviceInfo mDisplayDeviceInfo = new DisplayDeviceInfo();

    public void setDisplayInfo(DisplayDeviceInfo displayInfo) {
        this.mDisplayDeviceInfo = displayInfo;
    }

    public DisplayDeviceInfo getDisplayInfo() {
        return this.mDisplayDeviceInfo;
    }

    public void setName(String name) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.name = name;
        }
    }

    public void setUniqueId(String uniqueId) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.uniqueId = uniqueId;
        }
    }

    public void setWidth(int width) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.width = width;
        }
    }

    public void setHeight(int height) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.height = height;
        }
    }

    public void setModeId(int modeId) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.modeId = modeId;
        }
    }

    public void setDefaultModeId(int defaultModeId) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.defaultModeId = defaultModeId;
        }
    }

    public void setSupportedModes(Display.Mode[] supportedModes) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.supportedModes = supportedModes;
        }
    }

    public void setDensityDpi(int densityDpi) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.densityDpi = densityDpi;
        }
    }

    public void setXdpi(float xDpi) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.xDpi = xDpi;
        }
    }

    public void setYdpi(float yDpi) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.yDpi = yDpi;
        }
    }

    public void setPresentationDeadlineNanos(long presentationDeadlineNanos) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.presentationDeadlineNanos = presentationDeadlineNanos;
        }
    }

    public void setFlags(int flags) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.flags = flags;
        }
    }

    public void setType(int type) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.type = type;
        }
    }

    public void setTouch(int touch) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.touch = touch;
        }
    }

    public void setState(int state) {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            displayDeviceInfo.state = state;
        }
    }

    public int getFlags() {
        DisplayDeviceInfo displayDeviceInfo = this.mDisplayDeviceInfo;
        if (displayDeviceInfo != null) {
            return displayDeviceInfo.flags;
        }
        return 1;
    }
}
