package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var reminderDatabase: RemindersDatabase

    @Before
    fun openingDatabase() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        reminderDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @Test
     fun saveData_CheckSaving() = runBlockingTest{
        //GIVEN - add data to create an object
        val data = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)

        // WHEN - ask database to save the data
        reminderDatabase.reminderDao().saveReminder(data)
        //ask database to get the same object to check it
        val res = reminderDatabase.reminderDao().getReminderById(data.id)

        // THEN - check that data are the same
        assertThat(res?.id , `is`(data.id))
        assertThat(res?.title , `is`(data.title))
        assertThat(res?.description , `is`(data.description))
        assertThat(res?.location , `is`(data.location))
        assertThat(res?.latitude , `is`(data.latitude))
        assertThat(res?.longitude , `is`(data.longitude))
    }

    @Test
    fun getAllData_CheckExistence() = runBlockingTest{
        //GIVEN - add data to create an object
        val data = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)
        val data1 = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)

        // WHEN - ask database to save the data
        reminderDatabase.reminderDao().saveReminder(data)
        reminderDatabase.reminderDao().saveReminder(data1)
        //ask database to get the same object to check it
        val res = reminderDatabase.reminderDao().getReminders()

        // THEN - check that data are the same
        assertThat(res,`is`(notNullValue()))
    }

    @Test
    fun deleteDataById() = runBlockingTest{
        //GIVEN - add data to create an object
        val data = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)

        // WHEN - ask database to save the data
        reminderDatabase.reminderDao().saveReminder(data)
        //ask database to delete data by id
        reminderDatabase.reminderDao().deleteReminderById(data.id)
        //ask database to get the same object to check it
        val res = reminderDatabase.reminderDao().getReminders()

        // THEN - check that data are the same
        assertThat(res,`is`(emptyList()))
    }

    @Test
    fun deleteAllData() = runBlockingTest{
        //GIVEN - add data to create an object
        val data = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)
        val data1 = ReminderDTO("Home","My Home","Street", 4.6681, 9.87412)

        // WHEN - ask database to save the data
        reminderDatabase.reminderDao().saveReminder(data)
        reminderDatabase.reminderDao().saveReminder(data1)
        //ask database to delete all data
        reminderDatabase.reminderDao().deleteAllReminders()
        //ask database to get the same object to check it
        val res = reminderDatabase.reminderDao().getReminders()

        // THEN - check that data are the same
        assertThat(res,`is`(emptyList()))
    }


    @After
    fun closeDatabase() {
        reminderDatabase.close()
    }
}