package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_log ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutLog): Long

    @Query("DELETE FROM workout_log WHERE id = :id")
    suspend fun deleteWorkoutById(id: Int)

    @Query("SELECT * FROM exercise_log WHERE workoutLogId = :workoutId ORDER BY timestamp ASC")
    fun getExercisesForWorkout(workoutId: Int): Flow<List<ExerciseLog>>

    @Query("SELECT * FROM exercise_log WHERE workoutLogId = :workoutId ORDER BY timestamp ASC")
    suspend fun getExercisesForWorkoutSync(workoutId: Int): List<ExerciseLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseLog)

    @Query("DELETE FROM exercise_log WHERE id = :id")
    suspend fun deleteExerciseById(id: Int)

    @Query("DELETE FROM exercise_log WHERE workoutLogId = :workoutId")
    suspend fun deleteExercisesByWorkoutId(workoutId: Int)
}

@Dao
interface CardioDao {
    @Query("SELECT * FROM cardio_log ORDER BY date DESC, timestamp DESC")
    fun getAllCardioLogs(): Flow<List<CardioLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardioLog(log: CardioLog)

    @Query("DELETE FROM cardio_log WHERE id = :id")
    suspend fun deleteCardioLogById(id: Int)
}

@Dao
interface StepDao {
    @Query("SELECT * FROM step_log ORDER BY date DESC")
    fun getAllSteps(): Flow<List<StepLog>>

    @Query("SELECT * FROM step_log WHERE date = :date LIMIT 1")
    fun getStepsForDate(date: String): Flow<StepLog?>

    @Query("SELECT * FROM step_log WHERE date = :date LIMIT 1")
    suspend fun getStepsForDateSync(date: String): StepLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepLog(log: StepLog)
}

@Dao
interface DietDao {
    @Query("SELECT * FROM diet_log ORDER BY date DESC, timestamp DESC")
    fun getAllDietLogs(): Flow<List<DietLog>>

    @Query("SELECT * FROM diet_log WHERE date = :date ORDER BY timestamp ASC")
    fun getDietLogsForDate(date: String): Flow<List<DietLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietLog(log: DietLog)

    @Query("DELETE FROM diet_log WHERE id = :id")
    suspend fun deleteDietLogById(id: Int)
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_log ORDER BY date DESC, timestamp DESC")
    fun getAllWaterLogs(): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_log WHERE date = :date ORDER BY timestamp ASC")
    fun getWaterLogsForDate(date: String): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog)

    @Query("DELETE FROM water_log WHERE id = :id")
    suspend fun deleteWaterLogById(id: Int)
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_log ORDER BY date DESC")
    fun getAllSleepLogs(): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_log WHERE date = :date LIMIT 1")
    suspend fun getSleepLogForDateSync(date: String): SleepLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(log: SleepLog)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit ORDER BY name ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habit WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Int): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Query("DELETE FROM habit WHERE id = :id")
    suspend fun deleteHabitById(id: Int)

    @Query("SELECT * FROM habit_history ORDER BY date DESC")
    fun getHabitHistory(): Flow<List<HabitHistory>>

    @Query("SELECT * FROM habit_history WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getHistoryForHabitAndDate(habitId: Int, date: String): HabitHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitHistory(history: HabitHistory)

    @Query("DELETE FROM habit_history WHERE habitId = :habitId AND date = :date")
    suspend fun deleteHabitHistory(habitId: Int, date: String)

    @Query("DELETE FROM habit_history WHERE habitId = :habitId")
    suspend fun deleteHabitHistoryByHabitId(habitId: Int)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expense ORDER BY date DESC, timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE category = :category ORDER BY date DESC")
    fun getExpensesForCategory(category: String): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("DELETE FROM expense WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)
}
