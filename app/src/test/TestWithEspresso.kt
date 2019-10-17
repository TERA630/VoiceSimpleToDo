package com.example.voicesimpletodo

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class OnDeviceTest{
    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun activityShouldVisible(){
        val recyclerViewIA = onView(withId(R.id.originList))
        recyclerViewIA.check(matches(isDisplayed()))
    }

}