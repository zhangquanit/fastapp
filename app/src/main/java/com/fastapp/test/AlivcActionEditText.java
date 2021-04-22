package com.fastapp.test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;


public class AlivcActionEditText extends androidx.appcompat.widget.AppCompatEditText {
    public AlivcActionEditText(Context context) {
        super(context);
    }

    public AlivcActionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlivcActionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
//        InputConnection connection = super.onCreateInputConnection(outAttrs);
//        int imeActions = outAttrs.imeOptions& EditorInfo.IME_MASK_ACTION;
//        if ((imeActions& EditorInfo.IME_ACTION_DONE) != 0) {
//            // clear the existing action
//            outAttrs.imeOptions ^= imeActions;
//            // set the DONE action
//            outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
//        }
//        if ((outAttrs.imeOptions& EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
//            outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
//        }

        InputConnection connection = super.onCreateInputConnection(outAttrs);
//        int imeActions = outAttrs.imeOptions& EditorInfo.IME_MASK_ACTION;
//        if ((imeActions& EditorInfo.IME_ACTION_DONE) != 0) {
//            // clear the existing action
//            outAttrs.imeOptions ^= imeActions;
//            // set the DONE action
//            outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
//        }
//        if ((outAttrs.imeOptions& EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
//            outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
//        }
        outAttrs.imeOptions= EditorInfo.IME_ACTION_DONE;
        outAttrs.actionLabel = "完成";
        return connection;
    }
}
