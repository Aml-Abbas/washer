package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WashingProgram1 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;

    public WashingProgram1(WashingIO io,
                           ActorThread<WashingMessage> temp,
                           ActorThread<WashingMessage> water,
                           ActorThread<WashingMessage> spin)
    {
        this.io = io;
        this.temp = temp;
        this.water = water;
        this.spin = spin;
    }
    public void run() {
        try {
            System.out.println("washing program 1 started");
            // Lock the hatch
            io.lock(true);

            water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10));
            WashingMessage ackWaterFill = receive();
            System.out.println("washing program 1 got " + ackWaterFill);

            // Instruct SpinController to rotate barrel slowly, back and forth
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_SLOW...");
            spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));

            temp.send(new WashingMessage(this, WashingMessage.TEMP_SET, 40));
            WashingMessage ack1 = receive();
            System.out.println("washing program 1 got " + ack1.getSender().getClass().getSimpleName());



            // keep for 30 simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(30 * 60000 / Settings.SPEEDUP);

            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
            WashingMessage ackTempIdle = receive();
            System.out.println("washing program 1 got " + ackTempIdle);

            water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN, 10));
            WashingMessage ackWaterDrain = receive();
            System.out.println("washing program 1 got " + ackWaterDrain);

            for (int i=0;i<5;i++){
                water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10));
                WashingMessage ackWaterRinse = receive();
                System.out.println("washing program 1 got " + ackWaterRinse);

                Thread.sleep(2 * 60000 / Settings.SPEEDUP);

                water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN, 10));
                WashingMessage ackWaterDrain1 = receive();
                System.out.println("washing program 1 got " + ackWaterDrain1);
            }


            System.out.println("setting SPIN_Fast...");
            spin.send(new WashingMessage(this, WashingMessage.SPIN_FAST));

            // Spin for five simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(5 * 60000 / Settings.SPEEDUP);

            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
            WashingMessage ack = receive();
            System.out.println("washing program 1 got " + ack);

            // Now that the barrel is drained, we can turn off water regulation.
            // For the WATER_IDLE order, the water level regulator will not send
            // any acknowledgment.
            water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));

            io.lock(false);

            System.out.println("washing program 1 finished");
        } catch (InterruptedException e) {
            // If we end up here, it means the program was interrupt()'ed:
            // set all controllers to idle
            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
            water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));
            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
            System.out.println("washing program 1 terminated");
        }
    }

}
