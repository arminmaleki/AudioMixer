package mixer;

import java.util.HashMap;
import java.util.Map;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;

class Repository {
	
	static public Repository current=null;
   static private Clock  c;
	static public Map<String,Repository> list=new HashMap<String,Repository>();
	private static AudioContext ac;
	static public void setAudioContext(AudioContext ac){Repository.ac=ac;}
	public  int totalSamples,firstSample;
	/**
	 * a volume amplification factor for repository. final volume is volume*inSampleVolume
	 */
	public float volume;
	/**
	 * time during which Semaphore.current stays in a specific repository
	 */
	public float time;

	public String name,URI;
	/**
	 * Starts a sequence of state changes for Semaphore. after "time" it goes to a random but new state.
	 */
	static public void setRandomWithDelay(){
	
		c=new Clock(ac,current.time*1000);
		System.out.println("Set Random With Delay " + current.time * 1000);
		c.addMessageListener(new Bead(){


			@Override
			public void messageReceived(Bead message){
				Clock c = (Clock)message;
				//if (c.isBeat()) System.out.println("beat "+c.getBeatCount());
				if (c.isBeat()&&c.getBeatCount()==1){ 
					Repository.c.kill();
					setRandom(); setRandomWithDelay();System.out.println("Repository set to "+current.name);
				
				}
			}});
		ac.out.addDependent(c);
		c.start();
	}
	/**
	 * sets Semaphore.current to a random new state.
	 */
	static public void setRandom(){
		Repository candidate=null;
		do{
			int ind=(int)(Math.random()*list.values().size());
			candidate= (Repository) list.values().toArray()[ind];
		}while (candidate.equals(current)|| list.values().size()<2);
		current=candidate;

	}
	@Override
	public String toString(){
		String s;
		s="name: "+this.name+"\t";
		s+="URI: "+this.URI+"\t";
		s+="firstSample: "+this.firstSample+"\t";
		s+="totalSamples: "+this.totalSamples+"\t";
		s+="volume: "+this.volume+"\t";
		s+="time: "+this.time+"\t";
		return s;}

}

