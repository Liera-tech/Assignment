package liera.tech.lib_assignment;

public interface IAssignment<T> {

    void setAssignmentManager(AssignmentManager assignmentManager);

    Class<? extends IAssignment<?>>[] getAssignments();

    Class<? extends IAssignment<?>>[] getBeforeClassAssignments();

    /**
     * 是否运行在build线程
     * @return
     */
    boolean runOnBuildThread();

    /**
     * {@link IAssignment#runOnBuildThread()}为false时生效
     *
     * @return
     */
    boolean buildThreadWaitThisCompile();

    /**
     * 线程优先级
     * @return
     */
    int getThreadPriority();

    /**
     * 执行任务
     */
    void execute();

    /**
     * 配置执行任务的线程
     * @param r
     */
    void executeService(Runnable r);

    /**
     * 获取当前任务状态
     */
    int getStatus();

    /**
     * count down
     */
    void toNotify();
}