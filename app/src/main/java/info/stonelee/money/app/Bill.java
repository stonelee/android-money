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
        public static final String COLUMN_NAME_CREATED_DATE = "created";
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
                        BillEntity.COLUMN_NAME_CREATED_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
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

}