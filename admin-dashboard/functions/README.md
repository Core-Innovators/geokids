# GeoKids Firebase Cloud Functions

## Push Notifications for Pickup/Dropoff QR Scans

This folder contains Firebase Cloud Functions that send push notifications to parents when their child's QR code is scanned by a driver.

---

## 📋 Features

- **Pickup Notification**: Sends push notification when child is picked up
- **Dropoff Notification**: Sends push notification when child arrives at school
- **Notification History**: Logs all notifications in `notifications` collection
- **Status Updates**: Also triggers on status changes to 'completed'

---

## 🚀 Setup & Deployment

### Prerequisites

1. **Firebase CLI** installed globally:
   ```bash
   npm install -g firebase-tools
   ```

2. **Firebase Blaze Plan** (Pay-as-you-go) - Required for Cloud Functions

3. **Login to Firebase**:
   ```bash
   firebase login
   ```

### Step 1: Initialize Firebase in your project (if not done)

```bash
cd d:\geokids\admin-dashboard
firebase init functions
```

Select:
- Use existing project → Select your GeoKids project
- JavaScript
- ESLint: No (optional)
- Install dependencies: Yes

### Step 2: Install Dependencies

```bash
cd functions
npm install
```

### Step 3: Configure Region (Optional)

Edit `functions/index.js` and change the region if needed:
```javascript
.region('asia-southeast1')  // Change to your preferred region
```

Available regions: `us-central1`, `europe-west1`, `asia-southeast1`, etc.

### Step 4: Deploy Functions

```bash
firebase deploy --only functions
```

---

## 📱 Mobile App Requirements

### Store FCM Token for Parents

In your **Parent mobile app** (Android/iOS), you need to:

1. **Get the FCM token** when the app starts or user logs in
2. **Store it in Firebase** under the parent's document

#### Android (Java) Example:
```java
FirebaseMessaging.getInstance().getToken()
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            String token = task.getResult();
            
            // Save to Firestore
            FirebaseFirestore.getInstance()
                .collection("parents")
                .document(parentId)
                .update("fcmToken", token);
        }
    });
```

#### Android (Kotlin) Example:
```kotlin
FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
    FirebaseFirestore.getInstance()
        .collection("parents")
        .document(parentId)
        .update("fcmToken", token)
}
```

#### Flutter Example:
```dart
final fcmToken = await FirebaseMessaging.instance.getToken();
await FirebaseFirestore.instance
    .collection('parents')
    .doc(parentId)
    .update({'fcmToken': fcmToken});
```

---

## 🔔 Notification Format

### Pickup Notification
```
Title: 🚐 Child Picked Up!
Body: [ChildName] has been picked up by [DriverName] at [Time]
```

### Dropoff Notification
```
Title: 🏫 Child Dropped Off!
Body: [ChildName] has arrived at [SchoolName] at [Time]
```

---

## 📊 Database Requirements

### Parents Collection - Required Field

Your `parents` collection documents should have an `fcmToken` field:

```json
{
  "parentName": "Test P",
  "parentContact1": "0789898564",
  "parentNic": "1234567890",
  "fcmToken": "dK9x...FCM_TOKEN_HERE...bF2",  // ← Required for notifications
  "status": "active"
}
```

The Cloud Function looks for the token in these fields (in order):
1. `fcmToken`
2. `deviceToken`
3. `pushToken`

---

## 🧪 Testing

### Test Function (HTTP Callable)

You can test notifications using the `testNotification` function:

```javascript
// In your app or browser console
const testNotification = firebase.functions().httpsCallable('testNotification');
testNotification({
    parentId: 'KvYOTaazLlVhCcxTvmVYMDFhs6B2',
    message: 'This is a test!'
}).then(result => console.log(result));
```

### View Logs

```bash
firebase functions:log
```

Or view in Firebase Console → Functions → Logs

---

## 📁 Files Structure

```
functions/
├── index.js          # Main Cloud Functions code
├── package.json      # Dependencies
└── README.md         # This file
```

---

## 🔧 Troubleshooting

### "No FCM token found"
- Ensure the parent mobile app is storing the FCM token in Firebase
- Check the field name matches: `fcmToken`, `deviceToken`, or `pushToken`

### "Parent not found"
- Check that `parentId` in pickups/dropoffs matches the document ID in `parents` collection
- Or matches the `parentId` field inside a parent document

### Notifications not received
- Check if the app is in foreground (may need to handle differently)
- Verify FCM token is valid and not expired
- Check Firebase Cloud Messaging is enabled in Firebase Console

---

## 💰 Billing

Firebase Cloud Functions require the **Blaze (pay-as-you-go)** plan.

**Free tier includes:**
- 2 million invocations/month
- 400,000 GB-seconds/month
- 200,000 CPU-seconds/month

For a school transport app, this should be more than sufficient.

---

## 📞 Support

For issues with this implementation, check:
1. Firebase Console → Functions → Logs
2. Firebase Console → Cloud Messaging
3. Firestore → `notifications` collection for history
