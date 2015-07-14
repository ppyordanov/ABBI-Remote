package uk.ac.gla.abbi.abbi_library;

/**
 * Created by Peter Yordanov on 7.7.2015 ã..
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import uk.ac.gla.peteryordanov.abbi_library.gatt_communication.R;

public class AboutDialogue
{
    public static void show(Activity activity, String aboutText,
                            String okButtonText)
    {
        String versionNumber = "unknown";
        String appName = "unknown";
        try
        {
            PackageManager pm = activity.getPackageManager();
            versionNumber = pm.getPackageInfo(activity.getPackageName(), 0)
                    .versionName;
            ApplicationInfo ai = pm.getApplicationInfo(activity.getPackageName(),
                    0);
            appName = (String) pm.getApplicationLabel(ai);
            if (appName == null)
                appName = "unknown";
        }
        catch (NameNotFoundException e)
        {
        }
        View about;
        TextView tvAbout;
        try
        {
            LayoutInflater inflater = activity.getLayoutInflater();
            about = inflater.inflate(R.layout.about, null);
            tvAbout = (TextView) about.findViewById(R.id.aboutText);
        }
        catch(InflateException e)
        {
            about = tvAbout = new TextView(activity);
        }
        tvAbout.setText(Html.fromHtml(aboutText));
        tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
        tvAbout.setPadding(20, 20, 20, 20);
        new AlertDialog.Builder(activity)
                .setTitle(appName + " " + versionNumber)
                .setPositiveButton(okButtonText, null)
                .setView(about)
                .show();
    }
}
