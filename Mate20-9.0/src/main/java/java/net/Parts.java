package java.net;

/* compiled from: URL */
class Parts {
    String path;
    String query;
    String ref;

    Parts(String file, String host) {
        int ind = file.indexOf(35);
        this.ref = ind < 0 ? null : file.substring(ind + 1);
        String file2 = ind < 0 ? file : file.substring(0, ind);
        int q = file2.lastIndexOf(63);
        if (q != -1) {
            this.query = file2.substring(q + 1);
            this.path = file2.substring(0, q);
        } else {
            this.path = file2;
        }
        if (this.path != null && this.path.length() > 0 && this.path.charAt(0) != '/' && host != null && !host.isEmpty()) {
            this.path = '/' + this.path;
        }
    }

    /* access modifiers changed from: package-private */
    public String getPath() {
        return this.path;
    }

    /* access modifiers changed from: package-private */
    public String getQuery() {
        return this.query;
    }

    /* access modifiers changed from: package-private */
    public String getRef() {
        return this.ref;
    }
}
