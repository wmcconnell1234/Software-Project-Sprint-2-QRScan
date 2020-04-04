/*
 * Copyright 2015, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.photogalleryproject3;

import android.app.Activity;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


/**
 * Basic tests showcasing simple view matchers and actions like {@link ViewMatchers#withId},
 * {@link ViewActions#click} and {@link ViewActions#typeText}.
 * <p>
 * Note that there is no need to tell Espresso that a view is in a different {@link Activity}.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UITest {

    ///**
    // * Use {@link ActivityScenarioRule} to create and launch the activity under test, and close it
    // * after test completes. This is a replacement for {@link androidx.test.rule.ActivityTestRule}.
    //*/
    @Rule public ActivityScenarioRule<com.example.photogalleryproject3.MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(com.example.photogalleryproject3.MainActivity.class);

    @Test
    public void changeText_sameActivity() {
        //The test assumes that 2 pictures have been taken already!!


        //    UI test for previous sprint. IL
        /*
        //Enter a caption "Caption1"
        onView(withId(R.id.editTextCaption)).perform(typeText("Caption1"), closeSoftKeyboard());
        //Press Save
        onView(withId(R.id.buttonSaveCaption)).perform(click());
        //Press right
        onView(withId(R.id.buttonRight)).perform(click());
        //Enter another caption "Caption2"
        onView(withId(R.id.editTextCaption)).perform(typeText("Caption2"), closeSoftKeyboard());
        //Press Save
        onView(withId(R.id.buttonSaveCaption)).perform(click());
        //Press right (to show it does nothing)
        onView(withId(R.id.buttonRight)).perform(click());
        //Press left three times (to show attempting to move too far to the left does nothing)
        onView(withId(R.id.buttonLeft)).perform(click());
        onView(withId(R.id.buttonLeft)).perform(click());
        onView(withId(R.id.buttonLeft)).perform(click());
        //Press Search
        onView(withId(R.id.btnSnap2)).perform(click());
        //In the search window Enter a string "1"
        onView(withId(R.id.search_Captions)).perform(typeText("1"), closeSoftKeyboard());
        //In the search window Press Search
        onView(withId(R.id.button)).perform(click());
        //Press right
        onView(withId(R.id.buttonRight)).perform(click());
        //Press left twice
        onView(withId(R.id.buttonLeft)).perform(click());
        onView(withId(R.id.buttonLeft)).perform(click());
        //Press Snap (does nothing)
        onView(withId(R.id.btnSnap)).perform(click());

         */

        // UI test for sprint 2. IL
        //Press Search
        onView(withId(R.id.btnSnap2)).perform(click());
        //In the location search window Enter the desire search for upper bound latitude
        onView(withId(R.id.search_fromLatitude)).perform(typeText("49.260000"), closeSoftKeyboard());
        //In the location search window Enter the desire search for lower bound latitude
        onView(withId(R.id.search_toLatitude)).perform(typeText("49.240000"), closeSoftKeyboard());
        //In the location search window Enter the desire search for upper bound longitude
        onView(withId(R.id.search_fromLongitude)).perform(typeText("-123.002740"), closeSoftKeyboard());//-123.002600
        //In the location search window Enter the desire search for lower bound longitude
        onView(withId(R.id.search_toLongitude)).perform(typeText("-123.002760"), closeSoftKeyboard());//002740
        //In the search window Press Search to go back to main activity
        onView(withId(R.id.button)).perform(click());
        //Press Snap (does nothing)
        onView(withId(R.id.btnSnap)).perform(click());

    }
}
