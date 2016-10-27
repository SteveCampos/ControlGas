package gas.apps.steve.controlgas.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import gas.apps.steve.controlgas.R;
import gas.apps.steve.controlgas.entities.Dispatch;
import gas.apps.steve.controlgas.holders.DispatchHolder;
import gas.apps.steve.controlgas.listeners.DispatchListener;
import gas.apps.steve.controlgas.utils.Utils;

/**
 * Created by Steve on 24/10/2016.
 */

public class DispatchAdapter extends RecyclerView.Adapter<DispatchHolder> {


    private static final String TAG = DispatchAdapter.class.getSimpleName();
    // Store a member variable for the list;
    private List<Dispatch> list;
    // Store the context for easy access
    private Context mContext;

    private DispatchListener listener;

    public DispatchAdapter(List<Dispatch> list, Context mContext, DispatchListener listener) {
        this.list = list;
        this.mContext = mContext;
        this.listener = listener;
    }

    @Override
    public DispatchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // Inflate the custom layout
        View view = inflater.inflate(R.layout.item_despacho, parent, false);
        // Return a new holder instance
        return new DispatchHolder(view);
    }

    public void addDispatch(Dispatch dispatch){
        list.add(dispatch);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(DispatchHolder holder, int position) {
        final Dispatch dispatch = list.get(position);


        holder.tvDate.setText(dispatch.getFormatFechaFinal());
        holder.tvGalones.setText(dispatch.getGalones());
        holder.textPlaca.setText(dispatch.getPlaca());
        holder.cvContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDispatchClickListener(dispatch);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
