package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {
    private WashingIO io;
    private Double temp;
    private ActorThread<WashingMessage> sender;
    private int dt;
    private WashingMessage lastM;

    public TemperatureController(WashingIO io) {
        this.io= io;
        this.dt=10;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // wait for up to a (simulated) 10 sekund for a WashingMessage
                 WashingMessage m = receiveWithTimeout((long) (1000*dt / Settings.SPEEDUP));
                // if m is null, it means a minute passed and no message was received
                if (m != null) {
                    System.out.println("got " + m);
                    lastM=m;
                    sender= lastM.getSender();
                    int command = lastM.getCommand();

                    switch (command){
                        case WashingMessage.TEMP_IDLE:
                            temp=null;
                            break;
                            case WashingMessage.TEMP_SET:
                                temp= lastM.getValue();
                                break;
                    }
                }

                if (temp!=null && io.getTemperature()<= temp-2+0.008*dt){
                    if (io.getWaterLevel()!=0){
                        io.heat(true);
                    }
                    else {
                        io.heat(false);
                    }
                }else if(temp==null || io.getTemperature()>= temp-0.05*dt){
                    io.heat(false);
                    if (lastM!=null){
                        sender.send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
                        lastM=null;
                    }
                }

            }
        } catch (InterruptedException unexpected){
            throw new Error(unexpected);
        }
    }

}
