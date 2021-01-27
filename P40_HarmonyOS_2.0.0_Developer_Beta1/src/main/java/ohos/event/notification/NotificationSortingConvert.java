package ohos.event.notification;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class NotificationSortingConvert {
    private static final int INDEX_ID = 2;
    private static final int INDEX_IDENTIFIER = 0;
    private static final int INDEX_OVERRIDEGROUPKEY = 5;
    private static final int INDEX_PKG_NAME = 1;
    private static final int INDEX_TAG = 3;
    private static final int INDEX_UID = 4;
    private static final int KEY_LENGTH = 6;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.NOTIFICATION_DOMAIN, TAG);
    private static final int MIN_LENGTH = 5;
    private static final String MY_SPLIT_CHAR = "_";
    private static final String SPLIT_CHAR = "\\|";
    private static final String TAG = "NotificationSortingConvert";
    private static String currentPkg = null;
    private static Method visibilityOverrideMethod;

    static {
        visibilityOverrideMethod = null;
        try {
            visibilityOverrideMethod = NotificationListenerService.Ranking.class.getDeclaredMethod("getVisibilityOverride", new Class[0]);
        } catch (NoSuchMethodException unused) {
            HiLog.error(LABEL, "NotificationSortingConvert::Unable to obtain the reflect method", new Object[0]);
        }
    }

    static Optional<NotificationSortingMap> convertRankingMapToSorttingMap(NotificationListenerService.RankingMap rankingMap) {
        if (rankingMap == null) {
            HiLog.debug(LABEL, "NotificationSortingConvert::the param is invalid.", new Object[0]);
            return Optional.empty();
        }
        String[] orderedKeys = rankingMap.getOrderedKeys();
        if (orderedKeys == null) {
            return Optional.empty();
        }
        String currentPkg2 = getCurrentPkg(NotificationTransformer.getInstance().getAospContext());
        ArrayList arrayList = new ArrayList();
        for (String str : orderedKeys) {
            NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
            if (!rankingMap.getRanking(str, ranking)) {
                HiLog.debug(LABEL, "NotificationSortingConvert::get ranking from rankingMap failed.", new Object[0]);
            } else {
                Optional<NotificationSorting> convertRankingToNotificationSorting = convertRankingToNotificationSorting(ranking, currentPkg2);
                if (!convertRankingToNotificationSorting.isPresent()) {
                    HiLog.debug(LABEL, "NotificationSortingConvert:convert ranking to sorting failed.", new Object[0]);
                } else {
                    arrayList.add(convertRankingToNotificationSorting.get());
                }
            }
        }
        return Optional.of(new NotificationSortingMap(arrayList));
    }

    static Optional<NotificationSorting> convertRankingToNotificationSorting(NotificationListenerService.Ranking ranking, String str) {
        if (ranking == null) {
            HiLog.debug(LABEL, "NotificationSortingConvert::the ranking is null.", new Object[0]);
            return Optional.empty();
        }
        String key = ranking.getKey();
        if (key == null) {
            HiLog.debug(LABEL, "NotificationSortingConvert::get key failed.", new Object[0]);
            return Optional.empty();
        }
        String[] split = key.split(SPLIT_CHAR);
        if (split.length < 5) {
            return Optional.empty();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(split[2]);
        sb.append("_");
        sb.append(split[1]);
        sb.append("_");
        sb.append(split[4]);
        sb.append("_");
        sb.append(str);
        sb.append("_");
        sb.append(split[0]);
        sb.append("_");
        sb.append(split[3]);
        if (split.length == 6) {
            sb.append("_");
            sb.append(split[5]);
        }
        NotificationSorting notificationSorting = new NotificationSorting();
        notificationSorting.setHashCode(sb.toString());
        notificationSorting.setRanking(ranking.getRank());
        Optional<NotificationSlot> notificationSlot = NotificationTransformer.getInstance().getNotificationSlot(ranking.getChannel());
        if (!notificationSlot.isPresent()) {
            HiLog.debug(LABEL, "NotificationSortingConvert::convert channel to slot failed.", new Object[0]);
            return Optional.empty();
        }
        notificationSorting.setNotificationSlot(notificationSlot.get());
        notificationSorting.setIsDisplayBadge(ranking.canShowBadge());
        notificationSorting.setIsHiddenNotification(ranking.isSuspended());
        notificationSorting.setIsSuitInterruptionFilter(ranking.matchesInterruptionFilter());
        notificationSorting.setImportance(ranking.getImportance());
        notificationSorting.setGroupKeyOverride(ranking.getOverrideGroupKey());
        int i = -1;
        Method method = visibilityOverrideMethod;
        if (method != null) {
            try {
                i = ((Integer) method.invoke(ranking, new Object[0])).intValue();
            } catch (IllegalAccessException | InvocationTargetException e) {
                HiLog.error(LABEL, "NotificationSortingConvert::exception info is %{public}s.", new Object[]{e.toString()});
            }
        }
        notificationSorting.setVisiblenessOverride(i);
        return Optional.of(notificationSorting);
    }

    private static String getCurrentPkg(Context context) {
        if (currentPkg == null && context != null) {
            currentPkg = context.getPackageName();
        }
        return currentPkg;
    }
}
