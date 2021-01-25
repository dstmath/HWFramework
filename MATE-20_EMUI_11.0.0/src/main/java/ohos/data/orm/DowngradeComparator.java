package ohos.data.orm;

import java.util.Comparator;

public class DowngradeComparator implements Comparator<OrmMigration> {
    public int compare(OrmMigration ormMigration, OrmMigration ormMigration2) {
        if (ormMigration.getBeginVersion() < ormMigration2.getBeginVersion()) {
            return 1;
        }
        if (ormMigration.getBeginVersion() > ormMigration2.getBeginVersion()) {
            return -1;
        }
        if (ormMigration.getEndVersion() < ormMigration2.getBeginVersion()) {
            return 1;
        }
        if (ormMigration.getEndVersion() > ormMigration2.getBeginVersion()) {
            return -1;
        }
        return 0;
    }
}
