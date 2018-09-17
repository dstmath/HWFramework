package android.provider;

import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Process;
import android.os.UserHandle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Directory;
import android.text.TextUtils;
import android.widget.Toast;
import java.util.List;

public class ContactsInternal {
    private static final int CONTACTS_URI_LOOKUP = 1001;
    private static final int CONTACTS_URI_LOOKUP_ID = 1000;
    private static final UriMatcher sContactsUriMatcher = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.ContactsInternal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.ContactsInternal.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.provider.ContactsInternal.<clinit>():void");
    }

    private ContactsInternal() {
    }

    public static void startQuickContactWithErrorToast(Context context, Intent intent) {
        switch (sContactsUriMatcher.match(intent.getData())) {
            case CONTACTS_URI_LOOKUP_ID /*1000*/:
            case CONTACTS_URI_LOOKUP /*1001*/:
                if (maybeStartManagedQuickContact(context, intent)) {
                    return;
                }
                break;
        }
        startQuickContactWithErrorToastForUser(context, intent, Process.myUserHandle());
    }

    public static void startQuickContactWithErrorToastForUser(Context context, Intent intent, UserHandle user) {
        try {
            context.startActivityAsUser(intent, user);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, 17040001, 0).show();
        }
    }

    private static boolean maybeStartManagedQuickContact(Context context, Intent originalIntent) {
        long contactId;
        long directoryId;
        Uri uri = originalIntent.getData();
        List<String> pathSegments = uri.getPathSegments();
        boolean isContactIdIgnored = pathSegments.size() < 4;
        if (isContactIdIgnored) {
            contactId = Contacts.ENTERPRISE_CONTACT_ID_BASE;
        } else {
            contactId = ContentUris.parseId(uri);
        }
        String lookupKey = (String) pathSegments.get(2);
        String directoryIdStr = uri.getQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY);
        if (directoryIdStr == null) {
            directoryId = Directory.ENTERPRISE_DIRECTORY_ID_BASE;
        } else {
            directoryId = Long.parseLong(directoryIdStr);
        }
        if (TextUtils.isEmpty(lookupKey) || !lookupKey.startsWith(Contacts.ENTERPRISE_CONTACT_LOOKUP_PREFIX)) {
            return false;
        }
        if (!Contacts.isEnterpriseContactId(contactId)) {
            throw new IllegalArgumentException("Invalid enterprise contact id: " + contactId);
        } else if (Directory.isEnterpriseDirectoryId(directoryId)) {
            ((DevicePolicyManager) context.getSystemService(DevicePolicyManager.class)).startManagedQuickContact(lookupKey.substring(Contacts.ENTERPRISE_CONTACT_LOOKUP_PREFIX.length()), contactId - Contacts.ENTERPRISE_CONTACT_ID_BASE, isContactIdIgnored, directoryId - Directory.ENTERPRISE_DIRECTORY_ID_BASE, originalIntent);
            return true;
        } else {
            throw new IllegalArgumentException("Invalid enterprise directory id: " + directoryId);
        }
    }
}
