package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainRule = MainCoroutineRule()
    private lateinit var repository: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        repository = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), repository)
    }

    @Test
    fun checkData_EmptyTitle() {
        // GIVEN - add the reminder data to create an object
        val reminder = ReminderDataItem("", "Home", "Street", 4.6681, 9.87412,"1")

        // THEN - check that all data entered, but it return false so it detect that title is missing
        Truth.assertThat(viewModel.validateEnteredData(reminder)).isFalse()

        //check that snakeBar shows "Please enter title"
        Truth.assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun checkData_EmptyLocation() {
        // GIVEN - add the reminder data to create an object
        val reminder = ReminderDataItem("My Home", "Home", "", 4.6681, 9.87412,"2")

        // THEN - check that all data entered, but it return false so it detect that location is missing
        Truth.assertThat(viewModel.validateEnteredData(reminder)).isFalse()
        // check that snakeBar shows "Please select location"
        Truth.assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun saveData_showLoading(){
        // GIVEN - add the reminder data to create an object
        val reminder = ReminderDataItem("Title", "Description", "Airport", 7.32323, 6.54343,"3")

        mainRule.pauseDispatcher() //pausing the dispatcher to make any new coroutines will not execute immediately
        // WHEN -  ask to save the reminder in database
        viewModel.saveReminder(reminder)

        // THEN - while they loading check that loading states is true
        Truth.assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()

        //make the dispatcher resume to get other process
        mainRule.resumeDispatcher()

        // check that loading is finished and loading states is false
        Truth.assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

}