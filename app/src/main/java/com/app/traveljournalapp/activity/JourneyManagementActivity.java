package com.app.traveljournalapp.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.app.traveljournalapp.R;
import com.app.traveljournalapp.fragment.JourneyFragment;
import com.google.android.material.tabs.TabLayout;

public class JourneyManagementActivity extends AppCompatActivity {

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_management);

        tabLayout = findViewById(R.id.tabLayout);

        // Load the Journey fragment by default
        loadFragment(new JourneyFragment());

        // Set up TabLayout with ViewPager or manually handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadFragment(new JourneyFragment());
                } else if (tab.getPosition() == 1) {
//                    loadFragment(new MemoriesFragment());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
