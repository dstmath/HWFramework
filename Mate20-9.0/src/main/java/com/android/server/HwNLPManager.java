package com.android.server;

import android.content.Context;
import android.content.Intent;

public interface HwNLPManager {
    void setHwMultiNlpPolicy(Context context);

    void setLocationManagerService(LocationManagerService locationManagerService, Context context);

    void setPidGoogleLocation(int i, String str);

    boolean shouldSkipGoogleNlp(int i);

    boolean shouldSkipGoogleNlp(Intent intent, String str);

    boolean skipForeignNlpPackage(String str, String str2);

    boolean useCivilNlpPackage(String str, String str2);
}
