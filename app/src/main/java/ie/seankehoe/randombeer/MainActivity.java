package ie.seankehoe.randombeer;

//Imports
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//Sean Kehoe Random Beer App
// 27/05/2017

public class MainActivity extends AppCompatActivity {

    //use this for logging to console
    private static final String TAG = MainActivity.class.getSimpleName();

    //Setting up drawables, textviews, buttons
    private TextView textView_Desc,textView_Title;
    private ImageView imageView_Label;
    private BeerDB mCurrentBeer;
    private Button Btn_NextBeer,Btn_viewImage;

    //
    //
    //Root url with key to be used
    private String root_url = "http://api.brewerydb.com/v2//beer/random?hasLabels=Y&key=c9dd2bfc86379070eaa22cf9bc8b953c";
    //test changes
    //Retrofit leftovers
    private List<BeerDB> beers;
    private ListView listviewbeers;
    //

    //These are to store the random list of quips you see in the app. No fun without them.
    Random rand;
    ArrayList<String> list = new ArrayList<String>();
    ArrayList<String> list2 = new ArrayList<String>();

    //Creation
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Declaring all the drawables and mapping them to variables.
        textView_Desc = (TextView) findViewById(R.id.textView_Desc);
        textView_Desc.setMovementMethod(new ScrollingMovementMethod());
        textView_Title = (TextView) findViewById(R.id.textView_Title);
        imageView_Label = (ImageView) findViewById(R.id.imageView_Label);
        Btn_NextBeer = (Button) findViewById(R.id.Btn_nextBeer);
        Btn_viewImage = (Button) findViewById(R.id.Btn_viewImage);

