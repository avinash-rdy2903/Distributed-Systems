package sequencer;

import java.io.PrintWriter;

public class Request {
    private PrintWriter out;

    public Request(PrintWriter out) {
        this.out = out;
    }
    public void sendResponse(int id){
        out.println(id);
    }
}
