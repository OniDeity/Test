package com.onideity.rommcompanion.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.onideity.rommcompanion.di.AppContainer
import com.onideity.rommcompanion.ui.downloads.DownloadsScreen
import com.onideity.rommcompanion.ui.downloads.DownloadsViewModel
import com.onideity.rommcompanion.ui.library.PlatformListScreen
import com.onideity.rommcompanion.ui.library.PlatformListViewModel
import com.onideity.rommcompanion.ui.library.RomListScreen
import com.onideity.rommcompanion.ui.library.RomListViewModel
import com.onideity.rommcompanion.ui.pairing.PairingScreen
import com.onideity.rommcompanion.ui.pairing.PairingViewModel
import com.onideity.rommcompanion.ui.platform.PlatformFolderSettingsScreen
import com.onideity.rommcompanion.ui.platform.PlatformFolderSettingsViewModel
import com.onideity.rommcompanion.ui.settings.SettingsScreen
import com.onideity.rommcompanion.ui.settings.SettingsViewModel
import java.net.URLDecoder

@Composable
fun RommNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val isPaired by container.authRepository.isPaired.collectAsState(initial = null)
    val pairedState = isPaired

    if (pairedState == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (pairedState) Destinations.PLATFORMS else Destinations.PAIRING,
    ) {
        composable(Destinations.PAIRING) {
            val vm: PairingViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { PairingViewModel(container.authRepository, container.settingsRepository) }
                },
            )
            PairingScreen(
                viewModel = vm,
                onPaired = {
                    navController.navigate(Destinations.PLATFORMS) {
                        popUpTo(Destinations.PAIRING) { inclusive = true }
                    }
                },
            )
        }

        composable(Destinations.PLATFORMS) {
            val vm: PlatformListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { PlatformListViewModel(container.libraryRepository, container.settingsRepository) }
                },
            )
            PlatformListScreen(
                viewModel = vm,
                onPlatformClick = { platform ->
                    navController.navigate(Destinations.roms(platform.id, platform.displayName))
                },
                onOpenDownloads = { navController.navigate(Destinations.DOWNLOADS) },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
            )
        }

        composable(
            route = Destinations.ROMS_PATTERN,
            arguments = listOf(
                navArgument(Destinations.ARG_PLATFORM_ID) { type = NavType.LongType },
                navArgument(Destinations.ARG_PLATFORM_NAME) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val platformId = backStackEntry.arguments?.getLong(Destinations.ARG_PLATFORM_ID) ?: 0L
            val platformName = backStackEntry.arguments?.getString(Destinations.ARG_PLATFORM_NAME)
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""

            val vm: RomListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        RomListViewModel(
                            platformId,
                            container.libraryRepository,
                            container.downloadRepository,
                            container.settingsRepository,
                        )
                    }
                },
            )
            RomListScreen(
                viewModel = vm,
                platformName = platformName,
                onBack = { navController.popBackStack() },
                onOpenFolderSettings = {
                    navController.navigate(Destinations.platformFolders(platformId, platformName))
                },
            )
        }

        composable(
            route = Destinations.PLATFORM_FOLDERS_PATTERN,
            arguments = listOf(
                navArgument(Destinations.ARG_PLATFORM_ID) { type = NavType.LongType },
                navArgument(Destinations.ARG_PLATFORM_NAME) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            val platformId = backStackEntry.arguments?.getLong(Destinations.ARG_PLATFORM_ID) ?: 0L
            val platformName = backStackEntry.arguments?.getString(Destinations.ARG_PLATFORM_NAME)
                ?.let { URLDecoder.decode(it, "UTF-8") } ?: ""

            val vm: PlatformFolderSettingsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { PlatformFolderSettingsViewModel(platformId, container.settingsRepository) }
                },
            )
            PlatformFolderSettingsScreen(
                viewModel = vm,
                platformName = platformName,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Destinations.SETTINGS) {
            val vm: SettingsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        SettingsViewModel(container.authRepository, container.deviceRepository, container.settingsRepository)
                    }
                },
            )
            SettingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Destinations.PAIRING) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Destinations.DOWNLOADS) {
            val vm: DownloadsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { DownloadsViewModel(container.downloadRepository) }
                },
            )
            DownloadsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
