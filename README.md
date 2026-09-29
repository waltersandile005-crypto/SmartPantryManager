# Smart Pantry Manager

Smart Pantry Manager is an Android app (Java) built for the Mobile App
Development 700 practical assignment. It helps a user cut food waste by
tracking the ingredients they actually have at home and suggesting only the
recipes they can cook **right now**, using strictly what is already in their
pantry - no shopping trip required.

## Core feature: strict matching

A recipe is only shown on the Suggested Recipes screen if every single
ingredient it needs is present in the pantry, in at least the required
quantity. A recipe missing even one ingredient is excluded from that list. A
separate "Almost There" section lists recipes missing exactly one ingredient,
so a user can see what they're one item away from cooking, without that
recipe ever being mixed into the strict suggestions.

Matching is tolerant of two common real-world differences instead of relying
on a naive exact-string comparison:

- **Singular vs plural ingredient names** - e.g. "tomato" in a recipe still
  matches "tomatoes" in the pantry.
- **Compatible unit differences** - e.g. a recipe asking for 500 g of flour
  is satisfied by 0.5 kg of flour in the pantry; 100 ml of milk is satisfied
  by 0.1 l.

See `app/src/main/java/com/example/smartpantrymanager/util/IngredientMatcher.java`
for the implementation, and `DatabaseHelper.getSuggestedRecipes()` /
`getAlmostThereRecipes()` for how it is applied.

## Screens

1. **Pantry List** - shows every ingredient currently on hand in a
   RecyclerView, with add/edit/delete.
2. **Add / Edit Ingredient** - a validated form (name, quantity, unit,
   optional expiry date).
3. **Suggested Recipes** - the strict-matching results, plus the bonus
   "Almost There" list.
4. **Recipe Detail** - full ingredient list and method for a chosen recipe.
5. **Settings** - toggle for expiring-soon alerts and a preferred unit system,
   stored in SharedPreferences.

Navigation between all screens uses a bottom navigation bar and standard
Android Intents (see each Activity's `onCreate`/click listeners).

## Database choice: SQLite (SQLiteOpenHelper)

SQLite was chosen over Firebase or PostgreSQL because:

- The data model is small and entirely local (three tables: pantry items,
  recipes, recipe ingredients) - there's no need for a network round-trip,
  and the app should work with no internet connection, which fits a
  "what's in my kitchen right now" use case.
- `SQLiteOpenHelper` keeps every CRUD statement explicit and easy to read
  and defend line-by-line in the video walkthrough, rather than relying on
  a cloud console the marker can't see inside the code.
- Data genuinely persists on-device between app launches, satisfying the
  assignment's persistence requirement without any external account setup.

Full CRUD is implemented on the `pantry_items` table
(`DatabaseHelper.addPantryItem`, `getAllPantryItems`, `updatePantryItem`,
`deletePantryItem`). The `recipes` and `recipe_ingredients` tables are
seeded once on first run with 18 recipes and read from (not edited by the
user), per the assignment brief.

## Project structure

```
app/src/main/java/com/example/smartpantrymanager/
├── PantryListActivity.java          # launcher, pantry CRUD list
├── AddEditIngredientActivity.java   # add/edit form with validation
├── SuggestedRecipesActivity.java    # strict-matching + "almost there"
├── RecipeDetailActivity.java        # full recipe view
├── SettingsActivity.java            # preferences screen
├── adapter/                         # RecyclerView adapters
├── db/DatabaseHelper.java           # SQLiteOpenHelper, CRUD, matching, seed data
├── model/                           # PantryItem, Recipe, RecipeIngredient
└── util/IngredientMatcher.java      # name/unit normalisation + matching rules
```

## Setup / run instructions

1. Clone this repository (or unzip it if you received it as a ZIP).
2. Open the project folder in Android Studio (Giraffe/Koala/Iguana or
   later) via **File > Open**, selecting the `SmartPantryManager` folder
   itself (the one containing `settings.gradle`).
3. The project includes the Gradle wrapper (`gradlew`, `gradlew.bat`,
   `gradle/wrapper/`), pinned to Gradle 8.6 with Android Gradle Plugin
   8.4.2, so Android Studio will use the exact same build tooling on any
   machine - just let it sync (it needs an internet connection the first
   time, to download Gradle itself plus the AndroidX/Material/RecyclerView
   dependencies listed in `app/build.gradle`).
4. Run on an emulator or physical device with API 24 (Android 7.0) or
   higher.
5. On first run the app seeds its own recipe collection - no setup steps
   are required beyond installing and opening the app.

## What's out of scope (by design, per the brief)

- No Google Maps, mapping SDK, or GPS/location features of any kind.
- No payment processing.
- Not published to the Play Store.

## Author

Tshifhiwa
