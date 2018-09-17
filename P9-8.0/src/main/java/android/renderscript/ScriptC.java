package android.renderscript;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ScriptC extends Script {
    private static final String TAG = "ScriptC";

    protected ScriptC(int id, RenderScript rs) {
        super((long) id, rs);
    }

    protected ScriptC(long id, RenderScript rs) {
        super(id, rs);
    }

    protected ScriptC(RenderScript rs, Resources resources, int resourceID) {
        super(0, rs);
        long id = internalCreate(rs, resources, resourceID);
        if (id == 0) {
            throw new RSRuntimeException("Loading of ScriptC script failed.");
        }
        setID(id);
    }

    protected ScriptC(RenderScript rs, String resName, byte[] bitcode32, byte[] bitcode64) {
        long id;
        super(0, rs);
        if (RenderScript.sPointerSize == 4) {
            id = internalStringCreate(rs, resName, bitcode32);
        } else {
            id = internalStringCreate(rs, resName, bitcode64);
        }
        if (id == 0) {
            throw new RSRuntimeException("Loading of ScriptC script failed.");
        }
        setID(id);
    }

    private static synchronized long internalCreate(RenderScript rs, Resources resources, int resourceID) {
        long nScriptCCreate;
        synchronized (ScriptC.class) {
            InputStream is = resources.openRawResource(resourceID);
            try {
                byte[] pgm = new byte[1024];
                int pgmLength = 0;
                while (true) {
                    int bytesLeft = pgm.length - pgmLength;
                    if (bytesLeft == 0) {
                        byte[] buf2 = new byte[(pgm.length * 2)];
                        System.arraycopy(pgm, 0, buf2, 0, pgm.length);
                        pgm = buf2;
                        bytesLeft = buf2.length - pgmLength;
                    }
                    int bytesRead = is.read(pgm, pgmLength, bytesLeft);
                    if (bytesRead <= 0) {
                        is.close();
                        nScriptCCreate = rs.nScriptCCreate(resources.getResourceEntryName(resourceID), RenderScript.getCachePath(), pgm, pgmLength);
                    } else {
                        pgmLength += bytesRead;
                    }
                }
            } catch (IOException e) {
                throw new NotFoundException();
            } catch (Throwable th) {
                is.close();
            }
        }
        return nScriptCCreate;
    }

    private static synchronized long internalStringCreate(RenderScript rs, String resName, byte[] bitcode) {
        long nScriptCCreate;
        synchronized (ScriptC.class) {
            nScriptCCreate = rs.nScriptCCreate(resName, RenderScript.getCachePath(), bitcode, bitcode.length);
        }
        return nScriptCCreate;
    }
}
