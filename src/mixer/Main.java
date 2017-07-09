package mixer;

import java.io.IOException;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.TapIn;
import net.beadsproject.beads.ugens.TapOut;

public class Main {
	static AudioContext ac=new AudioContext();
	static Gain Master=new Gain(ac,2,(float) .5);
	static boolean noise=false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	
	Sample s=null;
	try {
		s=new Sample("p.mp3");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		System.out.println("error");
		e.printStackTrace();
	}
	System.out.println("Sample loaded");
	Envelope e=new Envelope(ac,1.0f);

	ReverbSample revs=new ReverbSample(ac, s, e);
	//e.addSegment(1.0f, 1f,new KillTrigger(revs));
	
	Clock c=new Clock(ac,1000f);
	
	c.addMessageListener(new Bead(){
		public void messageReceived(Bead message){
			Clock c = (Clock)message;
	        if(c.isBeat())
			System.out.println("hello "+c.getBeatCount());
	       if (Math.random()<0.3) noise=!noise;
	        
		}
	});
	
	 Glide timegl=new Glide(ac,0,100000f);
	    timegl.setValue(100000f);
	    Function f=new Function(revs,timegl){

			@Override
			public float calculate() {
				// TODO Auto-generated method stub
				int time=(int)x[1];
				float output;
				if (time%800<300 && (time%70 <30 )&&noise)
				output= (x[0]+(float)Math.random()*1f)/1.5f;
				else output=x[0];
				return output;
			}};
			
			 Gain delayGain=new Gain(ac,2,0.2f);
			 
			   TapIn ti=new TapIn(ac,2000);
				TapOut to=new TapOut(ac,ti,1000f);

			
			Gain echoGain=new Gain(ac,2,0.9f);
			ti.addInput(Master);
			echoGain.addInput(to);
			  Master.addInput(echoGain); 
	    Master.addInput(f);
	    ac.out.addDependent(c);
	    ac.out.addInput(Master);
	    ac.start();

	}

}
