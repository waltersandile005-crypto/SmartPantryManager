package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;

/**
 * Handles both creating a new pantry item and editing an existing one, decided
 * by whether an item id was passed in via the Intent extras. Performs simple
 * input validation before writing to the database (Section 3.1 of the brief).
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private EditText nameField;
    private EditText quantityField;
    private Spinner unitSpinner;
    private EditText expiryField;
    private long editingItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        Toolbar toolbar = findViewById(R.id.toolbarAddEdit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);
        nameField = findViewById(R.id.editIngredientName);
        quantityField = findViewById(R.id.editIngredientQuantity);
        unitSpinner = findViewById(R.id.spinnerUnit);
        expiryField = findViewById(R.id.editExpiryDate);
        Button saveButton = findViewById(R.id.buttonSave);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(this,
                R.array.units_array, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);

        editingItemId = getIntent().getLongExtra(PantryListActivity.EXTRA_ITEM_ID, -1);
        if (editingItemId != -1) {
            setTitle(R.string.title_edit_ingredient);
            loadExistingItem(editingItemId);
        } else {
            setTitle(R.string.title_add_ingredient);
        }

        saveButton.setOnClickListener(v -> saveIngredient());
    }

    private void loadExistingItem(long id) {
        PantryItem item = dbHelper.getPantryItem(id);
        if (item == null) {
            return;
        }
        nameField.setText(item.getName());
        quantityField.setText(trimTrailingZero(item.getQuantity()));
        expiryField.setText(item.getExpiryDate());

        ArrayAdapter adapter = (ArrayAdapter) unitSpinner.getAdapter();
        int position = adapter.getPosition(item.getUnit());
        if (position >= 0) {
            unitSpinner.setSelection(position);
        }
    }

    private void saveIngredient() {
        String name = nameField.getText().toString().trim();
        String quantityText = quantityField.getText().toString().trim();
        String unit = unitSpinner.getSelectedItem() != null
                ? unitSpinner.getSelectedItem().toString() : "";
        String expiry = expiryField.getText().toString().trim();

        if (name.isEmpty()) {
            nameField.setError(getString(R.string.error_name_required));
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
            if (quantity <= 0) {
                quantityField.setError(getString(R.string.error_quantity_positive));
                return;
            }
        } catch (NumberFormatException e) {
            quantityField.setError(getString(R.string.error_quantity_invalid));
            return;
        }

        PantryItem item = new PantryItem(editingItemId, name, quantity, unit, expiry);
        if (editingItemId == -1) {
            dbHelper.addPantryItem(item);
            Toast.makeText(this, R.string.msg_ingredient_added, Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updatePantryItem(item);
            Toast.makeText(this, R.string.msg_ingredient_updated, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private String trimTrailingZero(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
