package mixer;
import net.beadsproject.beads.core.AudioContext;


public class Main {

	

	private static AudioContext ac=new AudioContext();

	public static void main(String[] args) {
     Mixer mixer=new Mixer(ac,"properties.xml");
     Broadcast bc=new Broadcast(ac, 6666, 4000, 10, 1.0f);
	}
}
