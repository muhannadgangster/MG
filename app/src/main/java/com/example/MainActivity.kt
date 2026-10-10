package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppDestination
import com.example.ui.MainTab
import com.example.ui.MgViewModel
import com.example.ui.components.MgBottomNavigationBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.CreateStudioScreen
import com.example.ui.screens.CreatorStudioScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.LegalScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MgTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MgViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            MgTheme(darkTheme = isDarkMode) {
                MgAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MgAppRoot(viewModel: MgViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifsCount = notifications.count { !it.isRead }

    when (val dest = currentDestination) {
        is AppDestination.Splash -> {
            SplashScreen(
                onTimeout = {
                    if (currentUser != null) {
                        viewModel.navigateTo(AppDestination.Main)
                    } else {
                        viewModel.navigateTo(AppDestination.Onboarding)
                    }
                }
            )
        }

        is AppDestination.Onboarding -> {
            OnboardingScreen(
                onFinish = {
                    viewModel.navigateTo(AppDestination.Auth)
                }
            )
        }

        is AppDestination.Auth -> {
            AuthScreen(
                viewModel = viewModel,
                onAuthSuccess = {
                    viewModel.navigateTo(AppDestination.Main)
                }
            )
        }

        is AppDestination.Main -> {
            BackHandler(enabled = currentTab != MainTab.HOME) {
                viewModel.setTab(MainTab.HOME)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Tab Content
                when (currentTab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToProfile = { userId ->
                                viewModel.navigateTo(AppDestination.UserProfile(userId))
                            },
                            onNavigateToSearch = {
                                viewModel.navigateTo(AppDestination.Search)
                            }
                        )
                    }

                    MainTab.EXPLORE -> {
                        ExploreScreen(
                            viewModel = viewModel,
                            onNavigateToSearch = {
                                viewModel.navigateTo(AppDestination.Search)
                            },
                            onNavigateToProfile = { userId ->
                                viewModel.navigateTo(AppDestination.UserProfile(userId))
                            },
                            onVideoClick = { video ->
                                viewModel.setTab(MainTab.HOME)
                            }
                        )
                    }

                    MainTab.CREATE -> {
                        // Managed by setTab which routes directly to CreateStudio
                    }

                    MainTab.INBOX -> {
                        InboxScreen(
                            viewModel = viewModel,
                            onOpenChat = { targetUserId, targetUsername ->
                                viewModel.navigateTo(AppDestination.ChatDetail(targetUserId, targetUsername))
                            },
                            onNavigateToProfile = { userId ->
                                viewModel.navigateTo(AppDestination.UserProfile(userId))
                            }
                        )
                    }

                    MainTab.PROFILE -> {
                        ProfileScreen(
                            userId = null, // Current user profile
                            viewModel = viewModel,
                            onNavigateToSettings = {
                                viewModel.navigateTo(AppDestination.Settings)
                            },
                            onNavigateToCreatorStudio = {
                                viewModel.navigateTo(AppDestination.CreatorStudio)
                            },
                            onNavigateToAdmin = {
                                viewModel.navigateTo(AppDestination.AdminDashboard)
                            },
                            onVideoClick = { video ->
                                viewModel.setTab(MainTab.HOME)
                            }
                        )
                    }
                }

                // Bottom Navigation Bar overlay
                MgBottomNavigationBar(
                    currentTab = currentTab,
                    unreadNotificationsCount = unreadNotifsCount,
                    onTabSelected = { tab -> viewModel.setTab(tab) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        is AppDestination.Search -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.Main)
            }
            SearchScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppDestination.Main) },
                onNavigateToProfile = { userId ->
                    viewModel.navigateTo(AppDestination.UserProfile(userId))
                },
                onVideoClick = { video ->
                    viewModel.navigateTo(AppDestination.Main)
                    viewModel.setTab(MainTab.HOME)
                }
            )
        }

        is AppDestination.CreateStudio -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.Main)
            }
            CreateStudioScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppDestination.Main) },
                onPublished = {
                    viewModel.navigateTo(AppDestination.Main)
                    viewModel.setTab(MainTab.HOME)
                }
            )
        }

        is AppDestination.CreatorStudio -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.Main)
            }
            CreatorStudioScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppDestination.Main) }
            )
        }

        is AppDestination.AdminDashboard -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.Main)
            }
            AdminDashboardScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppDestination.Main) }
            )
        }

        is AppDestination.UserProfile -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.Main)
            }
            ProfileScreen(
                userId = dest.userId,
                viewModel = viewModel,
                onNavigateToSettings = {
                    viewModel.navigateTo(AppDestination.Settings)
                },
                onNavigateToCreatorStudio = {
                    viewModel.navigateTo(AppDestination.CreatorStudio)
                },
                onNavigateToAdmin = {
                    viewModel.navigateTo(AppDestination.AdminDashboard)
                },
                onVideoClick = { video ->
                    viewModel.navigateTo(AppDestination.Main)
                    viewModel.setTab(MainTab.HOME)
                }
            )
        }

        is AppDestination.ChatDetail -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.Main)
            }
            ChatDetailScreen(
                recipientId = dest.otherUserId,
                recipientUsername = dest.otherUsername,
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppDestination.Main) }
            )
        }

        is AppDestination.Settings -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.Main)
            }
            SettingsScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(AppDestination.Main) },
                onNavigateToLegal = { docType ->
                    viewModel.navigateTo(AppDestination.Legal(docType))
                }
            )
        }

        is AppDestination.LandingPage -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.Settings)
            }
            LegalScreen(
                docType = "landing",
                onBack = { viewModel.navigateTo(AppDestination.Settings) }
            )
        }

        is AppDestination.Legal -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.Settings)
            }
            LegalScreen(
                docType = dest.docType,
                onBack = { viewModel.navigateTo(AppDestination.Settings) }
            )
        }
    }
}
