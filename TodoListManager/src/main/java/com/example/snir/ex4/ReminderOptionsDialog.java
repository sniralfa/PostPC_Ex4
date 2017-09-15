package com.example.snir.ex4;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.example.snir.ex2.actions.Action;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SNIR on 15/09/2017.
 */

/**
 * Represents a reminder's actions' dialog box
 * It is a regular AlertDialog with a list
 * see https://developer.android.com/guide/topics/ui/dialogs.html#AddingAList
 */
public class ReminderOptionsDialog extends DialogFragment
{
    // The job for which we handling it's reminder's jobs
    private Job _job;
    // A handle to main activity context (for further use in running reminder's actions)
    private Context _mainActivityContext;
    // Node's db reference (needed to be able to delete the notification if 'cancel' was pressed)
    DatabaseReference _jobReference;

    /**
     * Getter for the reminder's dialog's instance
     * @param job the job we handling
     * @param jobReference the job's db node's reference
     * @param mainActivityContext the main activity context
     * @return an instance of the dialog
     */
    public static ReminderOptionsDialog newInstance(Job job, DatabaseReference jobReference, Context mainActivityContext)
    {
        ReminderOptionsDialog frag = new ReminderOptionsDialog();
        frag.setJobObj(job);
        frag.setJobReference(jobReference);
        frag._mainActivityContext = mainActivityContext;

        return frag;
    }

    /**
     * Setter for the job's db reference handle
     * @param jobReference the job's db node's reference
     */
    public void setJobReference(DatabaseReference jobReference)
    {
        this._jobReference = jobReference;
    }

    /**
     * Setter for the job's obj
     * @param job job's obj
     */
    public void setJobObj(Job job)
    {
        this._job = job;
    }

    /**
     * {@inheritDoc}
     *
     * Note it is different from 'AddReminderDialog' dialog.
     * Here, we don't have a custom dialog view, so we don't use the 'onCreateView'
     */
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final String[] actionsNames = this._job.getJobReminder().getActionsNames();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(this._job.getJobReminder().getDescription())
                .setItems(actionsNames, new DialogInterface.OnClickListener() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Action action = ReminderOptionsDialog.this._job.getJobReminder().getActions().get(which);
                        switch (actionsNames[which])
                        {
                            case "cancel":
                            {
                                // Removes job's reminder (by updating the node's 'jobReminder' child)
                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put("jobReminder", null /*Hacky way to delete child*/);
                                ReminderOptionsDialog.this._jobReference.updateChildren(childUpdates);
                            }
                            break;
                            case "call":
                            {
                                // Starts dialer
                                action.run(ReminderOptionsDialog.this._mainActivityContext);
                            }
                            break;
                            default:
                                break;
                        }


                        //dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
