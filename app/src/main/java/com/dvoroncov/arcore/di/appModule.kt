package com.dvoroncov.arcore.di

import com.dvoroncov.arcore.data.CloudAnchorStorageManager
import org.koin.dsl.module.module

val appModule = module {
    single { CloudAnchorStorageManager() }
}