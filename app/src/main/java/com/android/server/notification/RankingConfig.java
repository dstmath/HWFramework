package com.android.server.notification;

public interface RankingConfig {
    int getImportance(String str, int i);

    int getPriority(String str, int i);

    int getVisibilityOverride(String str, int i);

    void setImportance(String str, int i, int i2);

    void setPriority(String str, int i, int i2);

    void setVisibilityOverride(String str, int i, int i2);
}
