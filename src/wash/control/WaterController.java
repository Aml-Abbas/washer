package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WaterController extends ActorThread<WashingMessage> {
    private WashingIO io;
    private ActorThread<WashingMessage> sender;
    private WashingMessage lastM;

    public WaterController(WashingIO io) {
        this.io= io;
    }

    @Override
    public void run() {

       try {
           while (true) {
               WashingMessage m = receiveWithTimeout(5000 / Settings.SPEEDUP);

               // if m is null, it means a minute passed and no message was received
               if (m != null) {
                   System.out.println("got " + m);
                   lastM=m;
                   sender=lastM.getSender();
               }
               if (lastM==null){
                   continue;

               }
                   switch (lastM.getCommand()){
                       case WashingMessage.WATER_FILL:
                           io.drain(false);
                           if (io.getWaterLevel()<lastM.getValue()){
                               io.fill(true);
                           }else {
                               io.fill(false);
                               sender.send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
                               lastM=null;
                           }
                           break;
                       case WashingMessage.WATER_DRAIN:
                           io.drain(true);
                           if (io.getWaterLevel()==0){
                               sender.send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
                               lastM=null;
                           }
                           break;
                       case WashingMessage.WATER_IDLE:
                           io.fill(false);
                           io.drain(false);
                           lastM=null;
                           break;
                   }
               }
    }catch (InterruptedException unexpected){
           throw new Error(unexpected);
       }
    }
}
