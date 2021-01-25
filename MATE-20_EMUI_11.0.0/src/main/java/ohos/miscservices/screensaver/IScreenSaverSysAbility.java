package ohos.miscservices.screensaver;

import java.util.List;
import ohos.bundle.ElementName;
import ohos.miscservices.screensaver.ScreenSaver;
import ohos.rpc.IRemoteBroker;

public interface IScreenSaverSysAbility extends IRemoteBroker {

    public interface IScreenSaver {
        void activeElement(ElementName elementName);

        void disable();

        void enable();

        ElementName getActiveElement();

        List<ScreenSaver.ScreenSaverBean> getElementList();

        int getScreenSaverPolicy();

        boolean isEnabled();

        void preview(ElementName elementName);

        void setScreenSaverPolicy(int i);
    }
}
