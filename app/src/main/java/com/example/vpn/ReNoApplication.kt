package com.example.vpn

import android.app.Application
import com.example.BuildConfig
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.SetupOptions
import java.io.File
import java.util.Locale

class ReNoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        runCatching {

            Libbox.setLocale(
                Locale.getDefault()
                    .toLanguageTag()
                    .replace("-", "_")
            )

            val working =
                getExternalFilesDir(null)
                    ?: filesDir

            working.mkdirs()

            val options =
                SetupOptions().apply {

                    basePath =
                        filesDir.path

                    workingPath =
                        working.path

                    tempPath =
                        cacheDir.path

                    fixAndroidStack =
                        true

                    logMaxLines =
                        3000

                    debug =
                        BuildConfig.DEBUG
                }

            Libbox.setup(options)

            Libbox.redirectStderr(
                File(
                    working,
                    "singbox-stderr.log"
                ).path
            )

        }.onFailure {
            it.printStackTrace()
        }
    }
}