        //On click listeneres for a few things which leads to individual methods
        Btn_NextBeer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               getABeer();
            }
        });
        Btn_viewImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewImage();
            }
        });
        imageView_Label.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideImage();
            }
        });

        //Just gonna populate the list with my humorous one liners (Not Guaranteed Funny)
        populateQuips();

        //Instantly gets a beer on startup.
        getABeer();
    }

    //Method to get a beer from the api , uses beers/random
    private void getABeer() {

        //Just a loading box to show user something is happening
        final ProgressDialog beerload;
        beerload = ProgressDialog.show(MainActivity.this,"Fetching you a new brew",getAQuip(),true,true);
        //Small Quip displayed.

        //Building the request
        final Request requestBeer = new Request.Builder()
                .url(root_url)
                .build();

        //Using OkHttpClient to make the request
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(requestBeer);

        //Asycnhronous call using enqueue so the user is not locked up.
        call.enqueue(new Callback() {
            //Failure shows no connection to server.
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "No connection to the server :(", Toast.LENGTH_SHORT).show();
                beerload.dismiss();
            }

            @Override
            //Success puts all the data into a variable which holds the JSON data.
            public void onResponse(Call call, Response response) throws IOException {
                beerload.dismiss();
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    if (response.isSuccessful()) {

                        //Extracts the relevant data from the json return changeCurrentBEER(JSONdATA)
                        mCurrentBeer = changeCurrentBeer(jsonData);

                        //Updates what you see on the screen by going to updateDisplay()
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                updateDisplay();

                            }
                        });

                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception", e);
                } catch (JSONException e) {
                    Log.e(TAG, "Exception", e);
                }
            }

        });
        //Resets the state of the screen everytime a new beer is retrieved.
        hideImage();

    }

    //This method extracts the relevant data needed by the app.
    private BeerDB changeCurrentBeer(String jsonData) throws JSONException {
        String labelurl;
        BeerDB beerStat = new BeerDB();

        //Get Relevant JSON Objects
        JSONObject beerSummaryName = new JSONObject(jsonData);
        JSONObject summary = beerSummaryName.getJSONObject("data");
        beerStat.setName(summary.getString("name"));
        //Checks to see if it has a decription, so it doesnt crash.
        if(summary.has("description")) {
            beerStat.setDesc(summary.getString("description"));
        }
        //Sets it to one of my great one liners if it doesnt.
        else{
            beerStat.setDesc(getAQuote());
        }
        //Checks to see if there is a label, there always is since i only search for ones with labels, but back up is here anyway.
        if(summary.has("labels")) {
           labelurl = beerSummaryName.getJSONObject("data").getJSONObject("labels").getString("large");
        }
        else{
            labelurl ="http://www.novelupdates.com/img/noimagefound.jpg";
        }
        beerStat.setLabelUrl(labelurl);

        return beerStat;
    }


    //Updates what the users sees on screen
    private void updateDisplay(){
        String name = mCurrentBeer.getName();
        String desc = mCurrentBeer.getDesc();
        String label = mCurrentBeer.getLabelUrl();

        textView_Desc.setText(desc);
        textView_Title.setText(name);
        //Picasso is a really easy image viewing library i like to use
        Picasso.with(MainActivity.this).load(label).into(imageView_Label);
    }

    //I dont think this is used anymore.
    private void reloadABeer(View view){
        getABeer();
    }

    //Brings image to front, and bumps image alpha to max.
    private void viewImage(){
            imageView_Label.bringToFront();
            ViewCompat.setAlpha(imageView_Label, 1);
        Btn_viewImage.setEnabled(false);
        Toast.makeText(MainActivity.this, "Touch the image to return", Toast.LENGTH_SHORT).show();
    }

    //Brings text to front and bumps alpha of image back down to normal
    private void hideImage() {
        textView_Desc.bringToFront();
        ViewCompat.setAlpha(imageView_Label, (float) .2);
        Btn_viewImage.setEnabled(true);
    }

    //Loads a random One liner
    private String getAQuip(){
        rand = new Random();
        String randomQuip = list.get(rand.nextInt(list.size()));
        return randomQuip;
    }
    //Same deal as last method.
    private String getAQuote(){
        rand = new Random();
        String randomQuote = list2.get(rand.nextInt(list2.size()));
        return randomQuote;
    }
    //Puts all the one liners into an arraylist.
    private void populateQuips(){

        list.add("Pouring you a fresh one");
        list.add("That last one was a bit hoppy anyway");
        list.add("Can you believe there are over one thousand ways to make the same drink?");
        list.add("hmm, am i getting hints of ... oak?");
        list.add("More hops than a rabbit in heat");
        list.add("Don't let a stranger offer you that last one");
        list.add("I made the next one myself");
        list.add("Retrieved in front of a live studio audience");
        list.add("This next one is EXTRMELY FLAMMABLE");
        list.add("I can't believe it's not beer");
        list.add("I'm not drunk, you're drunk!");
        list.add("This next one is made from real dirt");
        list.add("Fun Fact: There is no fun allowed");
        list.add("Finally, one without Squirrel Hair!");
        list.add("01000010 01100101 01100101 01110010");
        list.add("I mean whats the point if its not BendërBrau...");
        list.add("Sorry, the quips are on a smoke break");

        list2.add("This beer needs no introduction, apparently, as it doesn't have one. Sorry!");
        list2.add("Sadly no description was provided for this one. Don't get too upset - hey now it's alright");
        list2.add("There are no words to describe this beer - literally. No one put a description on it");
        list2.add("Sorry, the description of this beer has gone for a lunch break, as it's not here");
        list2.add("Where is the description of this beer? Who knows? The world is full of mysteries");
        list2.add("We just can't describe this beer! Because no one did in the first place");

    }


    //
    //This is  the remnants of my attempt to use Retrofit.
    //Didn't work out.

/** private void getABeer() {

 Retrofit.Builder builder = new Retrofit.Builder()
 .baseUrl("http://api.brewerydb.com/v2/")
 .addConverterFactory(GsonConverterFactory.create());

 Retrofit retrofit = builder.build();

 Client clientcall = retrofit.create(Client.class);
 Call<List<BeerDB>> call = clientcall.loadRandom();

 call.enqueue((new Callback<List<BeerDB>>() {
@Override
public void onResponse(Call<List<BeerDB>> call, Response<List<BeerDB>> response) {
Toast.makeText(MainActivity.this, "It was successful", Toast.LENGTH_SHORT).show();

BeerDB b = new BeerDB();
String name = b.getName();
String desc = b.getDesc();
textView_Desc.setText("Description"+ "\n"+ desc);
textView_Title.setText("Beer Name: " + name);

}
@Override
public void onFailure(Call<List<BeerDB>> call, Throwable t) {
Toast.makeText(MainActivity.this, "Sorry, something went wrong!", Toast.LENGTH_SHORT).show();
}
}));

 **/
}

