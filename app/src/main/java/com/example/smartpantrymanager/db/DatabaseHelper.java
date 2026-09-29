package com.example.smartpantrymanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;
import com.example.smartpantrymanager.util.IngredientMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Single SQLiteOpenHelper for the whole app. Chosen over Room/Firebase/PostgreSQL
 * because the data model is small (three simple tables) and fully on-device, which
 * keeps the CRUD logic transparent and easy to defend in the video walkthrough -
 * see the README for the full justification.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "expiry_date TEXT" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "steps TEXT NOT NULL" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES " + TABLE_RECIPES + "(id) ON DELETE CASCADE" +
                ")");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ---------------------------------------------------------------------
    // Pantry CRUD
    // ---------------------------------------------------------------------

    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", item.getName());
        cv.put("quantity", item.getQuantity());
        cv.put("unit", item.getUnit());
        cv.put("expiry_date", item.getExpiryDate());
        return db.insert(TABLE_PANTRY, null, cv);
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", item.getName());
        cv.put("quantity", item.getQuantity());
        cv.put("unit", item.getUnit());
        cv.put("expiry_date", item.getExpiryDate());
        return db.update(TABLE_PANTRY, cv, "id = ?", new String[]{String.valueOf(item.getId())});
    }

    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, "name ASC");
        while (c.moveToNext()) {
            items.add(pantryItemFromCursor(c));
        }
        c.close();
        return items;
    }

    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = pantryItemFromCursor(c);
        }
        c.close();
        return item;
    }

    private PantryItem pantryItemFromCursor(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry_date"))
        );
    }

    // ---------------------------------------------------------------------
    // Recipe reads (recipes themselves are read-only / seeded)
    // ---------------------------------------------------------------------

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, "name ASC");
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow("id"));
            Recipe recipe = new Recipe(id,
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps")));
            recipe.setIngredients(getIngredientsForRecipe(db, id));
            recipes.add(recipe);
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(id,
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps")));
            recipe.setIngredients(getIngredientsForRecipe(db, id));
        }
        c.close();
        return recipe;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, "recipe_id = ?",
                new String[]{String.valueOf(recipeId)}, null, null, "name ASC");
        while (c.moveToNext()) {
            ingredients.add(new RecipeIngredient(
                    c.getLong(c.getColumnIndexOrThrow("id")),
                    c.getLong(c.getColumnIndexOrThrow("recipe_id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    c.getString(c.getColumnIndexOrThrow("unit"))
            ));
        }
        c.close();
        return ingredients;
    }

    // ---------------------------------------------------------------------
    // Strict-matching logic (Section 2.3 of the brief)
    // ---------------------------------------------------------------------

    /**
     * Recipes where EVERY required ingredient is covered by the pantry.
     * This is the list shown on the "Suggested Recipes" screen.
     */
    public List<Recipe> getSuggestedRecipes() {
        List<Recipe> result = new ArrayList<>();
        List<PantryItem> pantry = getAllPantryItems();
        for (Recipe recipe : getAllRecipes()) {
            if (countMissingIngredients(recipe, pantry) == 0) {
                result.add(recipe);
            }
        }
        return result;
    }

    /**
     * Bonus/stretch feature: recipes missing exactly one ingredient, kept
     * separate from the strict suggestions list as required by the brief.
     */
    public List<Recipe> getAlmostThereRecipes() {
        List<Recipe> result = new ArrayList<>();
        List<PantryItem> pantry = getAllPantryItems();
        for (Recipe recipe : getAllRecipes()) {
            if (countMissingIngredients(recipe, pantry) == 1) {
                result.add(recipe);
            }
        }
        return result;
    }

    /**
     * Counts how many of a recipe's required ingredients the pantry does NOT
     * currently cover, after normalising ingredient names and units. A recipe
     * only belongs on the strict suggestions list when this returns 0.
     */
    private int countMissingIngredients(Recipe recipe, List<PantryItem> pantry) {
        int missing = 0;
        for (RecipeIngredient required : recipe.getIngredients()) {
            String neededName = IngredientMatcher.normaliseName(required.getName());
            boolean covered = false;
            for (PantryItem pantryItem : pantry) {
                String haveName = IngredientMatcher.normaliseName(pantryItem.getName());
                if (haveName.equals(neededName)
                        && IngredientMatcher.pantryCovers(
                                pantryItem.getQuantity(), pantryItem.getUnit(),
                                required.getQuantity(), required.getUnit())) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                missing++;
            }
        }
        return missing;
    }

    // ---------------------------------------------------------------------
    // Seed data: 18 recipes so the "Suggested Recipes" screen has something
    // real to filter on first run, per Section 2.2 of the brief.
    // ---------------------------------------------------------------------

    private void seedRecipes(SQLiteDatabase db) {
        addSeedRecipe(db, "Tomato Onion Pasta",
                "1. Boil pasta until al dente.\n2. Fry chopped onion until soft.\n" +
                        "3. Add chopped tomato and simmer 10 minutes.\n4. Toss with pasta and serve.",
                new Object[][]{{"pasta", 200.0, "g"}, {"tomato", 2.0, "pcs"}, {"onion", 1.0, "pcs"}});

        addSeedRecipe(db, "Scrambled Eggs on Toast",
                "1. Whisk eggs with a splash of milk.\n2. Scramble in a pan on low heat.\n" +
                        "3. Toast bread.\n4. Serve eggs on top of the toast.",
                new Object[][]{{"egg", 2.0, "pcs"}, {"milk", 30.0, "ml"}, {"bread", 2.0, "pcs"}});

        addSeedRecipe(db, "Rice and Bean Bowl",
                "1. Cook rice according to package instructions.\n" +
                        "2. Warm beans in a saucepan with a pinch of salt.\n3. Serve beans over rice.",
                new Object[][]{{"rice", 150.0, "g"}, {"bean", 200.0, "g"}});

        addSeedRecipe(db, "Garlic Butter Mushrooms",
                "1. Melt butter in a hot pan.\n2. Add sliced mushroom and minced garlic.\n" +
                        "3. Fry 5-7 minutes until golden. Season and serve.",
                new Object[][]{{"mushroom", 250.0, "g"}, {"garlic", 2.0, "pcs"}, {"butter", 20.0, "g"}});

        addSeedRecipe(db, "Simple Potato Soup",
                "1. Boil chopped potato in stock until soft.\n2. Mash roughly.\n" +
                        "3. Stir in milk and season to taste.",
                new Object[][]{{"potato", 400.0, "g"}, {"milk", 200.0, "ml"}});

        addSeedRecipe(db, "Cheese Omelette",
                "1. Whisk eggs.\n2. Pour into a hot buttered pan.\n" +
                        "3. Sprinkle grated cheese, fold in half and serve.",
                new Object[][]{{"egg", 3.0, "pcs"}, {"cheese", 50.0, "g"}, {"butter", 10.0, "g"}});

        addSeedRecipe(db, "Chicken and Rice",
                "1. Season and pan-fry chicken until cooked through.\n" +
                        "2. Cook rice separately.\n3. Serve chicken sliced over rice.",
                new Object[][]{{"chicken", 300.0, "g"}, {"rice", 150.0, "g"}});

        addSeedRecipe(db, "Carrot and Onion Stir Fry",
                "1. Slice carrot and onion thinly.\n" +
                        "2. Stir-fry in oil over high heat for 5-6 minutes.\n3. Season and serve.",
                new Object[][]{{"carrot", 200.0, "g"}, {"onion", 1.0, "pcs"}});

        addSeedRecipe(db, "Banana Milk Smoothie",
                "1. Peel and slice banana.\n2. Blend with milk until smooth.\n3. Serve chilled.",
                new Object[][]{{"banana", 2.0, "pcs"}, {"milk", 250.0, "ml"}});

        addSeedRecipe(db, "Tomato Egg Stir Fry",
                "1. Scramble egg lightly and set aside.\n" +
                        "2. Fry chopped tomato until soft.\n3. Return egg to pan, mix and serve.",
                new Object[][]{{"tomato", 3.0, "pcs"}, {"egg", 2.0, "pcs"}});

        addSeedRecipe(db, "Butter Garlic Rice",
                "1. Melt butter and fry minced garlic until fragrant.\n" +
                        "2. Add cooked rice and stir through.\n3. Season and serve warm.",
                new Object[][]{{"rice", 150.0, "g"}, {"garlic", 2.0, "pcs"}, {"butter", 15.0, "g"}});

        addSeedRecipe(db, "Cheesy Potato Bake",
                "1. Slice potato thinly and layer in a dish.\n" +
                        "2. Top with grated cheese and milk.\n3. Bake at 180C for 35-40 minutes.",
                new Object[][]{{"potato", 400.0, "g"}, {"cheese", 80.0, "g"}, {"milk", 100.0, "ml"}});

        addSeedRecipe(db, "Bean and Onion Salad",
                "1. Rinse beans and drain.\n2. Finely dice onion.\n" +
                        "3. Toss together with oil, salt and pepper.",
                new Object[][]{{"bean", 200.0, "g"}, {"onion", 1.0, "pcs"}});

        addSeedRecipe(db, "Chicken Mushroom Stir Fry",
                "1. Slice chicken and mushroom.\n" +
                        "2. Stir-fry chicken until browned, then add mushroom.\n" +
                        "3. Cook until chicken is done through, season and serve.",
                new Object[][]{{"chicken", 250.0, "g"}, {"mushroom", 150.0, "g"}});

        addSeedRecipe(db, "Carrot Soup",
                "1. Boil chopped carrot in stock until soft.\n" +
                        "2. Blend until smooth.\n3. Stir in a splash of milk and reheat gently.",
                new Object[][]{{"carrot", 300.0, "g"}, {"milk", 100.0, "ml"}});

        addSeedRecipe(db, "Toasted Cheese Sandwich",
                "1. Butter one side of each bread slice.\n" +
                        "2. Fill with cheese and grill both sides until golden.",
                new Object[][]{{"bread", 2.0, "pcs"}, {"cheese", 60.0, "g"}, {"butter", 10.0, "g"}});

        addSeedRecipe(db, "Garlic Bean Mash",
                "1. Boil beans until very soft.\n" +
                        "2. Fry minced garlic briefly, then mash together with the beans.\n" +
                        "3. Season and serve warm.",
                new Object[][]{{"bean", 250.0, "g"}, {"garlic", 2.0, "pcs"}});

        addSeedRecipe(db, "Banana Toast",
                "1. Toast the bread.\n2. Slice banana on top.\n" +
                        "3. Drizzle with a little honey if available and serve.",
                new Object[][]{{"bread", 1.0, "pcs"}, {"banana", 1.0, "pcs"}});
    }

    private void addSeedRecipe(SQLiteDatabase db, String name, String steps, Object[][] ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("steps", steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (Object[] ingredient : ingredients) {
            ContentValues cv = new ContentValues();
            cv.put("recipe_id", recipeId);
            cv.put("name", (String) ingredient[0]);
            cv.put("quantity", (Double) ingredient[1]);
            cv.put("unit", (String) ingredient[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, cv);
        }
    }
}
