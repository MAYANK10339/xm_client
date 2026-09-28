package com.xmclient.mobile.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xmclient.mobile.R
import com.xmclient.mobile.XMApplication
import com.xmclient.mobile.controls.ControlLayout
import com.xmclient.mobile.controls.KeyCodes
import com.xmclient.mobile.controls.VirtualButton
import com.xmclient.mobile.databinding.ActivityControlEditorBinding
import java.io.File

class ControlEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityControlEditorBinding
    private val app by lazy { application as XMApplication }
    private lateinit var layoutFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControlEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        layoutFile = File(app.profileManager.controlsDirectory, "default_layout.json")

        // Load existing or default layout
        val layout = ControlLayout.loadFromFile(layoutFile)
        binding.editorCanvas.controlLayout = layout

        setupEditorListeners()
        setupActionButtons()
    }

    private fun setupEditorListeners() {
        binding.editorCanvas.onButtonSelectedListener = { selectedBtn ->
            if (selectedBtn != null) {
                binding.cardButtonProps.visibility = View.VISIBLE
                binding.tvSelectedLabel.text = "Button: ${selectedBtn.label}"
                binding.tvSelectedKey.text = "Key: ${KeyCodes.getKeyName(selectedBtn.keyCode)}"
            } else {
                binding.cardButtonProps.visibility = View.GONE
            }
        }

        // Size adjustment buttons
        binding.btnSizePlus.setOnClickListener {
            binding.editorCanvas.updateSelectedButtonSize(+6f)
        }
        binding.btnSizeMinus.setOnClickListener {
            binding.editorCanvas.updateSelectedButtonSize(-6f)
        }

        // Delete button
        binding.btnDeleteButton.setOnClickListener {
            val selected = binding.editorCanvas.selectedButton
            if (selected != null) {
                binding.editorCanvas.controlLayout.removeButton(selected.id)
                binding.editorCanvas.selectButton(null)
                Toast.makeText(this, "Button removed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupActionButtons() {
        // Close editor
        binding.btnCloseEditor.setOnClickListener { finish() }

        // Save layout
        binding.btnSave.setOnClickListener {
            ControlLayout.saveToFile(binding.editorCanvas.controlLayout, layoutFile)
            Toast.makeText(this, "Layout saved successfully!", Toast.LENGTH_SHORT).show()
        }

        // Reset default layout
        binding.btnResetDefault.setOnClickListener {
            val defaultLayout = ControlLayout.createDefaultLayout()
            binding.editorCanvas.controlLayout = defaultLayout
            Toast.makeText(this, "Controls reset to XM default layout", Toast.LENGTH_SHORT).show()
        }

        // + Add Button Dialog
        binding.btnAddButton.setOnClickListener {
            showAddButtonDialog()
        }
    }

    private fun showAddButtonDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_button, null)
        val etLabel = dialogView.findViewById<EditText>(R.id.et_button_label)
        val spinnerKeys = dialogView.findViewById<Spinner>(R.id.spinner_key_codes)

        // Setup KeyCode options in spinner
        val keyEntries = KeyCodes.KEY_NAME_MAP.toList()
        val keyDisplayList = keyEntries.map { it.second }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, keyDisplayList)
        spinnerKeys.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btn_cancel_add).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btn_confirm_add).setOnClickListener {
            val label = etLabel.text.toString().trim()
            if (label.isEmpty()) {
                Toast.makeText(this, "Please enter a button label", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedIndex = spinnerKeys.selectedItemPosition
            val selectedKeyCode = keyEntries[selectedIndex].first

            val newButton = binding.editorCanvas.controlLayout.addNewButton(label, selectedKeyCode)
            binding.editorCanvas.selectButton(newButton)
            dialog.dismiss()
            Toast.makeText(this, "Added button '$label'. Drag to position on screen.", Toast.LENGTH_LONG).show()
        }

        dialog.show()
    }
}
