// MainActivity.java
// Author: M.Metsola
package com.example.pokedex;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    LinearLayout layout;
    private RequestQueue mQueue;
    ArrayList<JSONObject> pokeList = new ArrayList<JSONObject>();
    ArrayList<Integer> pokesCaught = new ArrayList<Integer>(151);

    String pokeAPIUrl = "https://pokeapi.co/api/v2/pokemon?limit=151&offset=0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView caughtTextView = (TextView) findViewById(R.id.caugthTextBox);
        String caughtText = getString(R.string.caught);
        caughtTextView.setText( caughtText + ": " + pokesCaught.size() + "/151");

        layout = findViewById(R.id.container);
        mQueue = Volley.newRequestQueue(this);

        if(getIntent().getIntegerArrayListExtra("CAUGHT") != null){
            pokesCaught.addAll(getIntent().getIntegerArrayListExtra("CAUGHT"));
            caughtTextView.setText( caughtText + ": " + pokesCaught.size() + "/151");
        }

        fetchPokemonData();
        waitForDataAndGeneratePokes generatePokesClass = new waitForDataAndGeneratePokes();
        generatePokesClass.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putIntegerArrayList("CAUGHT", pokesCaught);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        pokesCaught.addAll(savedInstanceState.getIntegerArrayList("CAUGHT"));
        TextView caughtTextView = (TextView) findViewById(R.id.caugthTextBox);
        String caughtText = getString(R.string.caught);
        caughtTextView.setText( caughtText + ": " + pokesCaught.size() + "/151");
    }

    private class waitForDataAndGeneratePokes extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... objects) {
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){
                addErrorToView();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object aObject){
            generatePokes();
            ProgressBar loadingIcon = findViewById(R.id.progressBar);
            loadingIcon.setVisibility(View.GONE);
            return;
        }
    }

    public void fetchPokemonData() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, pokeAPIUrl, null,
                response -> {
                    try {
                        parseJsonAndGetPokes(response.getJSONArray("results"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            addErrorToView();
        });

        mQueue.add(jsonObjectRequest);
    }

    private void parseJsonAndGetPokes(JSONArray pokeArray) {
        try {
            for (int i = 0; i < pokeArray.length(); i++) {
                JSONObject pokeObject = pokeArray.getJSONObject(i);
                String name = pokeObject.getString("name");
                Integer dexNumber = i + 1;
                makePokeList(dexNumber,name,"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + dexNumber +".png");
                try{
                    Thread.sleep(10);
                }catch (InterruptedException e) {
                    Toast.makeText(getApplicationContext(), "GeneratingAsync: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            addErrorToView();
        }
    }

    private void makePokeList(Integer id, String name, String image) {
        try {
            JSONObject newPoke = new JSONObject();
            newPoke.put("number", id);
            newPoke.put("name", name);
            newPoke.put("type", "None");
            newPoke.put("image", image);
            pokeList.add(newPoke);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void generatePokes(){
        for (int i = 0; i < pokeList.size(); i++) {
            try{
                Integer number = pokeList.get(i).getInt("number");
                String name = pokeList.get(i).getString("name");
                String type = pokeList.get(i).getString("type");
                String image = pokeList.get(i).getString("image");
                addPokeToView(name, number, type, image);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPokeToView(String name, Integer number, String type, String imageUrl) {
        final View view = getLayoutInflater().inflate(R.layout.main_poke_card, null);

        TextView numberView = view.findViewById(R.id.number);
        TextView nameView = view.findViewById(R.id.name);
        ImageView imageView = view.findViewById(R.id.sprite);
        Picasso.get().load(imageUrl).into(imageView);

        numberView.setText("#" + number);
        nameView.setText(uppercaseFirstLetter(name));

        CardView cardContainer = view.findViewById(R.id.cardContainer);

        cardContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToInfoPage(v, number);
            }
        });

        CheckBox caughtCheckBox = view.findViewById(R.id.checkBox);
        caughtCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markCaughtPokemon(number);
            }
        });

        if(pokesCaught.contains(number)){
            caughtCheckBox.setChecked(true);
        }
        layout.addView(view);
    }

    private void addErrorToView() {
        final View view = getLayoutInflater().inflate(R.layout.error_card, null);
        TextView nameView = view.findViewById(R.id.name);

        String errorText = getString(R.string.error);
        nameView.setText(errorText);

        CardView cardContainer = view.findViewById(R.id.cardContainer);

        cardContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setVisibility(View.GONE);
                fetchPokemonData();
                waitForDataAndGeneratePokes generatePokesClass = new waitForDataAndGeneratePokes();
                generatePokesClass.execute();
            }
        });

        layout.addView(view);
    }

    public void goToInfoPage(View view, Integer poke){
        Intent intent = new Intent(this, PokeActivity.class);
        intent.putExtra("POKEMON", poke);
        intent.putIntegerArrayListExtra("CAUGHT", pokesCaught);
        startActivity(intent);
    }

    private String uppercaseFirstLetter(String str){
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void markCaughtPokemon(Integer index){
        TextView caughtTextView = (TextView) findViewById(R.id.caugthTextBox);
        if(pokesCaught.contains(index))
        {
            pokesCaught.remove(index);
        }else{
            pokesCaught.add(index);
        }
        String caughtText = getString(R.string.caught);
        caughtTextView.setText( caughtText + ": " + pokesCaught.size() + "/151");
    }
}