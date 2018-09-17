package java.util;

public class Observable {
    private boolean changed = false;
    private Vector<Observer> obs = new Vector();

    public synchronized void addObserver(Observer o) {
        if (o == null) {
            throw new NullPointerException();
        } else if (!this.obs.contains(o)) {
            this.obs.addElement(o);
        }
    }

    public synchronized void deleteObserver(Observer o) {
        this.obs.removeElement(o);
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

    /* JADX WARNING: Missing block: B:9:0x0013, code:
            r1 = r0.length - 1;
     */
    /* JADX WARNING: Missing block: B:10:0x0016, code:
            if (r1 < 0) goto L_0x0025;
     */
    /* JADX WARNING: Missing block: B:11:0x0018, code:
            ((java.util.Observer) r0[r1]).update(r3, r4);
            r1 = r1 - 1;
     */
    /* JADX WARNING: Missing block: B:15:0x0025, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyObservers(Object arg) {
        synchronized (this) {
            if (hasChanged()) {
                Object[] arrLocal = this.obs.toArray();
                clearChanged();
            }
        }
    }

    public synchronized void deleteObservers() {
        this.obs.removeAllElements();
    }

    protected synchronized void setChanged() {
        this.changed = true;
    }

    protected synchronized void clearChanged() {
        this.changed = false;
    }

    public synchronized boolean hasChanged() {
        return this.changed;
    }

    public synchronized int countObservers() {
        return this.obs.size();
    }
}
