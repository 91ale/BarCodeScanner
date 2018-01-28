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
 * Classe utilizzata per la gestione della recycleview e dei suoi elementi
 */

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context mCtx;
    private List<Product> productList;

    public ProductAdapter(Context mCtx, List<Product> productList) {
        this.mCtx = mCtx;
        this.productList = productList;
    }


    //impostazione layout della recycleview
    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.product_list, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new ProductViewHolder(view);
    }

    //imposta il corretto formato di visualizzazione dei prezzi dei prodotti e visualizza in maiuscolo il nome e la marca dei prodotti nella recycleview
    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        DecimalFormat prezzovdec = new DecimalFormat("€ 0.00");

        holder.txtNomeProdotto.setText(product.getmarca().toUpperCase() + " " + product.getnome().toUpperCase());
        holder.txtPrezzoV.setText(prezzovdec.format(product.getprezzov()));
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

    //restituisce la lunchezza della lista prodotti
    @Override
    public int getItemCount() {
        return productList.size();
    }

    //rimuove l'oggetto nella posizione passate
    public void removeItem(int position) {
        productList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    //ripristina l'oggetto nella posizione passata
    public void restoreItem(Product item, int position) {
        productList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    //rimuove tutti gli oggetti dalla lista
    public void removeAllItem() {
        productList.clear();
    }

    //restituisce la somma dei prezzi dei prodotti in lista
    public double sumAllItem() {
        int i;
        double sum = 0;
        for(i = 0; i < productList.size(); i++)
            sum += productList.get(i).getprezzov();
        return sum;
    }

}