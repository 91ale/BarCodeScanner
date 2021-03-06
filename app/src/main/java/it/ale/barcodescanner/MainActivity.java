package it.ale.barcodescanner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String URL_PRODUCTS = "http://192.168.42.50/select_from_bc.php?bc=";
    String currentBC = "";

    List<Product> productList = new ArrayList<>();

    RecyclerView recyclerView;
    ProductAdapter Padapter;
    TextView txtPrezzoTotale;
    SearchView searchView = null;
    //formato di visulizzazione dei prezzi
    DecimalFormat pdec = new DecimalFormat("€ 0.00");
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration itemDecor = new DividerItemDecoration(this, 1);
        recyclerView.addItemDecoration(itemDecor);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        recyclerView.requestFocus();

        txtPrezzoTotale = findViewById(R.id.txtPrezzoTotale);
    }

    @Override
    public void onBackPressed() {
        drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
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
        if (id == R.id.nuova_spesa) {
            if (Padapter != null) {
                Padapter.removeAllItem();
                Padapter.notifyDataSetChanged();
            }
            txtPrezzoTotale.setText("");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_leggiBC) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_aggiungiprodotto) {
            Intent intent = new Intent(this, AggiungiProdotto.class);
            startActivity(intent);
        } else if (id == R.id.nav_ricercaprodotto) {
            Intent intent = new Intent(this, RicercaProdotto.class);
            startActivity(intent);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * callback when recycler view is swiped
     * item will be removed on swiped
     * undo option will be provided in snackbar to restore the item
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ProductAdapter.ProductViewHolder) {
            // acquisisce il nome dell'oggetto eliminato per visualizzarlo sulla snackbar
            String prodotto = productList.get(viewHolder.getAdapterPosition()).getmarca().toUpperCase() + " " + productList.get(viewHolder.getAdapterPosition()).getnome().toUpperCase();

            // backup dell'oggetto rimosso per un eventuale ripristino
            final Product deletedItem = productList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // rimuove l'oggetto dalla recycler e dalla lista prodotti
            Padapter.removeItem(viewHolder.getAdapterPosition());
            double totalespesa = Padapter.sumAllItem();
            txtPrezzoTotale.setText(pdec.format(totalespesa));

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(drawerLayout, prodotto + " è stato rimosso dalle scansioni", Snackbar.LENGTH_LONG);
            snackbar.setAction("ANNULLA", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    Padapter.restoreItem(deletedItem, deletedIndex);
                    double totalespesa = Padapter.sumAllItem();
                    txtPrezzoTotale.setText(pdec.format(totalespesa));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    /*
    * Quando viene premuto un tasto viene eseguito il metodo
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //controlla se il carattere acquisito è quello di invio(66) (carattere di terminazione del BC)
        if (keyCode == 66) {
            //esegue la query condizionata dal BC scansionato
            sendRequest();
            //variabile temp a null
            currentBC = "";
        } else {
            //converte il tasto premuto in Unicode
            char pressedKey = (char) event.getUnicodeChar();
            //aggiunge la cifra alla variabile currentBC
            currentBC += pressedKey;
        }
        return true;
    }

    private void sendRequest() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_PRODUCTS + currentBC/*scannedBC.remove(scannedBC.size() - 1)*/,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseJSON pj = new ParseJSON(response);
                        pj.getProductFromDB();
                        productList.addAll(pj.getProduct());
                        //crea l'adapter e lo assegna alla recycleview
                        Padapter = new ProductAdapter(MainActivity.this, productList);
                        double totalespesa = Padapter.sumAllItem();
                        txtPrezzoTotale.setText(pdec.format(totalespesa));
                        recyclerView.setAdapter(Padapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //aggiunge la stringrequest alla coda
        Volley.newRequestQueue(this).add(stringRequest);
    }

}
