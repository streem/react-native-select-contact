# react-native-select-contact

Originally branched from [react-native-contacts-wrapper](https://github.com/LynxITDigital/react-native-contacts-wrapper)

This is a simple wrapper for the native iOS and Android Contact Picker UIs, with some optional help for selecting specific fields from the contact.

### Installation

```
yarn add react-native-select-contact
```
or with NPM
```
npm install react-native-select-contact
```

For React Native => 0.59 only:
```
react-native link react-native-select-contact
```

#### Android
For Android support, make sure your manifest file includes permission to read contacts along with a query intent for Android 11+:
```xml
<manifest>
    <!-- Add this for overall Android support -->
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <application>
        ...
    </application>
    <!-- Also add this for Android 11+ support -->
    <queries>
        <intent>
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <data android:mimeType="vnd.android.cursor.dir/contact" />
        </intent>
    </queries>
</manifest>
```
Also, in addition to declaring `READ_CONTACTS` permission in `AndroidManifest.xml`, you also need to explicitly request for the permission at runtime. So make sure `READ_CONTACT` permission is granted at runtime before calling API method(s).

#### iOS
For iOS support, make sure to include usage description for contacts in your `Info.plist` file
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    ...
	<key>NSContactsUsageDescription</key>
	<string>THis app uses your contacts to....</string>
    ...
</dict>
</plist>

```

### API

#### Methods

```javascript
selectContact(): Promise<Contact | null>;
selectContactPhone(): Promise<ContactPhoneSelection | null>;
selectContactEmail(): Promise<ContactEmailSelection | null>;
selectContactPostalAddress(): Promise<ContactPostalAddressSelection | null>;
```

These methods all open up a separate ViewController (on IOS) or Activity (on Android) to select a contact.  See Types below.

For `selectContactPhone`, `selectContactEmail`, or `selectContactPostalAddress`, if there are more than one phone or email, an `ActionSheetIOS` is
shown for IOS, and the first entry is returned for Android.

A return value `null` may be because the user cancelled the contact selection.  You shouldn't need to worry about doing
anything if the promise resolves to `null`.

#### Optional Android ActionSheet

You can enable ActionSheet functionality for Android by installing an optional dependency:

```
yarn add react-native-action-sheet
```

For React Native => 0.59 only:
```
react-native link react-native-action-sheet
```

This will provide an `ActionSheetAndroid` native module that this library will pick up on and use
when there are more than one phone number or email on a selected contact.

#### Types

```typescript
interface PhoneEntry {
    number: string,
    type: string
}

interface EmailEntry {
    address: string,
    type: string
}

interface AddressEntry {
    formattedAddress: string, // android only
    type: string, // android only
    street: string,
    city: string,
    state: string,
    postalCode: string,
    isoCountryCode: string
}

interface Contact {
    name: string,
    phones: PhoneEntry[],
    emails: EmailEntry[],
    postalAddresses: AddressEntry[]
}

interface ContactPhoneSelection {
    contact: Contact,
    selectedPhone: PhoneEntry
}

interface ContactEmailSelection {
    contact: Contact,
    selectedEmail: EmailEntry
}

interface ContactPostalAddressSelection {
    contact: Contact,
    selectedAddress: AddressEntry
}
```

### Example

```javascript

import { selectContactPhone } from 'react-native-select-contact';
import { PermissionsAndroid, Platform } from 'react-native';

async function getPhoneNumber() {
    // on android we need to explicitly request for contacts permission and make sure it's granted
    // before calling API methods
    if (Platform.OS === 'android') {
      const request = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.READ_CONTACTS,
      );

      // denied permission
      if (request === PermissionsAndroid.RESULTS.DENIED) throw Error("Permission Denied");
      
      // user chose 'deny, don't ask again'
      else if (request === PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN) throw Error("Permission Denied");
    }
    
    // Here we are sure permission is granted for android or that platform is not android
    const selection = await selectContactPhone();
    if (!selection) {
        return null;
    }
            
    let { contact, selectedPhone } = selection;
    console.log(`Selected ${selectedPhone.type} phone number ${selectedPhone.number} from ${contact.name}`);
    return selectedPhone.number;
}


```
