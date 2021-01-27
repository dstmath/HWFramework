package ohos.sysappcomponents.calendar.support;

import java.util.Optional;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.calendar.entity.CalendarEntity;

public interface Rule {
    Optional<? extends CalendarEntity> getEntity(ResultSet resultSet, String[] strArr);
}
