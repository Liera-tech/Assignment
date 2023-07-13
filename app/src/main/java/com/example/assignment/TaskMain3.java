package com.example.assignment;

import android.util.Log;

import liera.tech.lib_assignment.Assignment;
import liera.tech.lib_assignment.IAssignment;

public class TaskMain3 extends Assignment<String> {
    @Override
    public Class<? extends IAssignment<?>>[] getBeforeClassAssignments() {
        return new Class[]{
                TaskThread2.class
        };
    }

    @Override
    public boolean runOnBuildThread() {
        return true;
    }

    @Override
    public boolean buildThreadWaitThisCompile() {
        return false;
    }

    @Override
    protected String handle() {
        Log.d(getClass().getName(), "-->執行操作：TaskMain3");
        return null;
    }
}
