package oakwhelie.backme;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class UploadService extends IntentService
{
    NotificationManagerCompat notificationManagerCompat;
    final String notification_channel = "backme";
    final int notfication_manager_upload_id = 1012142;
    NotificationCompat.Builder notification;

    String server1 = "http://192.168.100.6";
    String server2 = "http://192.168.43.172";
    String upload = "/backme/backme.php";

    Handler handler;

    String path = null;
    File file = null;
    int filequantity = 0;

    int progress;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param 'name' Used to name the worker thread, important only for debugging.
     */
    //aboslutely necessary to run at boot
    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))};

    @Override
    public void onCreate()
    {
        super.onCreate();

        SharedPreferences pre = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        path = pre.getString("path", null);
        file = new File(path);
        filequantity = file.list().length;

        handler = new Handler();
        //aboslutely necessary to run at boot
        final SharedPreferences.Editor pref = getSharedPreferences("allow_notify", MODE_PRIVATE).edit();
        pref.apply();
        final SharedPreferences sp = getSharedPreferences("allow_notify", MODE_PRIVATE);

        if(!sp.getBoolean("protected",false))
        {
            for (final Intent intent : POWERMANAGER_INTENTS)
                getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        }
    }

    public UploadService() {
        super("upload_service");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = notification_channel;
            String description = notification_channel;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(notification_channel, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        try
        {
            initiateUpload(server1);
        }
        catch (IOException e)
        {
            try
            {
                initiateUpload(server2);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                notification.setContentText("FAIL TO CONNECT");
                notificationManagerCompat.notify(1000, notification.build());
            }
        }
    }

    private void initiateUpload(String server) throws IOException {
        createNotificationChannel();
        notification = new NotificationCompat.Builder(this, notification_channel)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManagerCompat = NotificationManagerCompat.from(this);
        Log.d("INTENT SERVICE", "START");

        Log.d("HTTPURLCONNECTION", "CONN");
        URL url;

        Log.d("HTTPURLCONNECTION", "TRY CONNECTION");
        url = new URL(server+upload);
        HttpURLConnection httpcon = (HttpURLConnection)url.openConnection();
        httpcon.setConnectTimeout(15000);
        httpcon.setReadTimeout(15000);
        httpcon.setRequestMethod("GET");
        httpcon.connect();
        Log.d("HTTPURLCONNECTION", "CONNECTED");

        send(server);
    }

    public void send(String server)
    {
        notification = new NotificationCompat.Builder(this, notification_channel)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notification_message_uploading))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(null)
                .setAutoCancel(false)
                .setOngoing(true)
                .setProgress(filequantity, 0, false);
        notificationManagerCompat.notify(notfication_manager_upload_id, notification.build());

        startForeground(notfication_manager_upload_id, notification.build());
        try
        {
            MultipartUtility m = new MultipartUtility(this, server+upload);//now add you different data parts;

            if(file.isDirectory())
            {
                String[] list = file.list();
                Log.d("SEND", file.list().length+"");
                if(list != null)
                {
                    ArrayList<String>backedlist = getListOfBackedFile(server);
                    Log.d("SEND", "BACKED: "+backedlist.size());

                    for (progress = 0; progress < list.length; progress++)
                    {
                        File temp = new File(path + list[progress]);

                        if(temp.isFile())
                        {
                            if(!backedlist.contains(temp.getName()))
                                m.addFilePart("file_part[]", temp);
                            else
                                Log.d("SEND", "ALREADY BACKED UP");
                        }
                        else Log.d("SEND", "TEMP IS NOT FILE");

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                notification.setProgress(filequantity, progress, false);
                                notificationManagerCompat.notify(notfication_manager_upload_id, notification.build());
                            }
                        }).start();
                    }
                    notification.setProgress(0,0,false);
                    notification.setContentText(getResources().getString(R.string.notification_message_finished));
                    notification.setPriority(NotificationCompat.PRIORITY_HIGH);
                    notification.setOngoing(false);
                    notification.setAutoCancel(false); //true
                    notificationManagerCompat.notify(notfication_manager_upload_id, notification.build());
                }
            }

            InputStream is=m.finish_with_inputstream(); //call this if the response is huge and not fitting in memory; don't forget to disconnect the connection afterwards;
            is.close();
            Log.d("UPLOAD:", "FINISHED");
            doneUploding();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doneUploding()
    {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String date = formatter.format(calendar.getTime());
        editor.putString(getString(R.string.last_backup), date);
        editor.apply();
    }

    private ArrayList<String> getListOfBackedFile(String server)
    {
        RequestQueue requestQueue;

// Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

// Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

// Start the queue
        requestQueue.start();

        final ArrayList<String> files = new ArrayList<>();
        String checkfiles = "/backme/filelist.php";

        StringRequest request = new StringRequest
                (Request.Method.GET, server+checkfiles, new Response.Listener<String>() {


                    @Override
                    public void onResponse(String response) {
                        String[] data = response.split("<br>");
                        for(int x=0; x<data.length; x++)
                            files.add(data[x]);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        // TODO: Handle error
                        Log.d("VOLLEY", error.getMessage());
                        Log.d("VOLLEY", error.getCause().toString());
                    }
                });

// Access the RequestQueue through your singleton class.
        requestQueue.add(request);

        return files;
    }
}
