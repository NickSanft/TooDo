package com.divora.toodo

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.divora.toodo.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private val prizesViewModel: PrizesViewModel by viewModels()
    private val pointLedgerViewModel: PointLedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        binding.disableConfirmationsSwitch.isChecked = sharedPreferences.getBoolean("disable_confirmations", false)
        binding.soundEffectsSwitch.isChecked = sharedPreferences.getBoolean("sound_effects", false)

        when (sharedPreferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            AppCompatDelegate.MODE_NIGHT_NO -> binding.lightThemeButton.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.darkThemeButton.isChecked = true
            else -> binding.systemThemeButton.isChecked = true
        }

        binding.disableConfirmationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("disable_confirmations", isChecked).apply()
        }

        binding.soundEffectsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("sound_effects", isChecked).apply()
        }

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.light_theme_button -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.dark_theme_button -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            sharedPreferences.edit().putInt("night_mode", mode).apply()
        }

        binding.clearDataButton.setOnClickListener {
            showClearDataConfirmationDialog()
        }

        // Auto-delete Spinner
        val autoDeleteOptions = listOf("Never", "After 1 week", "After 1 month")
        val autoDeleteAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, autoDeleteOptions)
        autoDeleteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.autoDeleteSpinner.adapter = autoDeleteAdapter
        
        val currentAutoDelete = sharedPreferences.getString("auto_delete_option", "Never")
        binding.autoDeleteSpinner.setSelection(autoDeleteOptions.indexOf(currentAutoDelete))

        binding.autoDeleteSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putString("auto_delete_option", autoDeleteOptions[position]).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Default Category Spinner
        val categories = listOf("General", "Work", "Personal", "Health", "Study", "Finance")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.defaultCategorySpinner.adapter = categoryAdapter

        val currentDefaultCategory = sharedPreferences.getString("default_category", "General")
        binding.defaultCategorySpinner.setSelection(categories.indexOf(currentDefaultCategory))

        binding.defaultCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putString("default_category", categories[position]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Default Difficulty Spinner
        val difficulties = listOf("Easy", "Medium", "Hard")
        val difficultyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficulties)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.defaultDifficultySpinner.adapter = difficultyAdapter

        val currentDefaultDifficulty = sharedPreferences.getString("default_difficulty", "Medium")
        binding.defaultDifficultySpinner.setSelection(difficulties.indexOf(currentDefaultDifficulty))

        binding.defaultDifficultySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putString("default_difficulty", difficulties[position]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showClearDataConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clear_data_confirmation_title)
            .setMessage(R.string.clear_data_confirmation_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                taskViewModel.deleteAll()
                prizesViewModel.deleteAll()
                pointLedgerViewModel.deleteAll()
                Toast.makeText(this, R.string.data_cleared, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
