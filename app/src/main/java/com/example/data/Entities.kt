package com.example.data

import androidx.room.*

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "DisciplinedUser",
    val passwordHash: String = "",
    val pin: String = "1234",
    val weightKg: Double = 75.0,
    val heightCm: Double = 175.0,
    val age: Int = 25,
    val dailyStepGoal: Int = 10000,
    val dailyCardioGoalMinutes: Int = 30,
    val dailyCalorieGoal: Int = 2200,
    val proteinGoalGrams: Int = 150,
    val carbsGoalGrams: Int = 250,
    val fatsGoalGrams: Int = 70,
    val weeklyBudgetLimit: Double = 250.0,
    val waterGoalMl: Int = 2500,
    val sleepGoalHours: Double = 8.0,
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "workout_log")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val name: String, // e.g. "Upper Body", "Leg Day"
    val durationMinutes: Int,
    val completed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercise_log")
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutLogId: Int, // Refers to WorkoutLog
    val name: String, // e.g. "Incline Dumbbell Press"
    val category: String, // e.g. "Chest"
    val setsText: String, // Comma separated set definitions, e.g. "12 reps x 60 kg,10 reps x 70 kg,8 reps x 80 kg"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cardio_log")
data class CardioLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val activityType: String, // Running, Cycling, Swimming
    val minutes: Int,
    val distanceKm: Double,
    val caloriesBurned: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "step_log")
data class StepLog(
    @PrimaryKey val date: String, // YYYY-MM-DD (One entry per day)
    val steps: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "diet_log")
data class DietLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val name: String, // e.g. "Protein Shake", "Oatmeal"
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatsGrams: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_log")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sleep_log")
data class SleepLog(
    @PrimaryKey val date: String, // YYYY-MM-DD (One entry per day)
    val hours: Double,
    val quality: String, // Good, Fair, Poor
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // e.g. "Wake up at 5:00 AM", "No Junk Food"
    val category: String, // e.g. "Discipline", "Mind", "Health"
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val totalCompletions: Int = 0,
    val lastCompletedDate: String = "", // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_history")
data class HabitHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val date: String, // YYYY-MM-DD
    val completed: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expense")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val category: String, // Food, Gym, Shopping, Transport, Entertainment, Bills, Investments
    val amount: Double,
    val notes: String = "",
    val receiptMockPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
