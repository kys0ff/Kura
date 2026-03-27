package off.kys.kura.core.di

import off.kys.kura.core.common.HapticFeedbackManager
import off.kys.kura.core.common.PackageResolver
import off.kys.kura.core.prefs.KuraPreferences
import off.kys.kura.core.registry.LockSessionManager
import off.kys.kura.features.lock.presentation.viewmodel.LockViewModel
import off.kys.kura.features.main.domain.BadgeLoader
import off.kys.kura.features.main.presentation.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { PackageResolver(androidContext()) }
    single { LockSessionManager(androidContext(), get()) }
    single { KuraPreferences(androidContext()) }
    single { HapticFeedbackManager(androidContext()) }
    single { BadgeLoader() }
    viewModel { MainViewModel(get(),get(), get(), get()) }
    viewModel { LockViewModel(get(), get(), get(), get(), get()) }
}