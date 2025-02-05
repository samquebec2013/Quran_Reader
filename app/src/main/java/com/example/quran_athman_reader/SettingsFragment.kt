package com.example.quran_athman_reader

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment

class SettingsFragment : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var nightModeSwitch: Switch
    private lateinit var resetButton: Button
    private var listener: SettingsChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Find Views
        nightModeSwitch = view.findViewById(R.id.nightModeSwitch)
        val closeButton: Button = view.findViewById(R.id.closeButton)
        resetButton = view.findViewById(R.id.resetButton)

        // Load saved preference for Night Mode
        val isNightMode = sharedPreferences.getBoolean("NightMode", false)
        nightModeSwitch.isChecked = isNightMode
        setNightMode(isNightMode)

        // Handle switch toggle
        nightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setNightMode(isChecked)
            sharedPreferences.edit().putBoolean("NightMode", isChecked).apply()
            listener?.onNightModeChanged(isChecked) // Notify MainActivity
        }

        // Close button
        closeButton.setOnClickListener { dismiss() }

        // Reset settings button
        resetButton.setOnClickListener { resetSettings() }

        return view
    }

    private fun setNightMode(enabled: Boolean) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun resetSettings() {
        // Reset Night Mode setting to default (Day Mode)
        sharedPreferences.edit().putBoolean("NightMode", false).apply()
        nightModeSwitch.isChecked = false
        setNightMode(false)
        listener?.onNightModeChanged(false) // Notify MainActivity
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SettingsChangeListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface SettingsChangeListener {
        fun onNightModeChanged(isEnabled: Boolean)
    }
}
