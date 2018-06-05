'use strict';

import {
    NativeModules,
    Platform
} from 'react-native';
import ActionSheet from '@yfuks/react-native-action-sheet';

const { SelectContact } = NativeModules;


module.exports = {

    selectContact() {
        return SelectContact.openContactSelection();
    },

    selectContactPhone() {
        return SelectContact.openContactSelection()
            .then(contact => {
                return selectPhone(contact)
                    .then(selectedPhone => {
                        return selectedPhone ? { contact, selectedPhone } : null;
                    });
            });
    },

    selectContactEmail() {
        return SelectContact.openContactSelection()
            .then(contact => {
                return selectEmail(contact)
                    .then(selectedEmail => {
                        return selectedEmail ? { contact, selectedEmail } : null;
                    });
            });
    }

};

function selectPhone(contact) {
    let phones = contact && contact.phones || [];
    if (phones.length < 2) {
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

function selectEmail(contact) {
    let emails = contact && contact.emails || [];
    if (emails.length < 2) {
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
