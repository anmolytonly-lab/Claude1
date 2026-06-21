package com.auraos.launcher.ai

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import com.auraos.launcher.AppsRepository
import com.auraos.launcher.data.ReminderReceiver

sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class NeedsConfirmation(
        val actionType: String,
        val description: String,
        val intentResponse: IntentResponse
    ) : ActionResult()
    data class AppOpened(val appName: String) : ActionResult()
    data class Info(val info: String) : ActionResult()
    data class Error(val message: String) : ActionResult()
}

class ActionExecutor(private val context: Context) {

    private val appsRepository = AppsRepository(context)
    private var flashlightOn = false

    fun execute(response: IntentResponse): ActionResult {
        return when (response.intent) {
            "open_app" -> openApp(response.params["app_name"] ?: "")
            "search_web" -> searchWeb(response.params["query"] ?: "")
            "set_reminder" -> setReminder(
                response.params["title"] ?: "Reminder",
                response.params["minutes_from_now"]?.toIntOrNull() ?: 5
            )
            "device_info" -> getDeviceInfo(response.params["info_type"] ?: "all")
            "set_theme" -> ActionResult.Success(response.replyText)
            "toggle_setting" -> toggleSetting(
                response.params["setting"] ?: "",
                response.params["action"] ?: "toggle"
            )
            "call_contact" -> ActionResult.NeedsConfirmation(
                "call",
                "Call ${response.params["contact_name"] ?: "unknown"}?",
                response
            )
            "send_sms" -> ActionResult.NeedsConfirmation(
                "sms",
                "Send SMS to ${response.params["contact_name"] ?: "unknown"}?",
                response
            )
            else -> ActionResult.Success(response.replyText)
        }
    }

    fun executeConfirmedAction(response: IntentResponse): ActionResult {
        return when (response.intent) {
            "call_contact" -> makeCall(
                response.params["contact_name"] ?: "",
                response.params["phone_number"] ?: ""
            )
            "send_sms" -> sendSms(
                response.params["contact_name"] ?: "",
                response.params["phone_number"] ?: "",
                response.params["message"] ?: ""
            )
            else -> execute(response)
        }
    }

    private fun openApp(appName: String): ActionResult {
        if (appName.isBlank()) return ActionResult.Error("No app name provided.")

        val apps = appsRepository.getInstalledApps()
        val match = apps.firstOrNull {
            it.label.equals(appName, ignoreCase = true)
        } ?: apps.firstOrNull {
            it.label.contains(appName, ignoreCase = true)
        } ?: apps.firstOrNull {
            it.packageName.contains(appName, ignoreCase = true)
        }

        return if (match != null) {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = match.componentName
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            ActionResult.AppOpened(match.label)
        } else {
            ActionResult.Error("Couldn't find \"$appName\". Try the exact app name.")
        }
    }

    private fun searchWeb(query: String): ActionResult {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return ActionResult.Success("Searching for \"$query\"...")
    }

    private fun setReminder(title: String, minutesFromNow: Int): ActionResult {
        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = System.currentTimeMillis() + (minutesFromNow * 60 * 1000L)
            val requestCode = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("id", requestCode)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }

            ActionResult.Success("Reminder set: \"$title\" in $minutesFromNow minutes.")
        } catch (e: Exception) {
            ActionResult.Error("Couldn't set reminder: ${e.message}")
        }
    }

    fun getDeviceInfo(infoType: String): ActionResult {
        val info = StringBuilder()

        if (infoType == "battery" || infoType == "all") {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val charging = bm.isCharging
            info.appendLine("🔋 Battery: $level%${if (charging) " (Charging)" else ""}")
        }

        if (infoType == "storage" || infoType == "all") {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalGb = (stat.totalBytes / (1024.0 * 1024 * 1024))
            val freeGb = (stat.availableBytes / (1024.0 * 1024 * 1024))
            val usedGb = totalGb - freeGb
            info.appendLine("💾 Storage: %.1f GB used / %.1f GB total (%.1f GB free)".format(usedGb, totalGb, freeGb))
        }

        if (infoType == "memory" || infoType == "all") {
            val runtime = Runtime.getRuntime()
            val totalMb = runtime.maxMemory() / (1024 * 1024)
            val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
            info.appendLine("📱 App Memory: $usedMb MB / $totalMb MB")
        }

        if (infoType == "all") {
            info.appendLine("📲 Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            info.appendLine("📱 ${Build.MANUFACTURER} ${Build.MODEL}")
        }

        return ActionResult.Info(info.toString().trim())
    }

    private fun toggleSetting(setting: String, action: String): ActionResult {
        return when (setting.lowercase()) {
            "flashlight" -> toggleFlashlight(action)
            "wifi" -> {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                ActionResult.Success("Opening WiFi settings...")
            }
            "bluetooth" -> {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                ActionResult.Success("Opening Bluetooth settings...")
            }
            "airplane_mode" -> {
                context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                ActionResult.Success("Opening Airplane Mode settings...")
            }
            "dnd" -> {
                context.startActivity(Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                ActionResult.Success("Opening Do Not Disturb settings...")
            }
            else -> ActionResult.Error("Unknown setting: $setting")
        }
    }

    private fun toggleFlashlight(action: String): ActionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return ActionResult.Error("No camera found")

            val turnOn = when (action) {
                "on" -> true
                "off" -> false
                else -> !flashlightOn
            }

            cameraManager.setTorchMode(cameraId, turnOn)
            flashlightOn = turnOn
            ActionResult.Success(if (turnOn) "Flashlight turned ON 🔦" else "Flashlight turned OFF")
        } catch (e: Exception) {
            ActionResult.Error("Couldn't toggle flashlight: ${e.message}")
        }
    }

    private fun makeCall(contactName: String, phoneNumber: String): ActionResult {
        val number = phoneNumber.ifBlank { contactName }
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${Uri.encode(number)}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return ActionResult.Success("Opening dialer for $contactName...")
    }

    private fun sendSms(contactName: String, phoneNumber: String, message: String): ActionResult {
        val number = phoneNumber.ifBlank { contactName }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(number)}")
            putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return ActionResult.Success("Opening SMS for $contactName...")
    }
}
