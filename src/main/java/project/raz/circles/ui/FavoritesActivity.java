package project.raz.circles.ui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import java.util.ArrayList;

import project.raz.circles.db.DBHandler;
import project.raz.circles.objects.Place;
import project.raz.circles.R;

// Favorites Activity - Show the favourite places saved in the app DB.

public class FavoritesActivity extends ActionBarActivity implements PlaceFrag.OnFragmentInteractionListener {

    DBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);


        db = new DBHandler(this);
        ArrayList<Place> list = db.GetAllPlaces();

        PlaceFrag frg = ((PlaceFrag) getSupportFragmentManager().findFragmentById(R.id.list_fragment_favorites));
        frg.SetNewList(list);
        frg.setOnFavorites();

        View btn1 = frg.getView().findViewById(R.id.btnPlace);
        btn1.setVisibility(View.GONE);
        View btn2 = frg.getView().findViewById(R.id.btnNear);
        btn2.setVisibility(View.GONE);
        View txt = frg.getView().findViewById(R.id.txtPlaceName);
        txt.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }


    @Override
    public void onPlaceClicked(Place p) {
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("NAME", p.getName());
        intent.putExtra("X", p.getX());
        intent.putExtra("Y", p.getY());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_delete_favorites:
                deleteFavorites();
                return true;
            case R.id.action_change_units:
                changeUnits();
                return true;
            case R.id.action_show_favorites:
                ShowFavorites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ShowFavorites() {

    }

    private void changeUnits() {
        PlaceFrag frg = ((PlaceFrag) getSupportFragmentManager().findFragmentById(R.id.list_fragment_favorites));
        frg.ChangeUnits();
    }

    private void deleteFavorites() {
        DBHandler db = new DBHandler(this);
        db.deleteTable();
        PlaceFrag frg = ((PlaceFrag) getSupportFragmentManager().findFragmentById(R.id.list_fragment_favorites));
        frg.SetNewList(new ArrayList<Place>());
    }
}
