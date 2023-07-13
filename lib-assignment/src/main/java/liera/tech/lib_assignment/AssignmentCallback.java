package liera.tech.lib_assignment;

public abstract class AssignmentCallback {

    public IAssignment<?> newConstructor(Class<? extends IAssignment<?>> assignmentClass){
        return null;
    }

    public void log(String level, String TAG, Object content) {
        System.out.println(TAG + " " + level + " ：" + content);
    }

    public void post(Class<? extends IAssignment<?>> assignmentClass, Runnable runnable) {
        runnable.run();
    }
}
