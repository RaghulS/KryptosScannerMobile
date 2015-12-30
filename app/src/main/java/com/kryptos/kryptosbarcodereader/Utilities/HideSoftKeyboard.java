package com.kryptos.kryptosbarcodereader.Utilities;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Raghul.S on 11-Jan-15.
 */
public class HideSoftKeyboard {

    /**
     * Hide Soft Keyboard onTouchEvent
     *
     * @param aView
     * @param aActivity
     */
    public static void setupUI(View aView, final Activity aActivity) {
        try {
            // --- Set up touch listener for non-text box views to hide
            // keyboard---
            if (!(aView instanceof EditText)) {

                aView.setOnTouchListener(new View.OnTouchListener() {

                    public boolean onTouch(View v, MotionEvent event) {

                        // ---Hide soft keyboard---
                        hideSoftKeyboard(aActivity);
                        return false;
                    }

                });
            }

            // --- If a layout container, iterate over children and seed
            // recursion---
            if (aView instanceof ViewGroup) {

                for (int aCount = 0; aCount < ((ViewGroup) aView)
                        .getChildCount(); aCount++) {

                    View aInnerView = ((ViewGroup) aView).getChildAt(aCount);

                    setupUI(aInnerView, aActivity);
                }
            }
        } catch (Exception aError) {
        }
    }

    // ---Function to hide soft key board---
    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity
                    .getCurrentFocus().getWindowToken(), 0);

        } catch (Exception aError) {
        }

    }
}
