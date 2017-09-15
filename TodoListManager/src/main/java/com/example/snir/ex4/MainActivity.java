package com.example.snir.ex4;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.guy.ex2.actions.Action;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Calendar;


/**
 * The MainActivity class of our program
 * It extends the AppComat activity
 * Also, it implements the 'AddReminderDialog.AddItemReminderDialogListener'
 * as a contract with 'AddReminderDialog' about handling a new reminder
 */
public class MainActivity extends AppCompatActivity implements AddReminderDialog.AddItemReminderDialogListener {

    // Internal structures
    private FirebaseListAdapter _lvAdapter; // FirebaseUI adapter
    private DatabaseReference _dbRef;   // Firebase reference

    // UI handlers
    private EditText _jobDescription;
    private ListView _lv;

    /**
     * Handles destroying of the activity
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this._lvAdapter.cleanup();
    }

    /**
     * This function handles the activity creating (in the activity pipeline)
     * @param savedInstanceState the instance of saved bundle (see 'onSaveInstanceState')
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Gets UI handlers
        this._jobDescription = (EditText)findViewById(R.id.itemText);
        this._lv = (ListView)findViewById(R.id.todoList);

        // Gets db reference (root)
        this._dbRef = FirebaseDatabase.getInstance().getReference();

        // Sets the button event listener
        final Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v)
            {
                // Show add item's dialog
                FragmentManager fm = getSupportFragmentManager();
                AddReminderDialog addItemDialog = AddReminderDialog.newInstance("Add item's reminder");
                addItemDialog.show(fm, "add_reminder_dialog");
            }
        });

        // Populate ListView data source
        _populateJobsListView();

        // Notify of changes (so we will update with no need to push)
        this._lvAdapter.notifyDataSetChanged();

        // Notify the user that data is loading
        this.notifyMessage("Getting data (if there is)", Toast.LENGTH_LONG);

        // Sets the ListView long click's event listener for multiple choice
        this._lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL /*for multiple choice ListView*/);
        this._lv.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_main, menu);

                return true;
            }

            /**
             * {@inheritDoc}
             *
             * Note we don't need to use it because our menu don't enter invalidated mode
             */
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId())
                {
                    case R.id.menu_delete:
                    {
                        SparseBooleanArray checkedItems = MainActivity.this._lv.getCheckedItemPositions();

                        if (null == checkedItems)
                        {
                            return false;
                        }

                        // Deleting
                        for (int i = 0 ; i < MainActivity.this._lv.getCount() ; i++)
                        {
                            if (checkedItems.get(i))
                            {
                                MainActivity.this._lvAdapter.getRef(i).removeValue();
                            }
                        }

                        // Repeat initial background color
                        for (int i = 0 ; i < MainActivity.this._lv.getChildCount() ; i++)
                        {
                            View itemView = MainActivity.this._lv.getChildAt(i);
                            itemView.setBackgroundColor(Color.TRANSPARENT);
                        }

                        mode.finish();  // Action picked, so close the CAB
                        return true;
                    }
                    default:
                        return false;
                }
            }

            /**
             * {@inheritDoc}
             *
             * Note we don't have things to do or clean when the action menu is destroyed
             */
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB

                /*
                 * Our calculations is position-firstVisiblePosition because the 'position'
                 * param is the position in the dataAdapter (i.e the index in _jobs)
                 * while, the getChildAt gets the child at index i while the counting starts
                 * from the current visible index (not 0, because we've rolled down), so we'll
                 * get a null ptr which leads to nullptr reference exception.
                 */
                View rowView = MainActivity.this._lv.getChildAt(position - MainActivity.this._lv.getFirstVisiblePosition());
                if (checked)
                {
                    // Highlight background
                    rowView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.checkedItemsBackground));
                }
                else
                {
                    // If de-selected, restore original background
                    rowView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });

        // Sets single short click listener - single-choice AlertDialog
        this._lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * {@inheritDoc}
             *
             * Note it gets the clicked job and show it's
             * reminder's dialog using it's reminder's actions
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Getting the clicked node's db reference
                final DatabaseReference jobReference = MainActivity.this._lvAdapter.getRef(position);

                // It triggers once and then does not trigger again
                jobReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    /**
                     * {@inheritDoc}
                     * @param dataSnapshot snapshot of the node's data
                     */
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Job job = dataSnapshot.getValue(Job.class);

                        if (null == job.getJobReminder())
                        {
                            MainActivity.this.notifyMessage("This job doesn't have a reminder");
                            return;
                        }

                        // Starting the reminder's dialog
                        ReminderOptionsDialog reminderDialog = ReminderOptionsDialog.newInstance(job, jobReference, MainActivity.this);
                        reminderDialog.show(getSupportFragmentManager(), "reminderDialog");

                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Does nothing right now
                    }
                });
            }
        });
    }

    /**
     * Handles populating the jobs ListView (builds an adapter between the data source
     * and the view)
     */
    private void _populateJobsListView()
    {
        // Creating ListView data adapter. Uses FirebaseUI ListView adapter
        this._lvAdapter = new FirebaseListAdapter<Job>(this, Job.class, R.layout.jobs_list, this._dbRef) {
            /**
             * {@inheritDoc}
             *
             * Note we override this function to be able to change the ListView's text's color
             */
            @Override
            protected void populateView(View view, Job job, int position)
            {
                TextView textView = (TextView)view.findViewById(R.id.jobsText);

                // Setting the job's row's text
                textView.setText(job.toString());

                // Setting alternating color
                textView.setTextColor((0 == position % 2) ? Color.RED : Color.BLUE);
            }
        };

        // Setting data adapter for the ListView obj
        this._lv.setAdapter(this._lvAdapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishAddItemDialog(@Nullable String reminderDescription, @Nullable Calendar reminderDate, Boolean isAddReminder)
    {
        // Dismiss adding job
        if (null == reminderDescription)
        {
            return;
        }

        Job job;
        if (isAddReminder)
        {
            job = new Job(this._jobDescription.getText().toString(), new Job.Reminder(reminderDescription, reminderDate));

            // Check if needs to add 'call' action
            try {
                // Splitting "call 97250391827" to "call" & "97250391827"
                String[] splittedReminderDesc = reminderDescription.split(" ");

                if ((splittedReminderDesc.length > 1) && (splittedReminderDesc[0].toLowerCase().equals(getString(R.string.call_action_name).toLowerCase())))
                {
                    job.getJobReminder().addAction(new Action(getString(R.string.call_action_name), splittedReminderDesc[1]));
                }
            }
            catch (Exception e)
            {
                MainActivity.this.notifyMessage("couldn't extract reminder name");
                return;
            }
        }
        else
        {
            job = new Job(this._jobDescription.getText().toString(), null);
        }

        // Add default cancel action
        job.getJobReminder().addAction(new Action(getString(R.string.cancel_action_name)));

        // Gets data from Firebase db
        MainActivity.this._dbRef.push().setValue(job);

        // Notify
        MainActivity.this.notifyMessage("Job has been successfully added to the list");
    }

    /**
     * We use it because fucking java has no function's default param's value mechanism
     * @param message the message to show
     */
    public void notifyMessage(String message)
    {
        this.notifyMessage(message, Toast.LENGTH_SHORT);
    }

    /**
     * Notify the user for an action
     * It notify using a small ellipse message box
     *
     * @param message the message to notify
     * @param isLongDuration whether to use LOND_DELAY(3.5 sec) or SHORT DELAY(2 sec)
     */
    public void notifyMessage(String message, Integer isLongDuration)
    {
        Toast toast = Toast.makeText(getApplicationContext(), message, isLongDuration);
        toast.show();
    }
}