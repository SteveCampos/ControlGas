package gas.apps.steve.controlgas.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.test.espresso.core.deps.guava.base.Charsets;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gas.apps.steve.controlgas.R;
import gas.apps.steve.controlgas.bluetooth.BluetoothSPP;
import gas.apps.steve.controlgas.bluetooth.BluetoothState;
import gas.apps.steve.controlgas.bluetooth.DeviceList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String MDI_PROGRAM = "#PP";
    public static final String MDI_INIT = "#ID";
    public static final String MDI_COUNTER = "#LP";
    public static final String MDI_STOP = "#TD";
    public static final String MDI_LAST = "#UD";
    public static final String MDI_DATASET = "#TR";

    private final static String MDI_ERROR = "#ER";
    private final static String MDI_SUCCESS_PROGRAM = "#CN";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.input_layout_message)
    TextInputLayout til;
    @BindView(R.id.aet_quantity)
    AppCompatEditText aetQuantity;

    @BindView(R.id.tv_mac)
    TextView tvMac;
    @BindView(R.id.tv_counter)
    TextView tvCounter;

    @BindView(R.id.fab_send)
    FloatingActionButton fabSend;


    //Bluetooth Connection vars
    BluetoothSPP bt;
    boolean DEVICE_ANDROID = false;
    private static final int REQUEST_ENABLE_BT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        initViews();
        setupBluetoothSPP();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        bt.stopService();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    private void initViews() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        navigationView.setNavigationItemSelectedListener(this);
    }


    @OnClick(R.id.fab_play)
    void init() {
        if (!isDeviceConnected()){
            return;
        }
        sendCommand(MDI_INIT);
        Snackbar.make(toolbar, "play", Snackbar.LENGTH_LONG).show();
    }

    @OnClick(R.id.fab_stop)
    void stop() {
        if (!isDeviceConnected()){
            return;
        }
        Snackbar.make(toolbar, "stop", Snackbar.LENGTH_LONG).show();
        sendCommand(MDI_STOP);
    }

    @OnClick(R.id.fab_send)
    void send() {
        if (!isDeviceConnected()){
            return;
        }
        Double quantity = 0.0;
        String text = aetQuantity.getText().toString();

        try{
            quantity = Double.parseDouble(text);
        }catch (Exception e){
            Snackbar.make(toolbar, R.string.message_invalid_quantity, Snackbar.LENGTH_LONG).show();
            return;
        }
        setProgramVisible(View.GONE);
        Snackbar.make(toolbar, String.format(Locale.US, getString(R.string.action_programming), quantity), Snackbar.LENGTH_INDEFINITE).show();
        sendCommand(MDI_PROGRAM + String.format(Locale.US, "%.1f", quantity));
    }

    private void setProgramVisible(int visibility) {
        til.setVisibility(visibility);
        fabSend.setVisibility(visibility);
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_programm:
                setProgramVisible(View.VISIBLE);
                break;
            case R.id.action_get_last:
                Snackbar.make(toolbar, "R.id.action_get_last", Snackbar.LENGTH_LONG).show();
                break;
            case R.id.action_get_placa:
                Snackbar.make(toolbar, "R.id.action_get_placa", Snackbar.LENGTH_LONG).show();
                break;
            case R.id.action_select_device:
                requestConnectDevice();
                break;
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);

                break;
        }
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_main:
                break;
            case R.id.nav_search:
                break;
            default:
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private AppCompatActivity getActivity() {
        return this;
    }


    private void sendCommand(String command) {
        char STX = (char) 2;
        char ETX = (char) 3;
        byte[] bytes = (STX + command + ETX).getBytes(Charsets.US_ASCII);
        bt.send(bytes, false);
    }

    private void manageReceivedData(byte[] bytes, String data) {

        if (data.isEmpty()) {
            return;
        }
        if (data.contains(MDI_COUNTER)) {
            tvCounter.setText(getMdiResponse(data, MDI_COUNTER));
            return;
        }

        if (data.contains(MDI_ERROR)) {
            Snackbar.make(toolbar, String.format(Locale.US, getString(R.string.error), getMdiResponse(data, MDI_ERROR)), Snackbar.LENGTH_LONG).show();
            return;
        }
        if (data.contains(MDI_SUCCESS_PROGRAM)) {
            tvMac.append("\n" + getString(R.string.state_programmed));

            Double q = 0.0;
            String unparse = getMdiResponse(data, MDI_SUCCESS_PROGRAM);
            String formatted = "";
            try{
                q = Double.parseDouble(unparse);
                formatted = String.format(Locale.US, "%.1f", q);
            }catch (Exception e){
                Log.d(TAG, "Double.parseDouble(unparse) Exception: " + e);
                Snackbar.make(toolbar, "No se pudo parsear la respuesta", Snackbar.LENGTH_LONG).show();
            }

            tvCounter.setText(formatted);

            Snackbar.make(toolbar, String.format(Locale.US, getString(R.string.mdi_programm_sucess), q), Snackbar.LENGTH_LONG).show();
            return;
        }

        if (data.contains(MDI_INIT)){
            Snackbar.make(toolbar, R.string.mdi_init_success, Snackbar.LENGTH_LONG).show();
            tvCounter.setText("0.0");
            return;
        }
        if (data.contains(MDI_STOP)){
            Snackbar.make(toolbar, R.string.mdi_stop_sucess, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (data.contains(MDI_DATASET)){
            //Parse data
            return;
        }

        Snackbar.make(toolbar, "Respuesta No Reconocida.", Snackbar.LENGTH_LONG).show();
    }
    private String getMdiResponse(String data, String prefix) {
        String[] received = data.split(prefix);
        //String prefix= received[0];
        return received[1];
    }

    //BLUETOTH. Functionality
    private void setupBluetoothSPP() {

        bt = new BluetoothSPP(getActivity());
        if (!bt.isBluetoothAvailable()) {
            // any command for bluetooth is not available
            Toast.makeText(getActivity(), "!bt.isBluetoothAvailable()", Toast.LENGTH_LONG).show();
        }


        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                // Do something when data incoming
                Log.d(TAG, "data: " + data + ", message>: " + message);

                manageReceivedData(data, message);
                /*
                MessageBluetooth messageBluetooth = new MessageBluetooth(
                        message,
                        getBytesToString(data),
                        new String(data, Charsets.US_ASCII),
                        "US_ASCII",
                        new Date().getTime(),
                        "RECEIVED",
                        "",
                        bt.getConnectedDeviceAddress()
                );

                fireBluetooth.saveMessage(messageBluetooth);
                textReaded.append("\n " + new String(Utils.clearUnsigned(data), Charsets.US_ASCII));

                scrollText();*/

            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String name, String address) {
                initText(name, address);
            }

            @Override
            public void onDeviceDisconnected() {
                initText(null, null);
                Snackbar.make(toolbar, "DESCONECTADO.", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onDeviceConnectionFailed() {
                initText(null, null);
                Snackbar.make(toolbar, "FALLÓ LA CONEXIÓN. ASEGURESE QUE EL BLUETOOTH ESTÉ ENCENDIDO.", Snackbar.LENGTH_LONG).show();
            }
        });


        if (!bt.isBluetoothEnabled()) {
            // Do somthing if bluetooth is disable
            enableBluetooth();
        } else {

            bt.setupService();
            bt.startService(DEVICE_ANDROID);

            initViews();
            //startConnect();
            // Do something if bluetooth is already enable
        }
    }

    private void initText(String device, String adress) {
        //textDevice.setText("DEVICE: " + device);
        tvMac.setText("Adress: " + adress);
        //textReaded.setText("LOG: \n");
    }

    private void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void requestConnectDevice() {
        Intent intent = new Intent(getApplicationContext(), DeviceList.class);
        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
    }

    private boolean isDeviceConnected() {
        if (TextUtils.isEmpty(bt.getConnectedDeviceAddress())) {
            initText(null, null);
            Snackbar.make(toolbar, R.string.action_select_device, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.action_go, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestConnectDevice();
                        }
                    })
                    .show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    Snackbar.make(toolbar, "Bluetooth Encendido", Snackbar.LENGTH_LONG).show();
                    bt.setupService();
                    bt.startService(DEVICE_ANDROID);
                    initViews();
                } else if (resultCode == RESULT_CANCELED) {
                    Snackbar.make(toolbar, "Por favor. Encender Bluetooth", Snackbar.LENGTH_LONG).show();
                }
                break;
            case BluetoothState.REQUEST_CONNECT_DEVICE:

                if (resultCode == Activity.RESULT_OK)

                    if (data != null) {

                        Snackbar.make(toolbar, "Dispositivo Seleccionado: " + data.getStringExtra(BluetoothState.EXTRA_DEVICE_ADDRESS), Snackbar.LENGTH_LONG).show();

                        if (bt == null) {
                            Toast.makeText(getActivity(), "bt==null", Toast.LENGTH_LONG).show();
                        }
                        initViews();
                        bt.connect(data);
                    } else {
                        Snackbar.make(toolbar, "No selecciono ningún dispositivo", Snackbar.LENGTH_LONG).show();
                    }

                break;
        }
    }
}
