/*
 * Copyright (C) 2015 The Android Open Source Project
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
package uk.ac.gla.abbi.abbi_library;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.R;

/**
 * This is a helper class that can be used to add an about dialogue containing some general information about the
 * application that has been developed. It can work with any aboutText parameter and appName
 * <p/>
 * Created by Peter Yordanov on 7.7.2015
 */

public class AboutDialogue {

    /**
     * This is the main function called in the main application when the 'About' option is selected from the drop-down menu in the ActionBar
     *
     * @param activity     the activity that is currently running
     * @param aboutText    the (formatted) about text in the form of a String that will be displayed in the dialogue
     * @param okButtonText
     */
    public static void show(Activity activity, String aboutText,
                            String okButtonText) {
        String versionNumber = "unknown"; //initialize version number
        String appName = "unknown"; //initialize application name
        //try to retrieve the actual values and assign them to the variables above
        try {
            PackageManager pm = activity.getPackageManager();
            versionNumber = pm.getPackageInfo(activity.getPackageName(), 0)
                    .versionName;
            ApplicationInfo ai = pm.getApplicationInfo(activity.getPackageName(),
                    0);
            appName = (String) pm.getApplicationLabel(ai);
            if (appName == null)
                appName = "unknown";
        } catch (NameNotFoundException e) {
        }
        View about;
        TextView tvAbout;
        //try to reference the about TextView and configure it
        try {
            LayoutInflater inflater = activity.getLayoutInflater();
            about = inflater.inflate(R.layout.about, null);
            tvAbout = (TextView) about.findViewById(R.id.aboutText);
        } catch (InflateException e) {
            about = tvAbout = new TextView(activity);
        }
        //set TextView contents and inner text arrangement - padding, font color, okButtonText, etc.
        tvAbout.setText(Html.fromHtml(aboutText));
        tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
        tvAbout.setPadding(20, 20, 20, 20);
        Dialog dialog = new AlertDialog.Builder(activity)
                .setTitle(Html.fromHtml("<font color='#000000'>" + appName + " " + versionNumber + "</font>"))
                .setPositiveButton(okButtonText, null)
                .setView(about)
                .show();

        //set the titleDivider background color
        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = dialog.findViewById(dividerId);
        divider.setBackgroundColor(Color.BLACK);

    }
}
