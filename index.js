'use strict';

import {
    Alert,
    ActionSheetIOS,
    NativeModules,
    Platform
} from 'react-native';

const { SelectContact, ActionSheetAndroid } = NativeModules;
const ActionSheet = Platform.select({
    ios: ActionSheetIOS,
    android: ActionSheetAndroid
});

const DEFAULT_TEXT_OPTIONS = {
    cancel: "Cancel", 
    selectPhone: "Select Phone", 
    selectPostalAddress: "Select Postal Address",
    selectEmail: "Select Email",
    errorNoPhoneNumbersTitle: "No Phone Numbers", 
    errorNoPhoneNumbersBody: "We could not find any phone numbers for ",
    errorNoAddressTitle: "No Postal Addresses",
    errorNoAddressBody: "We could not find any postal addresses for ",
    errorNoEmailTitle: "No Email Addresses",
    errorNoEmailBody: "We could not find any email addresses for ",
    errorOpenTwice: "Cannot open the contact selector twice",
}

let currentlyOpen = false;

const SelectContactApi = {

    selectContact(textOptions=DEFAULT_TEXT_OPTIONS) {
        if (currentlyOpen) {
            return Promise.reject(new Error(textOptions.errorOpenTwice));
        }

        currentlyOpen = true;

        return SelectContact.openContactSelection()
            .then(contact => {
                currentlyOpen = false;
                return contact;
            })
            .catch(err => {
                currentlyOpen = false;

                // Resolve to null when cancelled
                if (err.code === 'E_CONTACT_CANCELLED') {
                    return null;
                }

                throw err;
            });
    },

    selectContactPostalAddress(textOptions=DEFAULT_TEXT_OPTIONS) {
      return SelectContactApi.selectContact(textOptions)
          .then(contact => {
              if (!contact) {
                  return null;
              }

              let addresses = contact && contact.postalAddresses || [];
              if (addresses.length === 0) {
                  Alert.alert(textOptions.errorNoAddressTitle, `${textOptions.errorNoAddressBody}${contact.name}`);
                  return null;
              }

              return selectPostalAddress(addresses)
                  .then(selectedAddress => {
                      return selectedAddress ? { contact, selectedAddress } : null;
                  });
          })
    },

    selectContactPhone(textOptions=DEFAULT_TEXT_OPTIONS) {
        return SelectContactApi.selectContact(textOptions)
            .then(contact => {
                if (!contact) {
                    return null;
                }

                let phones = contact && contact.phones || [];
                if (phones.length === 0) {
                    Alert.alert(textOptions.errorNoPhoneNumbersTitle, `${textOptions.errorNoPhoneNumbersBody}${contact.name}`);
                    return null;
                }

                return selectPhone(phones, textOptions)
                    .then(selectedPhone => {
                        return selectedPhone ? { contact, selectedPhone } : null;
                    });
            });
    },

    selectContactEmail(textOptions=DEFAULT_TEXT_OPTIONS) {
        return SelectContactApi.selectContact(textOptions)
            .then(contact => {
                if (!contact) {
                    return null;
                }

                let emails = contact && contact.emails || [];
                if (emails.length === 0) {
                    Alert.alert(textOptions.errorNoEmailTitle, `${textOptions.errorNoEmailBody}${contact.name}`);
                    return null;
                }

                return selectEmail(emails)
                    .then(selectedEmail => {
                        return selectedEmail ? { contact, selectedEmail } : null;
                    });
            });
    }

};

module.exports = SelectContactApi;


function selectPhone(phones, textOptions) {
    if (phones.length < 2 || !ActionSheet) {
        return Promise.resolve(phones[0]);
    }

    let options = phones.map(phone => {
        let { number, type } = phone;
        return number + (type ? ` - ${type}` : '');
    });

    if (Platform.OS === 'ios') {
        options.push(textOptions.cancel);
    }

    return new Promise(((resolve) => {
        ActionSheet.showActionSheetWithOptions({
                title: textOptions.selectPhone,
                options: options,
                cancelButtonIndex: options.length - 1,
            },
            (buttonIndex) => {
                resolve(phones[buttonIndex]);
            });
    }));
}

function selectPostalAddress(addresses, textOptions) {
  if (addresses.length < 2 || !ActionSheet) {
    return Promise.resolve(addresses[0]);
  }

  let options = addresses.map(address => {
    let { formattedAddress, street, city, state, postalCode, isoCountryCode } = address;

    if (formattedAddress) {
      return formattedAddress;
    }

    return `${street} ${city}, ${state} ${postalCode} ${isoCountryCode}`;
  });

  if (Platform.OS === 'ios') {
    options.push(textOptions.cancel);
  }

  return new Promise(((resolve) => {
      ActionSheet.showActionSheetWithOptions({
              title: textOptions.selectPostalAddress,
              options: options,
              cancelButtonIndex: options.length - 1,
          },
          (buttonIndex) => {
              resolve(addresses[buttonIndex]);
          });
  }));
}

function selectEmail(emails) {
    if (emails.length < 2 || !ActionSheet) {
        return Promise.resolve(emails[0]);
    }

    let options = emails.map(email => {
        let { address, type } = email;
        return address + (type ? ` - ${type}` : '');
    });

    if (Platform.OS === 'ios') {
        options.push(textOptions.cancel);
    }

    return new Promise(((resolve) => {
        ActionSheet.showActionSheetWithOptions({
                title: textOptions.selectEmail,
                options: options,
                cancelButtonIndex: options.length - 1,
            },
            (buttonIndex) => {
                resolve(emails[buttonIndex]);
            });
    }));
}
