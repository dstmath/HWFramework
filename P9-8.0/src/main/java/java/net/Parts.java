package java.net;

/* compiled from: URL */
class Parts {
    String path;
    String query;
    String ref;

    Parts(String file, String host) {
        String str = null;
        int ind = file.indexOf(35);
        if (ind >= 0) {
            str = file.substring(ind + 1);
        }
        this.ref = str;
        if (ind >= 0) {
            file = file.substring(0, ind);
        }
        int q = file.lastIndexOf(63);
        if (q != -1) {
            this.query = file.substring(q + 1);
            this.path = file.substring(0, q);
        } else {
            this.path = file;
        }
        if (this.path != null && this.path.length() > 0 && this.path.charAt(0) != '/' && host != null && (host.isEmpty() ^ 1) != 0) {
            this.path = '/' + this.path;
        }
    }

    String getPath() {
        return this.path;
    }

    String getQuery() {
        return this.query;
    }

    String getRef() {
        return this.ref;
    }
}
