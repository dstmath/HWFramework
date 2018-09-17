package android.os;

public class Broadcaster {
    private Registration mReg;

    private class Registration {
        Registration next;
        Registration prev;
        int senderWhat;
        int[] targetWhats;
        Handler[] targets;

        /* synthetic */ Registration(Broadcaster this$0, Registration -this1) {
            this();
        }

        private Registration() {
        }
    }

    /* JADX WARNING: Missing block: B:10:0x002a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void request(int senderWhat, Handler target, int targetWhat) {
        Throwable th;
        synchronized (this) {
            try {
                Registration registration;
                if (this.mReg == null) {
                    Registration r = new Registration(this, null);
                    try {
                        r.senderWhat = senderWhat;
                        r.targets = new Handler[1];
                        r.targetWhats = new int[1];
                        r.targets[0] = target;
                        r.targetWhats[0] = targetWhat;
                        this.mReg = r;
                        r.next = r;
                        r.prev = r;
                    } catch (Throwable th2) {
                        th = th2;
                        registration = r;
                        throw th;
                    }
                }
                int n;
                Registration start = this.mReg;
                registration = start;
                while (registration.senderWhat < senderWhat) {
                    registration = registration.next;
                    if (registration == start) {
                        break;
                    }
                }
                if (registration.senderWhat != senderWhat) {
                    Registration reg = new Registration(this, null);
                    reg.senderWhat = senderWhat;
                    reg.targets = new Handler[1];
                    reg.targetWhats = new int[1];
                    reg.next = registration;
                    reg.prev = registration.prev;
                    registration.prev.next = reg;
                    registration.prev = reg;
                    if (registration == this.mReg && registration.senderWhat > reg.senderWhat) {
                        this.mReg = reg;
                    }
                    registration = reg;
                    n = 0;
                } else {
                    n = registration.targets.length;
                    Handler[] oldTargets = registration.targets;
                    int[] oldWhats = registration.targetWhats;
                    int i = 0;
                    while (i < n) {
                        if (oldTargets[i] == target && oldWhats[i] == targetWhat) {
                            return;
                        }
                        i++;
                    }
                    registration.targets = new Handler[(n + 1)];
                    System.arraycopy(oldTargets, 0, registration.targets, 0, n);
                    registration.targetWhats = new int[(n + 1)];
                    System.arraycopy(oldWhats, 0, registration.targetWhats, 0, n);
                }
                registration.targets[n] = target;
                registration.targetWhats[n] = targetWhat;
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    /* JADX WARNING: Missing block: B:27:0x0055, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancelRequest(int senderWhat, Handler target, int targetWhat) {
        synchronized (this) {
            Registration start = this.mReg;
            Registration r = start;
            if (start == null) {
                return;
            }
            while (r.senderWhat < senderWhat) {
                r = r.next;
                if (r == start) {
                    break;
                }
            }
            if (r.senderWhat == senderWhat) {
                Handler[] targets = r.targets;
                int[] whats = r.targetWhats;
                int oldLen = targets.length;
                int i = 0;
                while (i < oldLen) {
                    if (targets[i] == target && whats[i] == targetWhat) {
                        r.targets = new Handler[(oldLen - 1)];
                        r.targetWhats = new int[(oldLen - 1)];
                        if (i > 0) {
                            System.arraycopy(targets, 0, r.targets, 0, i);
                            System.arraycopy(whats, 0, r.targetWhats, 0, i);
                        }
                        int remainingLen = (oldLen - i) - 1;
                        if (remainingLen != 0) {
                            System.arraycopy(targets, i + 1, r.targets, i, remainingLen);
                            System.arraycopy(whats, i + 1, r.targetWhats, i, remainingLen);
                        }
                    } else {
                        i++;
                    }
                }
            }
        }
    }

    public void dumpRegistrations() {
        synchronized (this) {
            Registration start = this.mReg;
            System.out.println("Broadcaster " + this + " {");
            if (start != null) {
                Registration r = start;
                do {
                    System.out.println("    senderWhat=" + r.senderWhat);
                    int n = r.targets.length;
                    for (int i = 0; i < n; i++) {
                        System.out.println("        [" + r.targetWhats[i] + "] " + r.targets[i]);
                    }
                    r = r.next;
                } while (r != start);
            }
            System.out.println("}");
        }
    }

    /* JADX WARNING: Missing block: B:19:0x0035, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void broadcast(Message msg) {
        synchronized (this) {
            if (this.mReg == null) {
                return;
            }
            int senderWhat = msg.what;
            Registration start = this.mReg;
            Registration r = start;
            while (r.senderWhat < senderWhat) {
                r = r.next;
                if (r == start) {
                    break;
                }
            }
            if (r.senderWhat == senderWhat) {
                Handler[] targets = r.targets;
                int[] whats = r.targetWhats;
                int n = targets.length;
                for (int i = 0; i < n; i++) {
                    Handler target = targets[i];
                    Message m = Message.obtain();
                    m.copyFrom(msg);
                    m.what = whats[i];
                    target.sendMessage(m);
                }
            }
        }
    }
}
