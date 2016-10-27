package gas.apps.steve.controlgas.holders;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import gas.apps.steve.controlgas.R;

/**
 * Created by Steve on 24/10/2016.
 */

public class DispatchHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text_date)
    public TextView tvDate;
    @BindView(R.id.text_galones)
    public TextView tvGalones;
    @BindView(R.id.text_placa)
    public TextView textPlaca;

    @BindView(R.id.cv_container)
    public CardView cvContainer;

    public DispatchHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
