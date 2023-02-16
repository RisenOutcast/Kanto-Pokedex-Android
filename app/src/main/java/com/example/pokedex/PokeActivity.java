// PokeActivity.java
// Author: M.Metsola
package com.example.pokedex;

import android.content.Intent;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pokedex.databinding.ActivityPokeBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PokeActivity extends AppCompatActivity {
    LinearLayout layout;
    private RequestQueue mQueue;
    Integer CurrentPokemon = 0;
    ArrayList<Integer> pokesCaught = new ArrayList<Integer>(151);

    String pokeUrl = "https://pokeapi.co/api/v2/pokemon/";
    String speciesUrl = "https://pokeapi.co/api/v2/pokemon-species/";
    String evolutionUrl = "https://pokeapi.co/api/v2/evolution-chain/1/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poke);
        layout = findViewById(R.id.container);
        mQueue = Volley.newRequestQueue(this);

        CurrentPokemon = getIntent().getIntExtra("POKEMON", 0);
        pokesCaught.addAll(getIntent().getIntegerArrayListExtra("CAUGHT"));
        if(pokesCaught.contains(CurrentPokemon)){
            CheckBox caughtCheckBox = findViewById(R.id.caughtCheckBox);
            caughtCheckBox.setChecked(true);
        }

        if(CurrentPokemon == 1){
            Button previousButton = findViewById(R.id.backButton);
            previousButton.setVisibility(View.INVISIBLE);
        }
        if(CurrentPokemon == 151){
            Button nextButton = findViewById(R.id.nextButton);
            nextButton.setVisibility(View.INVISIBLE);
        }

        addPokeToView();
        getPoke(pokeUrl);
        getPokeSpeciesInfo(speciesUrl);
    }

    private void addPokeToView() {
        final View view = getLayoutInflater().inflate(R.layout.poke_info, null);
        layout.addView(view);
    }

    private void getPoke(String pokeUrl) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, pokeUrl + CurrentPokemon, null,
                response -> {
                    parseJsonAndUpdateUI(response);
                }, error -> {
            Toast.makeText(getApplicationContext(), "getPoke " + error.toString(), Toast.LENGTH_LONG).show();
        });

        mQueue.add(jsonObjectRequest);
    }

    private void getPokeSpeciesInfo(String pokeUrl) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, pokeUrl + CurrentPokemon, null,
                response -> {
                    parseJsonAndUpdateUI2(response);
                }, error -> {
            Toast.makeText(getApplicationContext(), "getPoke " + error.toString(), Toast.LENGTH_LONG).show();
        });

        mQueue.add(jsonObjectRequest);
    }

    private void parseJsonAndUpdateUI(JSONObject pokeListObject) {
        TextView nameTextView = (TextView) findViewById(R.id.nameText);
        TextView typeTextView = (TextView) findViewById(R.id.typeText);
        TextView abilitesTextView = (TextView) findViewById(R.id.abilitiesText);
        TextView heigthTextView = (TextView) findViewById(R.id.heightText);
        TextView statsTextView = (TextView) findViewById(R.id.statsText);

        try {
            Integer number = pokeListObject.getInt("id");
            String name = pokeListObject.getString("name");
            String type = uppercaseFirstLetter(pokeListObject.getJSONArray("types").getJSONObject(0).getJSONObject("type").getString("name"));
            if (pokeListObject.getJSONArray("types").length() > 1) {
                type += " / " + uppercaseFirstLetter(pokeListObject.getJSONArray("types").getJSONObject(1).getJSONObject("type").getString("name"));
            }
            String abilities = uppercaseFirstLetter(fixMultiWord(pokeListObject.getJSONArray("abilities").getJSONObject(0).getJSONObject("ability").getString("name")));
            if (pokeListObject.getJSONArray("abilities").length() > 1) {
                abilities += " / " + uppercaseFirstLetter(fixMultiWord(pokeListObject.getJSONArray("abilities").getJSONObject(1).getJSONObject("ability").getString("name")));
            }
            Double height = pokeListObject.getDouble("height") / 10;
            Double weight =  pokeListObject.getDouble("weight") / 10;
            String heigtAndWeigth = height + " m " + weight + " kg";

            String stats = "";
            JSONArray statsArray = pokeListObject.getJSONArray("stats");
            for (int i = 0; i < statsArray.length(); i++) {
                stats += uppercaseFirstLetter(fixMultiWord(statsArray.getJSONObject(i).getJSONObject("stat").getString("name"))) + ": ";
                stats += statsArray.getJSONObject(i).getInt("base_stat") + "\n";
            }

            String image = pokeListObject.getJSONObject("sprites").getJSONObject("other").getJSONObject("official-artwork").getString("front_default");

            nameTextView.setText("#" + number + " " + uppercaseFirstLetter(name));
            typeTextView.setText(type);
            abilitesTextView.setText(abilities);
            heigthTextView.setText(heigtAndWeigth);
            statsTextView.setText(stats);

            ImageView imageView = findViewById(R.id.imageView);
            Picasso.get().load(image).into(imageView);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    // Since the entry text is behind different URL
    private void parseJsonAndUpdateUI2(JSONObject pokeListObject) {
        TextView entryTextView = (TextView) findViewById(R.id.entryText);

        try {
            // Seems that the entries are not always in same order so we need to find the english one
            String entry = "";
            JSONArray flavorTexts = pokeListObject.getJSONArray("flavor_text_entries");
            for (int i = 0; i < flavorTexts.length(); i++) {
                if(flavorTexts.getJSONObject(i).getJSONObject("language").getString("name").equals("en")){
                    entry = flavorTexts.getJSONObject(i).getString("flavor_text");
                }
            }

            String[] splitEntry = entry.split(System.getProperty("line.separator"));
            String finalEntry = "";
            for (int i = 0; i < splitEntry.length; i++){
                finalEntry += splitEntry[i] + " ";
            }
            entryTextView.setText(finalEntry);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void goToNextPage(View view){
        Intent intent = new Intent(this, PokeActivity.class);
        intent.putExtra("POKEMON", CurrentPokemon + 1);
        intent.putIntegerArrayListExtra("CAUGHT", pokesCaught);
        startActivity(intent);
    }

    public void goToPreviousPage(View view){
        Intent intent = new Intent(this, PokeActivity.class);
        intent.putExtra("POKEMON", CurrentPokemon - 1);
        intent.putIntegerArrayListExtra("CAUGHT", pokesCaught);
        startActivity(intent);
    }

    public void goToMainPage(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putIntegerArrayListExtra("CAUGHT", pokesCaught);
        startActivity(intent);
    }

    private String uppercaseFirstLetter(String str){
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String fixMultiWord(String str){
        String[] words = str.split("-");
        str = "";
        for (int i = 0; i < words.length; i++){
            if (i > 0){
                str += " ";
            }
            str += uppercaseFirstLetter(words[i]);
        }
        return str;
    }

    public void markCaughtPokemon(View view){
        if(pokesCaught.contains(CurrentPokemon))
        {
            pokesCaught.remove(CurrentPokemon);
        }else{
            pokesCaught.add(CurrentPokemon);
        }
    }
}