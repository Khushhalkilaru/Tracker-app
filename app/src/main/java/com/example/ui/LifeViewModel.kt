package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class LifeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LifeDatabase.getDatabase(application)
    private val repository = LifeRepository(db)

    // --- Current Date Filter ---
    val currentDateStr = MutableStateFlow(LocalDate.now().toString())

    // --- State Streams ---
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allWorkouts: StateFlow<List<WorkoutLog>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCardioLogs: StateFlow<List<CardioLog>> = repository.allCardioLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSteps: StateFlow<List<StepLog>> = repository.allSteps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabitHistory: StateFlow<List<HabitHistory>> = repository.allHabitHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDietLogs: StateFlow<List<DietLog>> = repository.allDietLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWaterLogs: StateFlow<List<WaterLog>> = repository.allWaterLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSleepLogs: StateFlow<List<SleepLog>> = repository.allSleepLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interactive Selected Screen ---
    val activeTabStr = MutableStateFlow("dashboard")

    // --- AI Coach Responses ---
    val aiCoachSuggestion = MutableStateFlow("Click 'Generate Coach Report' to load tough-love discipline tips based on your metrics.")
    val isAiLoading = MutableStateFlow(false)

    // --- User Session / Lock State ---
    val isUserUnlocked = MutableStateFlow(false)

    init {
        // Run seeding in a coroutine
        viewModelScope.launch(Dispatchers.IO) {
            checkAndSeedDatabase()
        }
    }

    // --- SEED DATABASE ON LAUNCH ---
    private suspend fun checkAndSeedDatabase() {
        val currentProfile = repository.getProfileSync()
        if (currentProfile == null) {
            // Write default user profile
            repository.saveProfile(
                UserProfile(
                    id = 1,
                    username = "AlphaSelf",
                    pin = "1234",
                    weightKg = 82.5,
                    heightCm = 180.0,
                    age = 27,
                    dailyStepGoal = 10000,
                    dailyCardioGoalMinutes = 30,
                    dailyCalorieGoal = 2400,
                    proteinGoalGrams = 175,
                    carbsGoalGrams = 230,
                    fatsGoalGrams = 75,
                    weeklyBudgetLimit = 200.0,
                    waterGoalMl = 3000,
                    sleepGoalHours = 8.0,
                    isLoggedIn = true
                )
            )

            val today = LocalDate.now()

            // Seed habits
            repository.addHabit(Habit(id = 1, name = "Wake up at 5:00 AM", category = "Discipline"))
            repository.addHabit(Habit(id = 2, name = "Clean lifting & diet check", category = "Health"))
            repository.addHabit(Habit(id = 3, name = "Read 10 pages in marketing", category = "Mind"))
            repository.addHabit(Habit(id = 4, name = "No alcohol or fast food", category = "Discipline"))

            // Seed habit histories for backdates (7-day heatmap generation)
            for (i in 1..7) {
                val d = today.minusDays(i.toLong()).toString()
                if (i != 3 && i != 5) {
                    repository.toggleHabitCompletion(1, d)
                }
                if (i != 4) {
                    repository.toggleHabitCompletion(2, d)
                }
                repository.toggleHabitCompletion(3, d)
                if (i != 1) {
                    repository.toggleHabitCompletion(4, d)
                }
            }

            // Seed steps
            for (i in 0..7) {
                val d = today.minusDays(i.toLong()).toString()
                val stepval = if (i % 2 == 0) 10500 else 8900
                repository.updateSteps(d, stepval)
            }

            // Seed sleep logs
            for (i in 0..7) {
                val d = today.minusDays(i.toLong()).toString()
                val hrs = if (i % 3 == 0) 6.5 else if (i % 3 == 1) 8.2 else 7.5
                val q = if (hrs >= 8) "Good" else if (hrs >= 7) "Fair" else "Poor"
                repository.logSleep(d, hrs, q)
            }

            // Seed water logs for today & yesterday
            repository.addWaterMl(today.toString(), 2500)
            repository.addWaterMl(today.minusDays(1).toString(), 3000)

            // Seed meals
            val breakfast = DietLog(date = today.toString(), name = "Oats & Scrambled Eggs", mealType = "Breakfast", calories = 550, proteinGrams = 35, carbsGrams = 50, fatsGrams = 18)
            val lunch = DietLog(date = today.toString(), name = "Grilled Chicken Breast, Rice, Veg", mealType = "Lunch", calories = 700, proteinGrams = 60, carbsGrams = 75, fatsGrams = 12)
            val dinner = DietLog(date = today.toString(), name = "Salmon with Sweet Potato", mealType = "Dinner", calories = 650, proteinGrams = 45, carbsGrams = 45, fatsGrams = 22)
            val snack = DietLog(date = today.toString(), name = "Whey Protein Shake", mealType = "Snack", calories = 250, proteinGrams = 30, carbsGrams = 8, fatsGrams = 3)

            repository.addDietLog(breakfast)
            repository.addDietLog(lunch)
            repository.addDietLog(dinner)
            repository.addDietLog(snack)

            // Seed some expenses
            val ex1 = Expense(date = today.toString(), category = "Food", amount = 34.50, notes = "Weekly meal prep groceries")
            val ex2 = Expense(date = today.minusDays(2).toString(), category = "Gym", amount = 45.00, notes = "Monthly lifting subscription")
            val ex3 = Expense(date = today.minusDays(3).toString(), category = "Shopping", amount = 22.80, notes = "Weight lifting straps")
            val ex4 = Expense(date = today.minusDays(4).toString(), category = "Bills", amount = 15.00, notes = "Water purifier filter")

            repository.addExpense(ex1)
            repository.addExpense(ex2)
            repository.addExpense(ex3)
            repository.addExpense(ex4)

            // Seed a completed workout
            val workoutId1 = repository.logWorkout(
                WorkoutLog(date = today.minusDays(1).toString(), name = "Hypertrophy Push Day", durationMinutes = 75)
            )
            repository.addExerciseLog(ExerciseLog(workoutLogId = workoutId1.toInt(), name = "Incline DB Press", category = "Chest", setsText = "12 reps x 32 kg, 10 reps x 36 kg, 8 reps x 40 kg"))
            repository.addExerciseLog(ExerciseLog(workoutLogId = workoutId1.toInt(), name = "Overhead Barbell Press", category = "Shoulders", setsText = "10 reps x 50 kg, 8 reps x 60 kg"))
            repository.addExerciseLog(ExerciseLog(workoutLogId = workoutId1.toInt(), name = "Triceps Rope Pushdowns", category = "Triceps", setsText = "12 reps x 30 kg, 12 reps x 35 kg"))

            // Seed today's pending workout
            val workoutId2 = repository.logWorkout(
                WorkoutLog(date = today.toString(), name = "Pull Power Day", durationMinutes = 60, completed = false)
            )
            repository.addExerciseLog(ExerciseLog(workoutLogId = workoutId2.toInt(), name = "Weighted Pull Ups", category = "Back", setsText = "8 reps x 15 kg, 8 reps x 15 kg, 6 reps x 20 kg"))
            repository.addExerciseLog(ExerciseLog(workoutLogId = workoutId2.toInt(), name = "Barbell Bent Over Row", category = "Back", setsText = "10 reps x 80 kg, 8 reps x 90 kg"))
            repository.addExerciseLog(ExerciseLog(workoutLogId = workoutId2.toInt(), name = "Incline DB Dumbbell Curls", category = "Biceps", setsText = "12 reps x 16 kg, 10 reps x 18 kg"))

            // Seed some cardio
            repository.logCardio(CardioLog(date = today.minusDays(2).toString(), activityType = "Running", minutes = 35, distanceKm = 5.2, caloriesBurned = 380.0))
            repository.logCardio(CardioLog(date = today.minusDays(4).toString(), activityType = "Stationary Cycling", minutes = 45, distanceKm = 15.0, caloriesBurned = 420.0))
        }
    }

    // --- INTERACTIVE HANDLERS ---
    fun getWorkoutExercisesFlow(workoutId: Int): Flow<List<ExerciseLog>> {
        return repository.getExercisesForWorkout(workoutId)
    }

    fun updateDayValue(date: String) {
        currentDateStr.value = date
    }

    fun selectNavigationTab(tab: String) {
        activeTabStr.value = tab
    }

    fun tryPinUnlock(inputPin: String): Boolean {
        // Seed default check
        val currentProfile = userProfile.value
        val correctPin = currentProfile?.pin ?: "1234"
        return if (inputPin == correctPin) {
            isUserUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockProfile() {
        isUserUnlocked.value = false
    }

    fun updateProfile(username: String, weight: Double, height: Double, age: Int, stepGoal: Int, cardioGoal: Int, calorieGoal: Int, pGoal: Int, cGoal: Int, fGoal: Int, budget: Double, waterLimit: Int, sleepHours: Double, newPin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val prof = UserProfile(
                id = 1,
                username = username,
                weightKg = weight,
                heightCm = height,
                age = age,
                dailyStepGoal = stepGoal,
                dailyCardioGoalMinutes = cardioGoal,
                dailyCalorieGoal = calorieGoal,
                proteinGoalGrams = pGoal,
                carbsGoalGrams = cGoal,
                fatsGoalGrams = fGoal,
                weeklyBudgetLimit = budget,
                waterGoalMl = waterLimit,
                sleepGoalHours = sleepHours,
                pin = newPin,
                isLoggedIn = true
            )
            repository.saveProfile(prof)
        }
    }

    fun logWorkoutCompleted(name: String, duration: Int, exercisePayloads: List<Pair<String, Pair<String, String>>>) {
        viewModelScope.launch(Dispatchers.IO) {
            val workoutId = repository.logWorkout(
                WorkoutLog(
                    date = currentDateStr.value,
                    name = name,
                    durationMinutes = duration,
                    completed = true
                )
            )
            exercisePayloads.forEach { payload ->
                repository.addExerciseLog(
                    ExerciseLog(
                        workoutLogId = workoutId.toInt(),
                        name = payload.first,
                        category = payload.second.first,
                        setsText = payload.second.second
                    )
                )
            }
        }
    }

    fun toggleWorkoutState(workoutId: Int, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = allWorkouts.value
            val target = list.find { it.id == workoutId }
            if (target != null) {
                repository.logWorkout(target.copy(completed = isCompleted))
            }
        }
    }

    fun deleteWorkoutInstance(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorkout(id)
        }
    }

    fun addCardioSession(activityType: String, minutes: Int, kms: Double, calories: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.logCardio(
                CardioLog(
                    date = currentDateStr.value,
                    activityType = activityType,
                    minutes = minutes,
                    distanceKm = kms,
                    caloriesBurned = calories
                )
            )
        }
    }

    fun deleteCardioSession(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCardio(id)
        }
    }

    fun logDailySteps(steps: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSteps(currentDateStr.value, steps)
        }
    }

    fun logNewMeal(name: String, type: String, calories: Int, protein: Int, carbs: Int, fats: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addDietLog(
                DietLog(
                    date = currentDateStr.value,
                    name = name,
                    mealType = type,
                    calories = calories,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatsGrams = fats
                )
            )
        }
    }

    fun deleteMealLog(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDietLog(id)
        }
    }

    fun addWaterLogMl(amountMl: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addWaterMl(currentDateStr.value, amountMl)
        }
    }

    fun editSleepRecord(hours: Double, quality: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.logSleep(currentDateStr.value, hours, quality)
        }
    }

    fun addCustomHabit(name: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addHabit(Habit(name = name, category = category))
        }
    }

    fun deleteHabitInstance(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHabit(id)
        }
    }

    fun toggleHabitState(habitId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleHabitCompletion(habitId, currentDateStr.value)
        }
    }

    fun addExpenseRecord(category: String, amount: Double, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addExpense(
                Expense(
                    date = currentDateStr.value,
                    category = category,
                    amount = amount,
                    notes = notes
                )
            )
        }
    }

    fun deleteExpenseRecord(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(id)
        }
    }


    // --- DYNAMIC AI DISCIPLINE COACH WITH GEMINI API (OPTION B CLIENT) ---
    fun clickGenerateCoachReport() {
        val wkList = allWorkouts.value
        val exList = allExpenses.value
        val mealList = allDietLogs.value
        val hbtList = allHabits.value
        val hbtHistoryList = allHabitHistory.value
        val stepList = allSteps.value
        val prof = userProfile.value ?: UserProfile()

        isAiLoading.value = true

        viewModelScope.launch(Dispatchers.Default) {
            // Build metrics summary text for prompt
            val lastWeekWorkouts = wkList.filter { LocalDate.parse(it.date).isAfter(LocalDate.now().minusDays(8)) }
            val completedWorkouts = lastWeekWorkouts.count { it.completed }
            val totalSpentThisWeek = exList.filter { LocalDate.parse(it.date).isAfter(LocalDate.now().minusDays(8)) }.sumOf { it.amount }
            val totalCaloriesToday = mealList.filter { it.date == LocalDate.now().toString() }.sumOf { it.calories }
            val totalProteinToday = mealList.filter { it.date == LocalDate.now().toString() }.sumOf { it.proteinGrams }
            val completedHabitsTodayCount = hbtHistoryList.filter { it.date == LocalDate.now().toString() }.size
            val todaySteps = stepList.find { it.date == LocalDate.now().toString() }?.steps ?: 0

            val summaryPrompt = """
                You are a premium, tough-love military-discipline AI Personal Executive Coach for an app called LifeOS. 
                Your purpose is to look at the user's progress metrics and tell them exactly where they need to do better. 
                Be highly encouraging but completely direct, honest, actionable, and brutal if necessary. Do not waffle. 
                
                USER'S WEEKLY TRUTH:
                - Target Steps/Day: ${prof.dailyStepGoal} | Today's Steps: $todaySteps
                - Target Sleep/Night: ${prof.sleepGoalHours} hours
                - Weekly Spending Limit: $${prof.weeklyBudgetLimit} | Tracked Expenses this week: $${"%.2f".format(totalSpentThisWeek)}
                - Completed Workouts: $completedWorkouts out of ${lastWeekWorkouts.size} logged sessions
                - Daily Calorie Target: ${prof.dailyCalorieGoal} kcal | Today's Intake: $totalCaloriesToday kcal (Protein: ${totalProteinToday}g / Goal: ${prof.proteinGoalGrams}g)
                - Habits Check-off Today: $completedHabitsTodayCount out of ${hbtList.size} defined habits.

                Write an executive, high-impact report. Organize it into 3 clear headings:
                1. ⚔️ CRITICAL DEFICITS (Where the user is failing or slipping in self-control)
                2. 🔋 MOMENTUM MARKS (Where the user is demonstrating true consistency)
                3. 🛡️ TACTICAL COMMANDS (3 atomic, immediate actions they must execute in the next 24 hours to maximize performance)
                
                Format as crisp, clean prose with line-breaks between the bullet sections. Keep it concise (no more than 200 words total).
            """.trimIndent()

            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotEmpty() && key != "MY_GEMINI_API_KEY") {
                try {
                    val result = callGeminiRestApi(key, summaryPrompt)
                    withContext(Dispatchers.Main) {
                        aiCoachSuggestion.value = result
                        isAiLoading.value = false
                    }
                } catch (e: Exception) {
                    val fallback = generateLocalRuleBasedCoachReport(
                        todaySteps, prof.dailyStepGoal, totalSpentThisWeek, prof.weeklyBudgetLimit,
                        completedWorkouts, lastWeekWorkouts.size, totalCaloriesToday, prof.dailyCalorieGoal,
                        totalProteinToday, prof.proteinGoalGrams, completedHabitsTodayCount, hbtList.size
                    )
                    withContext(Dispatchers.Main) {
                        aiCoachSuggestion.value = "$fallback\n\n(Note: Fallback offline report shown due to network/API status: ${e.localizedMessage})"
                        isAiLoading.value = false
                    }
                }
            } else {
                // Rule-based prompt generator fallback
                val ruleFeedback = generateLocalRuleBasedCoachReport(
                    todaySteps, prof.dailyStepGoal, totalSpentThisWeek, prof.weeklyBudgetLimit,
                    completedWorkouts, lastWeekWorkouts.size, totalCaloriesToday, prof.dailyCalorieGoal,
                    totalProteinToday, prof.proteinGoalGrams, completedHabitsTodayCount, hbtList.size
                )
                TimeUnit.MILLISECONDS.sleep(1200) // Simulate analytical load
                withContext(Dispatchers.Main) {
                    aiCoachSuggestion.value = ruleFeedback
                    isAiLoading.value = false
                }
            }
        }
    }

    private suspend fun callGeminiRestApi(apiKey: String, promptText: String): String = withContext(Dispatchers.IO) {
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Create Request Payload JSON Object manually to avoid Kotlin-Serialization version collisions
        val requestJson = JSONObject().apply {
            val contentsArr = JSONArray().apply {
                val turnObj = JSONObject().apply {
                    val partsArr = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", promptText)
                        }
                        put(partObj)
                    }
                    put("parts", partsArr)
                }
                put(turnObj)
            }
            put("contents", contentsArr)
        }

        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val httprequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val httpresponse = client.newCall(httprequest).execute()
        if (httpresponse.isSuccessful) {
            val rawBodyString = httpresponse.body?.string() ?: throw Exception("Empty model response")
            val rootObj = JSONObject(rawBodyString)
            val candidates = rootObj.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val firstPart = parts.getJSONObject(0)
            firstPart.getString("text")
        } else {
            throw Exception("HTTP Error: Code ${httpresponse.code} ${httpresponse.message}")
        }
    }

    private fun generateLocalRuleBasedCoachReport(
        todaySteps: Int, stepGoal: Int,
        totalSpent: Double, weeklyLimit: Double,
        workoutsCompleted: Int, totalWorkouts: Int,
        todayCalories: Int, calorieGoal: Int,
        protein: Int, proteinGoal: Int,
        habitsDone: Int, habitsTotal: Int
    ): String {
        val sb = StringBuilder()
        sb.append("⚔️ CRITICAL DEFICITS:\n")
        var deficitsCount = 0
        if (todaySteps < stepGoal) {
            sb.append("- Steps level at $todaySteps. Low output. Go move your feet to unlock cognitive clarity.\n")
            deficitsCount++
        }
        if (totalSpent > weeklyLimit) {
            sb.append("- Spending has exceeded weekly budget limit by $${"%.2f".format(totalSpent - weeklyLimit)}! Lock down card logs instantly.\n")
            deficitsCount++
        }
        if (todayCalories > calorieGoal + 200) {
            sb.append("- Calorie intake is $todayCalories kcal, exceeding goal. Your self-discipline isn't a food playground. Refine macros.\n")
            deficitsCount++
        } else if (todayCalories < calorieGoal - 500 && todayCalories > 0) {
            sb.append("- Calorie intake is dangerously low ($todayCalories kcal). Fuel the machine for muscle retention.\n")
            deficitsCount++
        }
        if (protein < proteinGoal) {
            sb.append("- Protein is deficient (${protein}g logged vs ${proteinGoal}g goal). You are wasting muscle recovery potential.\n")
            deficitsCount++
        }
        if (habitsDone < habitsTotal && habitsTotal > 0) {
            sb.append("- Incomplete habits detected. Habits represent identity. Broken promises trigger mental rust.\n")
            deficitsCount++
        }
        if (workoutsCompleted < totalWorkouts) {
            sb.append("- You have outstanding pending training sessions in the gym queues.\n")
            deficitsCount++
        }

        if (deficitsCount == 0) {
            sb.append("- Zero critical deficits! Excellent tactical composure across all tracking lines.\n")
        }

        sb.append("\n🔋 MOMENTUM MARKS:\n")
        var momentumCount = 0
        if (todaySteps >= stepGoal) {
            sb.append("- Step targets fully secured today! Cardio levels remain active.\n")
            momentumCount++
        }
        if (totalSpent <= weeklyLimit) {
            sb.append("- Strict financial compliance. Weekly capital reserves remain operational ($${"%.2f".format(weeklyLimit - totalSpent)} left).\n")
            momentumCount++
        }
        if (workoutsCompleted > 0 && workoutsCompleted == totalWorkouts) {
            sb.append("- Flawless physical consistency. All training protocols hit successfully.\n")
            momentumCount++
        }
        if (habitsDone == habitsTotal && habitsTotal > 0) {
            sb.append("- Habit queues fully checked. Your daily discipline integrity is solid.\n")
            momentumCount++
        }
        if (momentumCount == 0) {
            sb.append("- No substantial execution spikes. Wake up, tighten up, and conquer today.\n")
        }

        sb.append("\n🛡️ TACTICAL COMMANDS:\n")
        if (todaySteps < stepGoal) {
            sb.append("1. Lace up your shoes and hit a fast 20-minute power walk right now.\n")
        } else {
            sb.append("1. Perform a 10-minute dynamic flexibility flow to maximize structural recovery.\n")
        }
        if (protein < proteinGoal) {
            sb.append("2. Consume 40 grams of high-absorption amino acids immediately (whey, lean meat, or eggs).\n")
        } else {
            sb.append("2. Plan and pack tomorrow's meals in containers tonight to avoid impulsive fast food traps.\n")
        }
        if (totalSpent > weeklyLimit) {
            sb.append("3. Initiate a 3-day 'Zero Spend' defense protocol. Only electricity, water, and meal preps are authorized.")
        } else {
            sb.append("3. Log all pending receipts instantly and allocate 20% of remaining capital directly into investments.")
        }

        return sb.toString()
    }

    // --- DIET PORTION ESTIMATION ENG DESIGN ---
    data class NutritionEstimation(
        val calories: Int,
        val protein: Int,
        val carbs: Int,
        val fats: Int,
        val explanation: String? = null
    )

    val isEstimatingNutrition = MutableStateFlow(false)
    val nutritionEstimationResult = MutableStateFlow<NutritionEstimation?>(null)

    // Indian Food Offline Dictionary
    private val IndianFoodDictionary = listOf(
        NutritionEstimation(350, 12, 10, 28, "Paneer Butter Masala"),
        NutritionEstimation(280, 15, 8, 20, "Paneer Tikka"),
        NutritionEstimation(320, 13, 9, 25, "Kadai Paneer"),
        NutritionEstimation(220, 10, 8, 16, "Palak Paneer"),
        NutritionEstimation(400, 28, 12, 26, "Butter Chicken"),
        NutritionEstimation(380, 26, 11, 24, "Chicken Tikka Masala"),
        NutritionEstimation(280, 24, 8, 16, "Chicken Curry"),
        NutritionEstimation(220, 13, 8, 15, "Egg Curry"),
        NutritionEstimation(200, 14, 4, 15, "Egg Bhurji"),
        NutritionEstimation(250, 9, 30, 11, "Dal Makhani"),
        NutritionEstimation(150, 7, 22, 4, "Dal Tadka"),
        NutritionEstimation(160, 7, 23, 4, "Dal Fry"),
        NutritionEstimation(85, 3, 18, 1, "Roti"),
        NutritionEstimation(85, 3, 18, 1, "Chapati"),
        NutritionEstimation(80, 3, 17, 0, "Phulka"),
        NutritionEstimation(260, 6, 45, 6, "Butter Naan"),
        NutritionEstimation(220, 5, 42, 2, "Plain Naan"),
        NutritionEstimation(110, 4, 22, 1, "Tandoori Roti"),
        NutritionEstimation(210, 4, 32, 7, "Aloo Paratha"),
        NutritionEstimation(280, 11, 35, 10, "Paneer Paratha"),
        NutritionEstimation(190, 4, 30, 5, "Gobi Paratha"),
        NutritionEstimation(180, 3, 28, 6, "Plain Paratha"),
        NutritionEstimation(350, 6, 55, 12, "Masala Dosa"),
        NutritionEstimation(220, 4, 42, 5, "Plain Dosa"),
        NutritionEstimation(60, 2, 12, 0, "Idli"),
        NutritionEstimation(120, 3, 15, 8, "Vada"),
        NutritionEstimation(80, 3, 14, 2, "Sambar"),
        NutritionEstimation(90, 1, 4, 8, "Coconut Chutney"),
        NutritionEstimation(200, 4, 35, 4, "Upma"),
        NutritionEstimation(220, 4, 40, 5, "Poha"),
        NutritionEstimation(450, 12, 60, 18, "Chole Bhature"),
        NutritionEstimation(180, 7, 28, 5, "Chole Masala"),
        NutritionEstimation(350, 6, 48, 14, "Pav Bhaji"),
        NutritionEstimation(450, 18, 65, 14, "Biryani"),
        NutritionEstimation(520, 28, 68, 16, "Chicken Biryani"),
        NutritionEstimation(580, 26, 68, 22, "Mutton Biryani"),
        NutritionEstimation(380, 10, 62, 10, "Veg Biryani"),
        NutritionEstimation(130, 3, 28, 0, "Steamed Basmati Rice"),
        NutritionEstimation(170, 3, 34, 3, "Jeera Rice"),
        NutritionEstimation(220, 7, 40, 3, "Khichdi"),
        NutritionEstimation(150, 3, 20, 7, "Samosa"),
        NutritionEstimation(140, 3, 16, 7, "Aloo Gobi"),
        NutritionEstimation(130, 2, 12, 8, "Bhindi Masala"),
        NutritionEstimation(160, 6, 26, 4, "Chana Masala"),
        NutritionEstimation(150, 2, 25, 5, "Gulab Jamun"),
        NutritionEstimation(120, 2, 26, 1, "Rasgulla"),
        NutritionEstimation(150, 1, 30, 3, "Jalebi"),
        NutritionEstimation(250, 4, 35, 10, "Gajar Halwa"),
        NutritionEstimation(60, 2, 10, 2, "Dhokla"),
        NutritionEstimation(200, 6, 28, 6, "Lassi"),
        NutritionEstimation(40, 2, 4, 1, "Chaas"),
        NutritionEstimation(60, 3, 5, 3, "Mixed Raita"),
        NutritionEstimation(550, 35, 50, 18, "Oats & Scrambled Eggs"),
        NutritionEstimation(350, 45, 5, 6, "Grilled Chicken Breast"),
        NutritionEstimation(250, 30, 8, 3, "Whey Protein Shake")
    )

    fun estimateNutritionLocal(name: String): NutritionEstimation? {
        val text = name.lowercase().trim()
        if (text.isEmpty()) return null

        val directMatch = IndianFoodDictionary.find { it.explanation?.lowercase() == text }
        if (directMatch != null) return directMatch

        var totalCalories = 0
        var totalProtein = 0
        var totalCarbs = 0
        var totalFats = 0
        var matchedCount = 0

        var remainingText = text
        val numberMap = mapOf(
            "one" to "1", "two" to "2", "three" to "3", "four" to "4", "five" to "5",
            "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9", "ten" to "10",
            "double" to "2", "couple" to "2"
        )
        for ((word, digit) in numberMap) {
            remainingText = remainingText.replace("\\b$word\\b".toRegex(), digit)
        }

        val sortedDict = IndianFoodDictionary.sortedByDescending { it.explanation?.length ?: 0 }
        val matchesList = mutableListOf<String>()

        for (food in sortedDict) {
            val foodLower = food.explanation?.lowercase() ?: ""
            if (foodLower.isEmpty()) continue

            if (remainingText.contains(foodLower)) {
                var multiplier = 1.0
                val index = remainingText.indexOf(foodLower)
                val prefix = remainingText.substring(0, index).trim()
                if (prefix.isNotEmpty()) {
                    val wordsInPrefix = prefix.split("\\s+".toRegex())
                    val lastWord = wordsInPrefix.lastOrNull()
                    if (lastWord != null) {
                        val num = lastWord.toDoubleOrNull()
                        if (num != null && num > 0) {
                            multiplier = num
                        }
                    }
                }

                totalCalories += (food.calories * multiplier).toInt()
                totalProtein += (food.protein * multiplier).toInt()
                totalCarbs += (food.carbs * multiplier).toInt()
                totalFats += (food.fats * multiplier).toInt()
                matchedCount++
                matchesList.add("${if (multiplier != 1.0) "${multiplier}x" else ""}${food.explanation}")

                remainingText = remainingText.replace(foodLower, "")
            }
        }

        if (matchedCount > 0) {
            return NutritionEstimation(
                calories = totalCalories,
                protein = totalProtein,
                carbs = totalCarbs,
                fats = totalFats,
                explanation = "Matched: " + matchesList.joinToString(" + ")
            )
        }
        return null
    }

    fun triggerNutritionEstimation(dishName: String) {
        if (dishName.isBlank()) return
        isEstimatingNutrition.value = true

        viewModelScope.launch(Dispatchers.Default) {
            val localResult = estimateNutritionLocal(dishName)
            if (localResult != null) {
                withContext(Dispatchers.Main) {
                    nutritionEstimationResult.value = localResult
                    isEstimatingNutrition.value = false
                }
                return@launch
            }

            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotEmpty() && key != "MY_GEMINI_API_KEY") {
                try {
                    val prompt = """
                        You are an expert nutrition analyst specializing in Indian and global cuisines.
                        Analyze the meal dish description: "$dishName"
                        
                        Provide a realistic, accurate estimation of its total nutritional profile (for reasonable/standard restaurant or home portion sizes if not specified). Focus heavily on standard Indian food preparation metrics.
                        
                        You MUST reply with ONLY a single raw valid JSON block. DO NOT use markdown format, do NOT include ```json or other text.
                        The JSON block format must be EXACTLY:
                        {"calories": 420, "protein": 15, "carbs": 50, "fats": 12, "explanation": "Brief description of dish & portion matched"}
                    """.trimIndent()
                    
                    val aiJsonText = callGeminiRestApi(key, prompt)
                    
                    var cleanJson = aiJsonText.trim()
                    if (cleanJson.startsWith("```")) {
                        cleanJson = cleanJson.substringAfter("\n").substringBeforeLast("```").trim()
                    }
                    if (cleanJson.startsWith("json")) {
                        cleanJson = cleanJson.substring(4).trim()
                    }
                    
                    val jsonObj = JSONObject(cleanJson)
                    val result = NutritionEstimation(
                        calories = jsonObj.optInt("calories", 0),
                        protein = jsonObj.optInt("protein", 0),
                        carbs = jsonObj.optInt("carbs", 0),
                        fats = jsonObj.optInt("fats", 0),
                        explanation = "AI Estimate: " + jsonObj.optString("explanation", "Parsed via AI")
                    )
                    
                    withContext(Dispatchers.Main) {
                        nutritionEstimationResult.value = result
                        isEstimatingNutrition.value = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        nutritionEstimationResult.value = NutritionEstimation(
                            calories = 300,
                            protein = 10,
                            carbs = 40,
                            fats = 10,
                            explanation = "Rough backup estimate (AI failed: ${e.localizedMessage})"
                        )
                        isEstimatingNutrition.value = false
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    nutritionEstimationResult.value = NutritionEstimation(
                        calories = 250,
                        protein = 8,
                        carbs = 35,
                        fats = 8,
                        explanation = "Offline standard estimate (Set Gemini API Key for smart parsing)"
                    )
                    isEstimatingNutrition.value = false
                }
            }
        }
    }

    fun clearNutritionEstimationResult() {
        nutritionEstimationResult.value = null
    }
}
