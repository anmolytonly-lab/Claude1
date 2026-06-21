package com.auraos.launcher

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle

class AppsRepository(context: Context) {

    private val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val packageManager = context.packageManager
    private val userHandle: UserHandle = Process.myUserHandle()

    fun getInstalledApps(): List<AppInfo> {
        // LauncherApps.getActivityList is the correct modern API for launchers.
        // It respects work profiles and returns only launchable activities.
        return launcherApps.getActivityList(null, userHandle)
            .map { info ->
                AppInfo(
                    label = info.label.toString(),
                    packageName = info.applicationInfo.packageName,
                    componentName = info.componentName,
                    icon = info.getBadgedIcon(0)
                )
            }
            .sortedBy { it.label.lowercase() }
    }
}
