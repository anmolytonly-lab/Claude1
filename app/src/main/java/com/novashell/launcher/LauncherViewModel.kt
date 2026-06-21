package com.novashell.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppsRepository(application)

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        _apps.value = repository.getInstalledApps()
    }
}
