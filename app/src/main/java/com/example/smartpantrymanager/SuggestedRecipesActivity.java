package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Runs the strict-matching rule against the current pantry contents (Section 2.3)
 * and lists only the recipes that can be made right now. A separate, clearly
 * labelled "Almost There" list (missing exactly one ingredient) is shown below
 * it as the optional bonus feature - it is never mixed into the strict list.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecipeAdapter suggestedAdapter;
    private RecipeAdapter almostThereAdapter;
    private TextView emptyView;
    private TextView almostThereHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        dbHelper = new DatabaseHelper(this);
        emptyView = findViewById(R.id.textNoSuggestions);
        almostThereHeader = findViewById(R.id.textAlmostThereHeader);

        RecyclerView suggestedRecycler = findViewById(R.id.recyclerSuggested);
        suggestedRecycler.setLayoutManager(new LinearLayoutManager(this));
        suggestedAdapter = new RecipeAdapter(dbHelper.getSuggestedRecipes());
        suggestedRecycler.setAdapter(suggestedAdapter);

        RecyclerView almostThereRecycler = findViewById(R.id.recyclerAlmostThere);
        almostThereRecycler.setLayoutManager(new LinearLayoutManager(this));
        almostThereAdapter = new RecipeAdapter(dbHelper.getAlmostThereRecipes());
        almostThereRecycler.setAdapter(almostThereAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation2);
        bottomNav.setSelectedItemId(R.id.nav_suggested);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, PantryListActivity.class));
                return true;
            } else if (id == R.id.nav_suggested) {
                return true;

            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLists();
    }

    private void refreshLists() {
        java.util.List<Recipe> suggested = dbHelper.getSuggestedRecipes();
        suggestedAdapter.updateRecipes(suggested);
        emptyView.setVisibility(suggested.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);

        java.util.List<Recipe> almostThere = dbHelper.getAlmostThereRecipes();
        almostThereAdapter.updateRecipes(almostThere);
        int almostVisibility = almostThere.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE;
        almostThereHeader.setVisibility(almostVisibility);
    }


}
