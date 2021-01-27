package android.os;

import android.annotation.UnsupportedAppUsage;
import java.io.PrintStream;

public class Broadcaster {
    private Registration mReg;

    @UnsupportedAppUsage
    public void request(int senderWhat, Handler target, int targetWhat) {
        int n;
        synchronized (this) {
            if (this.mReg == null) {
                Registration r = new Registration();
                r.senderWhat = senderWhat;
                r.targets = new Handler[1];
                r.targetWhats = new int[1];
                r.targets[0] = target;
                r.targetWhats[0] = targetWhat;
                this.mReg = r;
                r.next = r;
                r.prev = r;
            } else {
                Registration start = this.mReg;
                Registration r2 = start;
                while (true) {
                    if (r2.senderWhat >= senderWhat) {
                        break;
                    }
                    r2 = r2.next;
                    if (r2 == start) {
                        break;
                    }
                }
                if (r2.senderWhat != senderWhat) {
                    Registration reg = new Registration();
                    reg.senderWhat = senderWhat;
                    reg.targets = new Handler[1];
                    reg.targetWhats = new int[1];
                    reg.next = r2;
                    reg.prev = r2.prev;
                    r2.prev.next = reg;
                    r2.prev = reg;
                    if (r2 == this.mReg && r2.senderWhat > reg.senderWhat) {
                        this.mReg = reg;
                    }
                    r2 = reg;
                    n = 0;
                } else {
                    n = r2.targets.length;
                    Handler[] oldTargets = r2.targets;
                    int[] oldWhats = r2.targetWhats;
                    for (int i = 0; i < n; i++) {
                        if (oldTargets[i] == target && oldWhats[i] == targetWhat) {
                            return;
                        }
                    }
                    r2.targets = new Handler[(n + 1)];
                    System.arraycopy(oldTargets, 0, r2.targets, 0, n);
                    r2.targetWhats = new int[(n + 1)];
                    System.arraycopy(oldWhats, 0, r2.targetWhats, 0, n);
                }
                r2.targets[n] = target;
                r2.targetWhats[n] = targetWhat;
            }
        }
    }

    @UnsupportedAppUsage
    public void cancelRequest(int senderWhat, Handler target, int targetWhat) {
        synchronized (this) {
            Registration start = this.mReg;
            Registration r = start;
            if (r != null) {
                while (true) {
                    if (r.senderWhat < senderWhat) {
                        r = r.next;
                        if (r == start) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (r.senderWhat == senderWhat) {
                    Handler[] targets = r.targets;
                    int[] whats = r.targetWhats;
                    int oldLen = targets.length;
                    int i = 0;
                    while (true) {
                        if (i >= oldLen) {
                            break;
                        } else if (targets[i] == target && whats[i] == targetWhat) {
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
    }

    public void dumpRegistrations() {
        synchronized (this) {
            Registration start = this.mReg;
            PrintStream printStream = System.out;
            printStream.println("Broadcaster " + this + " {");
            if (start != null) {
                Registration r = start;
                do {
                    PrintStream printStream2 = System.out;
                    printStream2.println("    senderWhat=" + r.senderWhat);
                    int n = r.targets.length;
                    for (int i = 0; i < n; i++) {
                        PrintStream printStream3 = System.out;
                        printStream3.println("        [" + r.targetWhats[i] + "] " + r.targets[i]);
                    }
                    r = r.next;
                } while (r != start);
            }
            System.out.println("}");
        }
    }

    @UnsupportedAppUsage
    public void broadcast(Message msg) {
        synchronized (this) {
            if (this.mReg != null) {
                int senderWhat = msg.what;
                Registration start = this.mReg;
                Registration r = start;
                while (true) {
                    if (r.senderWhat >= senderWhat) {
                        break;
                    }
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

    private class Registration {
        Registration next;
        Registration prev;
        int senderWhat;
        int[] targetWhats;
        Handler[] targets;

        private Registration() {
        }
    }
}
