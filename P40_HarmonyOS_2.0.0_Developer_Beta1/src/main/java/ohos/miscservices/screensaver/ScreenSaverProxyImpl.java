package ohos.miscservices.screensaver;

import java.util.List;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.miscservices.screensaver.IScreenSaverSysAbility;
import ohos.miscservices.screensaver.ScreenSaver;
import ohos.rpc.IRemoteObject;

public class ScreenSaverProxyImpl implements IScreenSaverSysAbility, IScreenSaverSysAbility.IScreenSaver {
    private static final Object INSTANCE_LOCK = new Object();
    public static final int SCREEN_SAVER_ABILITY_ID = -1;
    private static volatile ScreenSaverProxyImpl sInstance;
    private IRemoteObject remoteObject;
    private IScreenSaverSysAbility.IScreenSaver screenSaverInnerAbility;

    private ScreenSaverProxyImpl(Context context, IRemoteObject iRemoteObject) {
        this.remoteObject = iRemoteObject;
        this.screenSaverInnerAbility = new ScreenSaverDefaultImpl(context);
    }

    public static ScreenSaverProxyImpl getInstance(Context context, IRemoteObject iRemoteObject) {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new ScreenSaverProxyImpl(context, iRemoteObject);
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remoteObject;
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public void activeElement(ElementName elementName) {
        this.screenSaverInnerAbility.activeElement(elementName);
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public ElementName getActiveElement() {
        return this.screenSaverInnerAbility.getActiveElement();
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public void preview(ElementName elementName) {
        this.screenSaverInnerAbility.preview(elementName);
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public List<ScreenSaver.ScreenSaverBean> getElementList() {
        return this.screenSaverInnerAbility.getElementList();
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public void setScreenSaverPolicy(int i) {
        this.screenSaverInnerAbility.setScreenSaverPolicy(i);
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public int getScreenSaverPolicy() {
        return this.screenSaverInnerAbility.getScreenSaverPolicy();
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public void enable() {
        this.screenSaverInnerAbility.enable();
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public void disable() {
        this.screenSaverInnerAbility.disable();
    }

    @Override // ohos.miscservices.screensaver.IScreenSaverSysAbility.IScreenSaver
    public boolean isEnabled() {
        return this.screenSaverInnerAbility.isEnabled();
    }
}
