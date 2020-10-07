package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class SpinController extends ActorThread<WashingMessage> {
    private WashingIO io;
    private int rotate;

    public SpinController(WashingIO io) {
        this.io= io;
        rotate= WashingIO.SPIN_RIGHT;
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

                    int command= m.getCommand();
                    switch (command){
                        case WashingMessage.SPIN_OFF:
                            io.setSpinMode(command);
                            break;
                        case WashingMessage.SPIN_FAST:
                            io.setSpinMode(command);
                            break;
                        case WashingMessage.SPIN_SLOW:
                            if (rotate== WashingIO.SPIN_RIGHT){
                                io.setSpinMode(WashingIO.SPIN_LEFT);
                                rotate= WashingIO.SPIN_LEFT;
                            }else {
                                io.setSpinMode(WashingIO.SPIN_RIGHT);
                                rotate= WashingIO.SPIN_RIGHT;
                            }
                            break;

                    }
                    ActorThread<WashingMessage> sender= m.getSender();
                    sender.send(new WashingMessage(this,WashingMessage.ACKNOWLEDGMENT));

                    sleep(1000000);

                }

            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}
