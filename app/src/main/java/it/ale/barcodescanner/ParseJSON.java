package it.ale.barcodescanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ParseJSON {

    List<Product> productList;

    private String json;

    public ParseJSON(String json) {

        this.json = json;
    }


    protected void getProductFromDB() {

        try {
            //converte la stringa in array JSON
            JSONArray array = new JSONArray(json);

            productList = new ArrayList<>();

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

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    List<Product> getProduct() {
        return productList;
    }

}
