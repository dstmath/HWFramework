package sun.net.www;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.util.StringTokenizer;
import sun.security.action.GetPropertyAction;

class MimeLauncher extends Thread {
    String execPath;
    String genericTempFileTemplate;
    InputStream is;
    MimeEntry m;
    URLConnection uc;

    MimeLauncher(MimeEntry M, URLConnection uc, InputStream is, String tempFileTemplate, String threadName) throws ApplicationLaunchException {
        super(threadName);
        this.m = M;
        this.uc = uc;
        this.is = is;
        this.genericTempFileTemplate = tempFileTemplate;
        String launchString = this.m.getLaunchString();
        if (!findExecutablePath(launchString)) {
            String appName;
            int index = launchString.indexOf(32);
            if (index != -1) {
                appName = launchString.substring(0, index);
            } else {
                appName = launchString;
            }
            throw new ApplicationLaunchException(appName);
        }
    }

    protected String getTempFileName(URL url, String template) {
        String tempFilename = template;
        int wildcard = template.lastIndexOf("%s");
        String prefix = template.substring(0, wildcard);
        String suffix = "";
        if (wildcard < template.length() - 2) {
            suffix = template.substring(wildcard + 2);
        }
        long timestamp = System.currentTimeMillis() / 1000;
        while (true) {
            int argIndex = prefix.indexOf("%s");
            if (argIndex < 0) {
                break;
            }
            prefix = prefix.substring(0, argIndex) + timestamp + prefix.substring(argIndex + 2);
        }
        String filename = url.getFile();
        String extension = "";
        int dot = filename.lastIndexOf(46);
        if (dot >= 0 && dot > filename.lastIndexOf(47)) {
            extension = filename.substring(dot);
        }
        return prefix + ("HJ" + url.hashCode()) + timestamp + extension + suffix;
    }

    public void run() {
        OutputStream os;
        try {
            int inx;
            String ofn = this.m.getTempFileTemplate();
            if (ofn == null) {
                ofn = this.genericTempFileTemplate;
            }
            ofn = getTempFileName(this.uc.getURL(), ofn);
            try {
                os = new FileOutputStream(ofn);
                byte[] buf = new byte[Modifier.STRICT];
                while (true) {
                    int i = this.is.read(buf);
                    if (i < 0) {
                        break;
                    }
                    os.write(buf, 0, i);
                }
                os.close();
                this.is.close();
            } catch (IOException e) {
                os.close();
                this.is.close();
            } catch (IOException e2) {
            } catch (Throwable th) {
                os.close();
                this.is.close();
            }
            String c = this.execPath;
            while (true) {
                inx = c.indexOf("%t");
                if (inx < 0) {
                    break;
                }
                c = c.substring(0, inx) + this.uc.getContentType() + c.substring(inx + 2);
            }
            boolean substituted = false;
            while (true) {
                inx = c.indexOf("%s");
                if (inx < 0) {
                    break;
                }
                c = c.substring(0, inx) + ofn + c.substring(inx + 2);
                substituted = true;
            }
            if (!substituted) {
                c = c + " <" + ofn;
            }
            Runtime.getRuntime().exec(c);
        } catch (IOException e3) {
        }
    }

    private boolean findExecutablePath(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        String command;
        int index = str.indexOf(32);
        if (index != -1) {
            command = str.substring(0, index);
        } else {
            command = str;
        }
        if (new File(command).isFile()) {
            this.execPath = str;
            return true;
        }
        String execPathList = (String) AccessController.doPrivileged(new GetPropertyAction("exec.path"));
        if (execPathList == null) {
            return false;
        }
        StringTokenizer iter = new StringTokenizer(execPathList, "|");
        while (iter.hasMoreElements()) {
            String prefix = (String) iter.nextElement();
            if (new File(prefix + File.separator + command).isFile()) {
                this.execPath = prefix + File.separator + str;
                return true;
            }
        }
        return false;
    }
}
