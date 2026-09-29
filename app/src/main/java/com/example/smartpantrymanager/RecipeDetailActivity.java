package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

/**
 * Shows the full ingredient list and preparation method for a single recipe,
 * reached by tapping a row on either the Suggested or Almost There lists.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbarRecipeDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = dbHelper.getRecipe(recipeId);

        TextView nameView = findViewById(R.id.textRecipeDetailName);
        TextView ingredientsView = findViewById(R.id.textRecipeIngredients);
        TextView stepsView = findViewById(R.id.textRecipeSteps);

        if (recipe == null) {
            nameView.setText(R.string.msg_recipe_not_found);
            return;
        }

        nameView.setText(recipe.getName());
        setTitle(recipe.getName());

        StringBuilder ingredientsText = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ingredientsText.append("• ")
                    .append(formatQuantity(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append(" ")
                    .append(ingredient.getName())
                    .append("\n");
        }
        ingredientsView.setText(ingredientsText.toString().trim());
        stepsView.setText(recipe.getSteps());
    }

    private String formatQuantity(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
