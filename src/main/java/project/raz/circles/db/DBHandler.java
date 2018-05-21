package project.raz.circles.db;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import project.raz.circles.objects.Place;

// Class used for accessing the DB.
// Inserts, Deletes, Get Places.

public class DBHandler {
	private DBHelper helper;

	public DBHandler(Activity activity) {
		helper = new DBHelper(activity, Constants.DATABASE_NAME, null, 1);
	}

	public void insertPlace(Place place) {

		SQLiteDatabase db = helper.getWritableDatabase();
		try {

			ContentValues values = new ContentValues();
			values.put(Constants.COLUMN_NAME, place.getName());
			values.put(Constants.COLUMN_ADDRESS, place.getFormatted_address());
			values.put(Constants.COLUMN_LAT, place.getX());
			values.put(Constants.COLUMN_LNG, place.getY());
			values.put(Constants.COLUMN_DISTANCE, place.getDistance());
			values.put(Constants.COLUMN_IMAGEURL, place.getPhoto_reference());

			db.insert(Constants.TABLE_NAME, null, values);

		} catch (SQLiteException e) {
			e.getCause();
		} finally {
			if (db.isOpen())
				db.close();
		}

	}

	public void deletePlace(String id) {

		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			String[] arr = { id };
			db.delete(Constants.TABLE_NAME, Constants.COLUMN_ID + "=?", arr);

		} catch (SQLiteException e) {
			e.getCause();
		} finally {
			if (db.isOpen())
				db.close();
		}

	}

	public void deleteTable() {

		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			db.delete(Constants.TABLE_NAME, null, null);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public ArrayList<Place> GetAllPlaces() {

		SQLiteDatabase db = helper.getReadableDatabase();
		ArrayList<Place> places = new ArrayList<Place>();
		Cursor cursor = null;
		try {

			cursor = db.query(Constants.TABLE_NAME, null, null, null, null,
					null, null);

		} catch (SQLiteException e) {
			e.getCause();
		}

		while (cursor.moveToNext()) {

			Place p = new Place();
			//p.setId(cursor.getInt(0));
			p.setName(cursor.getString(1));
			p.setFormatted_address(cursor.getString(2));
			p.setX(Double.parseDouble(cursor.getString(3)));
			p.setY(Double.parseDouble(cursor.getString(4)));
			p.setDistance(Double.parseDouble(cursor.getString(5)));
			p.setPhoto_reference(cursor.getString(6));

			places.add(p);

		}

		return places;
	}

}
