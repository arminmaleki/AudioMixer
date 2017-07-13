package mixer;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.TapIn;
import net.beadsproject.beads.ugens.TapOut;

public class Feedback extends net.beadsproject.beads.ugens.Gain {
	//AudioContext a
	public Glide sw;
	public float level=0.1f;
	public Gain rev2;
public Feedback(AudioContext ac){super(ac,2,0.75f);
sw=new Glide(ac,-0.5f,200);    
Function soovich=new Function (sw){

	@Override
	public float calculate() {
		// TODO Auto-generated method stub
		
		return (float) Math.exp(-x[0]*x[0]*1.0f)*(0.9f+0.01f*level);
	}};
Function sGain=new Function(soovich){

	@Override
	public float calculate() {
		// TODO Auto-generated method stub
		return x[0]*1.2f;
	}};
	Function sDelay=new Function(soovich){

		@Override
		public float calculate() {
			// TODO Auto-generated method stub
			return 10f-x[0]*9.998f;
		}}; 
		
//Gain reduce=new Gain(ac,2,0.3f);
//Gain thi=new Gain(ac,2,0.9f);
rev2=new Gain(ac,2,sGain);

//reduce.addInput(sp);
//rev1.addInput(reduce);

TapIn ti=new TapIn(ac, 1000);
TapOut to=new TapOut(ac, ti,sDelay);
ti.addInput(this);
rev2.addInput(to);
this.addInput(rev2);

}
}
