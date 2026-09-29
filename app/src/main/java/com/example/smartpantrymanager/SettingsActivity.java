package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A simple preferences screen (satisfies the minimum-screens requirement in
 * Section 3.1): lets the user toggle expiring-soon alerts and pick a default
 * unit system. Preferences are stored in SharedPreferences, separate from the
 * pantry/recipe data in SQLite.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    public static final String KEY_UNIT_SYSTEM = "unit_system"; // "metric" or "imperial"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Switch expiryAlertSwitch = findViewById(R.id.switchExpiryAlerts);
        RadioGroup unitGroup = findViewById(R.id.radioGroupUnits);

        expiryAlertSwitch.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        String currentUnitSystem = prefs.getString(KEY_UNIT_SYSTEM, "metric");
        if ("imperial".equals(currentUnitSystem)) {
            unitGroup.check(R.id.radioImperial);
        } else {
            unitGroup.check(R.id.radioMetric);
        }

        expiryAlertSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, isChecked).apply());

        unitGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String system = (checkedId == R.id.radioImperial) ? "imperial" : "metric";
            prefs.edit().putString(KEY_UNIT_SYSTEM, system).apply();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation3);
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, PantryListActivity.class));
                return true;
            } else if (id == R.id.nav_suggested) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }
}
