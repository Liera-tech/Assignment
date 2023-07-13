package liera.tech.lib_assignment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssignmentThreadPool {

    public static ExecutorService executorService;

    static {
        executorService = Executors.newCachedThreadPool();
    }
}