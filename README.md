# Assignment

```java
    //初始化任务
    @Override
    public void onCreate(){
        AssignmentManager assignmentManager=new AssignmentManager.Builder(TaskMain6.class).add(TaskThread5.class).setAssignmentCallback(new AssignmentCallback(){
            @Override
            public IAssignment<?> newConstructor(Class<?extends IAssignment<?>> assignmentClass){
                if(assignmentClass.equals(TaskThread2.class)){
                    return new TaskThread2(MyApplication.this);
                }
                return super.newConstructor(assignmentClass);
            }
    
            @Override
            public void log(String level,String TAG,Object content){
                super.log(level,TAG,content);
            }
    
            @Override
            public void post(Class<?extends IAssignment<?>> assignmentClass,Runnable runnable){
                if(assignmentClass.equals(TaskThread4.class)){
                    super.post(assignmentClass,runnable);
                    return;
                }
                mMainHandler.post(runnable);
            }
        }).build();
        //开始按照task顺序执行任务
        assignmentManager.handle(true);
    }
```

```java
    //需要再某个人物完成后执行操作时，可使用以下方法
    MyApplication.getInstance().getAssignmentManager().post(TaskThread4.class, () -> binding.sampleText.setText("張三李四王五"));
```