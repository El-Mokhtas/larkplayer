package com.larkplayer.app.presentation.ui.equalizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.larkplayer.app.R
import com.larkplayer.app.databinding.FragmentEqualizerBinding
import com.larkplayer.app.data.model.EqualizerPreset
import com.larkplayer.app.presentation.viewmodel.EqualizerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EqualizerFragment : Fragment(R.layout.fragment_equalizer) {

    private var _binding: FragmentEqualizerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EqualizerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEqualizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Preset chips
        val presets = listOf(
            EqualizerPreset.NORMAL, EqualizerPreset.POP, EqualizerPreset.ROCK,
            EqualizerPreset.JAZZ, EqualizerPreset.CLASSICAL, EqualizerPreset.DANCE,
            EqualizerPreset.HIP_HOP, EqualizerPreset.CUSTOM
        )

        binding.chipGroupPresets.removeAllViews()
        presets.forEach { preset ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = getPresetName(preset)
                isCheckable = true
                setOnClickListener {
                    viewModel.onPresetSelected(preset)
                }
            }
            binding.chipGroupPresets.addView(chip)
        }

        // Band seekbars
        setupBandSeekBars()

        // Bass boost
        binding.seekBarBassBoost.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val strength = progress / 100f
                    viewModel.onBassBoostChanged(progress > 0, strength)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.switchBassBoost.setOnCheckedChangeListener { _, isChecked ->
            val strength = binding.seekBarBassBoost.progress / 100f
            viewModel.onBassBoostChanged(isChecked, strength)
        }

        // Virtualizer
        binding.seekBarVirtualizer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val strength = progress / 100f
                    viewModel.onVirtualizerChanged(progress > 0, strength)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.switchVirtualizer.setOnCheckedChangeListener { _, isChecked ->
            val strength = binding.seekBarVirtualizer.progress / 100f
            viewModel.onVirtualizerChanged(isChecked, strength)
        }

        // Volume boost
        binding.seekBarVolumeBoost.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val level = 1f + (progress / 100f * 2f) // 1.0 to 3.0
                    viewModel.onVolumeBoostChanged(progress > 0, level)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.switchVolumeBoost.setOnCheckedChangeListener { _, isChecked ->
            val level = 1f + (binding.seekBarVolumeBoost.progress / 100f * 2f)
            viewModel.onVolumeBoostChanged(isChecked, level)
        }

        // Save custom preset
        binding.btnSavePreset.setOnClickListener {
            showSavePresetDialog()
        }

        binding.btnReset.setOnClickListener {
            viewModel.resetToDefault()
            updateUI()
        }
    }

    private fun setupBandSeekBars() {
        val bandContainer = binding.layoutBands
        bandContainer.removeAllViews()

        for (i in 0 until 5) {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_equalizer_band, bandContainer, false)
            val frequencyLabel = viewModel.getBandFrequencyLabel(i)

            view.findViewById<android.widget.TextView>(R.id.tv_frequency).text = frequencyLabel
            view.findViewById<android.widget.TextView>(R.id.tv_gain).text = "0 dB"

            val seekBar = view.findViewById<SeekBar>(R.id.seek_bar)
            seekBar.max = 240 // -12 to +12 dB in 0.1 steps
            seekBar.progress = 120 // 0 dB

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val gain = (progress - 120) / 10f // Convert to dB
                        viewModel.onBandChanged(i, gain)
                        view.findViewById<android.widget.TextView>(R.id.tv_gain).text = "${gain} dB"
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            bandContainer.addView(view)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.settings.collect { settings ->
                updateUIFromSettings(settings)
            }
        }

        lifecycleScope.launch {
            viewModel.selectedPreset.collect { preset ->
                updatePresetSelection(preset)
            }
        }

        lifecycleScope.launch {
            viewModel.customPresets.collect { presets ->
                updateCustomPresets(presets)
            }
        }
    }

    private fun updateUIFromSettings(settings: com.larkplayer.app.data.model.EqualizerSettings) {
        // Update band seekbars
        val bands = settings.getBandsForPreset()
        val bandContainer = binding.layoutBands
        for (i in 0 until bands.size.min(bandContainer.childCount)) {
            val child = bandContainer.getChildAt(i)
            val seekBar = child.findViewById<SeekBar>(R.id.seek_bar)
            val gain = bands[i]
            seekBar.progress = (gain * 10 + 120).toInt().coerceIn(0, 240)
            child.findViewById<android.widget.TextView>(R.id.tv_gain).text = "${gain} dB"
        }

        // Bass boost
        binding.switchBassBoost.isChecked = settings.bassBoostEnabled
        binding.seekBarBassBoost.progress = (settings.bassBoostStrength * 100).toInt()
        binding.seekBarBassBoost.isEnabled = settings.bassBoostEnabled

        // Virtualizer
        binding.switchVirtualizer.isChecked = settings.virtualizerEnabled
        binding.seekBarVirtualizer.progress = (settings.virtualizerStrength * 100).toInt()
        binding.seekBarVirtualizer.isEnabled = settings.virtualizerEnabled

        // Volume boost
        binding.switchVolumeBoost.isChecked = settings.volumeBoostEnabled
        binding.seekBarVolumeBoost.progress = ((settings.volumeBoostLevel - 1f) / 2f * 100).toInt()
        binding.seekBarVolumeBoost.isEnabled = settings.volumeBoostEnabled
    }

    private fun updatePresetSelection(preset: EqualizerPreset) {
        for (i in 0 until binding.chipGroupPresets.childCount) {
            val chip = binding.chipGroupPresets.getChildAt(i) as com.google.android.material.chip.Chip
            chip.isChecked = getPresetName(preset) == chip.text
        }
    }

    private fun updateCustomPresets(presets: List<com.larkplayer.app.data.model.EqualizerPresetCustom>) {
        // Add custom presets to chip group
    }

    private fun updateUI() {
        viewModel.settings.value.also { settings ->
            updateUIFromSettings(settings)
        }
    }

    private fun showSavePresetDialog() {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Save Preset")
            .setView(R.layout.dialog_save_preset)
            .setPositiveButton("Save") { _, _ ->
                // Get name from dialog
                val name = (dialog.findViewById(R.id.et_preset_name) as android.widget.EditText).text.toString()
                if (name.isNotBlank()) {
                    viewModel.saveCustomPreset(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun getPresetName(preset: EqualizerPreset): String {
        return when (preset) {
            EqualizerPreset.NORMAL -> "Normal"
            EqualizerPreset.POP -> "Pop"
            EqualizerPreset.ROCK -> "Rock"
            EqualizerPreset.JAZZ -> "Jazz"
            EqualizerPreset.CLASSICAL -> "Classical"
            EqualizerPreset.DANCE -> "Dance"
            EqualizerPreset.HIP_HOP -> "Hip Hop"
            EqualizerPreset.CUSTOM -> "Custom"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}