package ohos.media.audiofwk;

import java.util.ArrayList;
import ohos.rpc.IRemoteBroker;

public interface IAudioServiceProxy extends IRemoteBroker {
    int getMaxVolumeByStreamType(int i);

    int getMinVolumeByStreamType(int i);

    int getVolumeByStreamType(int i);

    int setVolumeByStreamType(int i, int i2, ArrayList<Integer> arrayList);
}
