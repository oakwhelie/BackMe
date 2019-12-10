package oakwhelie.backme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{

    /**********  File Path *************/
    //String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(getApplicationContext(), BackgroundService.class);
        startService(intent);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        ((TextView)findViewById(R.id.last_backup)).setText(prefs.getString(getString(R.string.last_backup), getString(R.string.txt_no_backup)));

        checkAlarmServiceStatus();
    }
    private void checkAlarmServiceStatus()
    {
        TextView txtalarmservice = findViewById(R.id.running_alarm_service_display);
        Intent intent = new Intent(this, UploadService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_NO_CREATE);

        if(pintent == null)
        {
            SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

            if( prefs.getString(getString(R.string.alarm_interval_mode), null).equals(getString(R.string.alarm_idle)) ||
                    prefs.getString(getString(R.string.alarm_interval_mode), null).equals(getString(R.string.alarm_once_every_x)))
                txtalarmservice.setText("Backup task is running");
            else
                txtalarmservice.setText("Backup task is not running");
        }
        else
            txtalarmservice.setText("Backup task is running");
    }

    public void toSettings(View v)
    {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void backupNow(View v)
    {
        Intent intent = new Intent(this, UploadService.class);
        startService(intent);
    }

    public void getFolder(View v)
    {
        SharedPreferences pre = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String path = pre.getString("path", null);
        if(path == null)
        {
            Toast.makeText(this, "set backup folder first in settings", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Backup Initiated", Toast.LENGTH_LONG).show();
        SharedPreferences pref = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);

        Intent intent = new Intent(this, UploadService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent everyintent = new Intent(getApplicationContext(), BootReceiver.class);
        everyintent.setAction("ACTION_EVERY_X");
        PendingIntent everypintent = PendingIntent.getBroadcast(getApplicationContext(), 1, everyintent, PendingIntent.FLAG_CANCEL_CURRENT);

        if(pref.getString(getString(R.string.alarm_interval_mode), null).equals(getString(R.string.alarm_hour)))
        {
            calendar.set(Calendar.HOUR_OF_DAY, pref.getInt(getString(R.string.alarm_interval), 0));
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pintent);
            alarm.cancel(everypintent);
        }
        else if(pref.getString(getString(R.string.alarm_interval_mode), null).equals(getString(R.string.alarm_day)))
        {
            calendar.set(Calendar.DAY_OF_WEEK, pref.getInt(getString(R.string.alarm_interval), 1));
            calendar.set(Calendar.HOUR_OF_DAY, pref.getInt(getString(R.string.interval_hour_of_day), 0));
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY*7, pintent);
            alarm.cancel(everypintent);
        }
        else if(pre.getString(getString(R.string.alarm_interval_mode), null).equals(getString(R.string.alarm_idle)))
        {
            alarm.cancel(pintent);
            alarm.cancel(everypintent);
        }
        else if(pref.getString(getString(R.string.alarm_interval_mode), null).equals(getString(R.string.alarm_once_every_x)))
        {
            int interval = pref.getInt(getString(R.string.alarm_interval), 1);
            alarm.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+(AlarmManager.INTERVAL_HOUR*interval), everypintent);
            alarm.cancel(pintent);

        }
        checkAlarmServiceStatus();
    }
}