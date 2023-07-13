package com.example.assignment;

import android.util.Log;

import liera.tech.lib_assignment.Assignment;
import liera.tech.lib_assignment.IAssignment;

public class TaskThread5 extends Assignment<String> {
    @Override
    public Class<? extends IAssignment<?>>[] getBeforeClassAssignments() {
        return new Class[]{
                TaskThread1.class
        };
    }

    @Override
    public boolean runOnBuildThread() {
        return false;
    }

    @Override
    public boolean buildThreadWaitThisCompile() {
        return false;
    }

    @Override
    protected String handle() {
        Log.d(getClass().getName(), "-->執行操作：TaskThread5");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
