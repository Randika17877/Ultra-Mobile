package lk.randika.ultramobile.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "UltraMobile.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_WISHLIST = "wishlist";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PRODUCT_ID = "product_id";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_WISHLIST + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCT_ID + " TEXT UNIQUE);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WISHLIST);
        onCreate(db);
    }

    public boolean addToWishlist(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_ID, productId);

        long result = db.insert(TABLE_WISHLIST, null, values);
        return result != -1;
    }

    public boolean removeFromWishlist(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_WISHLIST, COLUMN_PRODUCT_ID + "=?", new String[]{productId}) > 0;
    }

    public boolean isInWishlist(String productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WISHLIST, new String[]{COLUMN_PRODUCT_ID},
                COLUMN_PRODUCT_ID + "=?", new String[]{productId}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<String> getWishlistProductIds() {
        List<String> productIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PRODUCT_ID + " FROM " + TABLE_WISHLIST, null);

        if (cursor.moveToFirst()) {
            do {
                productIds.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return productIds;
    }

    public void clearWishlist() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WISHLIST, null, null);
    }
}
