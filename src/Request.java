public class Request implements Comparable {
    private final String resource = "CRITICAL SECTION";
    private final long time;
    private final String name;

    public Request(String name, long time) {
        this.time = time;
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public String getName() {
        return name;
    }


    @Override
    public int compareTo(Object o) {
        long r = ((Request) o).getTime();
        return Long.compare(time, r);
    }
}
