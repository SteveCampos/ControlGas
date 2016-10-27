package gas.apps.steve.controlgas.fire;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import gas.apps.steve.controlgas.entities.Dispatch;
import gas.apps.steve.controlgas.utils.Utils;

/**
 * Created by Steve on 24/10/2016.
 */

public class DispatchFire {

    private static final String CHILD_USERS = "usuarios";
    private static final String CHILD_PLACAS = "placas";
    private static final String CHILD_DESPACHOS = "despachos";
    private static final String CHILD_ALL = "todos";
    private static final String TAG = DispatchFire.class.getSimpleName();

    private DatabaseReference databaseReference;

    private FirebaseDatabase firebaseDatabase;

    public DispatchFire() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        this.databaseReference = firebaseDatabase.getReference();
    }

    public void saveDispatch(Dispatch d) {
        if (d == null) {
            return;
        }

        String keyDispatch = databaseReference.push().getKey();
        String toUser = "/" + CHILD_USERS + "/" + Utils.cleanKey(d.getUser()) + "/" + CHILD_DESPACHOS + "/" + CHILD_PLACAS + "/" + Utils.cleanKey(d.getPlaca()) + "/" + keyDispatch;
        String toPlaca = "/" + CHILD_DESPACHOS + "/" + CHILD_PLACAS + "/" + Utils.cleanKey(d.getPlaca()) + "/" + keyDispatch;
        String toAll = "/" + CHILD_DESPACHOS + "/" + CHILD_ALL + "/" + keyDispatch;
        String toUserPlacaAllowed = "/" + CHILD_USERS + "/" + Utils.cleanKey(d.getUser()) + "/" + CHILD_PLACAS + "/" + Utils.cleanKey(d.getPlaca());

        Log.d(TAG, "toUser: "+ toUser);
        Log.d(TAG, "toPlaca: "+ toPlaca);
        Log.d(TAG, "toAll: "+ toAll);
        Log.d(TAG, "toUserPlacaAllowed: "+ toUserPlacaAllowed);


        Map<String, Object> dispatchMap = new HashMap<>();
        dispatchMap.put(toUser, d.toMap());
        dispatchMap.put(toPlaca, d.toMap());
        dispatchMap.put(toAll, d.toMap());
        dispatchMap.put(toUserPlacaAllowed, true);
        databaseReference.updateChildren(dispatchMap);
    }

    public void listenPlacasAllowed(String email, ValueEventListener valueEventListener) {
        databaseReference.child(CHILD_USERS).child(Utils.cleanKey(email)).child(CHILD_PLACAS).addListenerForSingleValueEvent(valueEventListener);
    }

    public void listenForPlacaAllowed(String email, String placa, ValueEventListener listener) {
        databaseReference.child(CHILD_USERS).child(Utils.cleanKey(email)).child(CHILD_PLACAS).child(Utils.cleanKey(placa)).addValueEventListener(listener);
    }

    public void listenForPlaca(String placa, ValueEventListener listener) {
        databaseReference.child(CHILD_DESPACHOS).child(CHILD_PLACAS).child(Utils.cleanKey(placa)).addValueEventListener(listener);
    }


}

