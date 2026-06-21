package com.novashell.launcher

import android.content.ComponentName
import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val componentName: ComponentName,
    val icon: Drawable
)
