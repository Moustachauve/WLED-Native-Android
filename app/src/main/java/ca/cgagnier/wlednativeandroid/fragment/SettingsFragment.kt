package ca.cgagnier.wlednativeandroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.databinding.FragmentSettingsBinding
import ca.cgagnier.wlednativeandroid.repository.ThemeSettings
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch


class SettingsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(layoutInflater)
        val appContainer = (requireActivity().application as DevicesApplication).container

        lifecycleScope.launch {
            appContainer.userPreferencesRepository.themeMode.collect {
                when (it) {
                    ThemeSettings.Light -> binding.radioThemeLight.isChecked = true
                    ThemeSettings.Dark -> binding.radioThemeDark.isChecked = true
                    else -> binding.radioThemeAuto.isChecked = true
                }
            }
        }
        lifecycleScope.launch {
            appContainer.userPreferencesRepository.autoDiscovery.collect {
                binding.switchAutoDiscovery.isChecked = it
            }
        }
        lifecycleScope.launch {
            appContainer.userPreferencesRepository.showOfflineDevicesLast.collect {
                binding.switchOfflineLast.isChecked = it
            }
        }
        lifecycleScope.launch {
            appContainer.userPreferencesRepository.sendCrashData.collect {
                binding.switchSendCrashData.isChecked = it
            }
        }
        lifecycleScope.launch {
            appContainer.userPreferencesRepository.sendPerformanceData.collect {
                binding.switchSendPerformanceData.isChecked = it
            }
        }

        binding.radioThemeGroup.setOnCheckedChangeListener { _, checkedId ->
            lifecycleScope.launch {
                val mode = when(checkedId){
                    R.id.radio_theme_light -> ThemeSettings.Light
                    R.id.radio_theme_dark-> ThemeSettings.Dark
                    else -> ThemeSettings.Auto
                }
                appContainer.userPreferencesRepository.updateThemeMode(mode)
            }
        }

        binding.switchAutoDiscovery.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                appContainer.userPreferencesRepository.updateAutoDiscovery(isChecked)
            }
        }

        // TODO When this is on, add a separation before offline devices (Future update)
        binding.switchOfflineLast.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                appContainer.userPreferencesRepository.updateShowOfflineDeviceLast(isChecked)
            }
        }

        binding.switchSendCrashData.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                appContainer.userPreferencesRepository.updateSendCrashData(isChecked)
            }
        }

        binding.switchSendPerformanceData.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                appContainer.userPreferencesRepository.updateSendPerformanceData(isChecked)
            }
        }
        return binding.root
    }

    override fun onResume() {
        val alertDialog = dialog as BottomSheetDialog
        alertDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        alertDialog.behavior.skipCollapsed = true
        super.onResume()
    }
}