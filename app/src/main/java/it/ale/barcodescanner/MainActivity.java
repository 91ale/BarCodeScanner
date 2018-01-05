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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String URL_PRODUCTS = "http://192.168.42.50/select_from_bc.php?bc=";
    String currentBC = "";
    List<String> scannedBC = new ArrayList<>();

    List<Product> productList = new ArrayList<>();

    RecyclerView recyclerView;
    ProductAdapter Padapter;
    SearchView searchView = null;
    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        recyclerView = findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration itemDecor = new DividerItemDecoration(this, 1);
        recyclerView.addItemDecoration(itemDecor);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
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
        if (id == R.id.action_settings) {
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
        }
        else if (id == R.id.nav_ricercaprodotto) {
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
            // get the removed item name to display it in snack bar
            String prodotto = productList.get(viewHolder.getAdapterPosition()).getmarca().toUpperCase() + " " + productList.get(viewHolder.getAdapterPosition()).getnome().toUpperCase() ;

            // backup of removed item for undo purpose
            final Product deletedItem = productList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            Padapter.removeItem(viewHolder.getAdapterPosition());

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(drawerLayout, prodotto + " è stato rimosso dalle scansioni", Snackbar.LENGTH_LONG);
            snackbar.setAction("ANNULLA", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    Padapter.restoreItem(deletedItem, deletedIndex);
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
        if(keyCode==66) {
            //aggiunge il codice composto alla lista dei codici
            scannedBC.add(currentBC);
            //varibile temp a null
            currentBC="";
            //esegue la query condizionata dal BC scansionato
            loadProducts();
        }
        else {
            //converte il tasto premuto in Unicode
            char pressedKey = (char) event.getUnicodeChar();
            //aggiunge la cifra alla variabile currentBC
            currentBC += pressedKey;
        }
        return true;
    }

    private void loadProducts() {

        /*
        *Crea una string request
        * la richiesta è di tipo GET
        * L'URL della richiesta è definito nel secondo paramrtro
        * nel response listener otteniamo la risposta JSON come stringa
         */
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_PRODUCTS+scannedBC.remove(scannedBC.size()-1),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converte la stringa in array JSON
                            JSONArray array = new JSONArray(response);

                            //passa da tutti gli oggetti
                            for (int i = 0; i < array.length(); i++) {

                                //prende il prodotto dall'array JSON
                                JSONObject product = array.getJSONObject(i);

                                //aggiunge il prodotto alla lista
                                productList.add(0, new Product(
                                        product.getInt("id"),
                                        product.getString("bc"),
                                        product.getString("nome"),
                                        product.getDouble("prezzoa"),
                                        product.getDouble("prezzov"),
                                        product.getString("marca")
                                ));
                            }
                            //crea l'adapter e lo assegna alla recycleview
                            Padapter = new ProductAdapter(MainActivity.this, productList);
                            recyclerView.setAdapter(Padapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
