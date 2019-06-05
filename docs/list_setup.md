# Push Notification List Setup

## Grant Everlytic Access to Firebase

### Add Firebase Service Account

1. Navigate to your Firebase Project Settings > Service Accounts
1. Select _Manage service account permissions_
1. In your Google Developer Console, select _Create Service Account_
1. Give your service account a name ID and description, and create the account
1. Grant the service account the _Editor_ role to allow access to Firebase Messaging
1. In the next step, select _Create Key_
    - Key Type: **JSON**
    
### Setup Everlytic Push Notification List

1. Navigate to _Push Notifications > Push Projects_
1. Select _Add Push Notification Project_
1. Select a list to attach your Push Project to.
1. Copy and Paste the content of your service account key file into the _Push Notification JSON Configuration_ field.

## Obtaining your SDK Configuration String

1. Nativate to _Push Notifications > Push Projects_
1. Select _SDK Configuration_