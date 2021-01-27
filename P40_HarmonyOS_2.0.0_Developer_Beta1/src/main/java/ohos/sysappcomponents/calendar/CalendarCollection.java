package ohos.sysappcomponents.calendar;

import java.util.Locale;
import java.util.Optional;
import ohos.data.resultset.ResultSet;
import ohos.sysappcomponents.calendar.entity.CalendarEntity;
import ohos.sysappcomponents.calendar.support.Rule;

public class CalendarCollection {
    private static final String TAG = CalendarCollection.class.getSimpleName();
    private String[] columns;
    private ResultSet resultSet;
    private Rule rule;

    CalendarCollection(ResultSet resultSet2, Rule rule2, String[] strArr) {
        this.resultSet = resultSet2;
        this.rule = rule2;
        this.columns = strArr;
    }

    public boolean hasNext() {
        return this.resultSet.getRowIndex() <= count() + -2;
    }

    public Optional<? extends CalendarEntity> next() {
        if (!hasNext()) {
            return Optional.empty();
        }
        if (this.resultSet.goToNextRow()) {
            return this.rule.getEntity(this.resultSet, this.columns);
        }
        LogUtil.warn(TAG, String.format(Locale.ROOT, "Collection has next row but can't go to next. Current index: %s. Count: %s.", Integer.valueOf(this.resultSet.getRowIndex()), Integer.valueOf(count())));
        return Optional.empty();
    }

    public int count() {
        return this.resultSet.getRowCount();
    }

    public void close() {
        this.resultSet.close();
    }
}
