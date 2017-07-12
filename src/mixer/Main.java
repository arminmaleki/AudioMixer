package mixer;

import java.io.IOException;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Compressor;
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
	private static final int howManySamples=45,firstSample=20;
	private static final String sampleURI="pieces/out";
	private static final float inSampleVolume=1.5f,outSampleVolume=0.8f,granularVolume=1.0f;
	private static final float beatLength=0.5f;

	private static AudioContext ac=new AudioContext();
	private static Gain Master=new Gain(ac,2,(float) .3);
	private static ReverbSample reverbSample;
	private static Sample[] allSamples=new Sample[20];
	private static RecordToSample rts;
	private static RecordToSample totalRts;
	private static Sample samp=new Sample(10020);
	private static Sample totalSamp=new Sample(100000D,2);
	private static Feedback fb1=new Feedback(ac);
	private static UGen granularGain=new Gain(ac,2,0.9f);

	private static void loadSamples() {
		try {
			for (int i=0;i<10;i++)
				allSamples[i]=new Sample(sampleURI+(int)(Math.random()*howManySamples+firstSample)+".wav");
			for (int i=10;i<20;i++)
				allSamples[i]=new Sample("samp"+(int)(i-10)+".wav");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("error");
			e.printStackTrace();
		}
		System.out.println("Sample loaded");
	}


	public static void main(String[] args) {
		loadSamples();
		unit1 u1=new unit1();
		u1.message(null);
		rts= new RecordToSample(ac, samp, RecordToSample.Mode.FINITE);

		/*



	 revs=new ReverbSample(ac, allSamples[(int)Math.random()*19], e);


	totalRts= new RecordToSample(ac, totalSamp, RecordToSample.Mode.FINITE);
	totalRts.setKillListener(new Bead() {
		public void messageReceived(Bead message) {
			try {
				totalSamp.write("test.wav");
			} catch (IOException e) {
				e.printStackTrace();
			}
		//	ac.stop();
		}
	}); */
		Clock c=new Clock(ac,1000f);

		c.addMessageListener(new Bead(){
			private int nsamp=0;

			public void messageReceived(Bead message){
				Clock c = (Clock)message;
				if(c.isBeat()){

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
							allSamples[nsamp%10+10]=new Sample("samp"+(nsamp%10)+".wav");
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
							allSamples[(int)(Math.random()*10)]=new Sample("pieces/out"+(int)(Math.random()*howManySamples+firstSample)+".wav");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					if (c.getBeatCount()%6==4){
						int rand=(int)(Math.random()*20);
						SamplePlayer sp=new SamplePlayer(ac, allSamples[rand]);
						Glide pitch=new Glide(ac,0.8f+(float)(Math.random()*0.4),100);
						sp.setPitch(pitch);
						Gain g;
						if (rand<10)  g=new Gain(ac,2,outSampleVolume);

						else g=new Gain(ac,2,inSampleVolume);

						g.addInput(sp);
						Function spGainReg=new Function(g){

							@Override
							public float calculate() {return (float)Math.tanh(x[0]*1.8);}};
							Master.addInput(spGainReg);
							sp.start();

							System.out.println("new sp "+ rand);
					}}

			}
		});

		Function granGainReg=new Function(granularGain){

			@Override
			public float calculate() {return (float)Math.tanh(x[0]*1.8);}};

			Master.addInput(granGainReg); 


			granularGain.addInput(reverbSample);
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
						
						Compressor comp = new Compressor(ac, 1);
						comp.setAttack(30);
						comp.setDecay(200);
						comp.setRatio(4.0f);
						comp.setThreshold(0.4f);
						comp.addInput(sMax);
						rts.addInput(comp);
						ac.out.addInput(comp);
						//  totrts.getSample().clear();
						//   totalRts.addInput(ac.out);
						//  ac.out.addDependent(totalRts);
						ac.out.addDependent(rts);
						ac.start();
						//	ac.runForNMillisecondsNonRealTime(340000);

	}
	static class unit1 extends Bead{
		public void messageReceived(Bead message){
			System.out.println("end");

			if (reverbSample!=null) reverbSample.kill();
			float vol=granularVolume;
			Envelope e=new Envelope(ac,vol);
			reverbSample=new ReverbSample(ac, allSamples[(int)(Math.random()*9)], e);	
			granularGain.addInput(reverbSample);
			float n=beatLength/0.26f;
			for (int i=0;i<2;i++){
				e.addSegment(vol, 5);
				e.addSegment(vol, 100*n-5);
				e.addSegment(0, 5);
				e.addSegment(0, 30*n-5);
				e.addSegment(vol*0.6f, 5);
				e.addSegment(vol*0.6f, 100*n-5);
				e.addSegment(0, 5);
				e.addSegment(0, 30*n-5);
				e.addSegment(vol*1.5f, 5);
				e.addSegment(vol*1.5f, 200*n-5);
				e.addSegment(0, 5);
				e.addSegment(0, 60*n-5);
				if (Math.random()<0.3){
					e.addSegment(vol, 5);
					e.addSegment(vol, 100*n-5);
					e.addSegment(0, 5);
					e.addSegment(0, 30*n-5);

					e.addSegment(vol*0.6f, 5);
					e.addSegment(vol*0.6f, 100*n-5);
					e.addSegment(0, 5);
					e.addSegment(0, 30*n-5);

					e.addSegment(vol, 5);
					e.addSegment(vol, 100*n-5);
					e.addSegment(0, 5);
					e.addSegment(0, 30*n-5);

					e.addSegment(vol*0.6f, 5);
					e.addSegment(vol*0.6f, 100*n-5);
					e.addSegment(0, 5);
					e.addSegment(0, 30*n-5);

					e.addSegment(vol*1.5f, 5);
					e.addSegment(vol*1.5f, 200*n-5);
					e.addSegment(0, 5);
					e.addSegment(0, 60*n-5);

					e.addSegment(vol*1.5f, 5);
					e.addSegment(vol*1.5f, 200*n-5);
					e.addSegment(0, 5);
					e.addSegment(0, 60*n-5);

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
