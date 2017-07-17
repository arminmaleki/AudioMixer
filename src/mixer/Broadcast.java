package mixer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.RecordToSample;

public class Broadcast {
	AudioContext ac;
	 DataOutputStream dos;
	 RecordToSample rts;
	 Sample s;
	 Gain G;
	 float gap, interval;
	 long tick=0;
	 boolean processing=false;
	 long start=System.currentTimeMillis();
public Broadcast (AudioContext ac, int port,float interval ,float gap,float volume){
	
    this.ac=ac;
    s=new Sample(interval,2);
    G=new Gain(ac,2,volume);
    G.addInput(ac.out);
    this.gap=gap;
    this.interval=interval;
   
	Socket cs=null;
	try {
		cs = new Socket("127.0.0.1",6666);
		dos=new DataOutputStream (cs.getOutputStream());
	} catch (IOException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	System.out.println("conntected to socket");		

	rts= new RecordToSample(ac, s, RecordToSample.Mode.INFINITE);
	ac.out.addDependent(rts);
	rts.addInput(G);
	System.out.println("rts initialized");	
	loop();
	
}

 public void  loop(){
	tick++;
	long mytick=tick;
	System.out.println("timer   "+(mytick-1)*(interval/1000));
	long delay=(long) interval;
	if (processing) delay=1000; 
	Timer timer=new Timer();
	timer.schedule(new TimerTask() {
		@Override
		public void run() {
			loop();

		}
	}, (long)delay);
	if (processing) {
		System.out.println("still processing, timer " + (mytick - 1) * (interval / 1000) + " canceled");
		tick--;
		return;	
	}
	
	processing=true;
	long now=System.currentTimeMillis()-start;
	System.out.println("TIME " + now / 1000 + " " + (now/1000 - (mytick - 1) * (interval / 1000)));
	rts.reset();
	s.clear();
	long systime1=System.currentTimeMillis();
	System.out.println("timer   "+(mytick-1)*(interval/1000)+" is running");
	ac.runForNMillisecondsNonRealTime(interval-gap);
	long systime2=System.currentTimeMillis();
	System.out.println("timer   "+(mytick-1)*(interval/1000)+" is run in "+(systime2-systime1)/1000.0);
	processing=false;
	int i;
	/* try {
					s.write("sample"+tick+".wav",AudioFileType.WAV);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/

	float[] frame=new float[2];
	System.out.println("timer   "+(mytick-1)*(interval/1000)+" data is being transformed");
	byte[] buff=new byte[(int) (s.getNumFrames()*4)];
	short sh;
	for (i=0;i<s.getNumFrames();i++){s.getFrame(i, frame);// P.println(frame[1]);
	//buff[i*2]=(byte)(frame[1]*127);
	//buff[i*2+1]=(byte)(frame[1]*127);
	sh=(short)(frame[0]*(128*256-1));
	buff[i*4+1]=(byte)(sh>>8);
	buff[i*4]=(byte)(sh & 0xff);
	sh=(short)(frame[1]*(128*256-1));
	buff[i*4+1+2]=(byte)(sh>>8);
	buff[i*4+2]=(byte)(sh & 0xff);
	/*try {
					dos.writeByte((byte)(frame[1]*127));
					dos.writeByte((byte)(frame[0]*127));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
	}
	System.out.println("timer   "+(mytick-1)*(interval/1000)+" data is being written");
	try {
		dos.write(buff);
	} catch (IOException e) {
		System.out.println("unable to write data");
		e.printStackTrace();
	}
	//	P.close();
	System.out.println("timer   "+(mytick-1)*(interval/1000)+" data is being flushed");
	try {
		dos.flush();

		//if (cs!=null) cs.close();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		System.out.println("timer   "+(mytick-1)*(interval/1000)+" data flushing problem");
		e1.printStackTrace();
	}
	System.out.println("timer   "+(mytick-1)*(interval/1000)+" finished");
	
};

}
