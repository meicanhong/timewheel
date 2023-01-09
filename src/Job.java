public class Job {
    private int queueWheel;
    private int delay;

    public Job(int delay, int queueWheel) {
        this.delay = delay;
        this.queueWheel = queueWheel;
    }

    public int getQueueWheel() {
        return this.queueWheel;
    }

    public int getDelay() {
        return this.delay;
    }

    public void doing() {
        System.out.println("doing");
    }
}
