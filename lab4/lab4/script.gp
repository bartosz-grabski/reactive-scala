clear
reset

set border 3
set auto
set key off

set xrange[0:40]
set yrange[0:10]
set xtics 1
set ytics 1

# Make some suitable labels.
set title "Time/frequency histogram"
set xlabel "Milliseconds"
set ylabel "Count"

file = "rroutput20000"

set terminal png enhanced font arial 10 size 1000,600
ft="png"
# Set the output-file name.
set output file.ft
 
set style histogram clustered gap 1
set style fill solid border -1
 
binwidth=1
set boxwidth binwidth
bin(x,width)=width*floor(x/width) + binwidth/2.0
 
plot file using (bin($1,binwidth)):(1.0) smooth freq with boxes