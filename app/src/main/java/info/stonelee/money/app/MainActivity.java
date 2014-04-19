package info.stonelee.money.app;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    public static final String TAG = "MainActivity";
    private Bill bill;
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = (EditText) findViewById(R.id.money);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    insertMoney();
                    handled = true;
                }
                return handled;
            }
        });

        ImageButton button = (ImageButton) findViewById(R.id.ok);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                insertMoney();
            }
        });

        bill = new Bill(this);
        Cursor cursor = bill.query();
        adapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_list_item_2, cursor,
                new String[]{Bill.BillEntity.COLUMN_NAME_MONEY, Bill.BillEntity.COLUMN_NAME_CREATED_DATE},
                new int[]{android.R.id.text1, android.R.id.text2}, 0);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

        caleTotalMoney(cursor);
    }

    private void caleTotalMoney(Cursor cursor) {
        float total = 0;
        if (cursor.moveToFirst()) {
            do {
                total += cursor.getFloat(cursor.getColumnIndexOrThrow(Bill.BillEntity.COLUMN_NAME_MONEY));
            } while (cursor.moveToNext());
        }
        TextView editText = (TextView) findViewById(R.id.total);
        editText.setText("总计：" + String.valueOf(total));
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void insertMoney() {
        Switch way = (Switch) findViewById(R.id.way);
        EditText editText = (EditText) findViewById(R.id.money);

        if (TextUtils.isEmpty(editText.getText().toString())) {
            return;
        }

        float number = Float.valueOf(editText.getText().toString());
        if (way.isChecked()) {
            number = -number;
        }
        bill.insert(number);

        Cursor cursor = bill.query();
        adapter.changeCursor(cursor);
        caleTotalMoney(cursor);

        editText.setText("");
        way.setChecked(false);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
