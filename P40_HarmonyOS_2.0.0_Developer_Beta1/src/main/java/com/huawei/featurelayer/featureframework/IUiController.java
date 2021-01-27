package com.huawei.featurelayer.featureframework;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;

public interface IUiController extends IFeature {
    void callActivityOnCreate(Activity activity, Bundle bundle);

    void callActivityOnDestroy(Activity activity);

    int dispatchService(Intent intent);

    boolean fakeActivity(Context context, Intent intent);

    LayoutInflater getFeatureLayoutInflater(String str);

    Resources getFeatureResources(String str);

    boolean handleMessage(Message message);

    Activity newActivity(Instrumentation instrumentation, ClassLoader classLoader, String str, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException;
}
