package android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.widget.TextView;

public interface IHwNsdImpl {
    boolean StopSdrForSpecial(String str, int i);

    void adaptPowerSave(Context context, MotionEvent motionEvent);

    void adjNsd(String str);

    boolean checkAdBlock(View view, String str);

    boolean checkIfNsdSupportCursor();

    boolean checkIfNsdSupportLauncher();

    boolean checkIfSupportNsd();

    boolean checkIs2DSDRCase(Context context, ViewRootImpl viewRootImpl);

    float computeSDRRatio(Context context, View view, View view2, float f, float f2, int i);

    int computeSDRRatioBase(Context context, View view, View view2);

    void createEventAnalyzed();

    boolean drawBitmapCursor(int i, TextView textView, Rect rect);

    void enableNsdSave();

    String[] getCustAppList(int i);

    int getCustScreenDimDurationLocked(int i);

    int getDisplayOrientation(TextView textView);

    int getTextViewZOrderId(AttachInfo attachInfo);

    void initAPS(Context context, int i, int i2);

    boolean isAPSReady();

    boolean isCase(View view);

    boolean isCursorBlinkCase(TextView textView, Rect rect);

    boolean isCursorCompleteVisible(TextView textView, Rect rect, AttachInfo attachInfo);

    boolean isCursorOpaque(TextView textView);

    boolean isGameProcess(String str);

    boolean isNeedAppDraw();

    boolean isScreenHight(int i);

    boolean isSupportAPSEventAnalysis();

    boolean isSupportAps();

    void powerCtroll();

    void processCase(DisplayListCanvas displayListCanvas);

    void releaseNSD();

    void resetSaveNSD(int i);

    boolean sendCursorBmpToSF(int i, int i2, int i3, Bitmap bitmap);

    void setAPSOnPause();

    void setContext(Context context);

    void setCurrentDrawTime(long j);

    void setIsDrawCursor(int i);

    void setView(View view);

    void startNsd(int i);

    void stopNsd();
}
