import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public volatile String input = "";


    //class for creating a thread to ask input
    class Ask implements Runnable {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (!input.equalsIgnoreCase("exit")) {
                if (input.equals("")) {
                    System.out.print("Enter process to access CS: \n");
                    input = scanner.nextLine().strip();
                }
            }
        }
    }

    class Handle implements Runnable {
        ArrayList<Process> processes = new ArrayList<>();

        public Handle(ArrayList<Process> processes) {
            this.processes = processes;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (input.equals("exit")) break;
                    else if (!input.equals("")) {
                        String pName = input;
                        Process p = Process.getProcess(pName);

                        if (p == null) System.out.printf("No process %s found%n", pName);
                        else {
                            //process wants to access CS
                            p.sendMessage();
                        }
                        input = "";
                    } else input = "";

                    for (Process process : processes) {
                        //check if some process can start to access CS or end their access to CS
                        process.update();
                    }

                    //to simulate a tick
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void init(ArrayList<Process> processes) {
        Ask ask = new Ask();
        Handle handle = new Handle(processes);
        new Thread(ask).start();
        new Thread(handle).start();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("usage: Main P1,P2,P3,...,Pn");
        } else {
            String[] names = args[0].split(",");
            System.out.println(Arrays.toString(names));
            ArrayList<Process> processes = new ArrayList<>();
            for (String name : names) {
                Process p = new Process(name);
                processes.add(p);
            }
            Main main = new Main();
            main.init(processes);
        }

    }
}
