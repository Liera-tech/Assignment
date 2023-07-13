package liera.tech.lib_assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class AssignmentManager {

    private final Thread mBuildThread;
    private boolean mIsHandle;
    private boolean mIsAWait;
    private final CountDownLatch mCountDownLatch;
    private final AssignmentCallback mAssignmentCallback;
    private final Map<Class<? extends IAssignment<?>>, IAssignment<?>> mAssignmentClassMap;

    private final Map<Class<? extends IAssignment<?>>, List<Class<? extends IAssignment<?>>>> mAssignmentAfterListMap = new HashMap<>();
    private final Map<Class<? extends IAssignment<?>>, HandleResult<?>> mHandleResultMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends IAssignment<?>>, List<Runnable>> mHandlePostMap = new ConcurrentHashMap<>();

    private AssignmentManager(Map<Class<? extends IAssignment<?>>, IAssignment<?>> assignmentClassMap, CountDownLatch countDownLatch, AssignmentCallback assignmentCallback) {
        this.mBuildThread = Thread.currentThread();
        this.mAssignmentClassMap = assignmentClassMap;
        this.mCountDownLatch = countDownLatch;
        this.mAssignmentCallback = assignmentCallback;
    }

    /**
     * {@code @hide}
     *
     * @return
     */
    public AssignmentCallback getAssignmentCallback() {
        return this.mAssignmentCallback;
    }

    public void handle(boolean buildThreadWaitThreadCompile) {
        Thread thread = Thread.currentThread();
        if (mBuildThread != thread)
            throw new RuntimeException("current thread = " + thread + ", please com to " + mBuildThread + " use handle() method");
        if (!this.mIsHandle) {
            this.mIsHandle = true;
            Map<Class<? extends IAssignment<?>>, Integer> assignmentCountMap = new HashMap<>();
            List<IAssignment<?>> firstAssignmentList = new ArrayList<>();
            Iterator<Class<? extends IAssignment<?>>> iterator = mAssignmentClassMap.keySet().iterator();
            while (iterator.hasNext()) {
                Class<? extends IAssignment<?>> assignmentClass = iterator.next();
                IAssignment<?> assignment = mAssignmentClassMap.get(assignmentClass);
                assert assignment != null;
                assignment.setAssignmentManager(this);
                Class<? extends IAssignment<?>>[] assignments = assignment.getAssignments();

                int beforeClassAssignmentsCount = assignments != null ? assignments.length : 0;
                assignmentCountMap.put(assignmentClass, beforeClassAssignmentsCount);
                if (beforeClassAssignmentsCount == 0) {
                    firstAssignmentList.add(assignment);
                    continue;
                }
                for (Class<? extends IAssignment<?>> beforeClassAssignment : assignments) {
                    List<Class<? extends IAssignment<?>>> assignmentClassList = mAssignmentAfterListMap.get(beforeClassAssignment);
                    if (assignmentClassList == null) {
                        assignmentClassList = new ArrayList<>();
                        mAssignmentAfterListMap.put(beforeClassAssignment, assignmentClassList);
                    }
                    assignmentClassList.add(assignmentClass);
                }
            }
            if (mAssignmentCallback != null)
                mAssignmentCallback.log("D", getClass().getName(), "mAssignmentAfterListMap = " + mAssignmentAfterListMap);

            List<IAssignment<?>> mainAssignmentList = new ArrayList<>();
            List<IAssignment<?>> threadAssignmentList = new ArrayList<>();
            for (int i = 0; i < firstAssignmentList.size(); i++) {
                IAssignment<?> assignment = firstAssignmentList.get(i);
                assert assignment != null;
                if (!assignment.runOnBuildThread())
                    threadAssignmentList.add(assignment);
                else
                    mainAssignmentList.add(assignment);

                List<Class<? extends IAssignment<?>>> afterAssignmentClassList = mAssignmentAfterListMap.get(assignment);
                if (afterAssignmentClassList != null) {
                    for (Class<? extends IAssignment<?>> afterAssignmentClass : afterAssignmentClassList) {
                        Integer count = assignmentCountMap.get(afterAssignmentClass);
                        assignmentCountMap.put(afterAssignmentClass, --count);
                        if (count == 0)
                            firstAssignmentList.add(mAssignmentClassMap.get(afterAssignmentClass));
                    }
                }
            }
            if (mAssignmentCallback != null)
                mAssignmentCallback.log("D", getClass().getName(), "firstAssignmentList = " + firstAssignmentList);

            firstAssignmentList.clear();
            firstAssignmentList.addAll(threadAssignmentList);
            firstAssignmentList.addAll(mainAssignmentList);

            if (mAssignmentCallback != null)
                mAssignmentCallback.log("D", getClass().getName(), "result assignmentList = " + firstAssignmentList);

            for (IAssignment<?> assignment : firstAssignmentList) assignment.execute();
            if (buildThreadWaitThreadCompile) toWait();
        }
    }

    /**
     * {@code @hide}
     *
     * @param assignment
     * @param result
     * @param <T>
     */
    <T> void handleCompiled(IAssignment<T> assignment, T result) {
        Class<? extends IAssignment<?>> assignmentClass = (Class<? extends IAssignment<?>>) assignment.getClass();
        mHandleResultMap.put(assignmentClass, new HandleResult<>(result));
        post(assignmentClass, null);
        if (!assignment.runOnBuildThread() && assignment.buildThreadWaitThisCompile()) toNotify();

        List<Class<? extends IAssignment<?>>> assignmentAfterClassList = mAssignmentAfterListMap.get(assignmentClass);
        if (assignmentAfterClassList != null)
            for (Class<? extends IAssignment<?>> assignmentAfterClass : assignmentAfterClassList)
                Objects.requireNonNull(mAssignmentClassMap.get(assignmentAfterClass)).toNotify();
    }

    public HandleResult<?>[] getHandlerResult(Class<? extends IAssignment<?>>... classAssignments) {
        if (classAssignments == null || classAssignments.length == 0) {
            return null;
        }
        HandleResult<?>[] handleResults = new HandleResult<?>[classAssignments.length];
        for (int i = 0; i < classAssignments.length; i++) {
            Class<? extends IAssignment<?>> classAssignment = classAssignments[i];
            handleResults[i] = classAssignment != null ? mHandleResultMap.get(classAssignment) : null;
        }
        return handleResults;
    }

    public void postList(Class<? extends IAssignment<?>> assignmentClass, List<Runnable> runs) {
        List<Runnable> oldRuns = mHandlePostMap.get(assignmentClass);
        if (oldRuns != null) {
            if (runs != null)
                oldRuns.addAll(runs);
        } else {
            if (runs == null) {
                return;
            }
            mHandlePostMap.put(assignmentClass, oldRuns = new ArrayList<>());
            oldRuns.addAll(runs);
        }

        if (mHandleResultMap.get(assignmentClass) != null) {
            List<Runnable> runnableList = mHandlePostMap.remove(assignmentClass);
            if (mAssignmentCallback != null)
                mAssignmentCallback.log("D", getClass().getName(), assignmentClass + "-> runnable handle = " + runnableList);
            assert runnableList != null;
            for (Runnable runnable : runnableList)
                if (mAssignmentCallback != null) mAssignmentCallback.post(assignmentClass, runnable);
        }
    }

    public void post(Class<? extends IAssignment<?>> assignmentClass, Runnable run) {
        List<Runnable> runs = null;
        if (run != null) {
            runs = new ArrayList<>();
            runs.add(run);
        }
        postList(assignmentClass, runs);
    }

    /**
     * {@code @hide}
     */
    private void toWait() {
        if (mIsAWait) return;
        try {
            this.mIsAWait = true;
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@code @hide}
     */
    private void toNotify() {
        mCountDownLatch.countDown();
    }

    public static class HandleResult<T> {
        public final T result;

        public HandleResult(T result) {
            this.result = result;
        }
    }

    public static class Builder {

        private AssignmentCallback mAssignmentCallback;

        private final Set<Class<? extends IAssignment<?>>> mOutAssignmentClassSet = new HashSet<>();

        public Builder(Class<? extends IAssignment<?>> outAssignmentClass) {
            add(outAssignmentClass);
        }

        public Builder(Set<Class<? extends IAssignment<?>>> outAssignmentClassSet) {
            addAll(outAssignmentClassSet);
        }

        public Builder add(Class<? extends IAssignment<?>> outAssignmentClass) {
            this.mOutAssignmentClassSet.add(outAssignmentClass);
            return this;
        }

        public Builder addAll(Set<Class<? extends IAssignment<?>>> outAssignmentClassSet) {
            this.mOutAssignmentClassSet.addAll(outAssignmentClassSet);
            return this;
        }

        public Builder setAssignmentCallback(AssignmentCallback assignmentCallback) {
            this.mAssignmentCallback = assignmentCallback;
            return this;
        }

        public AssignmentManager build() {
            Map<Class<? extends IAssignment<?>>, IAssignment<?>> assignmentClassMap = new HashMap<>();
            Integer countDown = 0;
            if (!mOutAssignmentClassSet.isEmpty()) {
                Iterator<Class<? extends IAssignment<?>>> classIterator = mOutAssignmentClassSet.iterator();
                while (classIterator.hasNext())
                    getAssignment(classIterator.next(), assignmentClassMap, countDown);
            }
            CountDownLatch countDownLatch = new CountDownLatch(countDown);
            return new AssignmentManager(assignmentClassMap, countDownLatch, mAssignmentCallback);
        }

        private void getAssignment(Class<? extends IAssignment<?>> assignmentClass, Map<Class<? extends IAssignment<?>>, IAssignment<?>> assignmentClassMap, Integer countDown) {
            IAssignment<?> assignment = assignmentClassMap.get(assignmentClass);
            if (assignment == null) {
                if (this.mAssignmentCallback != null)
                    assignment = mAssignmentCallback.newConstructor(assignmentClass);
                if (assignment == null)
                    try {
                        assignment = assignmentClass.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                if (!assignment.runOnBuildThread()) ++countDown;
                assignmentClassMap.put(assignmentClass, assignment);
            }
            Class<? extends IAssignment<?>>[] beforeClassAssignments = assignment.getAssignments();
            if (beforeClassAssignments != null && beforeClassAssignments.length > 0)
                for (Class<? extends IAssignment<?>> beforeClassAssignment : beforeClassAssignments)
                    getAssignment(beforeClassAssignment, assignmentClassMap, countDown);
        }
    }
}
