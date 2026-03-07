const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

// =========================================
// Helper: Get FCM Token for Parent
// =========================================
async function getParentFCMToken(parentId) {
    try {
        // Try getting parent by document ID first
        let parentDoc = await db.collection('parents').doc(parentId).get();

        if (!parentDoc.exists) {
            // Try querying by parentId field
            const parentSnapshot = await db.collection('parents')
                .where('parentId', '==', parentId)
                .limit(1)
                .get();

            if (!parentSnapshot.empty) {
                parentDoc = parentSnapshot.docs[0];
            }
        }

        if (!parentDoc || !parentDoc.exists) {
            console.log('Parent document not found for ID:', parentId);
            return null;
        }

        const parentData = parentDoc.data();
        const fcmToken = parentData.fcmToken || parentData.deviceToken || parentData.pushToken;

        if (!fcmToken) {
            console.log('No FCM token found for parent:', parentId);
            return null;
        }

        return fcmToken;
    } catch (error) {
        console.error('Error getting parent FCM token:', error);
        return null;
    }
}

// =========================================
// Trigger: On Pickup Created
// =========================================
exports.sendPickupNotification = functions
    .region('asia-southeast1')
    .firestore
    .document('pickups/{pickupId}')
    .onCreate(async (snap, context) => {
        const pickupData = snap.data();
        const pickupId = context.params.pickupId;

        console.log('� New pickup detected:', pickupId, pickupData);

        try {
            const fcmToken = await getParentFCMToken(pickupData.parentId);

            if (!fcmToken) {
                console.log('Cannot send notification - no FCM token');
                return null;
            }

            // Format timestamp
            const timestamp = pickupData.timestamp || Date.now();
            const timeStr = new Date(timestamp).toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit',
                hour12: true
            });

            const message = {
                token: fcmToken,
                notification: {
                    title: '� Child Picked Up',
                    body: `${pickupData.childName} has been picked up by ${pickupData.driverName} at ${timeStr}`,
                },
                data: {
                    type: 'pickup',
                    childId: pickupData.childId || '',
                    childName: pickupData.childName || '',
                    driverName: pickupData.driverName || '',
                    timestamp: String(timestamp),
                    actionType: 'pickup',
                    pickupId: pickupId
                },
                android: {
                    priority: 'high',
                    notification: {
                        sound: 'default',
                        channelId: 'geokids_notifications',
                        color: '#ED5316',
                        icon: 'ic_notification'
                    }
                },
                apns: {
                    payload: {
                        aps: {
                            sound: 'default',
                            badge: 1
                        }
                    }
                }
            };

            const response = await messaging.send(message);
            console.log('✅ Pickup notification sent successfully:', response);

            // Save notification to history
            await db.collection('notifications').add({
                parentId: pickupData.parentId,
                childId: pickupData.childId,
                childName: pickupData.childName,
                driverName: pickupData.driverName,
                type: 'pickup',
                title: '🚌 Child Picked Up',
                message: `${pickupData.childName} has been picked up by ${pickupData.driverName}`,
                timestamp: admin.firestore.FieldValue.serverTimestamp(),
                read: false,
                createdAt: timestamp,
                relatedDocId: pickupId
            });

            console.log('✅ Notification saved to history');
            return response;

        } catch (error) {
            console.error('❌ Error sending pickup notification:', error);
            return null;
        }
    });

// =========================================
// Trigger: On Dropoff Created
// =========================================
exports.sendDropoffNotification = functions
    .region('asia-southeast1')
    .firestore
    .document('dropoffs/{dropoffId}')
    .onCreate(async (snap, context) => {
        const dropoffData = snap.data();
        const dropoffId = context.params.dropoffId;

        console.log('🏫 New dropoff detected:', dropoffId, dropoffData);

        try {
            const fcmToken = await getParentFCMToken(dropoffData.parentId);

            if (!fcmToken) {
                console.log('Cannot send notification - no FCM token');
                return null;
            }

            // Format timestamp
            const timestamp = dropoffData.timestamp || Date.now();
            const timeStr = new Date(timestamp).toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit',
                hour12: true
            });

            const message = {
                token: fcmToken,
                notification: {
                    title: '🏫 Child Dropped Off',
                    body: `${dropoffData.childName} has arrived at ${dropoffData.childSchool} at ${timeStr}`,
                },
                data: {
                    type: 'dropoff',
                    childId: dropoffData.childId || '',
                    childName: dropoffData.childName || '',
                    childSchool: dropoffData.childSchool || '',
                    driverName: dropoffData.driverName || '',
                    timestamp: String(timestamp),
                    actionType: 'dropoff',
                    dropoffId: dropoffId
                },
                android: {
                    priority: 'high',
                    notification: {
                        sound: 'default',
                        channelId: 'geokids_notifications',
                        color: '#ED5316',
                        icon: 'ic_notification'
                    }
                },
                apns: {
                    payload: {
                        aps: {
                            sound: 'default',
                            badge: 1
                        }
                    }
                }
            };

            const response = await messaging.send(message);
            console.log('✅ Dropoff notification sent successfully:', response);

            // Save notification to history
            await db.collection('notifications').add({
                parentId: dropoffData.parentId,
                childId: dropoffData.childId,
                childName: dropoffData.childName,
                driverName: dropoffData.driverName,
                childSchool: dropoffData.childSchool,
                type: 'dropoff',
                title: '🏫 Child Dropped Off',
                message: `${dropoffData.childName} has arrived at ${dropoffData.childSchool}`,
                timestamp: admin.firestore.FieldValue.serverTimestamp(),
                read: false,
                createdAt: timestamp,
                relatedDocId: dropoffId
            });

            console.log('✅ Notification saved to history');
            return response;

        } catch (error) {
            console.error('❌ Error sending dropoff notification:', error);
            return null;
        }
    });

// =========================================
// Test Function (for debugging)
// =========================================
exports.testNotification = functions
    .region('asia-southeast1')
    .https.onCall(async (data, context) => {
        const { parentId, message } = data;

        if (!parentId) {
            return { success: false, error: 'parentId is required' };
        }

        const fcmToken = await getParentFCMToken(parentId);

        if (!fcmToken) {
            return { success: false, error: 'No FCM token found for parent' };
        }

        try {
            const notificationMessage = {
                token: fcmToken,
                notification: {
                    title: '🧪 Test Notification',
                    body: message || 'This is a test notification from GeoKids!',
                },
                data: {
                    type: 'test',
                    timestamp: String(Date.now())
                },
                android: {
                    priority: 'high',
                    notification: {
                        sound: 'default',
                        channelId: 'geokids_notifications',
                        color: '#ED5316'
                    }
                }
            };

            const response = await messaging.send(notificationMessage);
            console.log('✅ Test notification sent:', response);

            return { success: true, messageId: response };
        } catch (error) {
            console.error('❌ Error sending test notification:', error);
            return { success: false, error: error.message };
        }
    });

console.log('🚀 GeoKids Cloud Functions loaded');
