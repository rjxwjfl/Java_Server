package src.Controller.Thread;


import src.Controller.Thread.Interface.OutputThreadListener;
import src.Controller.Thread.Interface.SCHListener;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class OutputThread extends Thread implements SCHListener {
    private List<PrintWriter> pws = new ArrayList<>();
    private final OutputThreadListener listener;

    public OutputThread(OutputThreadListener listener) {
        this.listener = listener;
    }

    @Override
    public void toOutputThread(String msg) {
        synchronized (pws){
            Iterator<PrintWriter> iterator = pws.listIterator();
            while (iterator.hasNext()){
                PrintWriter pw = iterator.next();
                pw.println(msg + "\n");
                pw.flush();
            }
            removePrintWriter();
        }
    }

    public synchronized void addPrintWriter(PrintWriter pw) {
        System.out.println("\"PRINT-WRITER CREATED\"");
        pws.add(pw);
    }

    public synchronized void removePrintWriter() {
        pws.clear();
        System.out.println("\"PRINT-WRITER REMOVED\"");
    }

    @Override
    public void run() {
    }
}
