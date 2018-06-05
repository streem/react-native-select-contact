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

let currentlyOpen = false;

const SelectContactApi = {

    selectContact() {
        if (currentlyOpen) {
            return Promise.reject(new Error('Cannot open the contact selector twice'));
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

    selectContactPhone() {
        return SelectContactApi.selectContact()
            .then(contact => {
                if (!contact) {
                    return null;
                }

                let phones = contact && contact.phones || [];
                if (phones.length === 0) {
                    Alert.alert('No Phone Numbers', `We could not find any phone numbers for ${contact.name}`);
                    return null;
                }

                return selectPhone(phones)
                    .then(selectedPhone => {
                        return selectedPhone ? { contact, selectedPhone } : null;
                    });
            });
    },

    selectContactEmail() {
        return SelectContactApi.selectContact()
            .then(contact => {
                if (!contact) {
                    return null;
                }

                let emails = contact && contact.emails || [];
                if (emails.length === 0) {
                    Alert.alert('No Email Addresses', `We could not find any email addresses for ${contact.name}`);
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


function selectPhone(phones) {
    if (phones.length < 2 || !ActionSheet) {
        return Promise.resolve(phones[0]);
    }

    let options = phones.map(phone => {
        let { number, type } = phone;
        return number + (type ? ` - ${type}` : '');
    });

    if (Platform.OS === 'ios') {
        options.push('Cancel');
    }

    return new Promise(((resolve) => {
        ActionSheet.showActionSheetWithOptions({
                title: 'Select Phone',
                options: options,
                cancelButtonIndex: options.length - 1,
                tintColor: 'blue'
            },
            (buttonIndex) => {
                resolve(phones[buttonIndex]);
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
        options.push('Cancel');
    }

    return new Promise(((resolve) => {
        ActionSheet.showActionSheetWithOptions({
                title: 'Select Email',
                options: options,
                cancelButtonIndex: options.length - 1,
                tintColor: 'blue'
            },
            (buttonIndex) => {
                resolve(emails[buttonIndex]);
            });
    }));
}
