package com.app.traveljournalapp.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.app.traveljournalapp.R;
import com.app.traveljournalapp.fragment.JourneyFragment;
import com.app.traveljournalapp.fragment.MemoriesFragment;
import com.google.android.material.tabs.TabLayout;

public class JourneyManagementActivity extends AppCompatActivity {

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_management);

        tabLayout = findViewById(R.id.tabLayout);

        TabLayout.Tab journeyTab = tabLayout.newTab();
        journeyTab.setCustomView(createTabView("Journeys", true));
        tabLayout.addTab(journeyTab);

        TabLayout.Tab memoriesTab = tabLayout.newTab();
        memoriesTab.setCustomView(createTabView("Memories", false));
        tabLayout.addTab(memoriesTab);

        // Load the Journey fragment by default
        loadFragment(new JourneyFragment());

        // Handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabStyle(tab, true);
                if (tab.getPosition() == 0) {
                    loadFragment(new JourneyFragment());
                } else if (tab.getPosition() == 1) {
                     loadFragment(new MemoriesFragment());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabStyle(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private View createTabView(String title, boolean isSelected) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item, null);
        TextView tabText = view.findViewById(R.id.tabText);
        tabText.setText(title);
        tabText.setBackgroundResource(isSelected ? R.drawable.tab_selected_bg : R.drawable.edittext_bg);
        tabText.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
        return view;
    }

    private void updateTabStyle(TabLayout.Tab tab, boolean isSelected) {
        View view = tab.getCustomView();
        if (view != null) {
            TextView tabText = view.findViewById(R.id.tabText);
            tabText.setBackgroundResource(isSelected ? R.drawable.tab_selected_bg : R.drawable.edittext_bg);
            tabText.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
