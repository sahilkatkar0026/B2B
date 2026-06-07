package com.example.medicalshopb2b.shopkeeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.medicalshopb2b.LoginActivity;
import com.example.medicalshopb2b.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    // UI Components
    private TextView txtShopkeeperName;
    private TextView txtEmail;
    private TextView txtPhone;
    private TextView txtShopName;
    private TextView txtAddress;
    private MaterialButton btnLogout;
    private MaterialButton btnEditProfile;
    private MaterialButton btnChangePassword;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private String currentUserId;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUserId);
        }

        // Initialize views
        initializeViews(view);

        // Set click listeners
        setupClickListeners();

        // Load user data from Firebase
        loadUserData();

        return view;
    }

    private void initializeViews(View view) {
        txtShopkeeperName = view.findViewById(R.id.txtShopkeeperName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtShopName = view.findViewById(R.id.txtShopName);
        txtAddress = view.findViewById(R.id.txtAddress);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
    }

    private void loadUserData() {
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state (optional)
        setLoadingState(true);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get user data from Firebase
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String shopName = snapshot.child("shopName").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);

                    // Update UI with user data
                    if (name != null && !name.isEmpty()) {
                        txtShopkeeperName.setText(name);
                    } else {
                        txtShopkeeperName.setText("N/A");
                    }

                    if (email != null && !email.isEmpty()) {
                        txtEmail.setText(email);
                    } else {
                        txtEmail.setText("N/A");
                    }

                    if (shopName != null && !shopName.isEmpty()) {
                        txtShopName.setText(shopName);
                    } else {
                        txtShopName.setText("N/A");
                    }

                    if (phone != null && !phone.isEmpty()) {
                        txtPhone.setText(phone);
                    } else {
                        txtPhone.setText("Not provided");
                    }

                    if (address != null && !address.isEmpty()) {
                        txtAddress.setText(address);
                    } else {
                        txtAddress.setText("Not provided");
                    }

                    // Hide loading state
                    setLoadingState(false);
                } else {
                    Toast.makeText(requireContext(),
                            "User data not found",
                            Toast.LENGTH_SHORT).show();
                    setLoadingState(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                        "Failed to load profile: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                setLoadingState(false);
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            // Optionally show a loading indicator
            txtShopkeeperName.setText("Loading...");
            txtEmail.setText("Loading...");
            txtShopName.setText("Loading...");
            txtPhone.setText("Loading...");
            txtAddress.setText("Loading...");
        }
    }

    private void setupClickListeners() {
        // Logout button click listener
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Edit Profile button click listener
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Edit Profile", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to edit profile screen
            // startActivity(new Intent(getContext(), EditProfileActivity.class));
        });

        // Change Password button click listener
        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Change Password", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to change password screen
            // startActivity(new Intent(getContext(), ChangePasswordActivity.class));
        });
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("Logout", (dialog, which) -> {
                    performLogout();
                })
                .show();
    }

    private void performLogout() {
        // Clear user session/preferences
        clearUserSession();

        // Sign out from Firebase
        auth.signOut();

        // Navigate to Login Activity
        Intent intent = new Intent(requireContext(), LoginActivity.class);

        // Clear the back stack so user can't go back to profile
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        // Show toast message
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void clearUserSession() {
        // Clear SharedPreferences if you store user data
        SharedPreferences sharedPref = requireContext()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data when fragment is resumed
        loadUserData();
    }
}