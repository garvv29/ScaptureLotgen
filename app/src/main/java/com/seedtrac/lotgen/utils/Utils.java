package com.seedtrac.lotgen.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.seedtrac.lotgen.R;
import com.seedtrac.lotgen.communicator.alertCommunicator;

public class Utils {
    private Context _context;
    private static Utils sSharedPrefs;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public Utils(Context context) {
        this._context = context;
    }

    public static Utils getInstance(Context context) {
        if (sSharedPrefs == null) {
            sSharedPrefs = new Utils(context);
        }
        return sSharedPrefs;
    }

    public static Utils getInstance() {
        if (sSharedPrefs != null) {
            return sSharedPrefs;
        }

        //Option 1:
        throw new IllegalArgumentException("Should use getInstance(Context) at least once before using this method.");
        //Option 2:
        // Alternatively, you can create a new instance here
        // with something like this:
        // getInstance(MyCustomApplication.getAppContext());
    }

    public static void showAlert(Context context, String message) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_alert);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView text = dialog.findViewById(R.id.tv_message);
        text.setText(message);
        Button okButton = dialog.findViewById(R.id.okbtn);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public static void showSubmitConfirmAlert(Context context, String message, final alertCommunicator communicator) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.submit_confirm_alert);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView text = dialog.findViewById(R.id.tv_message);
        text.setText(message);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);
        // if button is clicked, close the custom dialog
        btnSubmit.setOnClickListener(view -> {
            communicator.onClickPositiveBtn();
            dialog.cancel();
        });
        btnCancel.setOnClickListener(view -> {
            communicator.onClickNegativeBtn();
            dialog.cancel();
        });

        dialog.show();
    }
}
