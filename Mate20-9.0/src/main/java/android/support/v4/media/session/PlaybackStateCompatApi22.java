package android.support.v4.media.session;

import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import java.util.Iterator;
import java.util.List;

@RequiresApi(22)
class PlaybackStateCompatApi22 {
    public static Bundle getExtras(Object stateObj) {
        return ((PlaybackState) stateObj).getExtras();
    }

    public static Object newInstance(int state, long position, long bufferedPosition, float speed, long actions, CharSequence errorMessage, long updateTime, List<Object> customActions, long activeItemId, Bundle extras) {
        PlaybackState.Builder stateObj = new PlaybackState.Builder();
        stateObj.setState(state, position, speed, updateTime);
        stateObj.setBufferedPosition(bufferedPosition);
        stateObj.setActions(actions);
        stateObj.setErrorMessage(errorMessage);
        Iterator<Object> it = customActions.iterator();
        while (it.hasNext()) {
            stateObj.addCustomAction((PlaybackState.CustomAction) it.next());
        }
        stateObj.setActiveQueueItemId(activeItemId);
        stateObj.setExtras(extras);
        return stateObj.build();
    }

    private PlaybackStateCompatApi22() {
    }
}
