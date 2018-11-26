package com.dvoroncov.arcore

import android.app.Application
import com.dvoroncov.arcore.di.appModule
import org.koin.android.ext.android.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule))
    }
}