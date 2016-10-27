package gas.apps.steve.controlgas.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.test.espresso.core.deps.guava.base.Charsets;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gas.apps.steve.controlgas.R;
import gas.apps.steve.controlgas.adapters.DispatchAdapter;
import gas.apps.steve.controlgas.bluetooth.BluetoothSPP;
import gas.apps.steve.controlgas.bluetooth.BluetoothState;
import gas.apps.steve.controlgas.bluetooth.DeviceList;
import gas.apps.steve.controlgas.entities.Dispatch;
import gas.apps.steve.controlgas.fire.DispatchFire;
import gas.apps.steve.controlgas.listeners.DispatchListener;
import gas.apps.steve.controlgas.utils.Utils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DispatchListener {


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
    /*@BindView(R.id.tv_user_email)*/
    TextView tvUserEmail;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.input_layout_message)
    TextInputLayout til;
    @BindView(R.id.aet_quantity)
    AppCompatEditText aetQuantity;

    @BindView(R.id.tv_state)
    TextView tvState;
    @BindView(R.id.tv_counter)
    TextView tvCounter;

    @BindView(R.id.fab_send)
    FloatingActionButton fabSend;
    @BindView(R.id.fab_play)
    FloatingActionButton fabPlay;
    @BindView(R.id.fab_stop)
    FloatingActionButton fabStop;

    @BindView(R.id.text_device)
    TextView tvDevice;
    @BindView(R.id.rv_dispatch)
    RecyclerView rvDispatch;


    //Bluetooth Connection vars
    BluetoothSPP bt;
    boolean DEVICE_ANDROID = false;
    private static final int REQUEST_ENABLE_BT = 1000;

    DispatchAdapter adapter;
    List<Dispatch> dispatchList = new ArrayList<>();

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    DispatchFire fire;

    String userEmail = "root";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        auth();
        initViews();
        setupBluetoothSPP();
    }

    private void auth() {
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            finish();
            startActivity(new Intent(this, LoginActivity.class));
//            finish();
        } else {
            userEmail = mFirebaseUser.getEmail();
            Log.d(TAG, "userEmail: " + userEmail);
        }
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
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        bt.stopService();
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    private void initViews() {
        ButterKnife.bind(this);
        View headerLayout = navigationView.getHeaderView(0);
        tvUserEmail = (TextView) headerLayout.findViewById(R.id.tv_user_email);

        setSupportActionBar(toolbar);

        fire = new DispatchFire();

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        navigationView.setNavigationItemSelectedListener(this);
        tvUserEmail.setText(String.format(Locale.getDefault(), getString(R.string.user_email), userEmail));

        //Default Views
        tvState.setText(getString(R.string.state_no_programmed));
        tvCounter.setText("");


        dispatchList = Dispatch.findWithQuery(Dispatch.class, "Select * from Dispatch where user = ?", userEmail);

        adapter = new DispatchAdapter(dispatchList, this, this);
        rvDispatch.setAdapter(adapter);
        rvDispatch.setLayoutManager(new LinearLayoutManager(this));

    }


    @OnClick(R.id.fab_play)
    void init() {
        if (!isDeviceConnected()) {
            return;
        }
        sendCommand(MDI_INIT);
        Snackbar.make(toolbar, getString(R.string.action_play), Snackbar.LENGTH_LONG).show();
    }

    @OnClick(R.id.fab_stop)
    void stop() {
        if (!isDeviceConnected()) {
            return;
        }
        sendCommand(MDI_STOP);
        Snackbar.make(toolbar, getString(R.string.action_stop), Snackbar.LENGTH_LONG).show();
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
                if (!isDeviceConnected()) {
                    break;
                }
                showDialogProgram();
                break;
            case R.id.action_get_last:
                if (!isDeviceConnected()) {
                    break;
                }
                sendCommand(MDI_LAST);
                Snackbar.make(toolbar, getString(R.string.action_getting_last), Snackbar.LENGTH_LONG).show();
                break;
            /*case R.id.action_get_placa:
                Snackbar.make(toolbar, "R.id.action_get_placa", Snackbar.LENGTH_LONG).show();
                break;*/
            case R.id.action_select_device:
                requestConnectDevice();
                break;
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return true;
    }

    private void showDialogProgram() {
        MaterialDialog dialog;
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_program)
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .input(R.string.hint_input_quantity, R.string.hint_input_prefill, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        dialog.dismiss();
                        sendInput(input);
                    }
                });
        dialog = builder.build();
        dialog.show();
    }

    private void sendInput(CharSequence input) {
        if (!isDeviceConnected()) {
            return;
        }
        Double quantity;
        String text = input.toString();

        try {
            quantity = Double.parseDouble(text);
        } catch (Exception e) {
            Snackbar.make(toolbar, R.string.message_invalid_quantity, Snackbar.LENGTH_LONG).show();
            return;
        }
        Snackbar.make(toolbar, String.format(Locale.US, getString(R.string.action_programming), quantity), Snackbar.LENGTH_LONG).show();
        sendCommand(MDI_PROGRAM + quantity);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_singout:
                mFirebaseAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.nav_main:
                break;
            case R.id.nav_search:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("email", userEmail);
                startActivity(intent);
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
            tvState.setText(getString(R.string.state_programmed));

            String unparse = getMdiResponse(data, MDI_SUCCESS_PROGRAM);
            String quantityFormatted = formatDoubleReceived(unparse);
            tvCounter.setText(quantityFormatted);

            Snackbar.make(toolbar, String.format(Locale.US, getString(R.string.mdi_programm_sucess), quantityFormatted), Snackbar.LENGTH_LONG).show();
            return;
        }

        if (data.contains(MDI_INIT)) {
            Snackbar.make(toolbar, R.string.mdi_init_success, Snackbar.LENGTH_LONG).show();
            tvCounter.setText("0.0");
            return;
        }
        if (data.contains(MDI_STOP)) {
            tvState.setText(getString(R.string.state_stopped));
            Snackbar.make(toolbar, R.string.mdi_stop_sucess, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (data.contains(MDI_DATASET)) {
            //Parse data
            String dataset = getMdiResponse(data, MDI_DATASET);

            Dispatch dispatch = getDispatch(dataset);
            tvState.setText(R.string.state_received);
            tvCounter.setText(formatDoubleReceived(dispatch.getGalones()));
            List<Dispatch> dispatches = Dispatch.find(Dispatch.class, "fecha_final = ?", "" + dispatch.getFechaFinal());


            if (dispatches.size() < 1) {//NO HA SIDO GUARDADO ANTERIORMENTE, GUARDAR!
                dispatch.save();
                adapter.addDispatch(dispatch);
                fire.saveDispatch(dispatch);
            }


            showDispatchData(dispatch);
            return;
        }

        Snackbar.make(toolbar, "Respuesta No Reconocida.", Snackbar.LENGTH_LONG).show();
    }

    private String getMdiResponse(String data, String prefix) {
        String[] received = data.split(prefix);
        //String prefix= received[0];
        return received[1];
    }

    private String formatDoubleReceived(String unparse){
        Double q = 0.0;
        String formatted = "";
        try {
            q = Double.parseDouble(unparse);
            formatted = String.format(Locale.US, "%.1f", q);
            return formatted;
        } catch (Exception e) {

            Log.d(TAG, "Double.parseDouble(unparse) Exception: " + e);
            Snackbar.make(toolbar, "No se pudo parsear la respuesta", Snackbar.LENGTH_LONG).show();
            return "";
        }
    }


    private Dispatch getDispatch(String dataset) {
        String[] data = dataset.split("#");

        String placa = "no-placa";

        if (data.length >= 12) {
            placa = Utils.removeNotPrintableAscii(data[11]);
        }

        return new Dispatch(
                data[1],
                data[2],
                data[3],
                data[4],
                data[5],
                data[6],
                Utils.toMilliseconds(data[7] + " " + data[8]),
                Utils.toMilliseconds(data[9] + " " + data[10]),
                userEmail,
                placa
        );
    }

    private void showDispatchData(Dispatch d) {
        String formmated = String.format(getString(R.string.dialog_content_dispatch), d.getAcumuladoAnterior(), d.getAcumulateActual(), d.getGalones(), d.getKilos(), d.getDensidad(), d.getTemperatura(), Utils.getDate(d.getFechaInicio()), Utils.getDate(d.getFechaFinal()), d.getPlaca(), d.getUser());
        MaterialDialog dialog;
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_dispatch)
                .content(formmated)
                .positiveText(R.string.button_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.build();
        dialog.setCancelable(false);
        dialog.show();
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
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String name, String address) {
                initText(name, address);
            }

            @Override
            public void onDeviceDisconnected() {
                initText(null, getString(R.string.state_no_selected));
                Snackbar.make(toolbar, "DESCONECTADO.", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onDeviceConnectionFailed() {
                initText(null, getString(R.string.state_no_selected));
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
        tvDevice.setText("Dispositivo: " + adress);
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
            initText(null, getString(R.string.state_no_selected));
            Snackbar.make(toolbar, R.string.action_select_device, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.action_select, new View.OnClickListener() {
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

    @Override
    public void onDispatchClickListener(Dispatch dispatch) {
        showDispatchData(dispatch);
    }
}
