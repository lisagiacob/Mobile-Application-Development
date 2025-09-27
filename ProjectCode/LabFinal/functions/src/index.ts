import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

export const sendNotificationOnNewFirestoreNotification = functions.firestore
  .document("users/{userId}/notifications/{notificationId}")
  .onCreate(async (snap, context) => {
    const userId = context.params.userId;
    const notificationData = snap.data();

    if (!notificationData) {
      console.log("No notification data.");
      return null;
    }

    console.log(`Sending FCM to user ${userId}:`, notificationData.message);

    // Recupera il fcmToken dell'utente
    const userDoc = await admin.firestore().doc(`users/${userId}`).get();
    const fcmToken = userDoc.get("fcmToken");

    if (!fcmToken) {
      console.log(`No FCM token for user ${userId}`);
      return null;
    }

    // Costruisci il messaggio FCM
    const message = {
      token: fcmToken,
      notification: {
        title: "Nuova notifica",
        body: notificationData.message,
      },
      android: {
        priority: "high" as const,
      },
    };

    // Invia la FCM
    try {
      const response = await admin.messaging().send(message);
      console.log("Successfully sent FCM:", response);
    } catch (error) {
      console.error("Error sending FCM:", error);
    }

    return null;
  });
