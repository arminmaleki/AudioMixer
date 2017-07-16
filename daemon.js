var os=require('os');
var url=require('url');
var fs=require('fs');
var http=require('http');
var fileurl="./out.pcm";
var lame=require('lame');
var net=require('net');
var encoder = lame.Encoder({channels: 2
		, bitDepth: 16, sampleRate: 44100});
var exp=require('express');
var cr=require('crypto');
var decoder = lame.Decoder();

var ws=fs.createWriteStream('out2.pcm');
var mp3ws=fs.createWriteStream('beep.mp3');
var total=0;
var time=-1;
var clientres={};
var clientnumber=0;
var connected=false;
net.createServer(function(socket){
		 console.log('connection stablished');
    socket.on('data',function(chunk){//console.log(chunk);
//	ws.write(chunk);
	if (connected) {
	    encoder.write(chunk);
//	    console.log("writing from "+ total/4/44100+" seconds");
	}
	total+=chunk.length;
//	console.log(total/4/44100);
    });
}).listen(6666,'127.0.0.1');



var i=0;
var surpluss=0;
//var res;


encoder.on('data',function(chunk){i++;// console.log(i,chunk[0],chunk.length,surpluss);
				  surpluss+=chunk.length;
				  Object.keys(clientres).forEach(function(key){
				            clientres[key].write(chunk);

				  });
				
				
			
				 });


var app=exp();
app.use("/stream.mp3",function(request,response){
  
   
   // res=response;
   
    clientnumber++;
    var id=cr.randomBytes(20).toString('hex');
    clientres[id]=response;
    console.log("client connected. number of clients "+clientnumber+" "+Object.keys(clientres).length+" "+id);
    connected=true;
    
	
    request.on("close",function(){clientnumber--;
				  console.log("connection "+id+"  closed");
				  console.log("number of clients now:",clientnumber);
				  delete clientres[id];
				  ;});

}).listen(8081);
