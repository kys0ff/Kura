package off.kys.kura.core.di

import off.kys.kura.core.registry.AppLockRegistry
import off.kys.kura.core.common.PackageManagerUtils
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { PackageManagerUtils(androidContext()) }
    single { AppLockRegistry(androidContext()) }
}