package com.android.server.radar;

public class RadarHeader {
    private static final int HEAD_MAX_SIZE = 256;
    private int mBugType;
    private int mLevel;
    private String mPackageName;
    private int mScene;
    private String mVersion;

    public RadarHeader(String packageName, String version, int bugType, int scene, int level) {
        this.mPackageName = packageName;
        this.mVersion = version;
        this.mLevel = level;
        this.mBugType = bugType;
        this.mScene = scene;
    }

    public RadarHeader(String packageName, String version, int scene, int level) {
        this(packageName, version, 100, scene, level);
    }

    public String getRadarHeader() {
        return "Package: " + this.mPackageName + "\n" + "APK version: " + this.mVersion + "\n" + "Bug type: " + this.mBugType + "\n" + "Scene def: " + this.mScene + "\n";
    }

    public int getScene() {
        return this.mScene;
    }

    public int getLevel() {
        return this.mLevel;
    }
}
