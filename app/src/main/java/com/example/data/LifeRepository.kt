package com.example.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class LifeRepository(private val db: LifeDatabase) {

    // --- User Profile ---
    val userProfile: Flow<UserProfile?> = db.userProfileDao.getUserProfile()
    
    suspend fun getProfileSync(): UserProfile? {
        return db.userProfileDao.getUserProfileSync()
    }

    suspend fun saveProfile(profile: UserProfile) {
        db.userProfileDao.insertUserProfile(profile)
    }

    // --- Workouts & Exercises ---
    val allWorkouts: Flow<List<WorkoutLog>> = db.workoutDao.getAllWorkouts()

    suspend fun logWorkout(workout: WorkoutLog): Long {
        return db.workoutDao.insertWorkout(workout)
    }

    suspend fun deleteWorkout(id: Int) {
        db.workoutDao.deleteWorkoutById(id)
        db.workoutDao.deleteExercisesByWorkoutId(id)
    }

    fun getExercisesForWorkout(workoutId: Int): Flow<List<ExerciseLog>> {
        return db.workoutDao.getExercisesForWorkout(workoutId)
    }

    suspend fun addExerciseLog(exercise: ExerciseLog) {
        db.workoutDao.insertExercise(exercise)
    }

    suspend fun deleteExercise(id: Int) {
        db.workoutDao.deleteExerciseById(id)
    }

    // --- Cardio Logs ---
    val allCardioLogs: Flow<List<CardioLog>> = db.cardioDao.getAllCardioLogs()

    suspend fun logCardio(log: CardioLog) {
        db.cardioDao.insertCardioLog(log)
    }

    suspend fun deleteCardio(id: Int) {
        db.cardioDao.deleteCardioLogById(id)
    }

    // --- Steps (Daily Logs) ---
    val allSteps: Flow<List<StepLog>> = db.stepDao.getAllSteps()

    fun getStepsForDate(date: String): Flow<StepLog?> {
        return db.stepDao.getStepsForDate(date)
    }

    suspend fun updateSteps(date: String, steps: Int) {
        val existing = db.stepDao.getStepsForDateSync(date)
        val newSteps = if (existing != null) steps else steps // Direct update
        db.stepDao.insertStepLog(StepLog(date = date, steps = newSteps))
    }

    // --- Diet Logs ---
    val allDietLogs: Flow<List<DietLog>> = db.dietDao.getAllDietLogs()

    fun getDietLogsForDate(date: String): Flow<List<DietLog>> {
        return db.dietDao.getDietLogsForDate(date)
    }

    suspend fun addDietLog(log: DietLog) {
        db.dietDao.insertDietLog(log)
    }

    suspend fun deleteDietLog(id: Int) {
        db.dietDao.deleteDietLogById(id)
    }

    // --- Water Logs ---
    val allWaterLogs: Flow<List<WaterLog>> = db.waterDao.getAllWaterLogs()

    fun getWaterLogsForDate(date: String): Flow<List<WaterLog>> {
        return db.waterDao.getWaterLogsForDate(date)
    }

    suspend fun addWaterMl(date: String, amountMl: Int) {
        db.waterDao.insertWaterLog(WaterLog(date = date, amountMl = amountMl))
    }

    suspend fun deleteWaterLog(id: Int) {
        db.waterDao.deleteWaterLogById(id)
    }

    // --- Sleep Logs ---
    val allSleepLogs: Flow<List<SleepLog>> = db.sleepDao.getAllSleepLogs()

    suspend fun logSleep(date: String, hours: Double, quality: String) {
        db.sleepDao.insertSleepLog(SleepLog(date = date, hours = hours, quality = quality))
    }

    // --- Habits ---
    val allHabits: Flow<List<Habit>> = db.habitDao.getAllHabits()
    val allHabitHistory: Flow<List<HabitHistory>> = db.habitDao.getHabitHistory()

    suspend fun addHabit(habit: Habit) {
        db.habitDao.insertHabit(habit)
    }

    suspend fun deleteHabit(habitId: Int) {
        db.habitDao.deleteHabitById(habitId)
        db.habitDao.deleteHabitHistoryByHabitId(habitId)
    }

    suspend fun toggleHabitCompletion(habitId: Int, date: String) {
        val existingHistory = db.habitDao.getHistoryForHabitAndDate(habitId, date)
        val habit = db.habitDao.getHabitById(habitId) ?: return

        if (existingHistory != null) {
            // Undo completion
            db.habitDao.deleteHabitHistory(habitId, date)
            
            // Recalculate streaks (simplified revert)
            val newStreak = maxOf(0, habit.currentStreak - 1)
            val newTotal = maxOf(0, habit.totalCompletions - 1)
            
            db.habitDao.insertHabit(
                habit.copy(
                    currentStreak = newStreak,
                    totalCompletions = newTotal,
                    lastCompletedDate = "" // Simple fallback, actual calculation on next check
                )
            )
        } else {
            // Check off habit
            db.habitDao.insertHabitHistory(HabitHistory(habitId = habitId, date = date))

            // Calculate new streak based on previous last completed date
            var newStreak = 1
            if (habit.lastCompletedDate.isNotEmpty()) {
                val lastDate = LocalDate.parse(habit.lastCompletedDate)
                val currentDate = LocalDate.parse(date)
                val daysBetween = ChronoUnit.DAYS.between(lastDate, currentDate)
                if (daysBetween == 1L) {
                    newStreak = habit.currentStreak + 1
                } else if (daysBetween == 0L) {
                    newStreak = habit.currentStreak // same day, no change
                }
            }

            val newTotal = habit.totalCompletions + 1
            val newMax = maxOf(habit.maxStreak, newStreak)

            db.habitDao.insertHabit(
                habit.copy(
                    currentStreak = newStreak,
                    maxStreak = newMax,
                    totalCompletions = newTotal,
                    lastCompletedDate = date
                )
            )
        }
    }

    // --- Expenses ---
    val allExpenses: Flow<List<Expense>> = db.expenseDao.getAllExpenses()

    fun getExpensesForCategory(category: String): Flow<List<Expense>> {
        return db.expenseDao.getExpensesForCategory(category)
    }

    suspend fun addExpense(expense: Expense) {
        db.expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(id: Int) {
        db.expenseDao.deleteExpenseById(id)
    }
}
