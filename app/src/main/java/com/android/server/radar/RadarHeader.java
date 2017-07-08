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
        return new StringBuilder(HEAD_MAX_SIZE).append("Package: ").append(this.mPackageName).append("\n").append("APK version: ").append(this.mVersion).append("\n").append("Bug type: ").append(this.mBugType).append("\n").append("Scene def: ").append(this.mScene).append("\n").toString();
    }

    public int getScene() {
        return this.mScene;
    }

    public int getLevel() {
        return this.mLevel;
    }
}
