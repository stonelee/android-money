package info.stonelee.money.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity implements BillDialogFragment.BillDialogListener {
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
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndexOrThrow(Bill.BillEntity.COLUMN_NAME_MONEY)) {
                    float money = cursor.getFloat(columnIndex);
                    String way = money < 0 ? "欠钱" : "请客";

                    TextView textView = (TextView) view;
                    textView.setText("【" + way + "】" + String.valueOf(money));
                    return true;
                }
                return false;
            }
        });
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);
                float money = cursor.getFloat(cursor.getColumnIndexOrThrow(Bill.BillEntity.COLUMN_NAME_MONEY));
                DialogFragment dialog = BillDialogFragment.newInstance(id, money);
                dialog.show(getSupportFragmentManager(), "dialog_bill");
            }
        });

        caleTotalMoney(cursor);

        registerForContextMenu(list);
    }

    private void caleTotalMoney(Cursor cursor) {
        float total = bill.getTotalMoney(cursor);
        String way = total < 0 ? "欠" : "请";

        TextView editText = (TextView) findViewById(R.id.total);
        editText.setText("总计：" + String.format("%.2f", total) + "【" + way + "】");
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void insertMoney() {
        Switch switcher = (Switch) findViewById(R.id.way);
        EditText editText = (EditText) findViewById(R.id.money);

        if (TextUtils.isEmpty(editText.getText().toString())) {
            return;
        }

        float money = Float.valueOf(editText.getText().toString());
        if (switcher.isChecked()) {
            money = -money;
        }
        bill.insert(money);

        refreshList();

        editText.setText("");
        switcher.setChecked(false);
        closeKeyboard();

    }

    private void closeKeyboard() {
        EditText editText = (EditText) findViewById(R.id.money);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private ShareActionProvider mShareActionProvider;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        Context themedContext = this.getActionBar().getThemedContext();
        mShareActionProvider = new ShareActionProvider(themedContext);
        MenuItemCompat.setActionProvider(item, mShareActionProvider);
        onActionShare();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_chart:
                closeKeyboard();
                onActionChart();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onActionShare() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, getShareMessage());
        intent.setType("text/plain");
        mShareActionProvider.setShareIntent(intent);
    }

    private String getShareMessage() {
        float total = bill.getTotalMoney();
        return "总计：" + String.valueOf(total);
    }

    private void onActionChart() {
        Intent intent = new Intent(this, ChartActivity.class);
        intent.putExtra("bills", cursorToJSON(bill.query()));
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private String cursorToJSON(Cursor cursor) {
        JSONArray rows = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                JSONObject row = new JSONObject();
                int len = cursor.getColumnCount();
                for (int i = 0; i < len; i++) {
                    String colName = cursor.getColumnName(i);
                    if (colName != null) {
                        try {
                            switch (cursor.getType(i)) {
                                case Cursor.FIELD_TYPE_BLOB:
                                    row.put(colName, cursor.getBlob(i).toString());
                                    break;
                                case Cursor.FIELD_TYPE_FLOAT:
                                    row.put(colName, cursor.getDouble(i));
                                    break;
                                case Cursor.FIELD_TYPE_INTEGER:
                                    row.put(colName, cursor.getLong(i));
                                    break;
                                case Cursor.FIELD_TYPE_NULL:
                                    row.put(colName, null);
                                    break;
                                case Cursor.FIELD_TYPE_STRING:
                                    row.put(colName, cursor.getString(i));
                                    break;
                            }
                        } catch (JSONException e) {
                        }
                    }
                }
                rows.put(row);
            } while (cursor.moveToNext());
        }
        return rows.toString();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.remove:
                bill.remove(info.id);
                refreshList();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        BillDialogFragment d = (BillDialogFragment) dialog;
        bill.update(d.id, d.getMoney());
        refreshList();
    }

    private void refreshList() {
        Cursor cursor = bill.query();
        adapter.changeCursor(cursor);
        caleTotalMoney(cursor);
    }
}
