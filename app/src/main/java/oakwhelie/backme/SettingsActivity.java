package oakwhelie.backme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import common.globals.Globals;

public class SettingsActivity extends AppCompatActivity
{
    TextView backup_interval;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        backup_interval = findViewById(R.id.backup_interval_display);

        SharedPreferences pref = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        String interval_mode = pref.getString(getString(R.string.alarm_interval_mode), null);
        if(interval_mode != null)
        {
            String val = String.valueOf(pref.getInt(getString(R.string.alarm_interval), 0));
            if(interval_mode.equals(getString(R.string.alarm_hour)))
                backup_interval.setText("Hour: "+val);
            if (interval_mode.equals(getString(R.string.alarm_day)))
                backup_interval.setText("Day: "+val);
            if(interval_mode.equals(getString(R.string.alarm_idle)))
                backup_interval.setText("When idle");
        }

        String backup_folder = pref.getString("path", "no folder selected");
        ((TextView)findViewById(R.id.backup_folder_display)).setText(backup_folder);
    }

    public void backupFolder(View v)
    {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(i, "Choose backup folder"), 9999);
    }
    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent data)
    {
        super.onActivityResult(requestcode, requestcode, data);
        try {
            String uripath = data.getData().getPath();
            Log.d("PATH", uripath);
            String[] temp = uripath.split(":");

            String path = null;
            if(temp.length > 1)
            {
                if(temp[0].contains("/tree/primary"))
                    path = Environment.getExternalStorageDirectory()+"/"+temp[1]+"/";
                else
                {
                    String ext[] = Globals.getStorageDirectories();
                    path = ext[1]+"/"+temp[1];
                }
            }
            else
            {
                if(temp[0].contains("/tree/primary"))
                    path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
                else
                {
                    String ext[] = Globals.getStorageDirectories();
                    path = ext[1];
                }
            }

            SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
            editor.putString("path", path);
            editor.apply();

            ((TextView)findViewById(R.id.backup_folder_display)).setText(path);
        } catch (NullPointerException e)
        {

        }
    }

    public void backupInterval(View v)
    {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create();

        Button idlebutton = new Button(this);
        idlebutton.setText("when idle");
        idlebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
                editor.putString(getString(R.string.alarm_interval_mode), getString(R.string.alarm_idle));
                editor.apply();
                alertDialog.cancel();
                backup_interval.setText("When idle");
            }
        });

        Button hourbutton = new Button(this);
        hourbutton.setText("Once per day");
        hourbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final NumberPicker numberPicker = new NumberPicker(SettingsActivity.this);
                numberPicker.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                numberPicker.setMaxValue(23);
                numberPicker.setMinValue(0);
                new AlertDialog.Builder(SettingsActivity.this)
                        .setView(numberPicker)
                        .setTitle("Hour")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int val = numberPicker.getValue();
                                Toast.makeText(SettingsActivity.this, "Hour: "+val, Toast.LENGTH_LONG).show();
                                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
                                editor.putString(getString(R.string.alarm_interval_mode), getString(R.string.alarm_hour));
                                editor.putInt(getString(R.string.alarm_interval), val);
                                editor.apply();
                                alertDialog.cancel();
                                backup_interval.setText("Hour: "+val);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel
                            }
                        })
                        .show();
            }
        });

        final Button daybutton = new Button(this);
        daybutton.setText("Once per week");
        daybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final NumberPicker daypicker = new NumberPicker(SettingsActivity.this);
                daypicker.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                daypicker.setMaxValue(7);
                daypicker.setMaxValue(1);
                daypicker.setDisplayedValues(new String[]{  "Sunday",
                                                            "Monday",
                                                            "Tuesday",
                                                            "Wednesday",
                                                            "Thursday",
                                                            "Friday",
                                                            "Saturday"});
                new AlertDialog.Builder(SettingsActivity.this)
                        .setView(daypicker)
                        .setTitle("Hour")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int day = daypicker.getValue();
                                final String dispday;
                                switch (day)
                                {
                                    case 1:dispday = "Sunday"; break;
                                    case 2:dispday = "Monday"; break;
                                    case 3:dispday = "Tuesday"; break;
                                    case 4:dispday = "Wednesday"; break;
                                    case 5:dispday = "Thursday"; break;
                                    case 6:dispday = "Friday"; break;
                                    case 7:dispday = "Saturday"; break;
                                    default: dispday = null; break;
                                }
                                Toast.makeText(SettingsActivity.this, "Day: "+dispday, Toast.LENGTH_LONG).show();
                                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
                                editor.putString(getString(R.string.alarm_interval_mode), getString(R.string.alarm_day));
                                editor.putInt(getString(R.string.alarm_interval), daypicker.getValue());
                                editor.apply();

                                final NumberPicker num = new NumberPicker(SettingsActivity.this);
                                num.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                num.setMaxValue(23);
                                num.setMinValue(0);
                                new AlertDialog.Builder(SettingsActivity.this)
                                        .setView(num)
                                        .setTitle("Hour")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                int val = num.getValue();
                                                Toast.makeText(SettingsActivity.this, "Hour: "+val, Toast.LENGTH_LONG).show();
                                                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
                                                editor.putInt(getString(R.string.interval_hour_of_day), val);
                                                editor.apply();
                                                alertDialog.cancel();
                                                backup_interval.setText("Day: "+val);
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //cancel
                                            }
                                        })
                                        .show();

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel
                            }
                        })
                        .show();

            }
        });
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(idlebutton);
        linearLayout.addView(hourbutton);
        linearLayout.addView(daybutton);

        alertDialog.setTitle("Interval Mode");
        alertDialog.setView(linearLayout);
        alertDialog.show();
    }
}
