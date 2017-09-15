package com.example.snir.ex4.actions;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by SNIR on 15/09/2017.
 */

/**
 * A class represents an action for a reminder
 */
//public class Action extends Activity
public class Action
{
    private String _actionType;
    private String _params;

    public Action()
    {
        // For firebase
    }

    /**
     * C'tor for the action's obj
     * @param actionType the action's name
     */
    public Action(String actionType, String params)
    {
        this._actionType = actionType;
        this._params = params;
    }

    public Action(String actionType)
    {
        this._actionType = actionType;
    }

    /**
     * Runs the actions
     * @param args varags to the action
     */
    public void run(Object... args)
    {
        if (this._actionType.equals("call"))
        {
            /*
             * Start a dialer and pass it the phone
             * (pay attention, without 'tel' prefix, it will throw 'IllegalStateException' exception)
             */
            Activity mainActivityContext = (Activity)args[0];
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + this._params));
            mainActivityContext.startActivity(intent);
        }
    }

    /**
     * Getters && Setters (needed for the Firebase database processing
     * This handles the db keys' names (not the internal variable's name)
     *
     * If there is a getter || setter that we don't want the db manager to process, just put
     * a @Except before the method's deceleration
     */
    public String getActionType() { return this._actionType; }
    public void setActionType(String actionName) { this._actionType = actionName; }
    public String getParams() { return this._params; }
    public void setParams(String params) {this._params = params;}
}
