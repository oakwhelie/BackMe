package oakwhelie.backme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver
{
    String server_uploader = "/backme/backme.php";
    String upLoadServerUri = null;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(     intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED) ||
                intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
            String ip = "http://" + pref.getString("target_ip", null);//"No name defined" is the default value.
            upLoadServerUri = ip + server_uploader;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);

            Intent serviceintent = new Intent(context.getApplicationContext(), UploadService.class);
            serviceintent.putExtra("upLoadServerUri", upLoadServerUri);
            PendingIntent pintent = PendingIntent.getService(context.getApplicationContext(), 0, serviceintent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (pref.getString(context.getString(R.string.alarm_interval_mode), null).equals(context.getString(R.string.alarm_hour)))
            {
                calendar.set(Calendar.HOUR_OF_DAY, pref.getInt(context.getString(R.string.alarm_interval), 0));
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pintent);
            }
            else if (pref.getString(context.getString(R.string.alarm_interval_mode), null).equals(context.getString(R.string.alarm_day)))
            {
                calendar.set(Calendar.DAY_OF_WEEK, pref.getInt(context.getString(R.string.alarm_interval), 1));
                calendar.set(Calendar.HOUR_OF_DAY, pref.getInt(context.getString(R.string.interval_hour_of_day), 0));
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pintent);
            }
            else if(pref.getString(context.getString(R.string.alarm_interval_mode), null).equals(context.getString(R.string.alarm_idle)))
            {
                Intent service = new Intent(context.getApplicationContext(), UploadService.class);
                context.startService(service);
            }
            else if(pref.getString(context.getString(R.string.alarm_interval_mode), null).equals(context.getString(R.string.alarm_once_every_x)))
            {
                int interval = pref.getInt(context.getString(R.string.alarm_interval), 1);
                alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_HOUR*interval, pintent);
            }
        }
    }
}
