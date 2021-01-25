package ohos.data.rdb;

import java.util.ArrayList;
import java.util.Arrays;

public class RawRdbPredicates extends RdbPredicates {
    public RawRdbPredicates(String str, String str2, String[] strArr) {
        super(str);
        ArrayList arrayList;
        super.setWhereClause(str2);
        if (strArr != null) {
            arrayList = new ArrayList(Arrays.asList(strArr));
        }
        super.setWhereArgs(arrayList);
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates equalTo(String str, String str2) {
        throw new UnsupportedOperationException("equalTo isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates notEqualTo(String str, String str2) {
        throw new UnsupportedOperationException("notEqualTo isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates beginWrap() {
        throw new UnsupportedOperationException("beginWrap isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates endWrap() {
        throw new UnsupportedOperationException("endWrap isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates or() {
        throw new UnsupportedOperationException("or isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates and() {
        throw new UnsupportedOperationException("and isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates contains(String str, String str2) {
        throw new UnsupportedOperationException("contains isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates beginsWith(String str, String str2) {
        throw new UnsupportedOperationException("beginsWith isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates endsWith(String str, String str2) {
        throw new UnsupportedOperationException("endsWith isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates isNull(String str) {
        throw new UnsupportedOperationException("isNull isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates isNotNull(String str) {
        throw new UnsupportedOperationException("isNotNull isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates like(String str, String str2) {
        throw new UnsupportedOperationException("like isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates glob(String str, String str2) {
        throw new UnsupportedOperationException("glob isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates between(String str, String str2, String str3) {
        throw new UnsupportedOperationException("between isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates notBetween(String str, String str2, String str3) {
        throw new UnsupportedOperationException("notBetween isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates greaterThan(String str, String str2) {
        throw new UnsupportedOperationException("greaterThan isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates lessThan(String str, String str2) {
        throw new UnsupportedOperationException("lessThan isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates greaterThanOrEqualTo(String str, String str2) {
        throw new UnsupportedOperationException("greaterThanOrEqualTo isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates lessThanOrEqualTo(String str, String str2) {
        throw new UnsupportedOperationException("lessThanOrEqualTo isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates orderByAsc(String str) {
        throw new UnsupportedOperationException("orderByAsc isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates orderByDesc(String str) {
        throw new UnsupportedOperationException("orderByDesc isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates distinct() {
        throw new UnsupportedOperationException("distinct isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates limit(int i) {
        throw new UnsupportedOperationException("limit isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates offset(int i) {
        throw new UnsupportedOperationException("offset isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates groupBy(String[] strArr) {
        throw new UnsupportedOperationException("groupBy isn't supported in RawRawRdbPredicates");
    }

    @Override // ohos.data.rdb.RdbPredicates, ohos.data.AbsPredicates
    public RawRdbPredicates indexedBy(String str) {
        throw new UnsupportedOperationException("indexedBy isn't supported in RawRawRdbPredicates");
    }
}
