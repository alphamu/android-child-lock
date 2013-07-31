package com.alimuzaffar.android.childlock.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SetPinDialogFragment extends DialogFragment{
    
	DialogInterface.OnClickListener onClickNo;
	DialogInterface.OnClickListener onClickYes;
	
    public static SetPinDialogFragment newInstance() {
        return new SetPinDialogFragment();
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Set PIN");
        alertDialogBuilder.setMessage("In order to use this feature you must set a 4-digit PIN?");
        //null should be your on click listener
        alertDialogBuilder.setPositiveButton("Yes", onClickYes);
        alertDialogBuilder.setNegativeButton("No", onClickNo);

        return alertDialogBuilder.create();
    }

	public void setOnClickNo(DialogInterface.OnClickListener onClickNo) {
		this.onClickNo = onClickNo;
	}

	public void setOnClickYes(DialogInterface.OnClickListener onClickYes) {
		this.onClickYes = onClickYes;
	}
    
	@Override
	public void onDestroyView() {
	  if (getDialog() != null && getRetainInstance())
	    getDialog().setOnDismissListener(null);
	  super.onDestroyView();
	}
    
}