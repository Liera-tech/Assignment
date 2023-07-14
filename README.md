# Assignment

## 依赖步骤

#### Step 1. Add the JitPack repository to your build file
```agsl
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
#### Step 2. Add the dependency
```agsl
dependencies {
        implementation 'com.github.Liera-tech:Assignment:-SNAPSHOT'
}
```

## 实现原理
```
                
                        ②   →   ③
                    ↗               ↘
                ①                       ⑥   →
                    ↘               ↗
                            ⑤   
                            
                ④   →

以上拓扑图谱图简单解释一下：
1.任务1首先要执行，然后要么执行任务2，要么执行任务5，但要保证任务3在任务2执行完成后再执行，最终任务3和任务5执行完成后才能执行任务6
2.任务4随时可以执行

因为可能是异步执行，无法确定执行完成时机，有时候需求可能需要我们在另一个地方执行这个任务完成后的逻辑，此时可以通过assignmentManager
任务实例并通过assignmentManager.post(TaskThread4.class, () -> binding.sampleText.setText("张三李四王五"));
在需要的任务执行完成后会立即执行此逻辑，达到优化的目的
```
## 使用指南
```java
//初始化任务
@Override
public void onCreate(){
    AssignmentManager assignmentManager=new AssignmentManager.Builder(TaskMain6.class).add(TaskThread5.class).setAssignmentCallback(new AssignmentCallback(){
        //默认通过反射调用无参构造方法构建任务实例，有特殊需求时(如任务需要传参)，可通过此方法自定义实例化
        @Override
        public IAssignment<?> newConstructor(Class<?extends IAssignment<?>> assignmentClass){
            if(assignmentClass.equals(TaskThread2.class)){
                return new TaskThread2(MyApplication.this);
            }
            return super.newConstructor(assignmentClass);
        }

        //因为lib兼容web开发，默认使用的System.out.println打印日志，可自定义打印日志框架
        @Override
        public void log(String level,String TAG,Object content){
            super.log(level,TAG,content);
        }

        //逻辑任务执行，默认是在任务线程执行的，可能任务线程配置的是子线程，android如需要更新UI等操作时，此时就需要将任务推到主线程来执行
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
//需要在某个任务完成后执行操作时，可使用以下方法
MyApplication.getInstance().getAssignmentManager().post(TaskThread4.class, () -> binding.sampleText.setText("張三李四王五"));
```