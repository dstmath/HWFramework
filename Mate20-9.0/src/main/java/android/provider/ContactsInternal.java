package android.provider;

import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;
import java.util.List;

public class ContactsInternal {
    private static final int CONTACTS_URI_LOOKUP = 1001;
    private static final int CONTACTS_URI_LOOKUP_ID = 1000;
    private static final UriMatcher sContactsUriMatcher = new UriMatcher(-1);

    private ContactsInternal() {
    }

    static {
        UriMatcher matcher = sContactsUriMatcher;
        matcher.addURI("com.android.contacts", "contacts/lookup/*", 1001);
        matcher.addURI("com.android.contacts", "contacts/lookup/*/#", 1000);
    }

    public static void startQuickContactWithErrorToast(Context context, Intent intent) {
        switch (sContactsUriMatcher.match(intent.getData())) {
            case 1000:
            case 1001:
                if (maybeStartManagedQuickContact(context, intent)) {
                    return;
                }
                break;
        }
        startQuickContactWithErrorToastForUser(context, intent, context.getUser());
    }

    public static void startQuickContactWithErrorToastForUser(Context context, Intent intent, UserHandle user) {
        try {
            context.startActivityAsUser(intent, user);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, 17040966, 0).show();
        }
    }

    private static boolean maybeStartManagedQuickContact(Context context, Intent originalIntent) {
        long j;
        Uri uri = originalIntent.getData();
        List<String> pathSegments = uri.getPathSegments();
        boolean isContactIdIgnored = pathSegments.size() < 4;
        if (isContactIdIgnored) {
            j = ContactsContract.Contacts.ENTERPRISE_CONTACT_ID_BASE;
        } else {
            j = ContentUris.parseId(uri);
        }
        long contactId = j;
        String lookupKey = pathSegments.get(2);
        String directoryIdStr = uri.getQueryParameter("directory");
        long directoryId = directoryIdStr == null ? 1000000000 : Long.parseLong(directoryIdStr);
        if (TextUtils.isEmpty(lookupKey)) {
            String str = directoryIdStr;
        } else if (!lookupKey.startsWith(ContactsContract.Contacts.ENTERPRISE_CONTACT_LOOKUP_PREFIX)) {
            long j2 = directoryId;
            String str2 = directoryIdStr;
        } else if (!ContactsContract.Contacts.isEnterpriseContactId(contactId)) {
            String str3 = directoryIdStr;
            throw new IllegalArgumentException("Invalid enterprise contact id: " + contactId);
        } else if (ContactsContract.Directory.isEnterpriseDirectoryId(directoryId)) {
            long j3 = directoryId;
            String str4 = directoryIdStr;
            ((DevicePolicyManager) context.getSystemService(DevicePolicyManager.class)).startManagedQuickContact(lookupKey.substring(ContactsContract.Contacts.ENTERPRISE_CONTACT_LOOKUP_PREFIX.length()), contactId - ContactsContract.Contacts.ENTERPRISE_CONTACT_ID_BASE, isContactIdIgnored, directoryId - 1000000000, originalIntent);
            return true;
        } else {
            String str5 = directoryIdStr;
            throw new IllegalArgumentException("Invalid enterprise directory id: " + directoryId);
        }
        return false;
    }
}
