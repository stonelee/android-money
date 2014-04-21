package info.stonelee.money.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

public class BillDialogFragment extends DialogFragment {

    public interface BillDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }

    BillDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (BillDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement BillDialogListener");
        }
    }

    public static BillDialogFragment newInstance(long id, float money) {
        BillDialogFragment f = new BillDialogFragment();

        Bundle args = new Bundle();
        args.putLong(Bill.BillEntity._ID, id);
        args.putFloat(Bill.BillEntity.COLUMN_NAME_MONEY, money);
        f.setArguments(args);

        return f;
    }

    private float changedMoney;

    public float getMoney() {
        return changedMoney;
    }

    public void setMoney(float money) {
        this.changedMoney = money;
    }

    public long id;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.dialog_bill, null);

        final float money = getArguments().getFloat(Bill.BillEntity.COLUMN_NAME_MONEY);
        final Switch switcher = (Switch) root.findViewById(R.id.way);
        switcher.setChecked(money < 0);
        final EditText editText = (EditText) root.findViewById(R.id.money);
        editText.setText(String.valueOf(Math.abs(money)));

        this.id = getArguments().getLong(Bill.BillEntity._ID);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (TextUtils.isEmpty(editText.getText().toString())) {
                            return;
                        }

                        float changedMoney = Float.valueOf(editText.getText().toString());
                        if (switcher.isChecked()) {
                            changedMoney = -changedMoney;
                        }
                        if (changedMoney == money) return;

                        setMoney(changedMoney);
                        mListener.onDialogPositiveClick(BillDialogFragment.this);
                    }
                });
        return builder.create();
    }

}