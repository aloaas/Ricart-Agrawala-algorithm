import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class Process {
    private static final int assignedTime = 10; //fixed time how much time a process can use CS in total
    private String name; //process ID
    public static ArrayList<Process> allProcesses = new ArrayList<>(); //way to access all processes
    private long time = 0; //current timestamp
    private long timeLeft = 0; //when process is occupied, keep track of for how long
    private State state = State.DO_NOT_WANT; //for coordinating mutual exclusion
    private LinkedList<Request> requests = new LinkedList<>(); //for tracking if there are any requests in queue
    private Request currentRequest = null; //if process itself has a request
    //array containing booleans for each process;
    // if all are true, then process can access CS
    private boolean[] canStart = null;

    public static Process getProcess(String name) {
        for (Process process : allProcesses) {
            if (process.getName().equals(name)) return process;
        }
        return null;
    }

    public Process(String name) {
        this.name = name;
        Process.allProcesses.add(this);

    }

    public String getName() {
        return name;
    }

    //process sends out a request
    public void sendMessage() {
        //Process has already expressed interest in accessing CS
        if (currentRequest != null) {
            System.out.println("Process " + name + " is already occupied with a request.");
            return;
        }
        //initially no processes has given a green light, so the array is all false
        if (canStart == null) canStart = new boolean[Process.allProcesses.size()];
        this.time = this.time + 1;
        //create new request message
        Request request = new Request(this.name, time);
        currentRequest = request;
        //change state from DO_NOT_WANT to WANTED
        this.state = State.WANTED;
        for (int i = 0; i < allProcesses.size(); i++) {
            Process process = allProcesses.get(i);
            if (!process.getName().equals(this.name)) {
                //send process and receive boolean response, true is OK, no response is false
                canStart[i] = process.receiveRequest(request);
            } else canStart[i] = true; //process gives itself an OK
        }
        if (checkCanStart()) start();
    }

    //Check if boolean array is all true
    private boolean checkCanStart() {
        for (boolean b : canStart) {
            if (!b) return false;
        }
        return true;
    }

    private void start() {
        this.state = State.HELD;
        timeLeft = assignedTime;
        System.out.printf("%s(%d) started to use critical section.\n%n", this.name, currentRequest.getTime());
    }


    //When process receives a request
    public boolean receiveRequest(Request request) {
        //initial response is false
        boolean response = false;
        if (this.state == State.DO_NOT_WANT) {
            response = true; //true if not interested
        }
        //is this process is occupied, add request to queue
        else if (this.state == State.HELD) {
            requests.add(request);
            System.out.printf("Process %s (%d) is waiting behind process %s(%d)%n", request.getName(), request.getTime(), name, currentRequest.getTime());
            Collections.sort(requests);
        }
        //process is not occupied, but also wants to access CS
        else if (this.state == State.WANTED) {
            //if priority is lower, obey
            if (request.getTime() < this.currentRequest.getTime()) response = true;
            else {
                //if priority is higher, add to queue
                System.out.printf("Process %s (%d) is waiting behind process %s(%d)%n", request.getName(), request.getTime(), name, currentRequest.getTime());
                requests.add(request);
                Collections.sort(requests);
            }
        }
        this.time = Math.max(request.getTime(), this.time);
        return response;
    }

    public void update() {
        //update the process which is using CS
        if (state == State.HELD) {
            //reduce time
            this.timeLeft--;
            if (timeLeft == 0) {
                System.out.printf("%s(%d) finished using critical section.\n%n", this.name, currentRequest.getTime());
                this.state = State.DO_NOT_WANT;
                //resolve all requests
                for (Request request : requests) {
                    Process p = Process.getProcess(request.getName());
                    p.canStart[Process.allProcesses.indexOf(this)] = true;
                    if (p.checkCanStart()) p.start();
                }
                requests.clear();
                //process resets itself, it does not have any permissions from other processes
                canStart = new boolean[Process.allProcesses.size()];
                canStart[Process.allProcesses.indexOf(this)] = true;
                currentRequest = null;
            }
        }
    }
}
