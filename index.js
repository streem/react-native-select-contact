'use strict';

import {
    NativeModules,
    NativeEventEmitter,
    ActionSheet,
    Platform
} from 'react-native';
import ActionSheet from '@yfuks/react-native-action-sheet';

const { SelectContact } = NativeModules;


module.exports = {

    selectContactPhone() {
        return SelectContact.openContactSelection()
            .then(contact => selectPhone(contact));
    }

};

function selectPhone(contact) {
    let phones = contact && contact.phones || [];
    if (phones.length < 2) {
        return phones[0];
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
                options: options,
                cancelButtonIndex: options.length - 1,
                tintColor: 'blue'
            },
            (buttonIndex) => {
                let selected = phones[buttonIndex];
                let phone = selected && selected.number || null;
                resolve({ contact, phone });
            });
    }));
}
