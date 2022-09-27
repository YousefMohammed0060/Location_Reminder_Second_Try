package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.test.runBlockingTest

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainRule = MainCoroutineRule()
    private lateinit var repository: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        repository = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), repository)
    }

    @Test
    fun loadData_showLoading() {
        //pausing the dispatcher to make any new coroutines will not execute immediately
        mainRule.pauseDispatcher()

        // WHEN - ask to load the reminders to show them into the recyclerView
        viewModel.loadReminders()

        // THEN - while they loading check that loading states is true
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        //make the dispatcher resume to get other process
        mainRule.resumeDispatcher()
        // check that loading is finished and loading states is false
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }

    @Test
    fun loadData_dataListNotEmpty() = mainRule.runBlockingTest {
        // GIVEN - add the reminder data to create an object
        val reminder = ReminderDTO("My Home", "Home", "Street", 4.6681, 9.87412)

        // WHEN -  using the repository and dispatchers to ask database to save the reminder
        repository.saveReminder(reminder)
        // ask to load the reminders to show them into the recyclerView
        viewModel.loadReminders()

        // THEN - check that list in the viewModel is not empty
        assertThat(viewModel.remindersList.getOrAwaitValue()).isNotEmpty()
    }

    @Test
    fun loadData_updateSnackBarValue() {
        mainRule.pauseDispatcher() //pausing the dispatcher to make any new coroutines will not execute immediately

        // WHEN - the return error to check what the system will show
        repository.setReturnError(true)
        // ask to load the reminders to show them into the recyclerView
        viewModel.loadReminders()
        //make the dispatcher resume to get other process
        mainRule.resumeDispatcher()

        // THEN -  check that the system return to user that "Error getting reminders" to let him know that their is problem  to get data
        assertThat(viewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error getting reminders")
    }

    @After
    fun tearDown() {
        stopKoin()
    }

}