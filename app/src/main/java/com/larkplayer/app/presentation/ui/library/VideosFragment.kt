package com.larkplayer.app.presentation.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.larkplayer.app.R

/**
 * Placeholder screen for browsing video files.
 * Media loading is handled by the shared library ViewModel in later iterations.
 */
class VideosFragment : Fragment(R.layout.fragment_videos) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Empty-state is toggled once video scanning is wired to the ViewModel.
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = super.onCreateView(inflater, container, savedInstanceState)
        ?: inflater.inflate(R.layout.fragment_videos, container, false)
}
