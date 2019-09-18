package java.lang;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

public class SecurityManager {
    @Deprecated
    protected boolean inCheck;

    @Deprecated
    public boolean getInCheck() {
        return this.inCheck;
    }

    /* access modifiers changed from: protected */
    public Class[] getClassContext() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public ClassLoader currentClassLoader() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public Class<?> currentLoadedClass() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int classDepth(String name) {
        return -1;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int classLoaderDepth() {
        return -1;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean inClass(String name) {
        return false;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public boolean inClassLoader() {
        return false;
    }

    public Object getSecurityContext() {
        return null;
    }

    public void checkPermission(Permission perm) {
    }

    public void checkPermission(Permission perm, Object context) {
    }

    public void checkCreateClassLoader() {
    }

    public void checkAccess(Thread t) {
    }

    public void checkAccess(ThreadGroup g) {
    }

    public void checkExit(int status) {
    }

    public void checkExec(String cmd) {
    }

    public void checkLink(String lib) {
    }

    public void checkRead(FileDescriptor fd) {
    }

    public void checkRead(String file) {
    }

    public void checkRead(String file, Object context) {
    }

    public void checkWrite(FileDescriptor fd) {
    }

    public void checkWrite(String file) {
    }

    public void checkDelete(String file) {
    }

    public void checkConnect(String host, int port) {
    }

    public void checkConnect(String host, int port, Object context) {
    }

    public void checkListen(int port) {
    }

    public void checkAccept(String host, int port) {
    }

    public void checkMulticast(InetAddress maddr) {
    }

    @Deprecated
    public void checkMulticast(InetAddress maddr, byte ttl) {
    }

    public void checkPropertiesAccess() {
    }

    public void checkPropertyAccess(String key) {
    }

    public boolean checkTopLevelWindow(Object window) {
        return true;
    }

    public void checkPrintJobAccess() {
    }

    public void checkSystemClipboardAccess() {
    }

    public void checkAwtEventQueueAccess() {
    }

    public void checkPackageAccess(String pkg) {
    }

    public void checkPackageDefinition(String pkg) {
    }

    public void checkSetFactory() {
    }

    public void checkMemberAccess(Class<?> cls, int which) {
    }

    public void checkSecurityAccess(String target) {
    }

    public ThreadGroup getThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }
}
