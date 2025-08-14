package com.dailytree;

import org.junit.Test;
import static org.junit.Assert.*;

public class PatchReminderTest
{
    private static final int PATCH_VARBIT_ID = 100;

    private static class PatchReminder
    {
        private boolean remindersEnabled = true;
        private boolean reminderFired = false;
        private int lastValue = -1;

        void setRemindersEnabled(boolean enabled)
        {
            this.remindersEnabled = enabled;
        }

        void onVarbitChanged(int varbitId, int value)
        {
            if (varbitId != PATCH_VARBIT_ID || !remindersEnabled)
            {
                lastValue = value;
                return;
            }

            PatchState newState = PatchState.fromVarbit(value);
            PatchState oldState = PatchState.fromVarbit(lastValue);
            if (newState == PatchState.READY && oldState != PatchState.READY)
            {
                reminderFired = true;
            }

            lastValue = value;
        }

        boolean isReminderFired()
        {
            return reminderFired;
        }
    }

    private enum PatchState
    {
        EMPTY,
        GROWING,
        READY,
        DISEASED;

        static PatchState fromVarbit(int value)
        {
            switch (value)
            {
                case 1:
                    return GROWING;
                case 2:
                    return READY;
                case 3:
                    return DISEASED;
                case 0:
                default:
                    return EMPTY;
            }
        }
    }

    @Test
    public void reminderFiresWhenPatchBecomesReady()
    {
        PatchReminder reminder = new PatchReminder();
        reminder.onVarbitChanged(PATCH_VARBIT_ID, 1); // growing
        assertFalse(reminder.isReminderFired());
        reminder.onVarbitChanged(PATCH_VARBIT_ID, 2); // ready
        assertTrue(reminder.isReminderFired());
    }

    @Test
    public void reminderRespectsConfigurationToggle()
    {
        PatchReminder reminder = new PatchReminder();
        reminder.setRemindersEnabled(false);
        reminder.onVarbitChanged(PATCH_VARBIT_ID, 2); // ready but reminders disabled
        assertFalse("reminder should not fire when disabled", reminder.isReminderFired());
    }
}

