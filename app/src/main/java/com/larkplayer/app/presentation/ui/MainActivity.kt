package com.larkplayer.app.presentation.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.larkplayer.app.R
import com.larkplayer.app.databinding.ActivityMainBinding
import com.larkplayer.app.presentation.viewmodel.LibraryViewModel
import com.larkplayer.app.presentation.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val libraryViewModel: LibraryViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        observePlayerState()
    }

    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
        binding.bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_songs -> {
                    libraryViewModel.onTabSelected(com.larkplayer.app.presentation.viewmodel.LibraryTab.SONGS)
                    true
                }
                R.id.nav_videos -> {
                    libraryViewModel.onTabSelected(com.larkplayer.app.presentation.viewmodel.LibraryTab.SONGS) // Will filter video
                    true
                }
                R.id.nav_playlists -> true
                R.id.nav_folders -> true
                else -> false
            }
        }
    }

    private fun observePlayerState() {
        lifecycleScope.launch {
            playerViewModel.currentMedia.collect { media ->
                media?.let {
                    binding.miniPlayer.root.visibility = android.view.View.VISIBLE
                    updateMiniPlayer(it)
                } ?: run {
                    binding.miniPlayer.root.visibility = android.view.View.GONE
                }
            }
        }

        lifecycleScope.launch {
            playerViewModel.isPlaying.collect { isPlaying ->
                updatePlayPauseButton(isPlaying)
            }
        }
    }

    private fun updateMiniPlayer(media: com.larkplayer.app.data.model.MediaItem) {
        binding.miniPlayer.tvTitle.text = media.title
        binding.miniPlayer.tvArtist.text = media.artist ?: "Unknown Artist"
        // Load artwork with Coil
        // binding.miniPlayer.ivArtwork.load(media.artworkUrl)
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        val iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        binding.miniPlayer.btnPlayPause.setImageResource(iconRes)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}