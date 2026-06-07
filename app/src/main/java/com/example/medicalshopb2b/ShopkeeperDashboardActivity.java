package com.example.medicalshopb2b;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.medicalshopb2b.shopkeeper.BillingFragment;
import com.example.medicalshopb2b.shopkeeper.HomeFragment;
import com.example.medicalshopb2b.shopkeeper.ProfileFragment;
import com.example.medicalshopb2b.shopkeeper.ReorderListFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ShopkeeperDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;

    private final HomeFragment homeFragment = new HomeFragment();
    private final BillingFragment billingFragment = new BillingFragment();
    private final ReorderListFragment reorderListFragment = new ReorderListFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private final OrderMedicineFragment orderMedicineFragment = new OrderMedicineFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopkeeper_dashboard);

        toolbar          = findViewById(R.id.toolbar);
        drawerLayout     = findViewById(R.id.drawerLayout);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // ✅ Attach toolbar — NO title shown
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // ✅ Hamburger icon
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // ✅ Default fragment
        if (savedInstanceState == null) {
            loadFragment(homeFragment);
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        // ✅ Bottom nav listener
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selected = null;

            if (id == R.id.nav_home) {
                selected = homeFragment;
            } else if (id == R.id.nav_billing) {
                selected = billingFragment;
            } else if (id == R.id.nav_reorder) {
                selected = reorderListFragment;
            } else if (id == R.id.nav_profile) {
                selected = profileFragment;
            }

            if (selected != null) {
                loadFragment(selected);
                return true;
            }
            return false;
        });

        // ✅ Drawer listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.drawer_home) {
                loadFragment(homeFragment);
                bottomNavigation.setSelectedItemId(R.id.nav_home);

            } else if (id == R.id.drawer_order) {
                loadFragment(orderMedicineFragment);

            } else if (id == R.id.drawer_purchased) {
                startActivity(new Intent(this, ShopkeeperOrdersActivity.class));

            } else if (id == R.id.drawer_history) {
                startActivity(new Intent(this, CustomerHistoryActivity.class));

            } else if (id == R.id.drawer_profile) {
                loadFragment(profileFragment);
                bottomNavigation.setSelectedItemId(R.id.nav_profile);

            } else if (id == R.id.drawer_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.app_bar_main, fragment);
        transaction.commit();
    }

    public void switchToBillingTab() {
        loadFragment(billingFragment);
        bottomNavigation.setSelectedItemId(R.id.nav_billing);
    }
}