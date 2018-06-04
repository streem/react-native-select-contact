# react-native-select-contact

Originally branched from [react-native-contacts-wrapper](https://github.com/LynxITDigital/react-native-contacts-wrapper)

![alt tag](https://github.com/LynxITDigital/Screenshots/blob/master/RN%20Contacts%20Wrapper%20example.gif)

This is a simple wrapper for the native iOS and Android Contact Picker UIs.  When calling the API functions, the appropriate picker is launched.  If a contact is picked, the promise is resolved with the requested data about the picked contact.

This uses the ContactsContract API for Android, AddressBook library for iOS8 and below and the new Contacts library for ios9+.

The API is currently very basic.  This was started just as a way of selecting a contact's email address.  The getContact function was added as a more generic way of returning contact data.  Currently this returns Name, Phone and Email for picked contact.  In future more fields will be added to this, and possibly more specific methods similar to getEmail.  

Feel free to extend the functionality so it's more useful for everyone - all PRs welcome!

## Installation

```
yarn add react-native-contacts-wrapper
./node_modules/.bin/react-native link react-native-contacts-wrapper
```

##API

`getContact` (Promise) - returns basic contact data as a JS object.  Currently returns name, first phone number and first email for contact.
`getEmail` (Promise) - returns first email address (if found) for contact as string.


##Usage

Methods should be called from React Native as any other promise.
Prevent methods from being called multiple times (on Android).

###Example

An example project can be found in this repo: https://github.com/LynxITDigital/react-native-contacts-wrapper-example/tree/master

TODO: Update example:

```
import ContactsWrapper from 'react-native-contacts-wrapper';
...
if (!this.importingContactInfo) {
  this.importingContactInfo = true;

  ContactsWrapper.getEmail()
  .then((email) => {
    this.importingContactInfo = false;
    console.log("email is", email);
    })
    .catch((error) => {
      this.importingContactInfo = false;
      console.log("ERROR CODE: ", error.code);
      console.log("ERROR MESSAGE: ", error.message);
      });
```
