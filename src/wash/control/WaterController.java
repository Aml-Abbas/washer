package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WaterController extends ActorThread<WashingMessage> {
    private WashingIO io;


    public WaterController(WashingIO io) {
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
                   switch (command){
                       case WashingMessage.WATER_IDLE:
                           io.fill(false);
                           io.drain(false);
                           break;
                       case WashingMessage.WATER_FILL:
                           io.drain(false);
                           while (io.getWaterLevel()<m.getValue()){
                               io.fill(true);
                           }
                           io.fill(false);
                           break;
                       case WashingMessage.WATER_DRAIN:
                           while (io.getWaterLevel()>0){
                               io.drain(true);
                           }
                           io.drain(false);
                           break;
                   }

                   ActorThread<WashingMessage> sender= m.getSender();
                   sender.send(new WashingMessage(this,WashingMessage.ACKNOWLEDGMENT));
               }

           }

    }catch (InterruptedException unexpected){
           throw new Error(unexpected);
       }
    }
}
