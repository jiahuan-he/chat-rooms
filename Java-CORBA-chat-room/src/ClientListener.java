import _ChatRoom.Server;

public class ClientListener extends Thread {
    Server server;
    String clientName;

    ClientListener(String client, Server server){
        this.server = server;
        this.clientName = client;
    }

    @Override
    public void run() {
        super.run();
        while (true){
            String[] mq = this.server.retrieve(clientName);
            if (mq != null && mq.length > 0){
                for (String msg: mq){
                    System.out.println(msg);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}
