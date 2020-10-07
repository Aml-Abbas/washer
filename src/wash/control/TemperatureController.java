package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {
    private WashingIO io;

    public TemperatureController(WashingIO io) {
        this.io= io;
    }

    @Override
    public void run() {
        try {
            while (true){
                WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);

                // if m is null, it means a minute passed and no message was received
                if (m != null) {
                    System.out.println("got " + m);

                    int command= m.getCommand();
                    double temp= m.getValue();
                    switch (command){
                        case WashingMessage.TEMP_IDLE:
                            while (io.getTemperature()>0){
                                io.heat(false);
                            }
                            break;
                        case WashingMessage.TEMP_SET:
                            if (io.getTemperature()>temp-0.00952){
                                io.heat(false);
                            }
                            else if (io.getTemperature()<temp-0.478){
                                io.heat(true);
                            }
                            break;
                    }
                    ActorThread<WashingMessage> sender= m.getSender();
                    sender.send(new WashingMessage(this,WashingMessage.ACKNOWLEDGMENT));


                        sleep(10000);

                }


            }
        }catch (InterruptedException unexpected){
            throw new Error(unexpected);
        }
    }

}
