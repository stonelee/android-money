package info.stonelee.money.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class Bill {
    DatabaseHelper helper;

    public Bill(Context context) {
        helper = new DatabaseHelper(context);
    }


    public static final class BillEntity implements BaseColumns {
        public static final String TABLE_NAME = "bill";
        public static final String COLUMN_NAME_MONEY = "money";
        public static final String COLUMN_NAME_ORIGIN_CREATED_DATE = "created";
        public static final String COLUMN_NAME_CREATED_DATE = "datetime(created, 'localtime')";
    }


    public class DatabaseHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Bill.db";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        private final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + BillEntity.TABLE_NAME + " (" +
                        BillEntity._ID + " INTEGER PRIMARY KEY," +
                        BillEntity.COLUMN_NAME_MONEY + " FLOAT," +
                        BillEntity.COLUMN_NAME_ORIGIN_CREATED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                        " )";

        private final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + BillEntity.TABLE_NAME;
    }


    public long insert(float money) {
        ContentValues values = new ContentValues();
        values.put(BillEntity.COLUMN_NAME_MONEY, money);

        SQLiteDatabase db = helper.getWritableDatabase();
        long newRowId = db.insert(
                BillEntity.TABLE_NAME,
                null,
                values);
        return newRowId;
    }

    public Cursor query() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] projection = {
                BillEntity._ID,
                BillEntity.COLUMN_NAME_MONEY,
                BillEntity.COLUMN_NAME_CREATED_DATE
        };
        String sortOrder = BillEntity.COLUMN_NAME_CREATED_DATE + " DESC";
        Cursor c = db.query(
                BillEntity.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        return c;
    }

    public int update(long id, float money) {
        SQLiteDatabase db = helper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(BillEntity.COLUMN_NAME_MONEY, money);

        String selection = BillEntity._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};

        int count = db.update(BillEntity.TABLE_NAME, values, selection, selectionArgs);
        return count;
    }

    public void remove(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        String selection = BillEntity._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};
        db.delete(BillEntity.TABLE_NAME, selection, selectionArgs);
    }

    public float getTotalMoney(Cursor cursor) {
        float total = 0;
        if (cursor.moveToFirst()) {
            do {
                total += cursor.getFloat(cursor.getColumnIndexOrThrow(BillEntity.COLUMN_NAME_MONEY));
            } while (cursor.moveToNext());
        }
        return total;
    }

    public float getTotalMoney() {
        Cursor cursor = this.query();
        return getTotalMoney(cursor);
    }
}