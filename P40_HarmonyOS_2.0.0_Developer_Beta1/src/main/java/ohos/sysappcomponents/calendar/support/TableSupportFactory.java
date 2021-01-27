package ohos.sysappcomponents.calendar.support;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import ohos.sysappcomponents.calendar.LogUtil;
import ohos.sysappcomponents.calendar.entity.Accounts;
import ohos.sysappcomponents.calendar.entity.CalendarEntity;
import ohos.sysappcomponents.calendar.entity.Events;
import ohos.sysappcomponents.calendar.entity.Instances;
import ohos.sysappcomponents.calendar.entity.Participants;
import ohos.sysappcomponents.calendar.entity.Reminders;

public class TableSupportFactory {
    private static final Map<Class<? extends CalendarEntity>, TableSupport> ENTITY_TO_SUPPORT_MAP = new HashMap();
    private static final String TAG = TableSupportFactory.class.getSimpleName();

    static {
        ENTITY_TO_SUPPORT_MAP.put(Accounts.class, AccountsSupport.getInstance());
        ENTITY_TO_SUPPORT_MAP.put(Events.class, EventsSupport.getInstance());
        ENTITY_TO_SUPPORT_MAP.put(Instances.class, InstancesSupport.getInstance());
        ENTITY_TO_SUPPORT_MAP.put(Participants.class, ParticipantsSupport.getInstance());
        ENTITY_TO_SUPPORT_MAP.put(Reminders.class, RemindersSupport.getInstance());
    }

    public static TableSupport getTableSupport(Class<? extends CalendarEntity> cls) {
        if (ENTITY_TO_SUPPORT_MAP.containsKey(cls)) {
            return ENTITY_TO_SUPPORT_MAP.get(cls);
        }
        LogUtil.warn(TAG, "Only subclass of CalendarEntity is available.");
        throw new InvalidParameterException("Invalid entity class: " + cls.getName() + "Only subclass of CalendarEntity is available.");
    }
}
