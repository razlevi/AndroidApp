package project.raz.circles.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.apache.http.protocol.HTTP;

import java.util.ArrayList;

import project.raz.circles.db.DBHandler;
import project.raz.circles.objects.Place;
import project.raz.circles.services.ImageTask;
import project.raz.circles.services.PlacesService;
import project.raz.circles.R;


// PlaceFrag - Show 2 buttons and a list:
// 1st button - Search nearby resturants
// 2nd button - Search places by free text
// list showing the places received from GOOGLE PLACES API.
// list using a custom adapter to show pictures, address, distance in Km/miles.

public class PlaceFrag extends Fragment {
    EditText txtPlace;
    String strQuery = "";
    boolean isLastSearchByName = false;
    boolean isFavorites = false;
    PullToRefreshListView lstPlaces;
    ArrayList<Place> lst;
    DBHandler db;
    private LatLng myPos;
    private boolean isKM = true;
    private myAdapter adapter;
    private OnFragmentInteractionListener mListener;
    EditText txtDistance;
    SeekBar barDistance;
    private int distance=500;

    public PlaceFrag() {
        // Required empty public constructor
    }


    public static PlaceFrag newInstance() {
        PlaceFrag fragment = new PlaceFrag();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void SetNewList(ArrayList<Place> places) {
        lstPlaces.onRefreshComplete();
        lst = places;
        adapter = new myAdapter(getActivity(), R.layout.layout_row, lst);

        lstPlaces.setAdapter(adapter);
    }


    protected void ShowItemDialog(final Place p) {
        AlertDialog.Builder uploadBuilder = new AlertDialog.Builder(getActivity());
        uploadBuilder.setTitle(p.getName());
        uploadBuilder.setMessage("Select Option");
        uploadBuilder.setIcon(android.R.drawable.ic_input_add);
        uploadBuilder.setPositiveButton("Share",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharePlace(p);
                    }
                });
        uploadBuilder.setNegativeButton("Add Favourite",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddPlaceToFavourites(p);
                    }
                });
        uploadBuilder.show();

    }

    private void SharePlace(Place p) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                p.getName() + " " + p.getFormatted_address());
        sendIntent.setType(HTTP.PLAIN_TEXT_TYPE); // "text/plain" MIME type
        startActivity(sendIntent);
    }

    private void AddPlaceToFavourites(Place p) {
        db = new DBHandler(getActivity());
        db.insertPlace(p);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.fragment_place_list, container, false);
        txtPlace = (EditText) myView.findViewById(R.id.txtPlaceName);

        barDistance = (SeekBar) myView.findViewById(R.id.seekBar);
        txtDistance = (EditText) myView.findViewById(R.id.editTextDistance);

        barDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                distance = (progress+1)*500;

                txtDistance.setText(String.valueOf(distance)+"m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ImageButton btnSearchPlace = (ImageButton) myView.findViewById(R.id.btnPlace);

        btnSearchPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strQuery = txtPlace.getText().toString();
                strQuery = strQuery.replace(' ', '+');
                PlacesService.startActionName(getActivity(), strQuery);
                isLastSearchByName = true;
            }
        });

        ImageButton btnSearchNearby = (ImageButton) myView.findViewById(R.id.btnNear);
        btnSearchNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacesService.startActionNearBy(getActivity(), myPos,distance);
            }
        });

        lstPlaces = (PullToRefreshListView) myView.findViewById(R.id.lstPlaces);


        lstPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place p = adapter.getItem(position - 1);
                onPlaceClicked(p);
            }
        });

        lstPlaces.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if (isFavorites)
                    return;
                if (isLastSearchByName) {
                    strQuery = txtPlace.getText().toString();
                    strQuery = strQuery.replace(' ', '+');
                    PlacesService.startActionName(getActivity(), strQuery);
                } else {
                    PlacesService.startActionNearBy(getActivity(), myPos,distance);
                }
            }
        });

        lstPlaces.getRefreshableView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isFavorites) {
                    Place p = adapter.getItem(position);
                    ShowItemDialog(p);

                }
                return true;
            }
        });


        return myView;
    }

    public void setOnFavorites() {
        isFavorites = true;
    }

    public void onPlaceClicked(Place p) {
        if (mListener != null) {
            mListener.onPlaceClicked(p);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void SetCurrentLocation(LatLng myPos) {
        this.myPos = myPos;
    }

    public void ChangeUnits() {
        isKM = !isKM;

        if(lst ==null)
            return;

        adapter = new myAdapter(getActivity(), R.layout.layout_row, lst);

        lstPlaces.setAdapter(adapter);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onPlaceClicked(Place p);
    }

    class myAdapter extends ArrayAdapter<Place> {
        ArrayList<Place> places;
        Bitmap[] images;

        myAdapter(Context context, int resource, ArrayList<Place> objects) {
            super(context, resource, objects);

            places = objects;
            images = new Bitmap[places.size()];
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.layout_row, parent, false);

            TextView txtTitle = (TextView) rowView.findViewById(R.id.rowtxtTitle);
            txtTitle.setText(places.get(position).getName());

            TextView txtAddress = (TextView) rowView.findViewById(R.id.rowtxtAddress);
            txtAddress.setText(places.get(position).getFormatted_address());

            TextView txtDistance = (TextView) rowView.findViewById(R.id.rowtxtDistance);


            double d = places.get(position).getDistance();
            String units = "KM";

            if (!isKM) {
                units = "Miles";
                d = d * 0.6;
            }

            String dist = String.format("%.2f", d);
            txtDistance.setText(dist + units);

            final ImageView img = (ImageView) rowView.findViewById(R.id.rowImage);

            if (images[position] == null) {

                ImageTask task = new ImageTask() {
                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);

                        img.setImageBitmap(bitmap);
                        images[position] = bitmap;

                    }
                };


                 task.execute(places.get(position).getPhoto_reference());
            } else {
                img.setImageBitmap(images[position]);
            }


            return rowView;

        }
    }

}
