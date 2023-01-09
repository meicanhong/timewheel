import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeWheel {
    // 每个槽的间隔单位
    private int interval;
    // 一轮有多少个槽
    private int slot_num;
    // 当前第几轮
    private int loop;
    private Map<Integer, ArrayList<Job>> slot_job;

    private Thread workThread;

    public TimeWheel(int interval, int slot_num) {
        this.interval = interval;
        this.slot_num = slot_num;
        this.loop = 0;
        this.slot_job = new HashMap<>();
        this.workThread = new Thread(new JobWorker());
    }

    public void start() {
        workThread.start();
    }

    public void notifyJobs(int index) {
        // 这里可以优化成优先队列
        Iterator<Job> jobs = this.slot_job.getOrDefault(index, new ArrayList<>()).iterator();
        while (jobs.hasNext()) {
            Job job = jobs.next();
            if (job.getQueueWheel() == this.loop) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        job.doing();
                    }
                }).start();
                jobs.remove();
            }
        }
    }

    public void add(int delay) {
        int wheel_time = interval * slot_num;
        int queueWheel = delay / wheel_time + loop;
        int delay_time = (delay - queueWheel * wheel_time);
        Job job = new Job(delay_time, queueWheel);
        ArrayList jobs = this.slot_job.getOrDefault(delay_time, new ArrayList<Job>());
        jobs.add(job);
        this.slot_job.put(delay_time, jobs);
    }


    private class JobWorker implements Runnable {

        private long startTime = System.currentTimeMillis();

        @Override
        public void run() {
            int index = 0;
            while (true) {
                if (index >= slot_num) {
                    index = 0;
                    loop ++;
                }
                System.out.println(index);
                notifyJobs(index);
                waitNextSlot(index);
                index++;
            }
        }

        private void waitNextSlot(int index) {
            while (true) {
                long currentTime = System.currentTimeMillis();
                long sleepTime = interval * index * 1000 - currentTime + startTime + loop * 1000 * slot_num;
                if (sleepTime <= 0) {
                    break;
                }
                try {
                    Thread.sleep(Math.abs(sleepTime));
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
            }
        }
    }

    public static void main(String[] args) {
        // 每一秒走一下，60秒为一圈
        TimeWheel timeWheel = new TimeWheel(1, 60);
        timeWheel.start();
        // 第5秒的任务
        timeWheel.add(5);
        // 第65秒的任务
        timeWheel.add(65);
    }

}
