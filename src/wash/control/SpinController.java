package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class SpinController extends ActorThread<WashingMessage> {
    private WashingIO io;
    private boolean direction;
    private int command;
    private ActorThread<WashingMessage> sender;

    public SpinController(WashingIO io) {
        this.io= io;
    }

    @Override
    public void run() {
        try {

            while (true) {
                // wait for up to a (simulated) minute for a WashingMessage
                WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);

                // if m is null, it means a minute passed and no message was received
                if (m != null) {
                    System.out.println("got " + m);
                    sender = m.getSender();
                    command= m.getCommand();
                }


                    switch (command){
                        case WashingMessage.SPIN_OFF:
                            io.setSpinMode(WashingIO.SPIN_IDLE);
                            command=-1;
                            if (sender!=null) {
                                sender.send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
                                m = null;
                            }
                            break;
                        case WashingMessage.SPIN_FAST:
                                   io.setSpinMode(WashingIO.SPIN_FAST);
                            break;
                        case WashingMessage.SPIN_SLOW:
                            if (!direction){
                                io.setSpinMode(WashingIO.SPIN_LEFT);
                            }else {
                                io.setSpinMode(WashingIO.SPIN_RIGHT);
                            }
                            direction= !direction;
                            break;

                    }
                }

        } catch (InterruptedException unexpected) {
            throw new Error(unexpected);
        }
    }
}
