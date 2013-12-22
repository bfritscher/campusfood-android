package ch.fritscher.campusfood.android.ui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import ch.fritscher.campusfood.android.R;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class ErrorLoadingDialogFragment extends SherlockDialogFragment {
	
	private DialogInterface.OnClickListener onPositiveClick = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
        };
	};
	private DialogInterface.OnClickListener onNegativeClick = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
        };
	};
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);		
		return new AlertDialog.Builder(getActivity())
			.setMessage(R.string.error_load)
			.setPositiveButton(R.string.retry, onPositiveClick)
			.setNegativeButton(R.string.cancel, onNegativeClick)
			.create();
	}

	public DialogInterface.OnClickListener getOnPositiveClick() {
		return onPositiveClick;
	}

	public void setOnPositiveClick(DialogInterface.OnClickListener onPositiveClick) {
		this.onPositiveClick = onPositiveClick;
	}

	public DialogInterface.OnClickListener getOnNegativeClick() {
		return onNegativeClick;
	}

	public void setOnNegativeClick(DialogInterface.OnClickListener onNegativeClick) {
		this.onNegativeClick = onNegativeClick;
	}
}
