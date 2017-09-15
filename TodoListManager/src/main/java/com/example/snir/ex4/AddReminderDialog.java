package com.example.snir.ex4;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.Calendar;

/**
 * It is a custom dialog for adding new job
 * We've implemented it as a singleton
 * based on https://guides.codepath.com/android/Using-DialogFragment
 */
public class AddReminderDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener
{
    private EditText _jobText;
    private ImageButton _okButton;
    private ImageButton _cancelButton;
    private DatePicker _dp;

    /**
     * Defines the listener interface with a method passing back data result
     */
    public interface AddItemReminderDialogListener
    {
        /**
         * Handles what to do when adding a new reminder
         * @param reminderDescription the reminder's description
         * @param reminderDate the reminder's due date
         * @param isAddReminder true iff we've to add new reminder and false otherwise(dismiss)
         */
        void onFinishAddItemDialog(@Nullable String reminderDescription,
                                   @Nullable Calendar reminderDate,
                                   Boolean isAddReminder);
    }

    /**
     * Empty constructor is required for DialogFragment
     * Make sure not to add arguments to the constructor
     * Use `newInstance` instead as shown below
     */
    public AddReminderDialog()
    {
    }

    /**
     * Gets an instance for the reminder's dialog
     * @param title dialog's title
     * @return a dialog handle
     */
    public static AddReminderDialog newInstance(String title)
    {
        AddReminderDialog frag = new AddReminderDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);

        return frag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View dialogView = inflater.inflate(R.layout.add_reminder_dialog, container);

        this._jobText = (EditText)dialogView.findViewById(R.id.addItemText);
        this._okButton = (ImageButton)dialogView.findViewById(R.id.okButton);
        this._cancelButton = (ImageButton)dialogView.findViewById(R.id.cancelButton);
        this._dp = (DatePicker)dialogView.findViewById(R.id.dpReminder);

        return dialogView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // Add ok/cancel buttons listeners
        this._okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                EditText itemTextView = (EditText)view.findViewById(R.id.addItemText);
                String reminderDescription = itemTextView.getText().toString();

                // Passing data to MainActivity via 'AddItemDialogListener' interface
                AddItemReminderDialogListener listener = (AddItemReminderDialogListener)getActivity();
                final DatePicker dp = AddReminderDialog.this._dp;
                Calendar reminderDate = Calendar.getInstance();
                reminderDate.set(Calendar.YEAR, dp.getYear());
                reminderDate.set(Calendar.MONTH, dp.getMonth());
                reminderDate.set(Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
                listener.onFinishAddItemDialog(reminderDescription, reminderDate, true);

                // Close the dialog and return back to the parent activity
                AddReminderDialog.this.dismiss();
            }
        });

        this._cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AddItemReminderDialogListener listener = (AddItemReminderDialogListener)getActivity();
                listener.onFinishAddItemDialog(null, null, false);

                // Close the dialog and return back to the parent activity
                AddReminderDialog.this.dismiss();
            }
        });

        // Fetch args from bundle and set title
        String title = getArguments().getString("title");
        getDialog().setTitle(title);

        // Sets date picker to be the current date
        Calendar calendar = Calendar.getInstance();
        this._dp.updateDate(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));

        // Show soft keyboard automatically and request focus to field
        this._jobText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
    }
}
