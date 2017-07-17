package mixer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Compressor;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.RecordToSample;
import net.beadsproject.beads.ugens.SamplePlayer;

public class Mixer {
	AudioContext ac;
	private   float inSampleVolume=1.5f,outSampleVolume=0.8f,granularVolume=1.5f;
	private   float beatLength=0.5f;

	
	private  Gain Master;
	private  ReverbSample reverbSample;
	private  Sample[] allSamples;
	private  String[] tagSample;
	private  RecordToSample rts;
	private  RecordToSample totalRts;
	private  Sample samp=new Sample(10020);
	private  Sample totalSamp=new Sample(100000D,2);
	
	private  Feedback fb1;
	private  UGen granularGain;
	private  float metronomBeat;
	private  int loadSampleEvery;
	private  int loadSampleOn;
	private  int inSampleNumber;
	private  int outSampleNumber;
	private  float granularCutoff;
	private  float granularGrain;
	private  boolean fromScratch;
	private  String extension;
	private  boolean noRealTime;
	private  float norealTimeTime;
	private  boolean noFeedback;
	private  void loadSamples() {
		try {
			for (int i=0;i<outSampleNumber;i++){
				allSamples[i]=new Sample(Repository.current.URI+(int)(Math.random()*Repository.current.totalSamples+Repository.current.firstSample)+"."+extension);
				tagSample[i]=Repository.current.name;
			}
			if (!fromScratch)
			for (int i=outSampleNumber;i<inSampleNumber+outSampleNumber;i++)
			{	allSamples[i]=new Sample("samp"+(i-outSampleNumber)+"."+extension); tagSample[i]="RECORDED";}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("error");
			e.printStackTrace();
		}
		System.out.println("Sample loaded");
	}
public Mixer(AudioContext acc,String xmlfile){
	this.ac=acc;
	fb1=new Feedback(ac);
	 Master=new Gain(ac,2,(float) .5);
	 granularGain=new Gain(ac,2,0.9f);
	initialize(xmlfile);
	Repository.setRandom();
	System.out.println("Loading Samples from:"+Repository.current.name);
	allSamples=new Sample[inSampleNumber*((fromScratch)?0:1)+outSampleNumber];
	tagSample=new String[inSampleNumber*((fromScratch)?0:1)+outSampleNumber];
	loadSamples();
	Repository.setRandom();
	Repository.setRandomWithDelay();
	
	unit1 u1=new unit1();
	u1.message(null);
	
	rts= new RecordToSample(ac, samp, RecordToSample.Mode.FINITE);
	Clock c=new Clock(ac,1000f);

	c.addMessageListener(new Bead(){
		private int nsamp=0;

		@Override
		public void messageReceived(Bead message){
			Clock c = (Clock)message;
			if(c.isBeat()){
				if (c.getBeatCount()%4==0) System.out.println("Metronom beat " + c.getBeatCount()); 
				if ((c.getBeatCount()%10==0 )&&(c.getBeatCount()>30)){

					rts.pause(true);
					try {
						samp.write("samp"+(nsamp%inSampleNumber)+"."+extension);
						System.out.println("writing " + "samp" + (nsamp % inSampleNumber) + "."+extension);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {if (!fromScratch)
						allSamples[nsamp%inSampleNumber+outSampleNumber]=new Sample("samp"+(nsamp%inSampleNumber)+"."+extension);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					samp.clear();
					rts.reset();
					rts.start();
					nsamp+=1;
				}
				if (c.getBeatCount()%loadSampleEvery==loadSampleOn) {
					try {
						int rand1=(int)(Math.random()*outSampleNumber);
						int rand2=(int)(Math.random()*Repository.current.totalSamples+Repository.current.firstSample);
						String uri=Repository.current.URI+rand2+"."+extension;
						allSamples[rand1]=new Sample(uri);
					    tagSample[rand1]=Repository.current.name;
					   System.out.println( "Sample "+uri+" loaded from repository "+tagSample[rand1]+ " into slot "+ rand1);
					    
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				if (c.getBeatCount()%4==0) {
					if (Math.random()<0.0){
						fb1.sw.setValue((float)Math.random()*0.2f); System.out.println("SW "+fb1.sw.getValue());}
					else {fb1.sw.setValue((float)Math.random()*1.6f+0.25f);
						//fb1.sw.setValue((float)3.0);
					}	
				}

				if (c.getBeatCount()%6==4){
					int rand=(int)(Math.random()*(inSampleNumber*((fromScratch)?0:1)+outSampleNumber));
					SamplePlayer sp=new SamplePlayer(ac, allSamples[rand]);
					Glide pitch=new Glide(ac,0.8f+(float)(Math.random()*0.4),100);
					sp.setPitch(pitch);
					final Gain g;
					if (rand<outSampleNumber)  g=new Gain(ac,2,outSampleVolume*Repository.list.get(tagSample[rand]).volume);

					else g=new Gain(ac,2,inSampleVolume);

					g.addInput(sp);

					final Function spGainReg=new Function(g){

						@Override
						public float calculate() {return (float)Math.tanh(x[0]*1.8);}};
						sp.setKillListener(new Bead(){@Override
							public void messageReceived(Bead message){
							g.kill();
							spGainReg.kill();
						}});
						Master.addInput(spGainReg);
						if (!noFeedback) fb1.addInput(spGainReg);
						sp.start();

						System.out.println("new Sample "+ rand+" from "+tagSample[rand]);
				}}

		}
	});

	Function granGainReg=new Function(granularGain){

		@Override
		public float calculate() {return (float)Math.tanh(x[0]*1.8);}};

		Master.addInput(granGainReg); 


		granularGain.addInput(reverbSample);
		ac.out.addDependent(c);


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


			

			Function redfb1=new Function(fb1){

				@Override
				public float calculate() {
					
					if (x[0]>1) return 1;
					if (x[0]<-1) return -1;
					return x[0];
				}};

				if (!noFeedback) ac.out.addInput(redfb1);
				ac.out.addInput(comp);
				
				ac.out.addDependent(rts);
				
				
			//	if (noRealTime) 	ac.runForNMillisecondsNonRealTime(norealTimeTime*1000); else
			//		ac.start();
				
}
private  boolean initialize(String xmlfile) {

	File inputFile = new File(xmlfile);
	DocumentBuilderFactory dbFactory 
	= DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder;
	try {
		dBuilder = dbFactory.newDocumentBuilder();
		Document doc;
		try {
			doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			NodeList nl1=doc.getDocumentElement().getChildNodes();
			NodeList fscratch=doc.getDocumentElement().getElementsByTagName("fromScratch");
			if (fscratch.getLength()>0){ System.out.println("From Scratch"); fromScratch=true;} else fromScratch=false;
			NodeList wav=doc.getDocumentElement().getElementsByTagName("wav");
			if (wav.getLength()>0) {System.out.println("wav"); extension="wav"; } else extension="mp3";
			NodeList noRealTime=doc.getDocumentElement().getElementsByTagName("noRealTime");
			if (noRealTime.getLength()>0) { this.noRealTime=true; norealTimeTime=Float.parseFloat(((Element)noRealTime.item(0)).getAttribute("time"));
					System.out.println("noRealtime time="+norealTimeTime);} else this.noRealTime=false;
			NodeList noFeedback=doc.getDocumentElement().getElementsByTagName("noFeedback");
			if ( noFeedback.getLength()>0) { this. noFeedback=true;
					System.out.println("noFeeback");} else this.noFeedback=false;
			Element general=  (Element) doc.getDocumentElement().getElementsByTagName("general").item(0);
			
			Element elem= (Element) general.getElementsByTagName("tempo").item(0);
			System.out.println("Tempo:");
			beatLength=Float.parseFloat(elem.getAttribute("beatLength"));
			System.out.print("beatLength: "+beatLength+"\t");
			
			metronomBeat=Float.parseFloat(elem.getAttribute("metronomBeat"));
			System.out.print("metronomBeat: "+metronomBeat+"\t");
			
			loadSampleEvery=Integer.parseInt(elem.getAttribute("loadSampleEvery"));
			System.out.print("loadSampleEvery: "+loadSampleEvery+"\t");
			
			loadSampleOn=Integer.parseInt(elem.getAttribute("loadSampleOn"));
			System.out.println("loadSampleOn: "+loadSampleOn+"\t");
			
			 elem= (Element) general.getElementsByTagName("volume").item(0);
			 System.out.println();
			 System.out.println("Volume:");
			inSampleVolume=Float.parseFloat(elem.getAttribute("inSample"));
			System.out.print("inSampleVolume: "+inSampleVolume+"\t");
			outSampleVolume=Float.parseFloat(elem.getAttribute("outSample"));
			System.out.print("outSampleVolume: "+outSampleVolume+"\t");
			granularVolume=Float.parseFloat(elem.getAttribute("granular"));
			System.out.print("granularVolume: "+granularVolume+"\t");
			System.out.println();
			
			System.out.println();
			elem= (Element) general.getElementsByTagName("sampleNumber").item(0);
			System.out.println("SampleNumber:");
			inSampleNumber=Integer.parseInt(elem.getAttribute("in"));
			System.out.print("inSampleNumber: "+inSampleNumber+"\t");
			outSampleNumber=Integer.parseInt(elem.getAttribute("out"));
			System.out.print("outSampleNumber: "+outSampleNumber+"\t");
			System.out.println();
			System.out.println();
			elem= (Element) general.getElementsByTagName("granular").item(0);
			System.out.println("Granular:");
			granularCutoff=Float.parseFloat(elem.getAttribute("cutoff"));
			System.out.print("granularCutoff: "+granularCutoff+"\t");
			granularGrain=Float.parseFloat(elem.getAttribute("grain"));
			System.out.print("granularGrain: "+granularGrain+"\t");
			
			System.out.println();
			System.out.println();
			System.out.println("Loaded repositories:");
			System.out.println();
			/*
			 * <sampleNumber out="10" in="10" />
	<granular cutoff="400" grain="70" />
			 * <tempo beatLength="0.5" metronomBeat="1.0" loadSampleEvery="10"
		loadSampleOn="7" />
			 private static  float inSampleVolume=1.5f,outSampleVolume=0.8f,granularVolume=1.5f;
private static  float beatLength=0.5f;
			 */
			
			NodeList repositories=doc.getDocumentElement().getElementsByTagName("repository");
			Repository.setAudioContext(ac);
			for (int i=0;i<repositories.getLength();i++){
				Element rep=(Element) repositories.item(i);
				Repository s=new Repository();
				s.name=rep.getAttribute("name");
				s.URI=rep.getAttribute("URI");
				s.firstSample=Integer.parseInt(rep.getAttribute("firstSample"));
				s.totalSamples=Integer.parseInt(rep.getAttribute("totalSamples"));
				s.volume=Float.parseFloat(rep.getAttribute("volume"));
				s.time=Float.parseFloat(rep.getAttribute("time"));
                Repository.list.put(s.name, s);
				System.out.println(s.toString());
			}
			System.out.println("");

		} catch (SAXException e) {
			e.printStackTrace();
			return false;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}

	} catch (ParserConfigurationException e) {
		e.printStackTrace();
		return false;

	}
	return true;


}
/**
 /**
 * its a bead that controls the behavior of reverbSample. it generate a rhythm by manipulating it's Gain.
 * @author armin
 *
 */
class unit1 extends Bead{
	@Override
	public void messageReceived(Bead message){
		

		if (reverbSample!=null) reverbSample.kill();
		
		
		int rand=(int)(Math.random()*outSampleNumber);
		float vol=granularVolume*Repository.list.get(tagSample[rand]).volume;
		Envelope e=new Envelope(ac,vol);
	//	System.out.println("rhythm sample "+rand);
		reverbSample=new ReverbSample(ac, allSamples[rand], e);	
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
