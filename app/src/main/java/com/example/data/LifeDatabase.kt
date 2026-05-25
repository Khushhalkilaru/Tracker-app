package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        WorkoutLog::class,
        ExerciseLog::class,
        CardioLog::class,
        StepLog::class,
        DietLog::class,
        WaterLog::class,
        SleepLog::class,
        Habit::class,
        HabitHistory::class,
        Expense::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LifeDatabase : RoomDatabase() {
    abstract val userProfileDao: UserProfileDao
    abstract val workoutDao: WorkoutDao
    abstract val cardioDao: CardioDao
    abstract val stepDao: StepDao
    abstract val dietDao: DietDao
    abstract val waterDao: WaterDao
    abstract val sleepDao: SleepDao
    abstract val habitDao: HabitDao
    abstract val expenseDao: ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: LifeDatabase? = null

        fun getDatabase(context: Context): LifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeDatabase::class.java,
                    "life_os_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
