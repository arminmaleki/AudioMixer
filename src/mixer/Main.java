package mixer;

import java.io.IOException;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.RecordToSample;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.TapIn;
import net.beadsproject.beads.ugens.TapOut;

public class Main {
	static AudioContext ac=new AudioContext();
	static Gain Master=new Gain(ac,2,(float) .3);
	static boolean noise=false;
	static ReverbSample revs;
	static Sample[] s=new Sample[20];
	static RecordToSample rts;
	static RecordToSample totrts;
	static Sample samp=new Sample(10020);
	static Sample totalSamp=new Sample(10000D,2);
	static Feedback fb1=new Feedback(ac);
	private static UGen granGain=new Gain(ac,2,0.9f);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	
	
	try {
		for (int i=0;i<10;i++)
		s[i]=new Sample("pieces/out"+(int)(Math.random()*45+20)+".wav");
		for (int i=10;i<20;i++)
			s[i]=new Sample("samp"+(int)(i-10)+".wav");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		System.out.println("error");
		e.printStackTrace();
	}
	System.out.println("Sample loaded");
	try {
		s[3].write("sampleout.wav");
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	Envelope e=new Envelope(ac,1.0f);

	 revs=new ReverbSample(ac, s[(int)Math.random()*19], e);
	e.addSegment(1.0f, 10000f, new unit1());
	rts= new RecordToSample(ac, samp, RecordToSample.Mode.FINITE);
	totrts= new RecordToSample(ac, totalSamp, RecordToSample.Mode.FINITE);
	totrts.setKillListener(new Bead() {
		public void messageReceived(Bead message) {
			try {
				totalSamp.write("test.wav");
			} catch (IOException e) {
				e.printStackTrace();
			}
		//	ac.stop();
		}
	});
	Clock c=new Clock(ac,1000f);
	
	c.addMessageListener(new Bead(){
		private int nsamp=0;

		public void messageReceived(Bead message){
			Clock c = (Clock)message;
	        if(c.isBeat()){
			//System.out.println("hello "+c.getBeatCount());
	       if (Math.random()<0.3) noise=!noise;
	       /*if (c.getBeatCount()==20){
	    	   totrts.clip();
	    	 
	    	   try {
	    		   totrts.getSample().write("total.mp3");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} System.out.println("total written");}*/
	       if ((c.getBeatCount()%10==0 )&&(c.getBeatCount()>30)){
	    		
	    		rts.pause(true);
	    	try {
					samp.write("samp"+(nsamp%10)+".wav");
					System.out.println("writing " + "samp" + (nsamp % 10) + ".wav");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		try {
					s[nsamp%10+10]=new Sample("samp"+(nsamp%10)+".wav");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		samp.clear();
	    		rts.reset();
	    		rts.start();
	    		nsamp+=1;
	       }
	       if (c.getBeatCount()%10==7) {
	    	   try {
				s[(int)(Math.random()*10)]=new Sample("pieces/out"+(int)(Math.random()*45+20)+".wav");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	   
	       }
	       if (c.getBeatCount()%6==4){
	    	   int rand=(int)(Math.random()*20);
	    	   SamplePlayer sp=new SamplePlayer(ac, s[rand]);
	    	   Glide pitch=new Glide(ac,0.8f+(float)(Math.random()*0.4),100);
	    	   sp.setPitch(pitch);
	    	   Gain g=new Gain(ac,2,2.5f);
	    	  if (rand<10)
	    	  { g.addInput(sp);
	    	   Master.addInput(g);
	    	   }
	    	   else
	    	   { 
	    	   g.addInput(sp);
	    	   g.setValue(2.5f);
	    	   Master.addInput(g);
	    	   }
	    	   sp.start();
	    
	    	   System.out.println("new sp "+ rand);
	       }}
	        
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
		
			
			  Master.addInput(granGain); 
			 
		
	   granGain.addInput(revs);
	    ac.out.addDependent(c);
	  
	    Gain reduce= new Gain(ac,2,0.7f);
	    reduce.addInput(Master);
	    Function redMax=new Function(reduce){

			@Override
			public float calculate() {
				// TODO Auto-generated method stub
			if (x[0]>1) return 1;
			if (x[0]<-1) return -1;
				return x[0];
			}};
	    fb1.addInput(redMax);
	    Function fb1Max=new Function(fb1){

			@Override
			public float calculate() {
				// TODO Auto-generated method stub
			if (x[0]>1) return 1;
			if (x[0]<-1) return -1;
				return x[0];
			}};
	//    fb1.addInput(redMax);
	    ac.out.addInput(fb1);
	   
	    //ac.start();
	    rts.pause(true);

		Function sMax=new Function(Master){

			@Override
			public float calculate() {
				// TODO Auto-generated method stub
			if (x[0]>1) return 1;
			if (x[0]<-1) return -1;
				return x[0];
			}};
	    rts.addInput(sMax);
	  //  totrts.getSample().clear();
	    totrts.addInput(ac.out);
	    ac.out.addDependent(totrts);
	    ac.out.addDependent(rts);
	   ac.start();
	 //ac.runForNMillisecondsNonRealTime(340000);

	}
	static class unit1 extends Bead{
		public void messageReceived(Bead message){
			System.out.println("end"); revs.kill();
			float vol=(float) (Math.random()*0.9+0.1)*6f;
			//vol=0;
			Envelope e=new Envelope(ac,vol);
			revs=new ReverbSample(ac, s[(int)(Math.random()*9)], e);	
			granGain.addInput(revs);
			float n=2.2f;
			for (int i=0;i<2;i++){
				e.addSegment(vol, 5);
				e.addSegment(vol, 100*n);
				e.addSegment(0, 5);
				e.addSegment(0, 30*n);
				e.addSegment(vol*0.6f, 5);
				e.addSegment(vol*0.6f, 100*n);
				e.addSegment(0, 5);
				e.addSegment(0, 30*n);
				e.addSegment(vol*1.5f, 5);
				e.addSegment(vol*1.5f, 200*n);
				e.addSegment(0, 5);
				e.addSegment(0, 60*n);
				if (Math.random()<0.3){
					e.addSegment(vol, 5);
					e.addSegment(vol, 100*n);
					e.addSegment(0, 5);
					e.addSegment(0, 30*n);
					
					e.addSegment(vol*0.6f, 5);
					e.addSegment(vol*0.6f, 100*n);
					e.addSegment(0, 5);
					e.addSegment(0, 30*n);
					
					e.addSegment(vol, 5);
					e.addSegment(vol, 100*n);
					e.addSegment(0, 5);
					e.addSegment(0, 30*n);
					
					e.addSegment(vol*0.6f, 5);
					e.addSegment(vol*0.6f, 100*n);
					e.addSegment(0, 5);
					e.addSegment(0, 30*n);
					
					e.addSegment(vol*1.5f, 5);
					e.addSegment(vol*1.5f, 200*n);
					e.addSegment(0, 5);
					e.addSegment(0, 60*n);
					
					e.addSegment(vol*1.5f, 5);
					e.addSegment(vol*1.5f, 200*n);
					e.addSegment(0, 5);
					e.addSegment(0, 60*n);
					
				}
		/*	e.addSegment(vol, 50);
			e.addSegment(vol, 100);
			e.addSegment(0, 50);
			e.addSegment(0, 100*2);
			e.addSegment(vol*1.5f, 50);
			e.addSegment(vol*1.5f, 100*2);
			e.addSegment(0, 50);
			e.addSegment(0, 100*2);*/
			}
			e.addSegment(vol*1.2f, 10, new unit1());
		}
	}

}
