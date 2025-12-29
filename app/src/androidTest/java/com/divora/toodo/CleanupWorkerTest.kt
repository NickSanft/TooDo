package com.divora.toodo

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.TimeUnit
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@UninstallModules(AppModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CleanupWorkerTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var taskDao: TaskDao

    @Inject
    lateinit var database: AppDatabase

    private lateinit var context: Context

    @Module
    @InstallIn(SingletonComponent::class)
    object TestAppModule {
        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context,
                AppDatabase::class.java
            ).allowMainThreadQueries().build()
        }

        @Provides
        fun provideTaskDao(database: AppDatabase): TaskDao {
            return database.taskDao()
        }

        @Provides
        fun providePrizeDao(database: AppDatabase): PrizeDao {
            return database.prizeDao()
        }

        @Provides
        fun providePointLedgerDao(database: AppDatabase): PointLedgerDao {
            return database.pointLedgerDao()
        }
    }

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        
        // Clear shared preferences
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun testCleanupWorker_deleteOldTasks() = runBlocking {
        // Set cleanup preference to "After 1 week"
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("auto_delete_option", "After 1 week")
            .apply()

        // Insert a task completed 8 days ago
        val oldTask = Task(
            title = "Old Task",
            difficulty = "Easy",
            points = 1,
            isCompleted = true,
            completedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(8)
        )
        taskDao.insert(oldTask)

        // Insert a task completed 6 days ago (should not be deleted)
        val recentTask = Task(
            title = "Recent Task",
            difficulty = "Easy",
            points = 1,
            isCompleted = true,
            completedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(6)
        )
        taskDao.insert(recentTask)

        // Insert an active task (should not be deleted)
        val activeTask = Task(
            title = "Active Task",
            difficulty = "Easy",
            points = 1,
            isCompleted = false
        )
        taskDao.insert(activeTask)

        // Run the worker
        val worker = TestListenableWorkerBuilder<CleanupWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)

        // Verify "Old Task" is deleted, others remain
        // LiveData must be observed on the main thread
        withContext(Dispatchers.Main) {
            val tasks = taskDao.getAllTasks().getOrAwaitValue()
            assertEquals(2, tasks.size)
            assertEquals(false, tasks.any { it.title == "Old Task" })
            assertEquals(true, tasks.any { it.title == "Recent Task" })
            assertEquals(true, tasks.any { it.title == "Active Task" })
        }
    }

    @Test
    fun testCleanupWorker_doNothingIfNeverSelected() = runBlocking {
        // Set cleanup preference to "Never" (default)
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("auto_delete_option", "Never")
            .apply()

        // Insert a task completed 8 days ago
        val oldTask = Task(
            title = "Old Task",
            difficulty = "Easy",
            points = 1,
            isCompleted = true,
            completedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(8)
        )
        taskDao.insert(oldTask)

        // Run the worker
        val worker = TestListenableWorkerBuilder<CleanupWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)

        // Verify task is NOT deleted
        // LiveData must be observed on the main thread
        withContext(Dispatchers.Main) {
            val tasks = taskDao.getAllTasks().getOrAwaitValue()
            assertEquals(1, tasks.size)
            assertEquals(true, tasks.any { it.title == "Old Task" })
        }
    }
}
