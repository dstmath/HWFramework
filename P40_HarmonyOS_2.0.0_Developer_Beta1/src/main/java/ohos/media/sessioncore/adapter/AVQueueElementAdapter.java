package ohos.media.sessioncore.adapter;

import android.media.session.MediaSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import ohos.media.common.adapter.AVDescriptionAdapter;
import ohos.media.common.sessioncore.AVQueueElement;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVQueueElementAdapter {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVQueueElementAdapter.class);

    public static MediaSession.QueueItem getQueueItem(AVQueueElement aVQueueElement) {
        if (aVQueueElement != null && aVQueueElement.getDescription() != null && aVQueueElement.getElementId() != -1) {
            return new MediaSession.QueueItem(AVDescriptionAdapter.getMediaDescription(aVQueueElement.getDescription()), aVQueueElement.getElementId());
        }
        LOGGER.error("Invalid input queue element", new Object[0]);
        return null;
    }

    public static AVQueueElement getAVQueueElement(MediaSession.QueueItem queueItem) {
        if (queueItem != null && queueItem.getDescription() != null && queueItem.getQueueId() != -1) {
            return new AVQueueElement(AVDescriptionAdapter.getAVDescription(queueItem.getDescription()), queueItem.getQueueId());
        }
        LOGGER.error("Invalid input queue item", new Object[0]);
        return null;
    }

    public static List<MediaSession.QueueItem> getListQueueItem(List<AVQueueElement> list) {
        if (list.isEmpty()) {
            LOGGER.error("Invalid input queue", new Object[0]);
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        list.forEach(new Consumer(arrayList) {
            /* class ohos.media.sessioncore.adapter.$$Lambda$AVQueueElementAdapter$4Fq2KyJBeapDJlv3_4s5mtobBs */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.add(AVQueueElementAdapter.getQueueItem((AVQueueElement) obj));
            }
        });
        return arrayList;
    }
}
