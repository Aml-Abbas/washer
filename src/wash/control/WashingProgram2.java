package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WashingProgram2 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;

    public WashingProgram2(WashingIO io,
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
            System.out.println("washing program 2 started");
            // Lock the hatch
            io.lock(true);


            water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10));
            WashingMessage ackWaterFillPreWash = receive();
            System.out.println("washing program 2 got " + ackWaterFillPreWash);

            // Instruct SpinController to rotate barrel slowly, back and forth
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_SLOW...");
            spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));
            WashingMessage ackSpinSlow = receive();
            System.out.println("washing program 2 got " + ackSpinSlow);

            temp.send(new WashingMessage(this, WashingMessage.TEMP_SET, 40));
            // keep for 30 simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(20 * 60000 / Settings.SPEEDUP);

            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
            WashingMessage ackTempIdle = receive();
            System.out.println("washing program 2 got " + ackTempIdle);

            water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
            WashingMessage ackWaterDrain = receive();
            System.out.println("washing program 2 got " + ackWaterDrain);

            water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10));
            WashingMessage ackWaterFill = receive();
            System.out.println("washing program 2 got " + ackWaterFill);


            temp.send(new WashingMessage(this, WashingMessage.TEMP_SET, 60));

            spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));

            // keep for 30 simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(30 * 60000 / Settings.SPEEDUP);

            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
            WashingMessage ackTempIdle2 = receive();
            System.out.println("washing program 2 got " + ackTempIdle2);

            water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
            WashingMessage ackWaterDrain2 = receive();
            System.out.println("washing program 2 got " + ackWaterDrain2);


            for (int i=0;i<5;i++){
                water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10));
                WashingMessage ackWaterRinse = receive();
                System.out.println("washing program 1 got " + ackWaterRinse);
                spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));
                WashingMessage ack = receive();
                System.out.println("washing program 1 got " + ack);

                Thread.sleep(2 * 60000 / Settings.SPEEDUP);

                spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
                WashingMessage ackSpinOff = receive();
                System.out.println("washing program 1 got " + ackSpinOff);

                water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
                WashingMessage ackWaterDrainRinse = receive();
                System.out.println("washing program 1 got " + ackWaterDrainRinse);
            }



            System.out.println("setting SPIN_Fast...");
            spin.send(new WashingMessage(this, WashingMessage.SPIN_FAST));
            WashingMessage ackSpinFast = receive();
            System.out.println("washing program 1 got " + ackSpinFast);

            // Spin for five simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(5 * 60000 / Settings.SPEEDUP);

            // Instruct SpinController to stop spin barrel spin.
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_OFF...");
            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
            WashingMessage ack = receive();
            System.out.println("washing program 1 got " + ack);
            // Now that the barrel has stopped, it is safe to open the hatch.
            // Drain barrel, which may take some time. To ensure the barrel
            // is drained before we continue, an acknowledgment is required.
            water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
            WashingMessage ack2 = receive();  // wait for acknowledgment
            System.out.println("got " + ack2);


            // Now that the barrel is drained, we can turn off water regulation.
            // For the WATER_IDLE order, the water level regulator will not send
            // any acknowledgment.
            water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));

            io.lock(false);

            System.out.println("washing program 2 finished");
        } catch (InterruptedException e) {
            // If we end up here, it means the program was interrupt()'ed:
            // set all controllers to idle
            temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
            water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));
            spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
            System.out.println("washing program 2 terminated");
        }
    }

}
