package com.education.quran8

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
    private lateinit var scrollDirectionSwitch: Switch
    private lateinit var scrollOrientationSwitch: Switch
    private lateinit var fullscreenSwitch: Switch
    private lateinit var keepScreenOnSwitch: Switch // Added for Keep Screen On
    private lateinit var resetButton: Button
    private var listener: SettingsChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Initialize Views
        nightModeSwitch = view.findViewById(R.id.nightModeSwitch)
        //scrollDirectionSwitch = view.findViewById(R.id.scrollDirectionSwitch)
        scrollOrientationSwitch = view.findViewById(R.id.scrollOrientationSwitch)
        fullscreenSwitch = view.findViewById(R.id.fullscreenSwitch)
        keepScreenOnSwitch = view.findViewById(R.id.keepScreenOnSwitch) // Initialize Keep Screen On switch
        val closeButton: Button = view.findViewById(R.id.closeButton)
        resetButton = view.findViewById(R.id.resetButton)

        // Load Saved Preferences
        val isNightMode = sharedPreferences.getBoolean("NightMode", false)
        val isRightToLeft = sharedPreferences.getBoolean("ScrollDirection", true) // Default: Right-to-Left
        val isHorizontal = sharedPreferences.getBoolean("ScrollOrientation", true) // Default: Horizontal
        val isFullscreen = sharedPreferences.getBoolean("FullscreenMode", false) // Default: Not fullscreen
        val isKeepScreenOn = sharedPreferences.getBoolean("KeepScreenOn", true) // Default: Enabled

        // Set Initial States
        nightModeSwitch.isChecked = isNightMode
        //scrollDirectionSwitch.isChecked = isRightToLeft
        scrollOrientationSwitch.isChecked = isHorizontal
        fullscreenSwitch.isChecked = isFullscreen
        keepScreenOnSwitch.isChecked = isKeepScreenOn // Set initial state for Keep Screen On switch

        // Night Mode Listener
        nightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setNightMode(isChecked)
            sharedPreferences.edit().putBoolean("NightMode", isChecked).apply()
            listener?.onNightModeChanged(isChecked)
        }

        // Scroll Direction Listener
       // scrollDirectionSwitch.setOnCheckedChangeListener { _, isChecked ->
       //     sharedPreferences.edit().putBoolean("ScrollDirection", isChecked).apply()
        //    listener?.onScrollDirectionChanged(isChecked)
       // }

        // Scroll Orientation Listener
        scrollOrientationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("ScrollOrientation", isChecked).apply()
            listener?.onScrollOrientationChanged(isChecked)
        }

        // Fullscreen Mode Listener
        fullscreenSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("FullscreenMode", isChecked).apply()
            listener?.onFullscreenModeChanged(isChecked)
        }

        // Keep Screen On Listener
        keepScreenOnSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("KeepScreenOn", isChecked).apply()
            listener?.onKeepScreenOnChanged(isChecked)
        }

        // Close Button
        closeButton.setOnClickListener { dismiss() }

        // Reset Button
        resetButton.setOnClickListener {
            resetSettings()
        }

        return view
    }

    private fun setNightMode(enabled: Boolean) {
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val currentMode = sharedPreferences.getBoolean("NightMode", false)

        Log.e("DEBUG_APP_Night", "🔄 Changing Night Mode: Current=$currentMode, New=$enabled")

        // ✅ Prevent reapplying the same mode
        if (enabled == currentMode) {
            Log.e("DEBUG_APP_Night", "⚠️ Night mode is already set. No change needed.")
            return  // 🚀 STOP HERE! No restart needed.
        }

        // ✅ Save the new mode BEFORE changing UI
        sharedPreferences.edit().putBoolean("NightMode", enabled).apply()

        // ✅ Apply mode correctly
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        Log.e("DEBUG_APP_Night", "✅ Night mode changed! Restarting activity...")

        // ✅ Restart activity but prevent double toggling
        requireActivity().finish()
        requireActivity().overridePendingTransition(0, 0) // 🔥 Prevent flickering
        requireActivity().startActivity(requireActivity().intent)
    }





    private fun resetSettings() {
        sharedPreferences.edit()
            .putBoolean("NightMode", false) // Reset Night Mode
            .putBoolean("ScrollDirection", true) // Reset to Right-to-Left
            .putBoolean("ScrollOrientation", true) // Reset to Horizontal
            .putBoolean("FullscreenMode", false) // Reset Fullscreen Mode
            .putBoolean("KeepScreenOn", true) // Reset Keep Screen On
            .apply()

        // Update UI
        nightModeSwitch.isChecked = false
       // scrollDirectionSwitch.isChecked = true
        scrollOrientationSwitch.isChecked = true
        fullscreenSwitch.isChecked = false
        keepScreenOnSwitch.isChecked = true // Reset Keep Screen On switch

        // Notify listeners
        setNightMode(false)
        listener?.onNightModeChanged(false)
        listener?.onScrollDirectionChanged(true)
        listener?.onScrollOrientationChanged(true)
        listener?.onFullscreenModeChanged(false)
        listener?.onKeepScreenOnChanged(true) // Notify Keep Screen On change
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
        fun onScrollDirectionChanged(isRightToLeft: Boolean)
        fun onScrollOrientationChanged(isHorizontal: Boolean)
        fun onFullscreenModeChanged(isFullscreen: Boolean)
        fun onKeepScreenOnChanged(isEnabled: Boolean) // Added for Keep Screen On
    }
}
