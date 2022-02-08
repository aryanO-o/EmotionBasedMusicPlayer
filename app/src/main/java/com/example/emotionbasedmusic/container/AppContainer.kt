package com.example.emotionbasedmusic.container

import com.example.emotionbasedmusic.helper.HelpRepo
import com.example.emotionbasedmusic.helper.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppContainer @Inject constructor() : BaseFragment() {

    @Inject
    lateinit var repo: HelpRepo
}