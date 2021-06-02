package com.streem.selectcontact;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Entity;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

public class SelectContactModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final String TAG = "SelectContactModule";
    private static final int CONTACT_REQUEST = 11112;
    public static final String E_CONTACT_CANCELLED = "E_CONTACT_CANCELLED";
    public static final String E_CONTACT_NO_DATA = "E_CONTACT_NO_DATA";
    public static final String E_CONTACT_EXCEPTION = "E_CONTACT_EXCEPTION";
    public static final String E_CONTACT_PERMISSION = "E_CONTACT_PERMISSION";
    private Promise mContactsPromise;
    private final ContentResolver contentResolver;

    public SelectContactModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.contentResolver = getReactApplicationContext().getContentResolver();
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "SelectContact";
    }


    @ReactMethod
    public void openContactSelection(Promise contactsPromise) {
        launchPicker(contactsPromise, CONTACT_REQUEST);
    }

    /**
     * Lanch the contact picker, with the specified requestCode for returned data.
     *
     * @param contactsPromise - promise passed in from React Native.
     * @param requestCode     - request code to specify what contact data to return
     */
    private void launchPicker(Promise contactsPromise, int requestCode) {
        mContactsPromise = contactsPromise;
        Cursor cursor = this.contentResolver.query(Contacts.CONTENT_URI, null, null, null, null);
        
        if (cursor != null) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(Contacts.CONTENT_TYPE);
            Activity activity = getCurrentActivity();
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(intent, requestCode);
            }
            cursor.close();
        } else {
            mContactsPromise.reject(E_CONTACT_PERMISSION, "no permission");
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        if (mContactsPromise == null || requestCode != CONTACT_REQUEST) {
            return;
        }

        //Request was cancelled
        if (resultCode != Activity.RESULT_OK) {
            mContactsPromise.reject(E_CONTACT_CANCELLED, "Cancelled");
            return;
        }

        // Retrieve all possible data about contact and return as a JS object
        WritableMap contactData = Arguments.createMap();

        try {
            String id = getContactId(intent.getData());
            contactData.putString("recordId", id);
            Uri contactUri = buildContactUri(id);
            boolean foundData = false;

            WritableArray phones = Arguments.createArray();
            WritableArray emails = Arguments.createArray();
            WritableArray postalAddresses = Arguments.createArray();

            Cursor cursor = openContactQuery(contactUri);
            if (cursor.moveToFirst()) {
                do {
                    String mime = cursor.getString(cursor.getColumnIndex(Entity.MIMETYPE));
                    switch (mime) {
                        case StructuredName.CONTENT_ITEM_TYPE:
                            addNameData(contactData, cursor);
                            foundData = true;
                            break;

                        case StructuredPostal.CONTENT_ITEM_TYPE:
                            addPostalData(postalAddresses, cursor, activity);
                            foundData = true;
                            break;

                        case Phone.CONTENT_ITEM_TYPE:
                            addPhoneEntry(phones, cursor, activity);
                            foundData = true;
                            break;

                        case Email.CONTENT_ITEM_TYPE:
                            addEmailEntry(emails, cursor, activity);
                            foundData = true;
                            break;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            contactData.putArray("phones", phones);
            contactData.putArray("emails", emails);
            contactData.putArray("postalAddresses", postalAddresses);

            if (foundData) {
                mContactsPromise.resolve(contactData);
            } else {
                mContactsPromise.reject(E_CONTACT_NO_DATA, "No data found for contact");
            }
        } catch (SelectContactException e) {
            mContactsPromise.reject(E_CONTACT_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected exception reading from contacts", e);
            mContactsPromise.reject(E_CONTACT_EXCEPTION, e.getMessage());
        }
    }

    private String getContactId(Uri contactUri) throws SelectContactException {
        Cursor cursor = this.contentResolver.query(contactUri, null, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            throw new SelectContactException(E_CONTACT_NO_DATA, "Contact Data Not Found");
        }

        return cursor.getString(cursor.getColumnIndex(Contacts._ID));
    }

    private Uri buildContactUri(String id) {
        return Uri
                .withAppendedPath(Contacts.CONTENT_URI, id)
                .buildUpon()
                .appendPath(Entity.CONTENT_DIRECTORY)
                .build();
    }

    private Cursor openContactQuery(Uri contactUri) throws SelectContactException {
        String[] projection = {
                Entity.MIMETYPE,
                Entity.DATA1,
                Entity.DATA2,
                Entity.DATA3
        };
        String sortOrder = Entity.RAW_CONTACT_ID + " ASC";
        Cursor cursor = this.contentResolver.query(contactUri, projection, null, null, sortOrder);
        if (cursor == null) {
            throw new SelectContactException(E_CONTACT_EXCEPTION, "Could not query contacts data. Unable to create cursor.");
        }

        return cursor;
    }

    private void addNameData(WritableMap contactData, Cursor cursor) {
        int displayNameIndex = cursor.getColumnIndex(StructuredName.DISPLAY_NAME);
        contactData.putString("name", cursor.getString(displayNameIndex));

        int givenNameColumn = cursor.getColumnIndex(StructuredName.GIVEN_NAME);
        if (givenNameColumn != -1) {
            String givenName = cursor.getString(givenNameColumn);
            contactData.putString("givenName", givenName);
        }

        int familyNameColumn = cursor.getColumnIndex(StructuredName.FAMILY_NAME);
        if (familyNameColumn != -1) {
            String familyName = cursor.getString(cursor.getColumnIndex(StructuredName.FAMILY_NAME));
            contactData.putString("familyName", familyName);
        }

        int middleNameColumn = cursor.getColumnIndex(StructuredName.MIDDLE_NAME);
        if (middleNameColumn != -1) {
            String middleName = cursor.getString(middleNameColumn);
            contactData.putString("middleName", middleName);
        }
    }

    private void addPostalData(WritableArray postalAddresses, Cursor cursor, Activity activity) {
        // we need to see if the postal address columns exist, if so, add them
        int formattedAddressColumn = cursor.getColumnIndex(StructuredPostal.FORMATTED_ADDRESS);
        int streetColumn = cursor.getColumnIndex(StructuredPostal.STREET);
        int cityColumn = cursor.getColumnIndex(StructuredPostal.CITY);
        int stateColumn = cursor.getColumnIndex(StructuredPostal.REGION);
        int postalCodeColumn = cursor.getColumnIndex(StructuredPostal.POSTCODE);
        int isoCountryCodeColumn = cursor.getColumnIndex(StructuredPostal.COUNTRY);

        WritableMap addressEntry = Arguments.createMap();
        if (formattedAddressColumn != -1) {
            addressEntry.putString("formattedAddress", cursor.getString(formattedAddressColumn));
        }
        if (streetColumn != -1) {
            addressEntry.putString("street", cursor.getString(streetColumn));
        }
        if (cityColumn != -1) {
            addressEntry.putString("city", cursor.getString(cityColumn));
        }
        if (stateColumn != -1) {
            addressEntry.putString("state", cursor.getString(stateColumn));
        }
        if (postalCodeColumn != -1) {
            addressEntry.putString("postalCode", cursor.getString(postalCodeColumn));
        }
        if (isoCountryCodeColumn != -1) {
            addressEntry.putString("isoCountryCode", cursor.getString(isoCountryCodeColumn));
        }

        // add the address type here
        int addressTypeColumn = cursor.getColumnIndex(StructuredPostal.TYPE);
        int addressLabelColumn = cursor.getColumnIndex(StructuredPostal.LABEL);
        if (addressTypeColumn != -1 && addressLabelColumn != -1) {
            String addressLabel = cursor.getString(addressLabelColumn);
            int addressType = cursor.getInt(addressTypeColumn);
            CharSequence typeLabel = StructuredPostal.getTypeLabel(activity.getResources(), addressType, addressLabel);
            addressEntry.putString("type", String.valueOf(typeLabel));
        }

        postalAddresses.pushMap(addressEntry);
    }

    private void addPhoneEntry(WritableArray phones, Cursor cursor, Activity activity) {
        String phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
        int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
        String phoneLabel = cursor.getString(cursor.getColumnIndex(Phone.LABEL));
        CharSequence typeLabel = Phone.getTypeLabel(activity.getResources(), phoneType, phoneLabel);

        WritableMap phoneEntry = Arguments.createMap();
        phoneEntry.putString("number", phoneNumber);
        phoneEntry.putString("type", String.valueOf(typeLabel));

        phones.pushMap(phoneEntry);
    }

    private void addEmailEntry(WritableArray emails, Cursor cursor, Activity activity) {
        String emailAddress = cursor.getString(cursor.getColumnIndex(Email.ADDRESS));
        int emailType = cursor.getInt(cursor.getColumnIndex(Email.TYPE));
        String emailLabel = cursor.getString(cursor.getColumnIndex(Email.LABEL));
        CharSequence typeLabel = Email.getTypeLabel(activity.getResources(), emailType, emailLabel);

        WritableMap emailEntry = Arguments.createMap();
        emailEntry.putString("address", emailAddress);
        emailEntry.putString("type", String.valueOf(typeLabel));

        emails.pushMap(emailEntry);
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    public static class SelectContactException extends Exception {
        private final String errorCode;

        public SelectContactException(String errorCode, String errorMessage) {
            super(errorMessage);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
