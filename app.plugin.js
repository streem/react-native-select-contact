const {
    withAndroidManifest,
    withInfoPlist,
    withPlugins,
} = require("@expo/config-plugins");

const CONTACT_USAGE = "Allow $(PRODUCT_NAME) to access your contacts";

const withIosConfig = (config, props) => {
    let permissionString = undefined;
    if (props) {
        const { contactPermission } = props;
        permissionString = contactPermission;
    }

    return withInfoPlist(config, (config) => {
        config.modResults.NSContactsUsageDescription =
            permissionString ?? CONTACT_USAGE;

        return config;
    });
};

const withAndroidConfig = (config) => {
    return withAndroidManifest(config, async (config) => {
        const androidManifest = config.modResults.manifest;

        if (
            !config.android.permissions.find(
                (permission) =>
                    permission === "android.permission.READ_CONTACTS"
            )
        ) {
            config.android.permissions.push("android.permission.READ_CONTACTS");
        }

        androidManifest.application[0]?.activity?.[0]?.["intent-filter"].push({
            action: {
                $: {
                    "android:name": "android.intent.action.PICK",
                },
            },
            data: {
                $: {
                    "android:mimeType": "vnd.android.cursor.dir/contact",
                },
            },
            category: {
                $: {
                    "android:name": "android.intent.category.DEFAULT",
                },
            },
        });

        return config;
    });
};

const withSelectContact = (config) => {
    return withPlugins(config, [withAndroidConfig, withIosConfig]);
};

module.exports = withSelectContact;
