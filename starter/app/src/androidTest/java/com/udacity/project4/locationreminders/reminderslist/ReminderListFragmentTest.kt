package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsNot
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {


    private val reminderDataSource: ReminderDataSource by inject()

    @Before
    fun setup() {
        stopKoin()
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel { RemindersListViewModel(get(), get()) }
            single { FakeDataSource() as ReminderDataSource }
        }
        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(myModule))
        }
    }

    @Test
    fun fabButtonFunctionality() = runBlockingTest {
        //WHEN - ask to launch the Activity
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        // THEN - ask to click the fab
        onView(withId(R.id.addReminderFAB)).perform(click())
        //check its function is work
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun displayedListInUi() = runBlockingTest {
        // GIVEN - add the reminder data to create an object
        val reminder1 = ReminderDTO(
            "My Home",
            "Home",
            "Street1",
            4.6681,
            9.87412,
            "1"
        ) //add the reminder1 data to create an object1
        val reminder2 = ReminderDTO(
            "My Shop",
            "Shop",
            "Street2",
            4.6681,
            9.87412,
            "2"
        ) //add the reminder1 data to create an object1
        val reminder3 = ReminderDTO(
            "My GYM",
            "GYM",
            "Street3",
            4.6681,
            9.87412,
            "3"
        ) //add the reminder1 data to create an object1

        // ask to save in database
        reminderDataSource.saveReminder(reminder1)
        reminderDataSource.saveReminder(reminder2)
        reminderDataSource.saveReminder(reminder3)

        //WHEN - ask to launch the Activity
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        //THEN - check that data is loaded
        onView(ViewMatchers.withText(reminder1.title)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
        onView(ViewMatchers.withText(reminder2.description)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
        onView(ViewMatchers.withText(reminder3.title)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
        onView(withId(R.id.noDataTextView)).check(
            ViewAssertions.matches(
                IsNot.not(ViewMatchers.isDisplayed())
            )
        )
    }

    @Test
    fun displayedNoDataInUi() = runBlockingTest {
        // ask to delete all data
        reminderDataSource.deleteAllReminders()

        //WHEN - ask to launch the Activity
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        //THEN - check that their is no data found
        onView(ViewMatchers.withText(R.string.no_data)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
        onView(withId(R.id.noDataTextView)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
    }

    @After
    fun cleanDb() = runBlockingTest {
        reminderDataSource.deleteAllReminders()
    }
}