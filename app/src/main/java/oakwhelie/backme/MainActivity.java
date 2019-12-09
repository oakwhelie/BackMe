package oakwhelie.backme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import common.globals.Globals;

public class MainActivity extends AppCompatActivity
{

    /**********  File Path *************/
    //String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/";

    String server_uploader = "/backme/backme.php";
    String upLoadServerUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView txt_ip = findViewById(R.id.ip_display);
        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String ip = prefs.getString("target_ip", getString(R.string.txt_no_ip));//"No name defined" is the default value.
        txt_ip.setText(ip);
        checkAlarmServiceStatus();
        Intent intent = new Intent(getApplicationContext(), BackgroundService.class);
        startService(intent);

        ((TextView)findViewById(R.id.last_backup)).setText(prefs.getString(getString(R.string.last_backup), getString(R.string.txt_no_backup)));
    }
    private void checkAlarmServiceStatus()
    {
        TextView txtalarmservice = findViewById(R.id.running_alarm_service_display);
        Intent intent = new Intent(this, UploadService.class);
        intent.putExtra("upLoadServerUri", upLoadServerUri);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if(pintent == null)
        {
            SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
            if(prefs.getString(getString(R.string.alarm_interval_mode), null).equals(getString(R.string.alarm_idle)))
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

    public void getFolder(View v)
    {
        SharedPreferences pre = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String path = pre.getString("path", null);
        if(path == null)
        {
            Toast.makeText(this, "set backup folder first in settings", Toast.LENGTH_LONG).show();
            return;
        }
        String ip_input = ((EditText)findViewById(R.id.edit_target_ip)).getText().toString();
        TextView ip_display = findViewById(R.id.ip_display);
        if(!ip_input.equals(Globals.EMPTY_STRING))
        {
            SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
            editor.putString("target_ip", ip_input);
            editor.apply();
            ip_display.setText(ip_input);

            String target_ip = "http://"+ ip_input;
            upLoadServerUri = target_ip + server_uploader;

            Intent intent = new Intent(this, UploadService.class);
            intent.putExtra("upLoadServerUri", upLoadServerUri);
            //startService(intent);

        }
        else
        {
            SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
            String ip = prefs.getString("target_ip", null);//"No name defined" is the default value.
            if(ip != null)
            {
                String target_ip = "http://"+ ip;
                upLoadServerUri = target_ip + server_uploader;

                Intent intent = new Intent(this, UploadService.class);
                intent.putExtra("upLoadServerUri", upLoadServerUri);
                //startService(intent);
            }
            else
            {
                Toast.makeText(this, "insert target ip for the first time", Toast.LENGTH_LONG).show();
            }
        }
        Toast.makeText(this, "Backup Initiated", Toast.LENGTH_LONG).show();
        SharedPreferences pref = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Intent intent = new Intent(this, UploadService.class);
        intent.putExtra("upLoadServerUri", upLoadServerUri);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        if(pref.getString(getString(R.string.alarm_interval_mode), null).equals(getString(R.string.alarm_hour)))
        {
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, pref.getInt(getString(R.string.alarm_interval), 0));
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pintent);
        }
        else if(pref.getString(getString(R.string.alarm_interval_mode), null).equals(getString(R.string.alarm_day)))
        {
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.DAY_OF_WEEK, pref.getInt(getString(R.string.alarm_interval), 1));
            calendar.set(Calendar.HOUR_OF_DAY, pref.getInt(getString(R.string.interval_hour_of_day), 0));
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY*7, pintent);
        }
        else if(pre.getString(getString(R.string.alarm_interval_mode), null).equals(getString(R.string.alarm_idle)))
        {

        }
        checkAlarmServiceStatus();
    }
}