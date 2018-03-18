package it.ale.barcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.DecimalFormat;
import java.util.Locale;

public class AggiungiProdotto extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String URL_PRODUCTS_INSERT = "http://192.168.42.50/insert.php?";
    private static final String URL_PRODUCTS_SELECT = "http://192.168.42.50/select_from_bc.php?bc=";
    //private static final String URL_PRODUCTS_INSERT = "http://192.168.1.33/insert.php?";
    //private static final String URL_PRODUCTS_SELECT = "http://192.168.1.33/select_from_bc.php?bc=";
    Product prodottocorrente;
    String currentBC = "";
    int IVA = 0;
    TextView txtBC;
    EditText edtMarcaProdotto;
    EditText edtNomeProdotto;
    EditText edtPrezzoAcquisto;
    EditText edtPrezzoVendita;
    EditText edtRicaricoProdotto;
    RadioGroup rgrIVA;
    int existBC = 0;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aggiungi_prodotto);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button btnAggiungiProdotto = findViewById(R.id.btnAggiungiProdotto);
        edtMarcaProdotto = findViewById(R.id.edtMarcaProdotto);
        edtNomeProdotto = findViewById(R.id.edtNomeProdotto);
        edtPrezzoAcquisto = findViewById(R.id.edtPrezzoAcquisto);
        edtPrezzoVendita = findViewById(R.id.edtPrezzoVendita);
        edtRicaricoProdotto = findViewById(R.id.edtRicaricoProdotto);
        rgrIVA = findViewById(R.id.rgrIVA);

        txtBC = findViewById(R.id.txtBC);
        txtBC.requestFocus();

        rgrIVA.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                float PrezzoAcquisto = 0;
                float RicaricoProdotto = 0;
                float PrezzoVendita = 0;

                switch (checkedId) {
                    case R.id.rbt4:
                        IVA = 4;
                        break;
                    case R.id.rbt10:
                        IVA = 10;
                        break;
                    case R.id.rbt22:
                        IVA = 22;
                        break;
                }

                if (!edtPrezzoAcquisto.getText().toString().equals("") && !edtRicaricoProdotto.getText().toString().equals("")) {
                    PrezzoAcquisto = Float.parseFloat(edtPrezzoAcquisto.getText().toString().replace(",", "."));
                    RicaricoProdotto = Float.parseFloat(edtRicaricoProdotto.getText().toString());
                    PrezzoVendita = (PrezzoAcquisto * (100 + RicaricoProdotto) / 100) * (100 + IVA) / 100;
                    edtPrezzoVendita.setText(String.format(Locale.ITALY, "%.2f", PrezzoVendita));
                }
            }
        });

        btnAggiungiProdotto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String MarcaProdotto = edtMarcaProdotto.getText().toString();
                final String NomeProdotto = edtNomeProdotto.getText().toString();
                final String PrezzoAcquisto = edtPrezzoAcquisto.getText().toString();
                final String PrezzoVendita = edtPrezzoVendita.getText().toString();
                loadProducts(NomeProdotto, PrezzoAcquisto, PrezzoVendita, MarcaProdotto);
                resetCampi();
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
        } else if (id == R.id.nav_ricercaprodotto) {
            Intent intent = new Intent(this, RicercaProdotto.class);
            startActivity(intent);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //controlla se il carattere acquisito è quello di invio(66) (carattere di terminazione del BC)
        if (keyCode == 66) {
            //esegue la query condizionata dal BC scansionato
            txtBC.setText(currentBC);
            //se il prodotto scansionato è presente nel DB recupera i dati relativi e valorizza di conseguenza le textbox
            checkExist();
            //varibile temp a null
            currentBC = "";
        } else {
            //converte il tasto premuto in Unicode
            char pressedKey = (char) event.getUnicodeChar();
            //aggiunge la n-esima cifra del BC alla variabile temporanea
            currentBC += pressedKey;
        }
        return true;
    }

    private void checkExist() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_PRODUCTS_SELECT + currentBC,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParseJSON pj = new ParseJSON(response);
                        pj.getProductFromDB();
                        prodottocorrente = pj.getProduct().get(0);
                        //se il prodotto corrispondente al codice scansionato è già presente nel DB valorizza le textview con i dati presenti nel DB
                        if (pj.getProduct().size() != 0) {
                            DecimalFormat prezzodec = new DecimalFormat("0.00");
                            edtMarcaProdotto.setText(prodottocorrente.getmarca());
                            edtNomeProdotto.setText(prodottocorrente.getnome());
                            edtPrezzoAcquisto.setText(prezzodec.format(prodottocorrente.getprezzoa()).replace(".", ","));
                            edtPrezzoVendita.setText(prezzodec.format(prodottocorrente.getprezzov()).replace(".", ","));
                            prodottocorrente = pj.getProduct().get(0);
                            existBC = 1;
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

    private void resetCampi() {
        txtBC.setText("");
        edtMarcaProdotto.setText("");
        edtNomeProdotto.setText("");
        edtPrezzoAcquisto.setText("");
        edtPrezzoVendita.setText("");
        edtRicaricoProdotto.setText("");
        rgrIVA.clearCheck();
        txtBC.requestFocus();
    }

    private void loadProducts(String NomeProdotto, String PrezzoAcquisto, String PrezzoVendita, String MarcaProdotto) {

        /*
        * Creating a String Request
        * The request type is GET defined by first parameter
        * The URL is defined in the second parameter
        * Then we have a Response Listener and a Error Listener
        * In response listener we will get the JSON response as a String
        * */

        String queryurl;
        PrezzoAcquisto = PrezzoAcquisto.replace(",", ".");
        PrezzoVendita = PrezzoVendita.replace(",", ".");

        if (existBC == 1) {
            queryurl = URL_PRODUCTS_INSERT + "id=" + prodottocorrente.getId() + "&" + "bc=" + currentBC + "&" + "nome=" + NomeProdotto + "&" + "prezzoa=" + PrezzoAcquisto + "&" + "prezzov=" + PrezzoVendita + "&" + "marca=" + MarcaProdotto;

            //azzera la variabile che identifica se il prodotto scansionato esiste già nel DB
            existBC = 0;
        } else {
            queryurl = URL_PRODUCTS_INSERT + "bc=" + currentBC + "&" + "nome=" + NomeProdotto + "&" + "prezzoa=" + PrezzoAcquisto + "&" + "prezzov=" + PrezzoVendita + "&" + "marca=" + MarcaProdotto;
        }

        StringRequest stringRequestAdd = new StringRequest(Request.Method.GET, queryurl,
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
