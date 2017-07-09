package mixer;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.TapIn;
import net.beadsproject.beads.ugens.TapOut;

public class ReverbSample extends Gain{
	public ReverbSample(AudioContext ac,Sample s,Envelope vol){
		super(ac,2,vol);
		System.out.println("ReverbSample constructed");
		

		 Gain delayGain=new Gain(ac,2,0.2f);
		 
		   TapIn ti=new TapIn(ac,2000);
			TapOut to=new TapOut(ac,ti,1000f);

		
		Gain echoGain=new Gain(ac,2,0.9f);
		ti.addInput(this);
		echoGain.addInput(to);
		  this.addInput(echoGain); 
		 GranularSamplePlayer gsp = new GranularSamplePlayer(ac, s);


		    Glide randomnessValue = new Glide(ac, 5, 10);
		    Glide  intervalValue = new Glide(ac, 1000, 100);
		    Glide  grainSizeValue = new Glide(ac,2000, 50);
		    Glide  positionValue = new Glide(ac, 5000, 30);
		    Glide  pitchValue = new Glide(ac, (float) Math.exp((0-5-0.0+12)/12.0*Math.log(2)), 400);//
	     pitchValue.setValue(1.0f);
		    gsp.setRandomness(randomnessValue);
		    gsp.setGrainInterval(intervalValue);
		    gsp.setGrainSize(grainSizeValue);
		    gsp.setPosition(positionValue);
		    gsp.setPitch(pitchValue);
		   
		    this.addInput(gsp);
	}
     
	
}
