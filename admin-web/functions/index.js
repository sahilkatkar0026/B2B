const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * When a user's registrationStatus changes to 'approved', mark their account active.
 * Also mirrors activation to `shopkeepers/{uid}/isActive` if that node exists.
 */
exports.activateOnApproval = functions.database
  .ref('/users/{uid}/registrationStatus')
  .onWrite(async (change, context) => {
    const before = change.before.val();
    const after = change.after.val();
    const uid = context.params.uid;

    if (after !== 'approved' || before === after) {
      // Nothing to do unless it newly became 'approved'
      return null;
    }

    const updates = {};
    updates[`/users/${uid}/isActive`] = true;
    updates[`/shopkeepers/${uid}/isActive`] = true;

    try {
      await admin.database().ref().update(updates);
      return null;
    } catch (err) {
      console.error('Failed to activate user', uid, err);
      throw err;
    }
  });
