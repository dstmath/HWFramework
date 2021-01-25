package ohos.data.orm;

public class ObjectId {
    private String alias;
    private String entityName;
    private long id;

    public ObjectId(String str, String str2, long j) {
        this.entityName = str2;
        this.id = j;
        this.alias = str;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public String getAlias() {
        return this.alias;
    }

    public int hashCode() {
        String str = this.alias;
        int i = 0;
        int hashCode = ((str == null ? 0 : str.hashCode()) + 31) * 31;
        String str2 = this.entityName;
        if (str2 != null) {
            i = str2.hashCode();
        }
        long j = this.id;
        return ((hashCode + i) * 31) + ((int) (j ^ (j >>> 32)));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ObjectId)) {
            return false;
        }
        ObjectId objectId = (ObjectId) obj;
        if (!compareString(this.alias, objectId.alias) || !compareString(this.entityName, objectId.entityName)) {
            return false;
        }
        return this.id == objectId.id;
    }

    private boolean compareString(String str, String str2) {
        if (str == null) {
            return str2 == null;
        }
        return str.equals(str2);
    }
}
