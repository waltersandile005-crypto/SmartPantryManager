package com.example.smartpantrymanager.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles the "strict matching" business logic described in the assignment brief.
 *
 * A recipe only counts as suggested if EVERY ingredient it needs is present in the
 * pantry in at least the required quantity. Two pieces of real-world messiness are
 * normalised before comparing:
 *
 *   1. Singular vs plural ingredient names ("tomato" vs "tomatoes").
 *   2. Compatible unit differences (e.g. grams vs kilograms, millilitres vs litres).
 *
 * This is intentionally NOT a full NLP solution - it is a small set of rules that
 * covers the common cases the brief calls out, without pretending to solve general
 * natural-language ingredient matching.
 */
public class IngredientMatcher {

    // A short list of words whose naive singular form would be wrong if we just
    // stripped a trailing "s" (e.g. "molasses" is not the plural of "molasse").
    private static final Set<String> IRREGULAR_UNCOUNTABLE = new HashSet<>();
    static {
        IRREGULAR_UNCOUNTABLE.add("molasses");
        IRREGULAR_UNCOUNTABLE.add("couscous");
        IRREGULAR_UNCOUNTABLE.add("hummus");
        IRREGULAR_UNCOUNTABLE.add("asparagus");
    }

    // Base unit each known unit converts to, and the multiplier to reach it.
    // Grouping ingredients into a common base (grams for mass, millilitres for
    // volume) lets us compare "0.5 kg" of pantry flour against a recipe that
    // calls for "500 g" of flour.
    private static final Map<String, Double> TO_GRAMS = new HashMap<>();
    private static final Map<String, Double> TO_MILLILITRES = new HashMap<>();
    static {
        TO_GRAMS.put("g", 1.0);
        TO_GRAMS.put("gram", 1.0);
        TO_GRAMS.put("grams", 1.0);
        TO_GRAMS.put("kg", 1000.0);
        TO_GRAMS.put("kilogram", 1000.0);
        TO_GRAMS.put("kilograms", 1000.0);

        TO_MILLILITRES.put("ml", 1.0);
        TO_MILLILITRES.put("millilitre", 1.0);
        TO_MILLILITRES.put("millilitres", 1.0);
        TO_MILLILITRES.put("l", 1000.0);
        TO_MILLILITRES.put("litre", 1000.0);
        TO_MILLILITRES.put("litres", 1000.0);
    }

    /**
     * Normalises an ingredient name so trivial differences don't break matching:
     * trims whitespace, lower-cases it, and reduces a simple plural to its
     * singular form (e.g. "Tomatoes" -> "tomato", "Onions" -> "onion").
     */
    public static String normaliseName(String rawName) {
        if (rawName == null) {
            return "";
        }
        String name = rawName.trim().toLowerCase();
        if (IRREGULAR_UNCOUNTABLE.contains(name)) {
            return name;
        }
        if (name.endsWith("ies") && name.length() > 3) {
            // e.g. "berries" -> "berry"
            return name.substring(0, name.length() - 3) + "y";
        }
        if (name.endsWith("es") && name.length() > 2
                && (name.endsWith("shes") || name.endsWith("ches") || name.endsWith("xes"))) {
            // e.g. "dishes" -> "dish", "boxes" -> "box"
            return name.substring(0, name.length() - 2);
        }
        if (name.endsWith("s") && !name.endsWith("ss") && name.length() > 1) {
            // e.g. "tomatoes"/"onions" -> singular. Note: run after the "es" rules
            // above so we don't cut real letters off words like "potatoes".
            if (name.endsWith("oes")) {
                return name.substring(0, name.length() - 2);
            }
            return name.substring(0, name.length() - 1);
        }
        return name;
    }

    /**
     * Converts a quantity + unit pair into a common base unit so different but
     * compatible units can be compared. Returns null if the unit is a count
     * ("pcs"/"piece"/etc.) or is not recognised - callers should then fall back
     * to comparing the raw unit strings directly.
     */
    public static Double toBaseQuantity(double quantity, String unit) {
        if (unit == null) {
            return null;
        }
        String u = unit.trim().toLowerCase();
        if (TO_GRAMS.containsKey(u)) {
            return quantity * TO_GRAMS.get(u);
        }
        if (TO_MILLILITRES.containsKey(u)) {
            return quantity * TO_MILLILITRES.get(u);
        }
        return null;
    }

    /** True if the two unit strings both belong to the same convertible family. */
    public static boolean isMassUnit(String unit) {
        return unit != null && TO_GRAMS.containsKey(unit.trim().toLowerCase());
    }

    public static boolean isVolumeUnit(String unit) {
        return unit != null && TO_MILLILITRES.containsKey(unit.trim().toLowerCase());
    }

    /**
     * Decides whether the pantry has enough of a given ingredient to satisfy a
     * recipe's requirement for it, after normalising names and units.
     */
    public static boolean pantryCovers(double pantryQty, String pantryUnit,
                                        double requiredQty, String requiredUnit) {
        // Same convertible family (both mass, or both volume): compare in the base unit.
        if (isMassUnit(pantryUnit) && isMassUnit(requiredUnit)) {
            return toBaseQuantity(pantryQty, pantryUnit) >= toBaseQuantity(requiredQty, requiredUnit);
        }
        if (isVolumeUnit(pantryUnit) && isVolumeUnit(requiredUnit)) {
            return toBaseQuantity(pantryQty, pantryUnit) >= toBaseQuantity(requiredQty, requiredUnit);
        }
        // Otherwise (counts like "pcs", or mismatched families) compare directly.
        // We still normalise the unit text itself in case of case/whitespace differences.
        String pu = pantryUnit == null ? "" : pantryUnit.trim().toLowerCase();
        String ru = requiredUnit == null ? "" : requiredUnit.trim().toLowerCase();
        if (!pu.equals(ru)) {
            // Unknown/incompatible units - be conservative and require the raw quantity
            // to be at least as large, since we cannot safely convert between them.
            return pantryQty >= requiredQty;
        }
        return pantryQty >= requiredQty;
    }
}
