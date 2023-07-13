package com.example.assignment;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import liera.tech.lib_assignment.AssignmentCallback;
import liera.tech.lib_assignment.AssignmentManager;
import liera.tech.lib_assignment.IAssignment;

public class MyApplication extends Application {

    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private AssignmentManager assignmentManager;
    private static MyApplication mContext;

    public static MyApplication getInstance() {
        return mContext;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        assignmentManager = new AssignmentManager.Builder(TaskMain6.class).add(TaskThread5.class).setAssignmentCallback(new AssignmentCallback() {
            @Override
            public IAssignment<?> newConstructor(Class<? extends IAssignment<?>> assignmentClass) {
                if (assignmentClass.equals(TaskThread2.class)) {
                    return new TaskThread2(MyApplication.this);
                }
                return super.newConstructor(assignmentClass);
            }

            @Override
            public void log(String level, String TAG, Object content) {
                super.log(level, TAG, content);
            }

            @Override
            public void post(Class<? extends IAssignment<?>> assignmentClass, Runnable runnable) {
                if (assignmentClass.equals(TaskThread4.class)) {
                    super.post(assignmentClass, runnable);
                    return;
                }
                mMainHandler.post(runnable);
            }
        }).build();
        assignmentManager.handle(true);
    }
}
