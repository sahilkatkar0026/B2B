# Medical B2B Admin Panel – Deployment & Android

## 1. Folder structure

```
B2BAdmin/
├── index.html          # Login page (admin only)
├── login.html          # Redirects to index.html
├── dashboard.html      # Admin dashboard
├── css/
│   └── style.css       # All styles (no framework)
├── js/
│   ├── firebase-config.js   # Firebase init (v10 modular)
│   ├── auth.js              # Login + admin guard
│   ├── dashboard.js         # Nav + bootstrap
│   ├── approvals.js         # Supplier license verification
│   ├── medicines.js         # Medicine approve/disable/delete
│   └── stats.js             # Real-time stats + fraud
├── database.rules.json      # Realtime Database rules
├── firebase.json            # Hosting + database rules ref
└── DEPLOYMENT.md            # This file
```

## 2. Firebase Hosting – deploy steps

1. Install Firebase CLI (once):

   ```bash
   npm install -g firebase-tools
   ```

2. Log in and select project:

   ```bash
   firebase login
   firebase use medicalshopb2b
   ```

3. From project root (`B2BAdmin`):

   ```bash
   cd D:\B2BAdmin
   firebase deploy
   ```

   To deploy only hosting:

   ```bash
   firebase deploy --only hosting
   ```

   To deploy only database rules:

   ```bash
   firebase deploy --only database
   ```

4. After deploy, the site will be at:

   - `https://medicalshopb2b.web.app` or  
   - `https://medicalshopb2b.firebaseapp.com`

## 3. Realtime Database rules (strict)

Use the rules in `database.rules.json`. In production you should restrict writes so that:

- Only the admin (or backend) can write `users/{uid}/registrationStatus`, `suppliers/{id}/medicines/{mid}/isApproved`, and `suppliers/{id}/medicines/{mid}/isActive`.

Realtime Database cannot enforce “role === admin” in rules; that is enforced in the admin panel app (auth + `users/{uid}/role`). So keep:

- `registrationStatus`, `isApproved`, `isActive` writable only by authenticated users (or lock down by your backend if you add one).

To push rules from the repo:

```bash
firebase deploy --only database
```

## 4. How the Android app should check approval

- **Supplier (role = supplier)**  
  - After Firebase Auth sign-in, read `users/{uid}`.  
  - If `registrationStatus !== "approved"`, show “Pending approval” and block access to supplier features.  
  - Use a listener so that when admin approves, the app updates without re-login:

```kotlin
// Kotlin (Android)
val ref = FirebaseDatabase.getInstance().getReference("users").child(uid)
ref.addValueEventListener(object : ValueEventListener {
    override fun onDataChange(snap: DataSnapshot) {
        val status = snap.child("registrationStatus").getValue(String::class.java)
        if (status == "approved") {
            // Allow access
        } else {
            // Show "Pending approval" / block
        }
    }
    override fun onCancelled(e: DatabaseError) {}
})
```

- **Medicines visible in the app**  
  - List only medicines where `isApproved == true` and `isActive != false`.  
  - Filter out expired using `expiryDate` (and optionally low stock) as needed.

## 5. Index for Realtime Database

For the admin panel query “all users with role = supplier”, add an index in Firebase Console → Realtime Database → Rules → Indexes (or via `firebase.json`):

```json
{
  "rules": { ... },
  "indexes": {
    "users": {
      "role": { ".indexOn": ["role"] }
    }
  }
}
```

This supports `orderByChild("role").equalTo("supplier")` in `approvals.js`.
