package com.example.snir.ex4;

import com.example.guy.ex2.actions.Action;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This obj handles a Job in the job list
 */
public class Job
{
    /**
     * Represents a reminder for the job
     */
    public static class Reminder
    {
        private String description;
        private Map<String, Integer> date;
        private List<Action> actions;

        public Reminder()
        {
            // Need for firebase
        }

        /**
         * C'tor for the reminder
         * @param description the reminder's description
         * @param date the reminder's date
         */
        public Reminder(String description, Calendar date)
        {
            this.description = description;
            this.actions = new ArrayList<>();
            this.date = new HashMap<>();

            this.date.put("day", date.get(Calendar.DAY_OF_MONTH));
            this.date.put("month", date.get(Calendar.MONTH));
            this.date.put("year", date.get(Calendar.YEAR));
        }

        /**
         * Copy c'tor
         * @param reminder other obj
         */
        public Reminder(Reminder reminder)
        {
            this.description = reminder.getDescription();
            this.actions = new ArrayList<>();
            this.date = reminder.getDate();
        }

        /**
         * Getters && Setters (needed for the Firebase database processing
         * This handles the db keys' names (not the internal variable's name)
         *
         * If there is a getter || setter that we don't want the db manager to process, just put
         * a @Except before the method's deceleration
         */
        public String getDescription() { return this.description; }
        public void setDescription(String description) { this.description = description; }
        public Map<String, Integer> getDate() { return this.date; }
        public void setDate(Map<String, Integer> date) { this.date = date; }
        public List<Action> getActions() { return this.actions; }
        public void setActions(List<Action> actions) { this.actions = actions; }

        /**
         * Adding action to the reminder's actions list
         * @param action the new action to add
         */
        public void addAction(Action action) { this.actions.add(action); }

        /**
         * Gets reminder's actions' names
         * @return actions' name as a list
         */
        @Exclude
        public String[] getActionsNames()
        {
            String[] names = new String[this.actions.size()];
            for (int i = 0 ; i < this.actions.size() ; i++)
            {
                names[i] = this.actions.get(i).getActionType();
            }
            return names;
        }
    }

    // Job description
    private String _jobDescription;
    // Job reminder obj
    private Reminder _reminder;

    /**
     * Job's c'tor
     */
    public Job()
    {
        // Needed for Firebase
    }

    /**
     * Job c'tor
     * @param jobDescription job's description
     * @param reminder job's reminder's obj
     */
    public Job(String jobDescription, Reminder reminder)
    {
        this._jobDescription = jobDescription;
        this._reminder = reminder;
    }

    /**
     * Setter for the job's description
     * @param jobDescription job's description
     */
    public void setJobDescription(String jobDescription)
    {
        this._jobDescription = jobDescription;
    }

    /**
     * Setter for the job's reminder
     * @param reminder the job's reminder
     */
    public void setJobReminder(Reminder reminder)
    {
        this._reminder = reminder;
    }

    /**
     * Getter for job's description
     * @return job's description
     */
    public String getJobDescription()
    {
        return this._jobDescription;
    }

    /**
     * Getter for job's reminder
     * @return job's reminder
     */
    public Reminder getJobReminder()
    {
        return this._reminder;
    }

    /**
     * Getter for job's reminder's date
     * @return job's reminder's date
     */
    @Exclude
    public String getJobReminderDate()
    {
        if (null != this._reminder)
        {
            Map<String, Integer> date = this._reminder.getDate();
            return Integer.toString(date.get("year")) + "/" +
                    Integer.toString(date.get("month")) + "/" +
                    Integer.toString(date.get("day"));
        }
        else
        {
            return "No due date";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return this.getJobDescription() + "           " + this.getJobReminderDate();
    }
}
