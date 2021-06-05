'use strict';

import { NativeModules } from 'react-native';

const { SelectContact } = NativeModules;

let currentlyOpen = false;

const SelectContactApi = {

    selectContactData(field) {
        if (currentlyOpen) {
            return Promise.reject(new Error('Cannot open the contact selector twice'));
        }
        currentlyOpen = true;
        return SelectContact.openContactSelection(field)
            .then(contact => {
                currentlyOpen = false;
                return contact;
            })
            .catch(err => {
                currentlyOpen = false;
                throw err;
            });
    },
    
    selectContact() {
        return SelectContactApi.selectContactData();
    },

    selectContactPostalAddress() {
        return SelectContactApi.selectContactData('Address')
            .then(contact => contact ? { contact, selectedAddress: contact.postalAddresses[0]} : null);
    },

    selectContactPhone() {
        return SelectContactApi.selectContactData('PhoneNumber')
            .then(contact => contact ? { contact, selectedPhone: contact.phones[0]} : null);
    },

    selectContactEmail() {
        return SelectContactApi.selectContactData('Email')
            .then(contact => contact ? { contact, selectedEmail: contact.emails[0]} : null);
    }

};

module.exports = SelectContactApi;