package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.FactCheck
import com.example.architecture.views.AcademyTab
import com.example.architecture.views.VoiceProximityTab
import com.example.architecture.views.EconomyTab
import com.example.architecture.views.MarketplaceTab
import com.example.architecture.views.LandSandboxTab
import com.example.architecture.views.AvatarTab
import com.example.architecture.views.MissionsTab
import com.example.architecture.views.ModerationTab
import com.example.architecture.views.BattlePassTab
import com.example.architecture.views.TournamentsTab
import com.example.architecture.views.AdminTab
import com.example.architecture.views.LandscapeSandboxTab
import com.example.architecture.views.AInpcTab
import com.example.architecture.views.PetsTab
import com.example.architecture.views.SocialTab
import com.example.architecture.views.HousingTab
import com.example.architecture.views.GuildWarsTab
import com.example.architecture.views.LiveStreamingTab
import com.example.architecture.views.SenseiAiTab
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.architecture.viewmodel.ArchitectureViewModel
import com.example.architecture.views.ClientSimulatorTab
import com.example.architecture.views.ChatTab
import com.example.architecture.views.DatabaseTab
import com.example.architecture.views.DirectoryTab
import com.example.architecture.views.OverviewTab
import com.example.architecture.views.PlaybookTab
import com.example.architecture.views.RoadmapTab
import com.example.architecture.views.ScaleTab
import com.example.ui.theme.BlueprintBg
import com.example.ui.theme.BlueprintCard
import com.example.ui.theme.BlueprintCyan
import com.example.ui.theme.BlueprintGridLine
import com.example.ui.theme.BlueprintHeader
import com.example.ui.theme.BlueprintTeal
import com.example.ui.theme.BlueprintTextPrimary
import com.example.ui.theme.BlueprintTextSecondary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ArchitectureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force landscape orientation to open horizontally
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // Mandatory usage for clean immersive layouts
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BlueprintBg)
                        .safeDrawingPadding() // Safe drawing prevent notch / status bar overlaps
                ) { innerPadding ->
                    ArchitectureHubMainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class WorkbookTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun ArchitectureHubMainScreen(
    viewModel: ArchitectureViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab = viewModel.selectedTab.collectAsState()

    val tabs = listOf(
        WorkbookTabItem("App Cliente (RN)", Icons.Default.Smartphone, "tab_client_simulator"),
        WorkbookTabItem("Infra Geral", Icons.Default.Info, "tab_geral"),
        WorkbookTabItem("Diretórios", Icons.Default.FolderOpen, "tab_diretorios"),
        WorkbookTabItem("Banco SQL", Icons.Default.Storage, "tab_banco"),
        WorkbookTabItem("Simulador 100k", Icons.Default.Speed, "tab_simulador"),
        WorkbookTabItem("Anti-Cheat & Defesas", Icons.Default.Security, "tab_seguranca"),
        WorkbookTabItem("Cronograma", Icons.Default.TrackChanges, "tab_roadmap"),
        WorkbookTabItem("IA Chat Coach", Icons.Default.SupportAgent, "tab_chat_ia"),
        WorkbookTabItem("Sistema Academias", Icons.Default.Group, "tab_academy"),
        WorkbookTabItem("Voz Proximidade", Icons.Default.Hearing, "tab_voice"),
        WorkbookTabItem("Economia Geral", Icons.Default.Info, "tab_economy"),
        WorkbookTabItem("Mercado P2P", Icons.Default.ShoppingBag, "tab_marketplace"),
        WorkbookTabItem("Terrenos Sandbox", Icons.Default.Home, "tab_lands"),
        WorkbookTabItem("Avatar & Cosméticos", Icons.Default.Accessibility, "tab_avatar"),
        WorkbookTabItem("Missões & Retenção", Icons.Default.FactCheck, "tab_missions"),
        WorkbookTabItem("Moderação & Trust", Icons.Default.Shield, "tab_moderation"),
        WorkbookTabItem("Passe Batalha", Icons.Default.Star, "tab_battlepass"),
        WorkbookTabItem("Torneios eSports", Icons.Default.EmojiEvents, "tab_tournaments"),
        WorkbookTabItem("Painel Admin (CTO)", Icons.Default.Settings, "tab_admin_cockpit"),
        WorkbookTabItem("Mundo Landscape (Live)", Icons.Default.Landscape, "tab_landscape_mmo"),
        WorkbookTabItem("Mestres & NPCs (IA)", Icons.Default.Psychology, "tab_ai_npcs"),
        WorkbookTabItem("Mascotes & Pets", Icons.Default.Pets, "tab_pet_companions"),
        WorkbookTabItem("Rede Social & Casamentos", Icons.Default.Favorite, "tab_social_marriage"),
        WorkbookTabItem("Moradias & Sandbox CT", Icons.Default.Home, "tab_housing_sandbox"),
        WorkbookTabItem("Guild Wars & Conquistas", Icons.Default.SportsMartialArts, "tab_guild_wars"),
        WorkbookTabItem("Streaming & CDN", Icons.Default.LiveTv, "tab_live_streaming"),
        WorkbookTabItem("Sensei AI (Guardião)", Icons.Default.Psychology, "tab_sensei_ai")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlueprintBg)
    ) {
        // --- High Fidelity Branding Header ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = BlueprintGridLine, shape = RoundedCornerShape(0.dp)),
            colors = CardDefaults.cardColors(containerColor = BlueprintHeader),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(BlueprintCyan, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "JIUVERSE BLUEPRINT",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = BlueprintTextPrimary,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Senior Software Architect - Master Workbook Companion",
                            fontSize = 11.sp,
                            color = BlueprintCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Cybernetic Indicator
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF132D28), RoundedCornerShape(4.dp))
                            .border(1.dp, BlueprintTeal, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "STATUS: ONLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueprintTeal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- Horizontally Scrollable Selector Menu ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueprintHeader.copy(alpha = 0.95f))
                .border(width = 1.dp, color = BlueprintGridLine, shape = RoundedCornerShape(0.dp))
                .padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(tabs) { idx, tab ->
                val isSelected = selectedTab.value == idx
                val activeBg = if (isSelected) Color(0xFF1B3B4B) else BlueprintCard
                val activeBorder = if (isSelected) BlueprintCyan else BlueprintGridLine
                val activeText = if (isSelected) BlueprintCyan else BlueprintTextPrimary

                Row(
                    modifier = Modifier
                        .background(activeBg, RoundedCornerShape(18.dp))
                        .border(1.dp, activeBorder, RoundedCornerShape(18.dp))
                        .clickable { viewModel.selectTab(idx) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag(tab.testTag),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) BlueprintCyan else BlueprintTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeText
                    )
                }
            }
        }

        // --- Crossfade Rendering of Sheets based on tab state ---
        Box(modifier = Modifier.weight(1f)) {
            Crossfade(
                targetState = selectedTab.value,
                label = "BlueprintTransition"
            ) { tabIdx ->
                when (tabIdx) {
                    0 -> ClientSimulatorTab(viewModel = viewModel)
                    1 -> OverviewTab(viewModel = viewModel)
                    2 -> DirectoryTab(viewModel = viewModel)
                    3 -> DatabaseTab(viewModel = viewModel)
                    4 -> ScaleTab(viewModel = viewModel)
                    5 -> PlaybookTab(viewModel = viewModel)
                    6 -> RoadmapTab(viewModel = viewModel)
                    7 -> ChatTab(viewModel = viewModel)
                    8 -> AcademyTab(viewModel = viewModel)
                    9 -> VoiceProximityTab(viewModel = viewModel)
                    10 -> EconomyTab(viewModel = viewModel)
                    11 -> MarketplaceTab(viewModel = viewModel)
                    12 -> LandSandboxTab(viewModel = viewModel)
                    13 -> AvatarTab(viewModel = viewModel)
                    14 -> MissionsTab(viewModel = viewModel)
                    15 -> ModerationTab(viewModel = viewModel)
                    16 -> BattlePassTab(viewModel = viewModel)
                    17 -> TournamentsTab(viewModel = viewModel)
                    18 -> AdminTab(viewModel = viewModel)
                    19 -> LandscapeSandboxTab(viewModel = viewModel)
                    20 -> AInpcTab(viewModel = viewModel)
                    21 -> PetsTab(viewModel = viewModel)
                    22 -> SocialTab(viewModel = viewModel)
                    23 -> HousingTab(viewModel = viewModel)
                    24 -> GuildWarsTab(viewModel = viewModel)
                    25 -> LiveStreamingTab(viewModel = viewModel)
                    26 -> SenseiAiTab(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BorderWidthCheck(): androidx.compose.ui.unit.Dp {
    return 1.dp
}
