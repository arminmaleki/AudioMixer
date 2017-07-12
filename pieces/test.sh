let L=$((`ffmpeg -i $1 2>&1 | grep "Duration"| cut -d ' ' -f 4 | sed s/,// | sed 's@\..*@@g' | awk '{ split($1, A, ":"); split(A[3], B, "."); print 3600*A[1] + 60*A[2] + B[1] }'`/10))
echo "$L pieces"

echo "slicing file $1"
for ((i=1; i<$L-1;i++))
do
    let j=$i*10
    let k=$RANDOM%100
    echo "$2$i.wav"
     ffmpeg -i $1 -ss $j -t 10 $2$i.wav
done
