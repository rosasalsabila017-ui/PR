package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CodeToPromptScreen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.OptimizerScreen
import com.example.ui.screens.PrompterStudioScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PromptViewModel

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    private val viewModel: PromptViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val user by viewModel.currentUser.collectAsStateWithLifecycle()
            val themeMode = user?.themeMode ?: "SYSTEM"

            MyApplicationTheme(themePreference = themeMode) {
                var selectedTab by rememberSaveable { mutableIntStateOf(0) }
                val snackbarHostState = remember { SnackbarHostState() }
                val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

                val requestedNavTab by viewModel.requestedNavTab.collectAsStateWithLifecycle()

                LaunchedEffect(requestedNavTab) {
                    requestedNavTab?.let { tabIndex ->
                        selectedTab = tabIndex
                        viewModel.clearRequestedNavTab()
                    }
                }

                LaunchedEffect(userMessage) {
                    userMessage?.let { msg ->
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearUserMessage()
                    }
                }

                val navItems = listOf(
                    NavItem("Prompter", Icons.Default.Speed, "nav_prompter"),
                    NavItem("Generator", Icons.Default.Bolt, "nav_generator"),
                    NavItem("Perbaikan", Icons.Default.Tune, "nav_optimizer"),
                    NavItem("Dari Kode", Icons.Default.Code, "nav_code"),
                    NavItem("Riwayat", Icons.Default.History, "nav_history")
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "TnError Prompt",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            },
                            actions = {
                                if (user?.isPro == true) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF59E0B),
                                        modifier = Modifier.padding(end = 12.dp)
                                    ) {
                                        Text(
                                            text = "PRO",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            navItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.testTag(item.testTag),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Crossfade(
                        targetState = selectedTab,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        label = "screen_crossfade"
                    ) { tab ->
                        when (tab) {
                            0 -> PrompterStudioScreen(viewModel = viewModel)
                            1 -> GeneratorScreen(viewModel = viewModel)
                            2 -> OptimizerScreen(viewModel = viewModel)
                            3 -> CodeToPromptScreen(viewModel = viewModel)
                            4 -> HistoryScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
