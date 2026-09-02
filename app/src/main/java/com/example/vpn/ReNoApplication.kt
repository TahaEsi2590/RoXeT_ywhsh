package com.example.vpn

import android.app.Application
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.SetupOptions
import java.io.File
import java.util.Locale

class ReNoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        runCatching {
            Libbox.setLocale(Locale.getDefault().toLanguageTag().replace("-", "_"))
            val working = getExternalFilesDir(null) ?: filesDir
            working.mkdirs()
            Libbox.setup(SetupOptions().also {
                it.basePath = filesDir.path
                it.workingPath = working.path
                it.tempPath = cacheDir.path
                it.fixAndroidStack = true
                it.logMaxLines = 3000
                it.debug = BuildConfig.DEBUG
            })
            Libbox.redirectStderr(File(working, "singbox-stderr.log").path)
        }.onFailure { it.printStackTrace() }
    }
}
