package liera.tech.lib_assignment;

import android.os.Process;

import java.util.concurrent.CountDownLatch;

public abstract class Assignment<T> implements IAssignment<T>, Runnable {

    public static final int STATUS_NOR_RUNNING = -1;
    public static final int STATUS_WAITING = -2;
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_COMPILED = 1;
    private int status = STATUS_NOR_RUNNING;
    private AssignmentManager mAssignmentManager;
    private Class<? extends IAssignment<?>>[] assignments;
    private volatile boolean isAssignments;
    private CountDownLatch countDownLatch;

    @Override
    public void setAssignmentManager(AssignmentManager assignmentManager) {
        this.mAssignmentManager = assignmentManager;
    }

    /**
     * {@code @hide}
     *
     * @return
     */
    public Class<? extends IAssignment<?>>[] getAssignments() {
        if (!isAssignments) {
            this.isAssignments = true;
            this.assignments = getBeforeClassAssignments();
            this.countDownLatch = new CountDownLatch(assignments != null ? assignments.length : 0);
        }
        return assignments;
    }

    @Override
    public void execute() {
        if (runOnBuildThread()) {
            run();
            return;
        }
        this.status = STATUS_WAITING;
        AssignmentCallback assignmentCallback = mAssignmentManager.getAssignmentCallback();
        if (assignmentCallback != null)
            assignmentCallback.log("D", getClass().getName(), "assignment = " + this + " status = " + getStatus());
        executeService(this);
    }

    @Override
    public void run() {
        Process.setThreadPriority(getThreadPriority());
        toWait();
        this.status = STATUS_RUNNING;
        AssignmentCallback assignmentCallback = mAssignmentManager.getAssignmentCallback();
        if (assignmentCallback != null)
            assignmentCallback.log("D", getClass().getName(), "assignment = " + this + " thread = " + Thread.currentThread() + " status = " + getStatus());
        T result = handle();
        this.status = STATUS_COMPILED;
        if (assignmentCallback != null)
            assignmentCallback.log("D", getClass().getName(), "assignment = " + this + " status = " + getStatus());
        mAssignmentManager.handleCompiled(this, result);
    }

    @Override
    public int getThreadPriority() {
        return Process.THREAD_PRIORITY_DEFAULT;
    }

    @Override
    public void executeService(Runnable r) {
        AssignmentThreadPool.executorService.execute(r);
    }

    private void toWait() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getStatus() {
        return status;
    }

    public void toNotify() {
        countDownLatch.countDown();
    }

    protected abstract T handle();
}