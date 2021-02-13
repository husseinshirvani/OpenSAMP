import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_PERMISSION = 123;
    private static final int REQUEST_LOCATION_PERMISSION = 124;
    public static boolean ACTIVE_THREAD = false;
    
    static double latitude = 0;
    static double longitude = 0;
    static String providerType;
    static String transportationType;

    static String insertUrl = "ADDRESS OF PHP CODE IN SERVER TO EXECUTE INSERTION POSTED DATA INTO DATABASE";

    private static Method[] methods = android.telephony.SignalStrength.class.getMethods();

    private static int get2GDbm_Code;
    private static int get3GDbm_Code;
    private static int get4GDbm_Code;

    private static SignalStrength signalStrength;
    private static String signalStrengthDbm;
    private static TelephonyManager telephonyManager;
    private static WifiManager wifiManager;

    private static String brand;
    private static String model;
    private static String imei;
    private static String version_sdk_int;

    private static int networkTypeNumber;
    private static String networkTypeString;
    private static String networkName;
    private static String carrierName;
    private static String[] weatherData;

    private static CellIdentityGsm cellIdentityGsm;
    private static CellIdentityWcdma cellIdentityWcdma;
    private static CellIdentityLte cellIdentityLte;

    private static CellSignalStrengthGsm cellSignalStrengthGsm;
    private static CellSignalStrengthWcdma cellSignalStrengthWcdma;
    private static CellSignalStrengthLte cellSignalStrengthLte;

    private static int cellID;
    private static int cellFrequency;

    private static String neighboringCells;

    private static String signalStrengthDbmResult;
    private static String wifiResult;
    private static String neighboringAps;
    private static String frequency;

    private static StringRequest stringRequest;

    private static Map<String, String> parameters = new HashMap<>();

    TextView tvWeatherInfo;
    TextView tvSignalInfo;
    TextView tvWifiInfo;
    Spinner spinner;
    Button executeButton;
    Switch wifiSwitch;
    Switch cellularSwitch;

    GPSTracker gps;

    public static String fetchWeatherData() {

        HttpURLConnection urlConnection;

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("weather")
                .appendQueryParameter("units", "metric")
                .appendQueryParameter("lat",latitude + "")
                .appendQueryParameter("lon", longitude + "")
                .appendQueryParameter("APPID", BuildConfig.OPEN_WEATHER_MAP_API_KEY);

        String myUrl = builder.build().toString();
        URL url;
        InputStream inputStream;
        BufferedReader reader;
        String line;
        StringBuffer buffer = new StringBuffer();

        try {
            url = new URL(myUrl);
            Log.d("URL", url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();

            if (inputStream == null)
                return null;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = reader.readLine()) != null)
            {
                String s = line + "\n";
                buffer.append(s);
            }

            if (buffer.length() == 0)
                return null;

            return getWeatherDataFromJson(buffer.toString());
        } catch (Exception e) {
            Log.e("THREAD INFO :","URL Exception!");
            e.printStackTrace();
        }
        return  null;
    }

    private static String getWeatherDataFromJson(String forecastJsonStr) throws JSONException {
        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_SYS = "sys";
        final String OWM_COUNTRY = "country";
        final String OWM_NAME = "name";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        Log.d("forecastJsonStr INFO :", forecastJsonStr);

        String day;
        String time;
        String currentTimeZone;
        String description;
        double humidity;
        String country;
        String name;

        SimpleDateFormat shortenedDateFormat =
                new SimpleDateFormat("EEE MMM dd yyyy", Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        day = shortenedDateFormat.format(calendar.getTime());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

        TimeZone timeZone = simpleDateFormat.getTimeZone();
        currentTimeZone = timeZone.getID();

        time = simpleDateFormat.format(calendar.getTime());

        JSONObject weatherObject = forecastJson.getJSONArray(OWM_WEATHER).getJSONObject(0);
        description = weatherObject.getString(OWM_DESCRIPTION);

        JSONObject mainObject = forecastJson.getJSONObject(OWM_DESCRIPTION);
        humidity = mainObject.getDouble(OWM_HUMIDITY);

        JSONObject sysObject = forecastJson.getJSONObject(OWM_SYS);
        country = sysObject.getString(OWM_COUNTRY);

        name = forecastJson.getString(OWM_NAME);

        return day + ";" + time + ";" + currentTimeZone + ";" + description + ";" +
                humidity + ";" + country + ";" + name;
    }

    public static String formatWeatherData(String data) {
        if (data == null)
            return "";

        weatherData = data.split(";");

        return "Location Provider: " + providerType + "/" + weatherData[6] + "\n" +
                "Transportation Type: " + transportationType + "\n\n" +
                "Latitude: " + Double.toString(latitude) + "\n" +
                "Longitude: " + Double.toString(longitude) + "\n" +
                "Time: " + weatherData[0] + " - " + weatherData[1] + "\n" +
                "Weather: " + weatherData[3] + " " + " (H/" + weatherData[4] + ")\n";
    }

    private static int convertFrequencyToChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) {
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        } else {
            return -1;
        }
    }

    private static String getNetworkName(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GRPS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "IDEN";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO_0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO_A";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO_B";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "EHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPAP";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            default:
                return "Unknown";
        }
    }

    private static String getCellPhoneNetworkType(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "N/A";
        }
    }

    private static String getSignalStrength(String networkType) {
        try
        {
            switch (networkType) {
                case "2G":
                    return Integer.toString((Integer) methods[get2GDbm_Code].invoke(signalStrength));

                case "3G":
                    return Integer.toString((Integer) methods[get3GDbm_Code].invoke(signalStrength));

                case "4G":
                    return Integer.toString((Integer) methods[get4GDbm_Code].invoke(signalStrength));
            }
        }
        catch (Exception e)
        {
            switch (networkType) {
                case "2G":
                    Log.e("N2G_TAG", "Exception: " + e.toString());
                    break;
                case "3G":
                    Log.e("N3G_TAG", "Exception: " + e.toString());
                    break;
                case "4G":
                    Log.e("N4G_TAG", "Exception: " + e.toString());
                    break;
            }
        }
        return null;
    }

    private static void getMethodsCode() {
        int counter = 0;
        for (Method method : methods) {
            if (method.getName().equals("getGsmDbm"))
                get2GDbm_Code = counter++;
            else if (method.getName().equals("getDbm"))
                get3GDbm_Code = counter++;
            else if (method.getName().equals("getLteDbm"))
                get4GDbm_Code = counter++;
            else
                ++counter;
        }
    }

    private static String generateWiFiResult(
            String ssid, String frequency, String rssi)
    {
        return "Wi-Fi SSID: " + ssid + " - F/" +
                frequency + " C/" +
                convertFrequencyToChannel(
                        Integer.parseInt(frequency)) + "\n" +
                "Wi-Fi RSSI: " + rssi + " dBm\n";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ACTIVE_THREAD = true;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvWeatherInfo = (TextView) findViewById(R.id.weatherTextView);
        tvSignalInfo = (TextView) findViewById(R.id.signalTextView);
        tvWifiInfo = (TextView) findViewById(R.id.wifiTextView);

        spinner = (Spinner) findViewById(R.id.ts_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ts_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        executeButton = (Button) findViewById(R.id.executeButton);
        executeButton.setText(R.string.btn_start);

        wifiSwitch = (Switch) findViewById(R.id.wifi_enabled_switch);
        cellularSwitch = (Switch) findViewById(R.id.cellular_enabled_switch);

        getMethodsCode();

        startListenSignalStrengthData();
        showDeviceInfo();

        findViewById(R.id.executeButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (executeButton.getText().equals("Start")) {
                    transportationType = spinner.getSelectedItem().toString();
                    if (!transportationType.equals("None")) {
                        if (cellularSwitch.isChecked() || wifiSwitch.isChecked()) {
                            ACTIVE_THREAD = true;
                            startThread();
                            executeButton.setText(R.string.btn_stop);
                            return;
                        } else {
                            Toast.makeText(MainActivity.this, R.string.data_collection_type,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                    } else {
                        Toast.makeText(MainActivity.this, R.string.trans_type_alert,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (!wifiSwitch.isChecked())
                    tvWifiInfo.setText(null);
                if (!cellularSwitch.isChecked())
                    tvSignalInfo.setText(null);

                if (executeButton.getText().equals("Stop") |
                        (!wifiSwitch.isChecked() & !cellularSwitch.isChecked())) {

                    ACTIVE_THREAD = false;
                    executeButton.setText(R.string.btn_start);
                    tvWeatherInfo.setText(null);
                    tvSignalInfo.setText(null);
                    tvWifiInfo.setText(null);
                }
            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startThread() {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                Log.d("Thread Info","Thread Started.");

                while (ACTIVE_THREAD)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gps = new GPSTracker(MainActivity.this);

                            if (CheckPermission(MainActivity.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                                latitude = gps.getLatitude();
                                longitude = gps.getLongitude();
                                providerType = gps.providerType;

                                spinner.setOnItemSelectedListener(
                                        new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view,
                                                               int position, long id) {
                                        transportationType = parent.getItemAtPosition(position)
                                                .toString();

                                        if (transportationType.equals("None")) {
                                            ACTIVE_THREAD = false;

                                            executeButton.setText(R.string.btn_start);

                                            tvWeatherInfo.setText(null);
                                            tvSignalInfo.setText(null);
                                            tvWifiInfo.setText(null);

                                            if (Build.VERSION.SDK_INT >=
                                                    Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                                wifiSwitch.setChecked(false);
                                                cellularSwitch.setChecked(false);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            }
                            else {
                                RequestPermission(MainActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        REQUEST_LOCATION_PERMISSION);
                            }
                        }
                    });

                    if (!transportationType.equals("None")) {
                        String weatherData = fetchWeatherData();
                        final String formattedWeatherData = formatWeatherData(weatherData);

                        if (cellularSwitch.isChecked())
                            setCellularData();

                        if (wifiManager.isWifiEnabled() & wifiSwitch.isChecked())
                            setWifiData();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvWeatherInfo.setText(formattedWeatherData);
                                if (cellularSwitch.isChecked())
                                    tvSignalInfo.setText(signalStrengthDbmResult);
                                else
                                    tvSignalInfo.setText(null);
                                if (wifiSwitch.isChecked()) {
                                    if (wifiManager.isWifiEnabled())
                                        tvWifiInfo.setText(wifiResult);
                                    else
                                        tvWifiInfo.setText(
                                                generateWiFiResult("N/A", "-1", "N/A"));
                                } else
                                    tvWifiInfo.setText(null);

                                if (!ACTIVE_THREAD) {
                                    tvWeatherInfo.setText(null);
                                    tvSignalInfo.setText(null);
                                    tvWifiInfo.setText(null);
                                }
                            }
                        });

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Log.e("Thread Info", "Thread Sleep Exception.");
                            e.printStackTrace();
                        }
                    }
                }
                Log.d("Thread Info","Thread Finished.");
            }
        }).start();
    }

    @Override
    protected void onResume() {
        ACTIVE_THREAD = true;
        super.onResume();
    }

    @Override
    protected void onStop () {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ACTIVE_THREAD = false;
        super.onDestroy();
    }

    public void showDeviceInfo() {

        if (CheckPermission(this, android.Manifest.permission.READ_PHONE_STATE)) {

            brand = Build.BRAND.toUpperCase();
            model = Build.MODEL;
            imei = telephonyManager.getDeviceId();

            version_sdk_int = Integer.toString(Build.VERSION.SDK_INT);
        }
        else
            RequestPermission(MainActivity.this,
                    Manifest.permission.READ_PHONE_STATE, REQUEST_READ_PERMISSION);
    }

    public void RequestPermission(Activity thisActivity, String Permission, int Code) {
        if (ContextCompat.checkSelfPermission(thisActivity,
                Permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Permission)) {
            } else {
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Permission},
                        Code);
            }
        }
    }

    public boolean CheckPermission(Context context, String Permission) {
        return ContextCompat.checkSelfPermission(context,
                Permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void startListenSignalStrengthData() {

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        networkTypeNumber = telephonyManager.getNetworkType();
        networkTypeString = getCellPhoneNetworkType(networkTypeNumber);

        networkName = getNetworkName(networkTypeNumber);
        carrierName = telephonyManager.getNetworkOperatorName();

        final PhoneStateListener phoneStateListener =
                new PhoneStateListener() {

            @Override
            public void onSignalStrengthsChanged(SignalStrength newSignalStrength) {
                super.onSignalStrengthsChanged(newSignalStrength);
                signalStrength = newSignalStrength;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();

                        if (cellInfos != null) {

                            if (cellInfos.get(0) instanceof CellInfoGsm) {

                                cellIdentityGsm = ((CellInfoGsm) cellInfos.get(0))
                                        .getCellIdentity();
                                cellSignalStrengthGsm = ((CellInfoGsm) cellInfos.get(0))
                                        .getCellSignalStrength();

                                if (cellIdentityGsm.getMnc() == 11) {
                                    if (cellIdentityGsm.getCid() != Integer.MAX_VALUE) {

                                        cellID = cellIdentityGsm.getCid();
                                        signalStrengthDbm = Integer.toString(
                                                cellSignalStrengthGsm.getDbm());

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                            cellFrequency = cellIdentityGsm.getArfcn();
                                        else
                                            cellFrequency = -1;
                                    } else {
                                        cellID = 0;
                                        Log.e("Cell Info", "GSM - Cell ID Error");
                                    }
                                }
                            } else if (cellInfos.get(0) instanceof CellInfoWcdma) {

                                cellIdentityWcdma = ((CellInfoWcdma) cellInfos.get(0))
                                        .getCellIdentity();
                                cellSignalStrengthWcdma = ((CellInfoWcdma) cellInfos.get(0))
                                        .getCellSignalStrength();

                                if (cellIdentityWcdma.getMnc() == 11) {
                                    if (cellIdentityWcdma.getCid() != Integer.MAX_VALUE) {

                                        cellID = cellIdentityWcdma.getCid();
                                        signalStrengthDbm = Integer.toString(
                                                cellSignalStrengthWcdma.getDbm());

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                            cellFrequency = cellIdentityWcdma.getUarfcn();
                                        else
                                            cellFrequency = -1;
                                    } else {
                                        cellID = 0;
                                        Log.e("Cell Info", "WCDMA - Cell ID Error");
                                    }
                                }
                            } else if (cellInfos.get(0) instanceof CellInfoLte) {

                                cellIdentityLte = ((CellInfoLte) cellInfos.get(0))
                                        .getCellIdentity();
                                cellSignalStrengthLte = ((CellInfoLte) cellInfos.get(0))
                                        .getCellSignalStrength();

                                if (cellIdentityLte.getMnc() == 11) {
                                    if (cellIdentityLte.getCi() != Integer.MAX_VALUE) {

                                        cellID = cellIdentityLte.getCi();
                                        signalStrengthDbm = Integer.toString(
                                                cellSignalStrengthLte.getDbm());

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                            cellFrequency = cellIdentityLte.getEarfcn();
                                        else
                                            cellFrequency = -1;
                                    } else {
                                        cellID = 0;
                                        Log.e("Cell Info", "LTE - Cell ID Error");
                                    }
                                }
                            } else {
                                Log.e("Cell Info", "Error");
                            }

                            neighboringCells = "";

                            int counter = 0;

                            for (int i = 1; i < cellInfos.size(); i++) {

                                if (cellInfos.get(i) instanceof CellInfoGsm) {

                                    cellIdentityGsm = ((CellInfoGsm) cellInfos.get(i))
                                            .getCellIdentity();
                                    cellSignalStrengthGsm = ((CellInfoGsm) cellInfos.get(i))
                                            .getCellSignalStrength();

                                    if (cellIdentityGsm.getCid() > 0 &
                                            cellIdentityGsm.getMnc() == 11)
                                        neighboringCells +=
                                                cellIdentityGsm.getCid() + ":" +
                                                        cellSignalStrengthGsm.getDbm() + ":ON:1";

                                    if (cellIdentityGsm.getMnc() != 11)
                                        counter++;

                                } else if (cellInfos.get(i) instanceof CellInfoWcdma) {

                                    cellIdentityWcdma = ((CellInfoWcdma) cellInfos.get(i))
                                            .getCellIdentity();
                                    cellSignalStrengthWcdma = ((CellInfoWcdma) cellInfos.get(i))
                                            .getCellSignalStrength();

                                    if (cellIdentityWcdma.getCid() > 0 &
                                            cellIdentityWcdma.getMnc() == 11)
                                        neighboringCells +=
                                                cellIdentityWcdma.getCid() + ":" +
                                                        cellSignalStrengthWcdma.getDbm() + ":ON:1";

                                    if (cellIdentityWcdma.getMnc() != 11)
                                        counter++;

                                } else if (cellInfos.get(i) instanceof CellInfoLte) {

                                    cellIdentityLte = ((CellInfoLte) cellInfos.get(i))
                                            .getCellIdentity();
                                    cellSignalStrengthLte = ((CellInfoLte) cellInfos.get(i))
                                            .getCellSignalStrength();

                                    if (cellIdentityLte.getCi() > 0 &
                                            cellIdentityLte.getMnc() == 11)
                                        neighboringCells +=
                                                cellIdentityLte.getCi() + ":" +
                                                        cellSignalStrengthLte.getDbm() + ":ON:1";

                                    if (cellIdentityLte.getMnc() != 11)
                                        counter++;

                                } else {
                                    break;
                                }

                                if (i + 1 < cellInfos.size())
                                    neighboringCells += ";";
                                else
                                    break;
                            }

                            if (neighboringCells.length() - counter > 0)
                                neighboringCells = neighboringCells.
                                        substring(0, neighboringCells.length() - counter);

                            if (cellInfos.size() > 1)
                                neighboringCells = Integer.toString(cellInfos.size() - counter)
                                        + ";" + cellID + ":" + signalStrengthDbm + ":ON:1"
                                        + ";" + neighboringCells;
                        }

                    signalStrengthDbmResult =
                        "Cell ID: " + Integer.toString(cellID) + "\n" +
                        "Cell Network: " + carrierName + " (" +
                                networkTypeString + "/" + getNetworkName(networkTypeNumber) + ")\n" +
                        "Cell RSS: " + signalStrengthDbm + " dBm (F/" +
                                Integer.toString(cellFrequency) + ")\n";
                }
            }
        };

        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public void setCellularData() {

        if (weatherData == null)
            return;

        if (latitude == 0 | longitude == 0 | weatherData[6].equals("Earth"))
            return;

        stringRequest = new StringRequest(Request.Method.POST,
                insertUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                if (signalStrengthDbm != null & cellID != 0) {
                    parameters.put("latitude", Double.toString(latitude));
                    parameters.put("longitude", Double.toString(longitude));
                    parameters.put("country_city", weatherData[5] + "/" + weatherData[6]);
                    parameters.put("location_provider", providerType);
                    parameters.put("trans_type", transportationType);
                    parameters.put("date", weatherData[0]);
                    parameters.put("time", weatherData[1]);
                    parameters.put("time_zone", weatherData[2]);
                    parameters.put("description", weatherData[3]);
                    parameters.put("humidity", weatherData[4]);
                    parameters.put("carrier", carrierName);
                    parameters.put("network_type", networkTypeString);
                    parameters.put("network_name", networkName);
                    parameters.put("neighbor_cells", neighboringCells);
                    parameters.put("cell_id", Integer.toString(cellID));
                    parameters.put("signal_strength_dbm", signalStrengthDbm);
                    parameters.put("device_name", brand + " " + model);
                    parameters.put("imei", imei);
                    parameters.put("os", "Android API " + version_sdk_int);

                }
                return parameters;
            }
        };
        stringRequest.setTag("CELLULAR_WEATHER_TAG");
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);

    }

    public void setWifiData() {

        if (weatherData == null)
            return;

        if (latitude == 0 | longitude == 0 | weatherData[6].equals("Earth"))
            return;

        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        List<ScanResult> scanResults = wifiManager.getScanResults();

        neighboringAps = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            frequency = Integer.toString(wifiInfo.getFrequency());
        else
            frequency = "N/A";

        if (scanResults.size() > 0) {
            for (int i = 0; i < scanResults.size(); i++) {
                neighboringAps += scanResults.get(i).SSID + ":" +
                        Integer.toString(scanResults.get(i).frequency) + ":" +
                        convertFrequencyToChannel(scanResults.get(i).frequency) + ":" +
                        Integer.toString(scanResults.get(i).level);

                if (Integer.toString(scanResults.get(i).frequency).equals(frequency))
                    neighboringAps += ":ON:1";
                else
                    neighboringAps += ":OFF:1";

                if (i + 1 < scanResults.size())
                    neighboringAps += ";";
            }
        }

        wifiResult = generateWiFiResult(wifiInfo.getSSID(), frequency,
                Integer.toString(wifiInfo.getRssi()));

        stringRequest = new StringRequest(Request.Method.POST,
                insertUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                parameters.put("latitude", Double.toString(latitude));
                parameters.put("longitude", Double.toString(longitude));
                parameters.put("country_city", weatherData[5] + "/" + weatherData[6]);
                parameters.put("location_provider", providerType);
                parameters.put("trans_type", transportationType);
                parameters.put("date", weatherData[0]);
                parameters.put("time", weatherData[1]);
                parameters.put("time_zone", weatherData[2]);
                parameters.put("description", weatherData[3]);
                parameters.put("humidity", weatherData[4]);
                parameters.put("network_name", "WiFi");
                parameters.put("neighbor_aps", neighboringAps);
                parameters.put("ssid", wifiInfo.getSSID());
                parameters.put("frequency", frequency);
                parameters.put("channel", Integer.toString(
                        convertFrequencyToChannel(
                                Integer.parseInt(frequency))));
                                
                parameters.put("signal_strength_dbm", Integer.toString(wifiInfo.getRssi()));
                parameters.put("device_name", brand + " " + model);
                parameters.put("imei", imei);
                parameters.put("os", "Android API " + version_sdk_int);

                return parameters;
            }
        };

        stringRequest.setTag("WIFI_WEATHER_TAG");

        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}
