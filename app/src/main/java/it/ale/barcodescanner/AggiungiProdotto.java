package it.ale.barcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class AggiungiProdotto extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

    private static final String URL_PRODUCTS_INSERT = "http://192.168.1.33/insert.php?";
    private static final String URL_PRODUCTS_SELECT = "http://192.168.1.33/select_from_bc.php?bc=";
    List<String> scannedBC = new ArrayList<>();
    String currentBC="";
    List<Product> productList = new ArrayList<>();

    TextView txtBC;
    EditText edtNomeProdotto;
    EditText edtPrezzoAcquisto;
    EditText edtPrezzoVendita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggiungi_prodotto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button btnAggiungiProdotto = findViewById(R.id.btnAggiungiProdotto);
        edtNomeProdotto = findViewById(R.id.edtNomeProdotto);
        edtPrezzoAcquisto = findViewById(R.id.edtPrezzoAcquisto);
        edtPrezzoVendita = findViewById(R.id.edtPrezzoVendita);

        txtBC = findViewById(R.id.txtBC);
        txtBC.requestFocus();

        btnAggiungiProdotto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String NomeProdotto = edtNomeProdotto.getText().toString();
                final String PrezzoAcquisto = edtPrezzoAcquisto.getText().toString();
                final String PrezzoVendita = edtPrezzoVendita.getText().toString();
                loadProducts(NomeProdotto,PrezzoAcquisto,PrezzoVendita);
            }
        });

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //controlla se il carattere acquisito è quello di invio(66) (carattere di terminazione del BC)
        if(keyCode==66) {
            //aggiunge il codice composto alla lista dei codici
            scannedBC.add(currentBC);
            //varibile temp a null
            currentBC="";
            //esegue la query condizionata dal BC scansionato
            txtBC.setText(scannedBC.get(scannedBC.size()-1));
            checkExist();
        }
        else {
            //converte il tasto premuto in Unicode
            char pressedKey = (char) event.getUnicodeChar();
            //aggiunge la cifra alla variabile temp
            currentBC += pressedKey;
        }
        return true;
    }

    private void checkExist() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_PRODUCTS_SELECT+scannedBC.get(scannedBC.size()-1),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject product = array.getJSONObject(i);

                                //adding the product to product list
                                productList.add(new Product(
                                        product.getInt("id"),
                                        product.getString("bc"),
                                        product.getString("nome"),
                                        product.getDouble("prezzoa"),
                                        product.getDouble("prezzov"),
                                        product.getString("marca"),
                                        product.getInt("giacenza")
                                ));

                                if (product.length()!=0) {

                                    edtNomeProdotto.setText(product.getString("nome"));
                                    edtPrezzoAcquisto.setText(product.getString("prezzoa"));
                                    edtPrezzoVendita.setText(product.getString("prezzov"));
                                    Log.v("NOME_P",product.getString("nome"));
                                }
                            }

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

        //adding our stringrequest to queue
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void loadProducts(String NomeProdotto, String PrezzoAcquisto, String PrezzoVendita) {

        /*
        * Creating a String Request
        * The request type is GET defined by first parameter
        * The URL is defined in the second parameter
        * Then we have a Response Listener and a Error Listener
        * In response listener we will get the JSON response as a String
        * */

        StringRequest stringRequestAdd = new StringRequest(Request.Method.GET, URL_PRODUCTS_INSERT+"id="+productList.get(productList.size()-1).getId()+"&"+"bc="+scannedBC.get(scannedBC.size()-1)+"&"+"nome="+NomeProdotto+"&"+"prezzoa="+PrezzoAcquisto+"&"+"prezzov="+PrezzoVendita,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //adding our stringrequest to queue
        Volley.newRequestQueue(this).add(stringRequestAdd);
    }
}
