package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.LifeViewModel
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun LifeOSApp(viewModel: LifeViewModel) {
    val isUnlocked by viewModel.isUserUnlocked.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTabStr.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    if (!isUnlocked) {
        AuthLockScreen(
            onUnlock = { pin -> viewModel.tryPinUnlock(pin) },
            username = profile?.username ?: "DisciplinedUser"
        )
    } else {
        Scaffold(
            bottomBar = {
                LifeOSBottomBar(
                    activeTab = activeTab,
                    onTabSelected = { tab -> viewModel.selectNavigationTab(tab) }
                )
            },
            containerColor = ObsidianBg
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith
                                fadeOut(animationSpec = tween(220))
                    },
                    label = "TabContent"
                ) { targetTab ->
                    when (targetTab) {
                        "dashboard" -> DashboardScreen(viewModel)
                        "fitness" -> FitnessScreen(viewModel)
                        "diet" -> DietScreen(viewModel)
                        "habits" -> HabitsScreen(viewModel)
                        "finances" -> FinancesScreen(viewModel)
                        "coach" -> AIAndProfileScreen(viewModel)
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. PIN LOCK SCREEN
// ==========================================
@Composable
fun AuthLockScreen(onUnlock: (String) -> Boolean, username: String) {
    var inputtedPin by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ObsidianBg, Color(0xFF111116), ObsidianBg)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(24.dp)
        ) {
            // Identity Avatar Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(VoltGreen.copy(alpha = 0.15f))
                    .border(2.dp, VoltGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = "Security lock",
                    tint = VoltGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "LifeOS Control Center",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                letterSpacing = 1.sp
            )

            Text(
                text = "OPERATOR: ${username.uppercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextGrayMuted,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bullet Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val active = inputtedPin.length > i
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) {
                                    if (hasError) CyberPink else VoltGreen
                                } else {
                                    CardGrayBorder
                                }
                            )
                            .border(
                                1.5.dp,
                                if (active) {
                                    if (hasError) CyberPink else VoltGreen
                                } else {
                                    TextGrayDark
                                },
                                CircleShape
                            )
                    )
                }
            }

            if (hasError) {
                Text(
                    text = "ACCESS DENIED. INVALID PASS-CODE PROTOCOL.",
                    color = CyberPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    letterSpacing = 1.sp
                )
            } else {
                Text(
                    text = "Enter secure system PIN to load dataset",
                    color = TextGrayMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Num Keypad Pad
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.testTag("pin_keypad")
            ) {
                val digits = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("CLR", "0", "UNLOCK")
                )

                digits.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { char ->
                            val isSpecial = char == "CLR" || char == "UNLOCK"
                            Box(
                                modifier = Modifier
                                    .size(if (char == "UNLOCK") 100.dp else 74.dp, 74.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSpecial) {
                                            if (char == "UNLOCK") VoltGreen.copy(alpha = 0.2f) else CardGrayBorder.copy(alpha = 0.5f)
                                        } else {
                                            CardGray
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (char == "UNLOCK") VoltGreen else CardGrayBorder,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        hasError = false
                                        when (char) {
                                            "CLR" -> {
                                                if (inputtedPin.isNotEmpty()) {
                                                    inputtedPin = inputtedPin.dropLast(1)
                                                }
                                            }
                                            "UNLOCK" -> {
                                                if (inputtedPin.length == 4) {
                                                    val success = onUnlock(inputtedPin)
                                                    if (!success) {
                                                        hasError = true
                                                        inputtedPin = ""
                                                    }
                                                }
                                            }
                                            else -> {
                                                if (inputtedPin.length < 4) {
                                                    inputtedPin += char
                                                    if (inputtedPin.length == 4) {
                                                        val success = onUnlock(inputtedPin)
                                                        if (!success) {
                                                            hasError = true
                                                            inputtedPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = if (isSpecial && char == "UNLOCK") 12.sp else 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (char == "UNLOCK") VoltGreen else PureWhite
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick bypass to help testing
            TextButton(
                onClick = {
                    onUnlock("1234")
                },
                modifier = Modifier.testTag("bypass_button")
            ) {
                Text(
                    text = "BYPASS LOCK (DEMO MODE PIN: 1234)",
                    color = VoltGreen.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ==========================================
// CUSTOM NAVIGATION BAR
// ==========================================
@Composable
fun LifeOSBottomBar(activeTab: String, onTabSelected: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            },
        color = Color(0xFF0A0A0A),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                NavigationItem("dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "Home"),
                NavigationItem("fitness", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, "Gym"),
                NavigationItem("diet", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "Calorie"),
                NavigationItem("habits", Icons.Filled.OfflinePin, Icons.Outlined.OfflinePin, "Habits"),
                NavigationItem("finances", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "YNAB"),
                NavigationItem("coach", Icons.Filled.Insights, Icons.Outlined.Insights, "Coach")
            )

            tabs.forEach { item ->
                val selected = activeTab == item.id
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(item.id) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) VoltGreen.copy(alpha = 0.15f) else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                            contentDescription = item.label,
                            tint = if (selected) VoltGreen else TextGrayMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = if (selected) VoltGreen else TextGrayMuted,
                        fontSize = 9.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

data class NavigationItem(
    val id: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val label: String
)

// ==========================================
// 2. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: LifeViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val workouts by viewModel.allWorkouts.collectAsStateWithLifecycle()
    val checkHistory by viewModel.allHabitHistory.collectAsStateWithLifecycle()
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val stepsList by viewModel.allSteps.collectAsStateWithLifecycle()
    val dietList by viewModel.allDietLogs.collectAsStateWithLifecycle()
    val waterList by viewModel.allWaterLogs.collectAsStateWithLifecycle()
    val sleepList by viewModel.allSleepLogs.collectAsStateWithLifecycle()
    val expenseList by viewModel.allExpenses.collectAsStateWithLifecycle()
    val todayStr by viewModel.currentDateStr.collectAsStateWithLifecycle()

    // Calculated metrics
    val profSafe = profile ?: UserProfile()
    val stepsToday = stepsList.find { it.date == todayStr }?.steps ?: 0
    val sleepToday = sleepList.find { it.date == todayStr }?.hours ?: 0.0
    val waterTodayMl = waterList.filter { it.date == todayStr }.sumOf { it.amountMl }
    val caloriesToday = dietList.filter { it.date == todayStr }.sumOf { it.calories }
    val proteinToday = dietList.filter { it.date == todayStr }.sumOf { it.proteinGrams }

    val completedHabitsCount = checkHistory.filter { it.date == todayStr }.size
    val totalHabitsCount = habits.size

    val workoutsDoneThisWeek = workouts.filter {
        val parsedDate = LocalDate.parse(it.date)
        parsedDate.isAfter(LocalDate.now().minusDays(8)) && it.completed
    }.size

    // Expense Metrics
    val totalSpentThisWeek = expenseList.filter {
        LocalDate.parse(it.date).isAfter(LocalDate.now().minusDays(8))
    }.sumOf { it.amount }

    // Aggregate Discipline percentage to estimate current level
    val stepsPercent = (stepsToday.toFloat() / profSafe.dailyStepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val waterPercent = (waterTodayMl.toFloat() / profSafe.waterGoalMl.coerceAtLeast(1)).coerceIn(0f, 1f)
    val habitPercent = if (totalHabitsCount > 0) (completedHabitsCount.toFloat() / totalHabitsCount).coerceIn(0f, 1f) else 1f
    val caloriePercent = if (caloriesToday <= profSafe.dailyCalorieGoal) 1f else (1f - ((caloriesToday - profSafe.dailyCalorieGoal).toFloat() / profSafe.dailyCalorieGoal.coerceAtLeast(1))).coerceIn(0f, 1f)
    val budgetPercent = if (totalSpentThisWeek <= profSafe.weeklyBudgetLimit) 1f else 0f

    val aggregateScore = ((stepsPercent + waterPercent + habitPercent + caloriePercent + budgetPercent) / 5f * 100f).roundToInt()

    val levelStatus = when {
        aggregateScore >= 90 -> "RELENTLESS UNSTOPPABLE"
        aggregateScore >= 75 -> "DETERMINED & FOCUSED"
        aggregateScore >= 50 -> "SLACKING ON TASKS"
        else -> "CRITICAL LEVEL - TAKE CONTROL"
    }

    val levelColor = when {
        aggregateScore >= 90 -> VoltGreen
        aggregateScore >= 75 -> NeonCyan
        aggregateScore >= 50 -> MutedBlue
        else -> CyberPink
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Profile
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CORE DASHBOARD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoltGreen,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Greetings, ${profSafe.username}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureWhite
                    )
                }

                // Date Display Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardGray)
                        .border(1.dp, CardGrayBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = todayStr,
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Daily accountability status scoring card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(VoltGreen.copy(alpha = 0.15f), CardGray, Color(0xFF020202))
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DAILY DISCIPLINE SCORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGrayMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background concentric support
                        Canvas(modifier = Modifier.size(150.dp)) {
                            val r1 = size.minDimension / 2 - 4.dp.toPx()
                            val r2 = size.minDimension / 2 - 20.dp.toPx()
                            val r3 = size.minDimension / 2 - 36.dp.toPx()

                            // Steps outer support track
                            if (r1 > 0f) {
                                drawCircle(
                                    color = CardGrayBorder,
                                    radius = r1,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                            }
                            // Water inner track
                            if (r2 > 0f) {
                                drawCircle(
                                    color = CardGrayBorder,
                                    radius = r2,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                            }
                            // Habits inner track
                            if (r3 > 0f) {
                                drawCircle(
                                    color = CardGrayBorder,
                                    radius = r3,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                            }
                        }

                        // Drawing Custom Nested Progress Rings
                        CustomNestedRings(
                            stepsPct = stepsPercent,
                            waterPct = waterPercent,
                            habitPct = habitPercent,
                            modifier = Modifier.size(150.dp)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$aggregateScore%",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = levelColor,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "EFFICIENCY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextGrayMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "STATUS PROTOCOL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGrayMuted,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = levelStatus,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = levelColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Quick Indicators Grid (Steps, Sleep, Hydration, Gym etc.)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardWidgetCard(
                    title = "DAILY HYDRATION",
                    value = "$waterTodayMl ml",
                    subtitle = "Goal: ${profSafe.waterGoalMl} ml",
                    icon = Icons.Filled.WaterDrop,
                    iconColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )

                DashboardWidgetCard(
                    title = "CARDIO PROTOCOL",
                    value = "$stepsToday",
                    subtitle = "Goal: ${profSafe.dailyStepGoal} steps",
                    icon = Icons.Filled.DirectionsWalk,
                    iconColor = VoltGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardWidgetCard(
                    title = "DIET TARGETS",
                    value = "$caloriesToday kcal",
                    subtitle = "Goal: ${profSafe.dailyCalorieGoal} kcal",
                    icon = Icons.Filled.LocalFireDepartment,
                    iconColor = CyberPink,
                    modifier = Modifier.weight(1f)
                )

                DashboardWidgetCard(
                    title = "WEEK SPECS",
                    value = "$workoutsDoneThisWeek Gym Sessions",
                    subtitle = "YNAB Total: $${"%.2f".format(totalSpentThisWeek)}",
                    icon = Icons.Filled.FitnessCenter,
                    iconColor = SoftEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Accountability Alerts Segment
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardGray)
                    .border(1.dp, CardGrayBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "ACTIVE PROTOCOL NOTIFICATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoltGreen,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                val alerts = mutableListOf<String>()
                if (stepsToday < profSafe.dailyStepGoal) {
                    alerts.add("🚨 STEP GAP alert: ${profSafe.dailyStepGoal - stepsToday} more steps needed to clear cardio metrics.")
                }
                if (waterTodayMl < profSafe.waterGoalMl) {
                    alerts.add("💧 HYDRATION warning: Lacking ${profSafe.waterGoalMl - waterTodayMl} ml needed for proper structural body weight ratio.")
                }
                if (completedHabitsCount < totalHabitsCount) {
                    alerts.add("⚡ UNCONQUERED HABITS: $completedHabitsCount of $totalHabitsCount habits completed today. Do not give in to weakness.")
                }
                if (totalSpentThisWeek > profSafe.weeklyBudgetLimit) {
                    alerts.add("🔥 DANGER OVERSPEND: Spend limit is exceeded by $${"%.2f".format(totalSpentThisWeek - profSafe.weeklyBudgetLimit)}. Lock your wallet logs immediately.")
                }
                if (proteinToday < profSafe.proteinGoalGrams) {
                    alerts.add("🍖 MACRO DEPLETION: Lacking ${profSafe.proteinGoalGrams - proteinToday}g protein. Prepare amino acid supplement.")
                }

                if (alerts.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Shield, contentDescription = "Safe", tint = SoftEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ALL CRITICAL METRICS COMPLIANT. STEADY VELOCITY INTENSIFIES.",
                            color = SoftEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    alerts.forEach { alert ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "•",
                                color = CyberPink,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = alert,
                                color = TextWhite,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Nested Rings Draw Canvas Component
@Composable
fun CustomNestedRings(
    stepsPct: Float,
    waterPct: Float,
    habitPct: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val outerRadius = size.minDimension / 2 - 4.dp.toPx()
        val middleRadius = size.minDimension / 2 - 20.dp.toPx()
        val innerRadius = size.minDimension / 2 - 36.dp.toPx()
        val strokeWidthPx = 8.dp.toPx()

        // Outer (Steps) - Neon Cyan
        if (outerRadius > 0f) {
            drawArc(
                color = NeonCyan,
                startAngle = -90f,
                sweepAngle = stepsPct * 360f,
                useCenter = false,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // Middle (Water) - Volt Green
        if (middleRadius > 0f) {
            drawArc(
                color = VoltGreen,
                startAngle = -90f,
                sweepAngle = waterPct * 360f,
                useCenter = false,
                topLeft = Offset(center.x - middleRadius, center.y - middleRadius),
                size = Size(middleRadius * 2, middleRadius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // Inner (Habits) - Cyber Pink
        if (innerRadius > 0f) {
            drawArc(
                color = CyberPink,
                startAngle = -90f,
                sweepAngle = habitPct * 360f,
                useCenter = false,
                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                size = Size(innerRadius * 2, innerRadius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun DashboardWidgetCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardGray),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGrayMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextGrayMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


// ==========================================
// 3. FITNESS MODULE (WORKOUT TRACKER)
// ==========================================
@Composable
fun FitnessScreen(viewModel: LifeViewModel) {
    val workouts by viewModel.allWorkouts.collectAsStateWithLifecycle()
    var isAddingWorkout by remember { mutableStateOf(false) }

    // Forms states
    var workoutNameInput by remember { mutableStateOf("") }
    var durationInput by remember { mutableStateOf("60") }
    var exerciseNameInput by remember { mutableStateOf("") }
    var exerciseCatInput by remember { mutableStateOf("Chest") }
    var exerciseSetsInput by remember { mutableStateOf("12 reps x 80 kg,10 reps x 90 kg") }

    val exerciseListTemp = remember { mutableStateListOf<Pair<String, Pair<String, String>>>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FITNESS MODULE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoltGreen,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Training Protocol",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureWhite
                    )
                }

                Button(
                    onClick = {
                        isAddingWorkout = true
                        workoutNameInput = ""
                        exerciseListTemp.clear()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = ObsidianBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Log workout")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add Session", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Workout template dialog log
        if (isAddingWorkout) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardGray),
                    border = BorderStroke(1.dp, VoltGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "LOG RECENT WORKOUT SESSION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VoltGreen
                            )
                            IconButton(onClick = { isAddingWorkout = false }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = CyberPink)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = workoutNameInput,
                            onValueChange = { workoutNameInput = it },
                            label = { Text("Workout Split / Title (e.g. Legs Day)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VoltGreen,
                                unfocusedBorderColor = CardGrayBorder,
                                focusedLabelColor = VoltGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = durationInput,
                            onValueChange = { durationInput = it },
                            label = { Text("Duration (Minutes)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VoltGreen,
                                unfocusedBorderColor = CardGrayBorder,
                                focusedLabelColor = VoltGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = CardGrayBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "ADD EXERCISES TO SPLIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGrayMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = exerciseNameInput,
                            onValueChange = { exerciseNameInput = it },
                            label = { Text("Exercise Name (e.g., Bench Press)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CardGrayBorder,
                                focusedLabelColor = NeonCyan
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = exerciseCatInput,
                                onValueChange = { exerciseCatInput = it },
                                label = { Text("Muscle Target") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CardGrayBorder,
                                    focusedLabelColor = NeonCyan
                                )
                            )

                            OutlinedTextField(
                                value = exerciseSetsInput,
                                onValueChange = { exerciseSetsInput = it },
                                label = { Text("Sets Specs (e.g., 3x10)") },
                                modifier = Modifier.weight(1.5f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CardGrayBorder,
                                    focusedLabelColor = NeonCyan
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (exerciseNameInput.isNotEmpty()) {
                                    exerciseListTemp.add(
                                        Pair(
                                            exerciseNameInput,
                                            Pair(exerciseCatInput, exerciseSetsInput)
                                        )
                                    )
                                    exerciseNameInput = ""
                                    exerciseSetsInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ObsidianBg),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Append Exercise", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Added exercises list display
                        exerciseListTemp.forEachIndexed { idx, pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "${idx + 1}. ${pair.first}", color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(text = "${pair.second.first} | ${pair.second.second}", color = TextGrayMuted, fontSize = 11.sp)
                                }
                                IconButton(onClick = { exerciseListTemp.removeAt(idx) }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = CyberPink)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (workoutNameInput.isNotEmpty()) {
                                    viewModel.logWorkoutCompleted(
                                        workoutNameInput,
                                        durationInput.toIntOrNull() ?: 60,
                                        exerciseListTemp.toList()
                                    )
                                    isAddingWorkout = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = ObsidianBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE COMPLETE WORKOUT SPLIT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Workout Records Lists
        items(workouts) { workout ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray),
                border = BorderStroke(1.dp, if (workout.completed) SoftEmerald.copy(alpha = 0.5f) else CardGrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DATE LOGGED: ${workout.date}",
                                color = TextGrayMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = workout.name,
                                color = PureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { viewModel.deleteWorkoutInstance(workout.id) }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = CyberPink)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Timer, contentDescription = null, tint = TextGrayMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${workout.durationMinutes} minutes", color = TextWhite, fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = workout.completed,
                                onCheckedChange = { viewModel.toggleWorkoutState(workout.id, it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SoftEmerald,
                                    uncheckedColor = TextGrayMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = if (workout.completed) "Session Done" else "Pending Logs", color = if (workout.completed) SoftEmerald else MutedBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Display custom exercises for the workout
                    val exercisesFlowByWorkout = remember(workout.id) { viewModel.getWorkoutExercisesFlow(workout.id) }
                    val exercisesByWorkout by exercisesFlowByWorkout.collectAsStateWithLifecycle(initialValue = emptyList())

                    if (exercisesByWorkout.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = CardGrayBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        exercisesByWorkout.forEachIndexed { i, ex ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(text = "${i + 1}. ${ex.name} (Target: ${ex.category})", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "   Sets: ${ex.setsText}", color = VoltGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. DIET & CARDIO (DIET, MACROS & CARDIOS)
// ==========================================
@Composable
fun DietScreen(viewModel: LifeViewModel) {
    val dietLogs by viewModel.allDietLogs.collectAsStateWithLifecycle()
    val waterLogs by viewModel.allWaterLogs.collectAsStateWithLifecycle()
    val cardioLogs by viewModel.allCardioLogs.collectAsStateWithLifecycle()
    val stepsList by viewModel.allSteps.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val todayStr by viewModel.currentDateStr.collectAsStateWithLifecycle()

    val isEstimatingNutrition by viewModel.isEstimatingNutrition.collectAsStateWithLifecycle()
    val nutritionEstimationResult by viewModel.nutritionEstimationResult.collectAsStateWithLifecycle()

    val profSafe = profile ?: UserProfile()

    // Aggregate values
    val waterToday = waterLogs.filter { it.date == todayStr }.sumOf { it.amountMl }
    val dietToday = dietLogs.filter { it.date == todayStr }
    val totalCalories = dietToday.sumOf { it.calories }
    val totalProtein = dietToday.sumOf { it.proteinGrams }
    val totalCarbs = dietToday.sumOf { it.carbsGrams }
    val totalFats = dietToday.sumOf { it.fatsGrams }
    val stepsToday = stepsList.find { it.date == todayStr }?.steps ?: 0

    // Add dialogs states
    var isAddingMeal by remember { mutableStateOf(false) }
    var mealName by remember { mutableStateOf("") }
    var mealTypeSelected by remember { mutableStateOf("Breakfast") }
    var mealCals by remember { mutableStateOf("") }
    var mealProtein by remember { mutableStateOf("") }
    var mealCarbs by remember { mutableStateOf("") }
    var mealFats by remember { mutableStateOf("") }

    LaunchedEffect(nutritionEstimationResult) {
        nutritionEstimationResult?.let { estimation ->
            mealCals = estimation.calories.toString()
            mealProtein = estimation.protein.toString()
            mealCarbs = estimation.carbs.toString()
            mealFats = estimation.fats.toString()
        }
    }

    var isAddingCardio by remember { mutableStateOf(false) }
    var cardioType by remember { mutableStateOf("Running") }
    var cardioMinutes by remember { mutableStateOf("30") }
    var cardioDistance by remember { mutableStateOf("4.2") }
    var cardioCalsBurned by remember { mutableStateOf("320") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "DIET & PERFORMANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoltGreen,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Machinery Intake & Output",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PureWhite
                )
            }
        }

        // Calorie tracking / macro bars
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray),
                border = BorderStroke(1.dp, CardGrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MACRONUTRIENT BALANCE TRACKING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGrayMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(text = "Calories Consumed", color = TextGrayMuted, fontSize = 11.sp)
                            Text(
                                text = "$totalCalories / ${profSafe.dailyCalorieGoal} kcal",
                                color = PureWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        val calLimitRatio = if (totalCalories > 0) (totalCalories.toFloat() / profSafe.dailyCalorieGoal.coerceAtLeast(1)) else 0f
                        Text(
                            text = if (totalCalories > profSafe.dailyCalorieGoal) "CALORIC EXTRA" else "${(profSafe.dailyCalorieGoal - totalCalories)} kcal left",
                            color = if (totalCalories > profSafe.dailyCalorieGoal) CyberPink else VoltGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Linear progressive bar for calories
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CardGrayBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (totalCalories.toFloat() / profSafe.dailyCalorieGoal.coerceAtLeast(1)).coerceIn(0f, 1f))
                                .height(8.dp)
                                .background(if (totalCalories > profSafe.dailyCalorieGoal) CyberPink else VoltGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Individual macros details
                    MacroProgressBar(label = "Protein", value = totalProtein, goal = profSafe.proteinGoalGrams, color = VoltGreen)
                    MacroProgressBar(label = "Carb Blocks", value = totalCarbs, goal = profSafe.carbsGoalGrams, color = NeonCyan)
                    MacroProgressBar(label = "Essential Fats", value = totalFats, goal = profSafe.fatsGoalGrams, color = CyberPink)
                }
            }
        }

        // Hydration Card Control Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray),
                border = BorderStroke(1.dp, CardGrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HYDRATION RATIO: $waterToday ml / ${profSafe.waterGoalMl} ml",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGrayMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val sizes = listOf(250, 500, 750)
                        sizes.forEach { size ->
                            Button(
                                onClick = { viewModel.addWaterLogMl(size) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f), contentColor = NeonCyan),
                                border = BorderStroke(1.dp, NeonCyan),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+$size ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Input Buttons Segment
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.clearNutritionEstimationResult()
                        mealName = ""
                        mealCals = ""
                        mealProtein = ""
                        mealCarbs = ""
                        mealFats = ""
                        isAddingMeal = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = ObsidianBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Log Meal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isAddingCardio = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ObsidianBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Log Cardio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Log Meal Dialog box
        if (isAddingMeal) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardGray),
                    border = BorderStroke(1.dp, VoltGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ADD DIET ENTRY", fontWeight = FontWeight.Bold, color = VoltGreen, fontSize = 13.sp)
                            IconButton(onClick = { isAddingMeal = false }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = CyberPink)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = mealName,
                            onValueChange = { 
                                mealName = it
                                if (nutritionEstimationResult != null) {
                                    viewModel.clearNutritionEstimationResult()
                                }
                            },
                            label = { Text("Food/Meal Name (e.g. 2 Roti & Paneer Butter Masala)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (mealName.isNotBlank() && !isEstimatingNutrition) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    viewModel.triggerNutritionEstimation(mealName)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VoltGreen.copy(alpha = 0.15f), 
                                    contentColor = VoltGreen
                                ),
                                border = BorderStroke(1.dp, VoltGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🔮 AUTO-ESTIMATE NUTRITION", 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isEstimatingNutrition) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = VoltGreen,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Estimating macro payload...", 
                                    color = TextWhite, 
                                    fontSize = 11.sp, 
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }

                        if (nutritionEstimationResult != null && !isEstimatingNutrition) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VoltGreen.copy(alpha = 0.15f))
                                    .border(1.dp, VoltGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = nutritionEstimationResult?.explanation ?: "Values auto-estimated!",
                                    color = VoltGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")
                            meals.forEach { type ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (mealTypeSelected == type) VoltGreen else CardGrayBorder)
                                        .clickable { mealTypeSelected = type }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (mealTypeSelected == type) ObsidianBg else TextWhite
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = mealCals,
                            onValueChange = { mealCals = it },
                            label = { Text("Calories (kcal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mealProtein,
                                onValueChange = { mealProtein = it },
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mealCarbs,
                                onValueChange = { mealCarbs = it },
                                label = { Text("Carbs (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mealFats,
                                onValueChange = { mealFats = it },
                                label = { Text("Fats (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (mealName.isNotEmpty()) {
                                    viewModel.logNewMeal(
                                        mealName,
                                        mealTypeSelected,
                                        mealCals.toIntOrNull() ?: 0,
                                        mealProtein.toIntOrNull() ?: 0,
                                        mealCarbs.toIntOrNull() ?: 0,
                                        mealFats.toIntOrNull() ?: 0
                                    )
                                    isAddingMeal = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = ObsidianBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("APPEND MEAL PROTOCOL", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Log Cardio Dialog box
        if (isAddingCardio) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardGray),
                    border = BorderStroke(1.dp, NeonCyan)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ADD CARDIO RECORD", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
                            IconButton(onClick = { isAddingCardio = false }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = CyberPink)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = cardioType,
                            onValueChange = { cardioType = it },
                            label = { Text("Cardio Type (e.g., Trail Running)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cardioMinutes,
                                onValueChange = { cardioMinutes = it },
                                label = { Text("Minutes") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cardioDistance,
                                onValueChange = { cardioDistance = it },
                                label = { Text("Distance (km)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = cardioCalsBurned,
                            onValueChange = { cardioCalsBurned = it },
                            label = { Text("Calories Burned (kcal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.addCardioSession(
                                    cardioType,
                                    cardioMinutes.toIntOrNull() ?: 20,
                                    cardioDistance.toDoubleOrNull() ?: 0.0,
                                    cardioCalsBurned.toDoubleOrNull() ?: 0.0
                                )
                                isAddingCardio = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ObsidianBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE CARDIO LOG", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Daily Step Goals Manual update card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray),
                border = BorderStroke(1.dp, CardGrayBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TODAY'S STEP TRACKER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGrayMuted
                        )
                        Text(
                            text = "$stepsToday / ${profSafe.dailyStepGoal} Steps",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PureWhite
                        )
                    }

                    Box(modifier = Modifier.width(130.dp)) {
                        var stepInputText by remember { mutableStateOf(stepsToday.toString()) }
                        OutlinedTextField(
                            value = stepInputText,
                            onValueChange = {
                                stepInputText = it
                                val parsed = it.toIntOrNull()
                                if (parsed != null) viewModel.logDailySteps(parsed)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Actual steps") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VoltGreen,
                                unfocusedBorderColor = CardGrayBorder
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Meal & Cardio Lists
        if (dietToday.isNotEmpty()) {
            item {
                Text(
                    text = "TODAY'S INSTANT DIET LOGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoltGreen,
                    letterSpacing = 1.sp
                )
            }

            items(dietToday) { meal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardGray)
                        .border(1.dp, CardGrayBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = meal.mealType.uppercase(), color = VoltGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = meal.name, color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = "${meal.calories} kcal | Protein: ${meal.proteinGrams}g | Carbs: ${meal.carbsGrams}g | Fats: ${meal.fatsGrams}g", color = TextGrayMuted, fontSize = 11.sp)
                    }

                    IconButton(onClick = { viewModel.deleteMealLog(meal.id) }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = CyberPink)
                    }
                }
            }
        }

        if (cardioLogs.isNotEmpty()) {
            item {
                Text(
                    text = "CARDIO PROTOCOL LOG HISTORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
            }

            items(cardioLogs) { cardio ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardGray)
                        .border(1.dp, CardGrayBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "DATE LOGGED: ${cardio.date}", color = TextGrayMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text(text = cardio.activityType, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "${cardio.minutes} mins | ${cardio.distanceKm} km | ${cardio.caloriesBurned} kcal burned", color = NeonCyan, fontSize = 11.sp)
                    }

                    IconButton(onClick = { viewModel.deleteCardioSession(cardio.id) }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = CyberPink)
                    }
                }
            }
        }
    }
}

@Composable
fun MacroProgressBar(label: String, value: Int, goal: Int, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "$value g / $goal g",
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CardGrayBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (value.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(color)
            )
        }
    }
}


// ==========================================
// 5. HABIT MODULE & HEATMAP
// ==========================================
@Composable
fun HabitsScreen(viewModel: LifeViewModel) {
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val checkHistory by viewModel.allHabitHistory.collectAsStateWithLifecycle()
    val todayStr by viewModel.currentDateStr.collectAsStateWithLifecycle()

    var isAddingHabit by remember { mutableStateOf(false) }
    var habitNameInput by remember { mutableStateOf("") }
    var habitCategoryInput by remember { mutableStateOf("Discipline") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HABITS & REPETITIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoltGreen,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Consistency Pillars",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureWhite
                    )
                }

                Button(
                    onClick = {
                        isAddingHabit = true
                        habitNameInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = ObsidianBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add habit")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Define", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Heatmap panel block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray),
                border = BorderStroke(1.dp, CardGrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DISCIPLINE HEATMAP (LAST 28 DAYS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGrayMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Drawing Custom 28-day Grid Heatmap mimicking GitHub contributions
                    HabitContributionHeatmap(
                        history = checkHistory,
                        habitsTotalCount = habits.size
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Less Consistent", color = TextGrayMuted, fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val colors = listOf(CardGrayBorder, VoltGreen.copy(alpha = 0.3f), VoltGreen.copy(alpha = 0.6f), VoltGreen)
                            colors.forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(c, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                        Text(text = "Brutal Consistency", color = VoltGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Add habit dialog/split block
        if (isAddingHabit) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardGray),
                    border = BorderStroke(1.dp, VoltGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("DEFINE NEW IDENTITY HABIT", fontWeight = FontWeight.Bold, color = VoltGreen, fontSize = 13.sp)
                            IconButton(onClick = { isAddingHabit = false }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = CyberPink)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = habitNameInput,
                            onValueChange = { habitNameInput = it },
                            label = { Text("Habit Objective (e.g. Wake up at 5am)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = habitCategoryInput,
                            onValueChange = { habitCategoryInput = it },
                            label = { Text("Category (e.g. Health, Discipline, Mind)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (habitNameInput.isNotEmpty()) {
                                    viewModel.addCustomHabit(habitNameInput, habitCategoryInput)
                                    isAddingHabit = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = ObsidianBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE HABIT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Habit Lists
        items(habits) { habit ->
            val checkedToday = checkHistory.any { it.habitId == habit.id && it.date == todayStr }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardGray)
                    .border(1.dp, if (checkedToday) VoltGreen.copy(alpha = 0.5f) else CardGrayBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleHabitState(habit.id) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (checkedToday) VoltGreen.copy(alpha = 0.2f) else CardGrayBorder)
                            .border(1.dp, if (checkedToday) VoltGreen else TextGrayMuted, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (checkedToday) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = "Check habit",
                            tint = if (checkedToday) VoltGreen else TextGrayMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = habit.name,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Streak: ${habit.currentStreak} days | Peak: ${habit.maxStreak} | Total: ${habit.totalCompletions}",
                            color = TextGrayMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = { viewModel.deleteHabitInstance(habit.id) }) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = CyberPink)
                }
            }
        }
    }
}

// Draw Grid heatmap for past 28 days
@Composable
fun HabitContributionHeatmap(
    history: List<HabitHistory>,
    habitsTotalCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellCount = 28 // 4 weeks x 7 days
            val columns = 4
            val rows = 7
            
            val totalHorizontalGaps = columns - 1
            val totalVerticalGaps = rows - 1
            
            // Available space calculations
            val gapSizePx = 4.dp.toPx()
            val cellSizePx = minOf(
                (size.width - totalHorizontalGaps * gapSizePx) / columns,
                (size.height - totalVerticalGaps * gapSizePx) / rows
            ).coerceAtLeast(0f)

            // Centering drawing
            val drawWidth = columns * cellSizePx + totalHorizontalGaps * gapSizePx
            val drawHeight = rows * cellSizePx + totalVerticalGaps * gapSizePx
            val offsetX = (size.width - drawWidth) / 2
            val offsetY = (size.height - drawHeight) / 2

            val today = LocalDate.now()

            for (col in 0 until columns) {
                for (row in 0 until rows) {
                    // Index backwards: day index from 0 (27 days ago) to 27 (today)
                    val daysAgo = 27 - (col * rows + row)
                    val dateToCheck = today.minusDays(daysAgo.toLong()).toString()

                    // Count checks on this particular backdate
                    val checksCount = history.count { it.date == dateToCheck }
                    
                    val cellColor = when {
                        habitsTotalCount == 0 -> CardGrayBorder
                        checksCount == 0 -> CardGrayBorder
                        checksCount >= habitsTotalCount -> VoltGreen
                        checksCount >= (habitsTotalCount / 2f) -> VoltGreen.copy(alpha = 0.6f)
                        else -> VoltGreen.copy(alpha = 0.3f)
                    }

                    drawRoundRect(
                        color = cellColor,
                        topLeft = Offset(
                            x = offsetX + col * (cellSizePx + gapSizePx),
                            y = offsetY + row * (cellSizePx + gapSizePx)
                        ),
                        size = Size(cellSizePx, cellSizePx),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }
        }
    }
}


// ==========================================
// 6. FINANCES MODULE (YNAB EXPENSES & BUDGET)
// ==========================================
@Composable
fun FinancesScreen(viewModel: LifeViewModel) {
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val todayStr by viewModel.currentDateStr.collectAsStateWithLifecycle()

    val profSafe = profile ?: UserProfile()

    // Aggregate spend details
    val totalSpentThisWeek = expenses.filter {
        val parsedDate = LocalDate.parse(it.date)
        parsedDate.isAfter(LocalDate.now().minusDays(8))
    }.sumOf { it.amount }

    // Categories details allocation
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    var isAddingExpense by remember { mutableStateOf(false) }
    var expenseCategory by remember { mutableStateOf("Food") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseNotes by remember { mutableStateOf("") }

    val categories = listOf("Food", "Gym", "Bills", "Shopping", "Transport", "Entertainment", "Investments")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FINANCIAL CONTROL (YNAB)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VoltGreen,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Discipline Ledger",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureWhite
                    )
                }

                Button(
                    onClick = {
                        isAddingExpense = true
                        expenseAmount = ""
                        expenseNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = ObsidianBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Log Expense")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Log Buy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Spending ledger widget alert
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray),
                border = BorderStroke(1.dp, CardGrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WEEKLY YNAB LEDGER SUMMARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGrayMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(text = "Weekly Tracked Outflow", color = TextGrayMuted, fontSize = 11.sp)
                            Text(
                                text = "$${"%.2f".format(totalSpentThisWeek)} / $${"%.2f".format(profSafe.weeklyBudgetLimit)}",
                                color = PureWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        val limitsPct = (totalSpentThisWeek / profSafe.weeklyBudgetLimit.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
                        Text(
                            text = if (totalSpentThisWeek > profSafe.weeklyBudgetLimit) "OVERBUDGET DEBT" else "$${"%.2f".format(profSafe.weeklyBudgetLimit - totalSpentThisWeek)} left",
                            color = if (totalSpentThisWeek > profSafe.weeklyBudgetLimit) CyberPink else VoltGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Linear progressive bar for budgets
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CardGrayBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (totalSpentThisWeek.toFloat() / profSafe.weeklyBudgetLimit.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f))
                                .height(8.dp)
                                .background(if (totalSpentThisWeek > profSafe.weeklyBudgetLimit) CyberPink else VoltGreen)
                        )
                    }

                    // Dynamic Spending Pie Segment indicator
                    if (expenses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "CATEGORY SPEND ALLOCATION RATIO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGrayMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CategoryProportionalBar(categoryTotals = categoryTotals)
                    }
                }
            }
        }

        // Add expense dialog card/split block
        if (isAddingExpense) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardGray),
                    border = BorderStroke(1.dp, VoltGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("LOG NEW LEDGER EXPENSE", fontWeight = FontWeight.Bold, color = VoltGreen, fontSize = 13.sp)
                            IconButton(onClick = { isAddingExpense = false }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = CyberPink)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "SELECT CATEGORY PROTOCOL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGrayMuted
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Pills Select
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        ) {
                            categories.forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (expenseCategory == cat) VoltGreen else CardGrayBorder)
                                        .clickable { expenseCategory = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (expenseCategory == cat) ObsidianBg else TextWhite
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            label = { Text("Expense Value (USD $)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = expenseNotes,
                            onValueChange = { expenseNotes = it },
                            label = { Text("Notes / Groceries description") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val amtPrs = expenseAmount.toDoubleOrNull()
                                if (amtPrs != null && amtPrs > 0) {
                                    viewModel.addExpenseRecord(expenseCategory, amtPrs, expenseNotes)
                                    isAddingExpense = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = ObsidianBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("COMMIT EXPENDITURE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Expenses History Records
        if (expenses.isNotEmpty()) {
            item {
                Text(
                    text = "HISTORICAL LEDGER STATEMENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoltGreen,
                    letterSpacing = 1.sp
                )
            }

            items(expenses) { exp ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardGray)
                        .border(1.dp, CardGrayBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VoltGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = exp.category.uppercase(), color = VoltGreen, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = exp.date, color = TextGrayMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = if (exp.notes.isNotEmpty()) exp.notes else "Custom Expense Log", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "$${"%.2f".format(exp.amount)}", color = VoltGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.deleteExpenseRecord(exp.id) }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = CyberPink, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// Proportional Stacked Segment Bar indicator
@Composable
fun CategoryProportionalBar(categoryTotals: Map<String, Double>) {
    val total = categoryTotals.values.sum().coerceAtLeast(1.0)
    
    val colors = listOf(VoltGreen, NeonCyan, CyberPink, SoftEmerald, MutedBlue, Color(0xFFD946EF), Color(0xFFF59E0B))
    val categoryList = categoryTotals.toList().sortedByDescending { it.second }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(CardGrayBorder)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                categoryList.forEachIndexed { idx, entry ->
                    val weight = (entry.second / total).toFloat()
                    if (weight > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(weight.coerceAtLeast(0.01f))
                                .background(colors[idx % colors.size])
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend grids
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        ) {
            categoryList.forEachIndexed { idx, entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(colors[idx % colors.size], CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entry.first}: $${"%.2f".format(entry.second)}",
                        fontSize = 10.sp,
                        color = TextWhite
                    )
                }
            }
        }
    }
}


// ==========================================
// 7. AI COACH & PROFILE SETTINGS
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AIAndProfileScreen(viewModel: LifeViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiCoachSuggestion by viewModel.aiCoachSuggestion.collectAsStateWithLifecycle()

    val profSafe = profile ?: UserProfile()

    // Forms profile states
    var editUsername by remember { mutableStateOf(profSafe.username) }
    var editPIN by remember { mutableStateOf(profSafe.pin) }
    var editWeight by remember { mutableStateOf(profSafe.weightKg.toString()) }
    var editHeight by remember { mutableStateOf(profSafe.heightCm.toString()) }
    var editAge by remember { mutableStateOf(profSafe.age.toString()) }
    var editStepGoal by remember { mutableStateOf(profSafe.dailyStepGoal.toString()) }
    var editCardioGoal by remember { mutableStateOf(profSafe.dailyCardioGoalMinutes.toString()) }
    var editCalorieGoal by remember { mutableStateOf(profSafe.dailyCalorieGoal.toString()) }
    var editWeeklyBudget by remember { mutableStateOf(profSafe.weeklyBudgetLimit.toString()) }
    var editWaterGoal by remember { mutableStateOf(profSafe.waterGoalMl.toString()) }
    var editSleepGoal by remember { mutableStateOf(profSafe.sleepGoalHours.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "DISCIPLINE COACH & TARGETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoltGreen,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "AI Coach & Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PureWhite
                )
            }
        }

        // AI Discipline Coach Card Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray),
                border = BorderStroke(1.dp, VoltGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Insights,
                                contentDescription = null,
                                tint = VoltGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI EXECUTIVE COACH PROTOCOL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VoltGreen,
                                letterSpacing = 1.sp
                            )
                        }

                        if (isAiLoading) {
                            CircularProgressIndicator(
                                color = VoltGreen,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = aiCoachSuggestion,
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBg)
                            .border(1.dp, CardGrayBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.clickGenerateCoachReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = ObsidianBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GENERATE COACH REPORT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Profile Targets forms edit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray),
                border = BorderStroke(1.dp, CardGrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MODIFY CRITICAL GOALS & CONFIGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGrayMuted,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Operator Name / Call-Sign") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editPIN,
                            onValueChange = { editPIN = it },
                            label = { Text("Secure PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.weight(1.2f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                        )

                        OutlinedTextField(
                            value = editAge,
                            onValueChange = { editAge = it },
                            label = { Text("Age (yrs)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editWeight,
                            onValueChange = { editWeight = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                        )

                        OutlinedTextField(
                            value = editHeight,
                            onValueChange = { editHeight = it },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editStepGoal,
                        onValueChange = { editStepGoal = it },
                        label = { Text("Cardio Step Goals / Day") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editCardioGoal,
                            onValueChange = { editCardioGoal = it },
                            label = { Text("Cardio Limit (Mins)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                        )

                        OutlinedTextField(
                            value = editCalorieGoal,
                            onValueChange = { editCalorieGoal = it },
                            label = { Text("Diet Cap (kcal)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editWeeklyBudget,
                            onValueChange = { editWeeklyBudget = it },
                            label = { Text("Weekly Spend Cap") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                        )

                        OutlinedTextField(
                            value = editWaterGoal,
                            onValueChange = { editWaterGoal = it },
                            label = { Text("Water Limit (ml)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editSleepGoal,
                        onValueChange = { editSleepGoal = it },
                        label = { Text("Sleep Duration Target (hrs)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VoltGreen, focusedLabelColor = VoltGreen)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateProfile(
                                username = editUsername,
                                weight = editWeight.toDoubleOrNull() ?: 75.0,
                                height = editHeight.toDoubleOrNull() ?: 175.0,
                                age = editAge.toIntOrNull() ?: 25,
                                stepGoal = editStepGoal.toIntOrNull() ?: 10000,
                                cardioGoal = editCardioGoal.toIntOrNull() ?: 30,
                                calorieGoal = editCalorieGoal.toIntOrNull() ?: 2200,
                                pGoal = profSafe.proteinGoalGrams,
                                cGoal = profSafe.carbsGoalGrams,
                                fGoal = profSafe.fatsGoalGrams,
                                budget = editWeeklyBudget.toDoubleOrNull() ?: 250.0,
                                waterLimit = editWaterGoal.toIntOrNull() ?: 2500,
                                sleepHours = editSleepGoal.toDoubleOrNull() ?: 8.0,
                                newPin = editPIN
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VoltGreen, contentColor = ObsidianBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("COMMIT TARGET ADJUSTMENTS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Log out / Lock Center
        item {
            Button(
                onClick = { viewModel.lockProfile() },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPink, contentColor = TextWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Lock, contentDescription = "Lock device")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "LOCK SYSTEM DATA ACCESS", fontWeight = FontWeight.Bold)
            }
        }
    }
}
