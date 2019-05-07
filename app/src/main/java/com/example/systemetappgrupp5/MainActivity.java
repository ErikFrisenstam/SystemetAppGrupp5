package com.example.systemetappgrupp5;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

  private static final String LOG_TAG = MainActivity.class.getSimpleName();
  private List<Product> products;
  private ListView listView;
  private ArrayAdapter<Product> adapter;

  private static final String MIN_ALCO = "min_alcohol";
  private static final String MAX_ALCO = "max_alcohol";
  private static final String MIN_PRICE = "min_price";
  private static final String MAX_PRICE = "max_price";
  private static final String TYPE = "product_group";
  private static final String NAME = "name";


  private void createArrayList() {
    products = new ArrayList<>();
  }


  private void setupListView() {
    // look up a reference to the ListView object
    listView = findViewById(R.id.product_list);

    // create an adapter (with the faked products)
    adapter = new ArrayAdapter<Product>(this,
        android.R.layout.simple_list_item_1,
        products);

    listView.setOnItemClickListener(new ListView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent,
          final View view,
          int position /*The position of the view in the adapter.*/,
          long id /* The row id of the item that was clicked */) {
        Log.d(LOG_TAG, "item clicked, pos:" + position + " id: " + id);

        Product p = products.get(position);
        Intent intent = new Intent(MainActivity.this, ProductActivity.class);
        intent.putExtra("product", p);
        startActivity(intent);

      }
    });

    // Set listView's adapter to the new adapter
    listView.setAdapter(adapter);
  }

  // get the entered text from a view
  private String valueFromView(View inflated, int viewId) {
    return ((EditText) inflated.findViewById(viewId)).getText().toString();
  }

  // if the value is valid, add it to the map
  private void addToMap(Map<String, String> map, String key, String value) {
    if (value!=null && !value.equals("")) {
      map.put(key, value);
    }
  }

  private void showSearchDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Sök produkter");
    final View viewInflated = LayoutInflater
        .from(this).inflate(R.layout.search_dialog, null);

    builder.setView(viewInflated);

    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        // Create a map to pass to the search method
        // The map makes it easy to add more search parameters with no changes in method signatures
        Map<String, String> arguments = new HashMap<>();

        // Add user supplied argument (if valid) to the map
        addToMap(arguments, MIN_ALCO, valueFromView(viewInflated, R.id.min_alco_input));
        addToMap(arguments, MAX_ALCO, valueFromView(viewInflated, R.id.max_alco_input));
        addToMap(arguments, MIN_PRICE, valueFromView(viewInflated, R.id.min_price_input));
        addToMap(arguments, MAX_PRICE, valueFromView(viewInflated, R.id.max_price_input));

        // Given the map, s earch for products and update the listview
        searchProducts(arguments);
      }
    });
    builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Log.d(LOG_TAG, " User cancelled search");
        dialog.cancel();
      }
    });
    builder.show();
  }

  private void searchProducts(Map<String, String> arguments) {
    // empty search string will give a lot of products :)
    String argumentString = "";

    // iterate over the map and build up a string to pass over the network
    for (Map.Entry<String, String> entry : arguments.entrySet())
    {
      // If first arg use "?", otherwise use "&"
      // E g:    ?min_alcohol=4.4&max_alcohol=5.4
      argumentString += (argumentString.equals("")?"?":"&")
          + entry.getKey()
          + "="
          + entry.getValue();
    }
    // print argument
    Log.d(LOG_TAG, " arguments: " + argumentString);

    RequestQueue queue = Volley.newRequestQueue(this);
    String url = "http://rameau.sandklef.com:9090/search/products/all/" + argumentString;
    Log.d(LOG_TAG, "Searching using url: " + url);
    JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
        Request.Method.GET,
        url,
        null,
        new Response.Listener<JSONArray>() {

          @Override
          public void onResponse(JSONArray array) {
            Log.d(LOG_TAG, "onResponse()");
            products.clear();
            products.addAll(jsonToProducts(array));
            adapter.notifyDataSetChanged();
          }
        }, new Response.ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError error) {
        Log.d(LOG_TAG, " cause: " + error.getCause().getMessage());
      }
    });

    // Add the request to the RequestQueue.
    queue.add(jsonArrayRequest);
  }


  private List<Product> jsonToProducts(JSONArray array) {
    Log.d(LOG_TAG, "jsonToProducts()");
    List<Product> productList = new ArrayList<>();
    for (int i = 0; i < array.length(); i++) {
      try {
        JSONObject row = array.getJSONObject(i);
        String name = row.getString("name");
        double alcohol = row.getDouble("alcohol");
        double price = row.getDouble("price");
        int volume = row.getInt("volume");

        Product m = new Product(name, alcohol, price, volume);
        productList .add(m);
        Log.d(LOG_TAG, " * " + m);
      } catch (JSONException e) {
        ; // is ok since this is debug
      }
    }
    return productList;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

       setContentView(R.layout.frontpage);
       createArrayList();

  }

  public void buttonOnClick(View view)
  {
    switch(view.getId())
    {
      case R.id.yes_button:
        setContentView(R.layout.activity_main);
        setupListView();
        break;

      case R.id.no_button:
        setContentView(R.layout.under_20);
        break;

      case R.id.search_button:
        showSearchDialog();
        break;

      case R.id.back_button:
        setContentView(R.layout.frontpage);
        break;
    }
  }


}
