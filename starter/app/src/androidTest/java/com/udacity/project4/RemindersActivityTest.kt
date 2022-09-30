package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        // Register Resource
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @Test
    fun saveDataMissingTitle() {
        // WHEN - ask to launch the Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // ask to click the fab
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        // ask to click the save button
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        //THEN - check that snakeBar message is "Please enter title"
        val snackBarMessage = appContext.getString(R.string.err_enter_title)
        Espresso.onView(ViewMatchers.withText(snackBarMessage)).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

        // Closing the activity
        activityScenario.close()
    }

    @Test
    fun saveDataMissingLocation() {
        // WHEN - ask to launch the Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // ask to click the fab
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        // ask to click the title EditText and type a text on it
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle)).perform(ViewActions.typeText("Title"))
        // ask to click the description EditText and type a text on it
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.typeText("Description"))
        // ask to close the keyboard
        Espresso.closeSoftKeyboard()
        // ask to click the save button
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        //THEN - check that snakeBar message is "Please Select Location"
        val snackBarMessage = appContext.getString(R.string.err_select_location)
        Espresso.onView(ViewMatchers.withText(snackBarMessage)).check(
                ViewAssertions.matches(
                    ViewMatchers.isDisplayed()
                )
        )

        // Closing the activity
        activityScenario.close()
    }

    @Test
    fun saveData() {

        // WHEN - ask to launch the Activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // ask to click the fab
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        // ask to click the title EditText and type a text on it
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle)).perform(ViewActions.typeText("Title"))
        // ask to click the description EditText and type a text on it
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription)).perform(ViewActions.typeText("Description"))
        // ask to close the keyboard
        Espresso.closeSoftKeyboard()
        // ask to click on selectLocation
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(ViewActions.click())
        // ask for a long click on any where
        Espresso.onView(ViewMatchers.withId(R.id.map)).perform(ViewActions.longClick())
        // ask to click the save button on select location
        Espresso.onView(ViewMatchers.withId(R.id.button_save)).perform(ViewActions.click())
        // ask to click the save button on save reminder
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        // THEN - check that the app sent Toast with "Reminder Saved !" and navigate to list fragment
        Espresso.onView(ViewMatchers.withText(R.string.reminder_saved)).inRoot(
            RootMatchers.withDecorView(
                CoreMatchers.not(
                    CoreMatchers.`is`(
                        getActivity(activityScenario).window.decorView
                    )
                )
            )
        )
    }

    @After
    fun endTesting() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

}
