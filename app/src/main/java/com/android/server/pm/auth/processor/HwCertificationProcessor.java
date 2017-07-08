package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;

public class HwCertificationProcessor {
    private static final int MAX_LINE_LENGHT = 20000;
    public static final String TAG = "HwCertificationManager";
    protected static final HashMap<String, IProcessor> mProcessorMap = null;
    private List<String> mAvailableTagList;
    private ZipFile mZipFile;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.auth.processor.HwCertificationProcessor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.auth.processor.HwCertificationProcessor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.auth.processor.HwCertificationProcessor.<clinit>():void");
    }

    public HwCertificationProcessor() {
        this.mZipFile = null;
        this.mAvailableTagList = new ArrayList();
    }

    private InputStream readCertFileFromApk(String apkPath, HwCertification cert) {
        File apkFile = new File(apkPath);
        if (apkFile.exists()) {
            InputStream input;
            cert.mCertificationData.mApkFile = apkFile;
            cert.setApkFile(apkFile);
            if (this.mZipFile != null) {
                input = Utils.readHwCertFromApk(this.mZipFile, this.mZipFile.getEntry(Utils.CERT_NAME));
            } else {
                input = Utils.readHwCertFromApk(apkPath);
            }
            return input;
        }
        HwAuthLogger.e(TAG, "read cert error:read apk file error!");
        return null;
    }

    public boolean createZipFile(String apkPath) {
        File apkFile = new File(apkPath);
        if (apkFile.exists()) {
            try {
                this.mZipFile = new ZipFile(apkFile);
                return true;
            } catch (IOException e) {
                this.mZipFile = null;
                HwAuthLogger.e(TAG, "IOException in createZipFile, e is " + e);
                return false;
            }
        }
        HwAuthLogger.e(TAG, "createZipFile error:read apk file error!");
        return false;
    }

    public boolean releaseZipFileResource() {
        try {
            if (this.mZipFile != null) {
                this.mZipFile.close();
                this.mZipFile = null;
            }
            return true;
        } catch (IOException e) {
            HwAuthLogger.e(TAG, "IOException in releaseZipFileResource, e is " + e);
            return false;
        } catch (Exception e2) {
            HwAuthLogger.e(TAG, "Exception in releaseZipFileResource, e is " + e2);
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean readCert(String apkPath, HwCertification cert) {
        RuntimeException e;
        Throwable th;
        this.mAvailableTagList.clear();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = readCertFileFromApk(apkPath, cert);
            if (inputStream == null) {
                HwAuthLogger.e(TAG, "read cert error:read certification file error!");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                    }
                }
                return false;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while (true) {
                try {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    String str = line;
                    String keyTag = line.split(":")[0].trim();
                    if (HwCertification.isHwCertKeyContainsTag(keyTag)) {
                        IProcessor processor = (IProcessor) mProcessorMap.get(keyTag);
                        if (processor == null) {
                            break;
                        }
                        if (!readCert(processor, line, cert.mCertificationData)) {
                            break;
                        }
                        this.mAvailableTagList.add(keyTag);
                    }
                } catch (RuntimeException e3) {
                    e = e3;
                    bufferedReader = br;
                } catch (Exception e4) {
                    bufferedReader = br;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = br;
                }
            }
            HwAuthLogger.e(TAG, "read cert error:line mismatch!");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e6) {
                }
            }
            return false;
        } catch (RuntimeException e7) {
            e = e7;
            try {
                HwAuthLogger.e(TAG, "read cert error:throw RuntimeException !e is " + e);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e9) {
                    }
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e10) {
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e11) {
                    }
                }
                throw th;
            }
        } catch (Exception e12) {
            HwAuthLogger.e(TAG, "read cert error:throw exception!");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e13) {
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e14) {
                }
            }
            return false;
        }
    }

    private boolean readCert(IProcessor processor, String line, CertificationData rawCert) {
        if (line.length() < MAX_LINE_LENGHT) {
            return processor.readCert(line, rawCert);
        }
        HwAuthLogger.e(TAG, "read cert error:certification line length is more than expected");
        return false;
    }

    public boolean parserCert(HwCertification rawCert) {
        int i = 0;
        while (i < this.mAvailableTagList.size()) {
            String parserTag = (String) this.mAvailableTagList.get(i);
            IProcessor processor = (IProcessor) mProcessorMap.get(parserTag);
            if (processor == null) {
                HwAuthLogger.e(TAG, "parserCert error:processor is null");
                return false;
            } else if (processor.parserCert(rawCert)) {
                i++;
            } else {
                HwAuthLogger.e(TAG, "parserCert error, parserTag =" + parserTag);
                return false;
            }
        }
        return true;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        int i = 0;
        while (i < this.mAvailableTagList.size()) {
            String verifyTag = (String) this.mAvailableTagList.get(i);
            IProcessor processor = (IProcessor) mProcessorMap.get(verifyTag);
            if (processor == null) {
                HwAuthLogger.e(TAG, "verifyCert error, processor is null, tag =" + verifyTag);
                return false;
            } else if (processor.verifyCert(pkg, cert)) {
                i++;
            } else {
                HwAuthLogger.e(TAG, "verifyCert error, verifyTag =" + verifyTag);
                return false;
            }
        }
        HwAuthLogger.i(TAG, "verifyCert all ok");
        return true;
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        IProcessor processor = (IProcessor) mProcessorMap.get(tag);
        if (processor == null) {
            HwAuthLogger.e(TAG, "parseXmlTag error, processor is null, tag =" + tag);
            return false;
        } else if (processor.parseXmlTag(tag, parser, cert)) {
            return true;
        } else {
            return false;
        }
    }

    public static IProcessor getProcessors(String tag) {
        return (IProcessor) mProcessorMap.get(tag);
    }
}
