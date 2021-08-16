const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();

exports.helloWorld = functions.https.onRequest((request, response) => {
  functions.logger.info("Hello logs!", {structuredData: true});
  response.send("Hello from Firebase!");
});

exports.onAuthUser = functions.auth.user().onCreate(async (user) => {
  const userRef = db.collection("users").doc(user.uid);
  await db.runTransaction(async (transaction) => {
    const userData = {
      role: "user",
    };
    transaction.set(userRef, userData);
  });
});
