package android.support.v4.media.session;

import android.media.session.PlaybackState;
import android.media.session.PlaybackState.Builder;
import android.media.session.PlaybackState.CustomAction;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import java.util.Iterator;
import java.util.List;

@RequiresApi(22)
class PlaybackStateCompatApi22 {
    PlaybackStateCompatApi22() {
    }

    public static Bundle getExtras(Object stateObj) {
        return ((PlaybackState) stateObj).getExtras();
    }

    public static Object newInstance(int state, long position, long bufferedPosition, float speed, long actions, CharSequence errorMessage, long updateTime, List<Object> customActions, long activeItemId, Bundle extras) {
        Builder stateObj = new Builder();
        stateObj.setState(state, position, speed, updateTime);
        stateObj.setBufferedPosition(bufferedPosition);
        stateObj.setActions(actions);
        stateObj.setErrorMessage(errorMessage);
        Iterator customAction$iterator = customActions.iterator();
        while (customAction$iterator.hasNext()) {
            stateObj.addCustomAction((CustomAction) customAction$iterator.next());
        }
        stateObj.setActiveQueueItemId(activeItemId);
        stateObj.setExtras(extras);
        return stateObj.build();
    }
}
