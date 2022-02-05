package com.example.emotionbasedmusic.container

import android.content.Context
import com.example.emotionbasedmusic.helper.HelpRepo
import com.example.emotionbasedmusic.helper.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@AndroidEntryPoint
class AppContainer @Inject constructor(): BaseFragment() {

    @Inject
    lateinit var repo: HelpRepo

    @Inject
    lateinit var permissionHelper: PermissionHelper
}