package it.ale.barcodescanner;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by hale on 22/10/2017.
 */

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {


    private Context mCtx;
    private List<Product> productList;

    public ProductAdapter(Context mCtx, List<Product> productList) {
        this.mCtx = mCtx;
        this.productList = productList;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.product_list, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        DecimalFormat prezzovdec = new DecimalFormat("€ #.00");

        holder.txtNomeProdotto.setText(product.getmarca().toUpperCase() + " " + product.getnome().toUpperCase());
        holder.txtPrezzoV.setText(prezzovdec.format(product.getprezzov()));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView txtNomeProdotto, txtPrezzoV;
        public RelativeLayout viewBackground;
        public ConstraintLayout viewForeground;

        public ProductViewHolder(View itemView) {
            super(itemView);

            txtNomeProdotto = itemView.findViewById(R.id.txtNomeProdotto);
            txtPrezzoV = itemView.findViewById(R.id.txtPrezzoV);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
        }
    }

    public void removeItem(int position) {
        productList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreItem(Product item, int position) {
        productList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }
}