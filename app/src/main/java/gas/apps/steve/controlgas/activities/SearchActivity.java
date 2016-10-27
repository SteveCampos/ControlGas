package gas.apps.steve.controlgas.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gas.apps.steve.controlgas.R;
import gas.apps.steve.controlgas.adapters.DispatchAdapter;
import gas.apps.steve.controlgas.adapters.SpinnerToolbarAdapter;
import gas.apps.steve.controlgas.entities.Dispatch;
import gas.apps.steve.controlgas.fire.DispatchFire;
import gas.apps.steve.controlgas.listeners.DispatchListener;
import gas.apps.steve.controlgas.utils.Utils;

public class SearchActivity extends AppCompatActivity implements DispatchListener {


    private static final String TAG = SearchActivity.class.getSimpleName();
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rv_dispatch)
    RecyclerView rvDispatchs;

    @BindView(R.id.text_placa) TextView tvPlacas;
    @BindView(R.id.spn_placas)
    AppCompatSpinner spnPlacas;

    DispatchFire fire;
    String email;
    List<String> placas;
    List<Dispatch> list = new ArrayList<>();
    DispatchAdapter adapter;

    String lastPlacaSelected = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        email = getIntent().getExtras().getString("email", "no-email@gmail.com");
        init();
    }

    private void init() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        fire = new DispatchFire();
        spnPlacas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                lastPlacaSelected = adapterView.getItemAtPosition(i).toString();
                //Snackbar.make(toolbar, placaSelected, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        fire.listenPlacasAllowed(email, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot==null){ return;}

                Log.d(TAG, "listenPlacasAllowed dataSnapshot: " + dataSnapshot);
                if (dataSnapshot.getChildrenCount()>0){
                    placas = new ArrayList<>();
                    for (DataSnapshot d: dataSnapshot.getChildren()){
                        Log.d(TAG, "DataSnapshot d: dataSnapshot.getChildren(): " + d);
                        placas.add(d.getKey());
                    }
                    SpinnerToolbarAdapter adapter = new SpinnerToolbarAdapter(getActivity(), placas);
                    spnPlacas.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "listenPlacasAllowed databaseError: " + databaseError);
            }
        });
    }

    public AppCompatActivity getActivity(){
        return this;
    }



    @OnClick(R.id.fab)
    public void fabClick(){
        showDialogPlaca();
    }
    private void showDialogPlaca() {
        MaterialDialog dialog;
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_search)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.hint_input_placa), lastPlacaSelected, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        dialog.dismiss();
                        manageInput(input.toString());
                    }
                })
                ;
        dialog = builder.build();
        dialog.show();
    }

    private void manageInput(final String placa) {
        showMessage(R.string.action_searching);

        fire.listenForPlacaAllowed(email, placa.replaceAll("\\s+", ""), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot==null){
                    return;
                }
                if (dataSnapshot.getValue()==null){
                    showMessage(R.string.message_not_permission);
                    return;
                }
                tvPlacas.setText("Buscando por placa: "+ placa);
                getDispatchs(placa);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDispatchs(String placa){
        fire.listenForPlaca(placa, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "listenForPlaca dataSnapshot: " + dataSnapshot);
                if (dataSnapshot ==null){
                    return;
                }
                if (dataSnapshot.getChildrenCount()>0){
                    list = new ArrayList<>();
                    for (DataSnapshot d: dataSnapshot.getChildren()){
                        Dispatch dispatch = d.getValue(Dispatch.class);
                        Log.d(TAG, "dispatch.getUser(): " + dispatch.getUser());
                        list.add(dispatch);
                    }
                    adapter = new DispatchAdapter(list, getActivity(), SearchActivity.this);
                    rvDispatchs.setAdapter(adapter);
                    rvDispatchs.setLayoutManager(new LinearLayoutManager(getActivity()));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(toolbar, String.format(Locale.getDefault(), getString(R.string.error), databaseError.getMessage()), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showMessage(int message){
        Snackbar.make(toolbar, message, Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void onDispatchClickListener(Dispatch dispatch) {
        showDispatchData(dispatch);
    }

    private void showDispatchData(Dispatch d){
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
                })
                ;
        dialog = builder.build();
        dialog.setCancelable(false);
        dialog.show();
    }
}
