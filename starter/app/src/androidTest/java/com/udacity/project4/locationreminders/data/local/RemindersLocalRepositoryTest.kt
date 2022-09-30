package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: RemindersLocalRepository
    private lateinit var reminderDatabase: RemindersDatabase

    @Before
    fun openingDatabase() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        reminderDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(reminderDatabase.reminderDao(),Dispatchers.Main)
    }

    @Test
    fun saveData_CheckSaving() = runBlocking{
        //GIVEN - add data to create an object
        val data = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)

        // WHEN - ask repository to save the data
        repository.saveReminder(data)
        //ask repository to get the same object to check it
        val res = repository.getReminder(data.id) as? Result.Success

        // THEN - check that data are the same
        MatcherAssert.assertThat(res?.data?.id, `is`(data.id))
        MatcherAssert.assertThat(res?.data?.title, `is`(data.title))
        MatcherAssert.assertThat(res?.data?.description, `is`(data.description))
        MatcherAssert.assertThat(res?.data?.location, `is`(data.location))
        MatcherAssert.assertThat(res?.data?.latitude, `is`(data.latitude))
        MatcherAssert.assertThat(res?.data?.longitude, `is`(data.longitude))
    }

    @Test
    fun getMissingData() = runBlocking{
        //GIVEN - add data to create an object
        val data = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)

        // WHEN - ask repository to delete all data
        repository.deleteAllReminders()
        //ask repository to get the all data
        val res = repository.getReminder(data.id) as? Result.Error

        // THEN - check that data are not found
        MatcherAssert.assertThat(res?.message, `is`("Reminder not found!"))

    }

    @Test
    fun deleteDataById() = runBlocking {
        //GIVEN - add data to create an object
        val data = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)

        // WHEN - ask repository to save the data
        repository.saveReminder(data)
        //ask repository to delete data by id
        repository.deleteReminder(data.id)
        //ask repository to get the all data
        val res = repository.getReminders() as? Result.Success

        // THEN - check that data are the same
        MatcherAssert.assertThat(res?.data,`is`(emptyList()))

    }

    @Test
    fun deleteAllData() = runBlocking {
        //GIVEN - add data to create an object
        val data = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)
        val data1 = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)

        // WHEN - ask repository to save the data
        repository.saveReminder(data)
        repository.saveReminder(data1)
        //ask repository to delete all data
        repository.deleteAllReminders()

        //ask repository to get the all data
        val res = repository.getReminders() as? Result.Success

        // THEN - check that data are the same
        MatcherAssert.assertThat(res?.data,`is`(emptyList()))

    }

    @After
    fun closeDatabase() {
        reminderDatabase.close()
    }
}