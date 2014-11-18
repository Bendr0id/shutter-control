ShutterControl 1.0
==============

is a simple commandline tool written in Java to control shutters.

It lets you connect a 4-channel (optional 8-Channel) remote control for shutters to your raspberry pi and takes care of controlling the shutter via the the raspberry pi's io ports.

The project is using the library Pi4J (http://pi4j.com/)

----------------------------------------------
Hardware requirements
----------------------------------------------

Raspberry Pi board (http://www.raspberrypi.org/)

Raspbian installed on your Raspberry Pi (http://raspbian.org/)

A 4-channel remote control for shutters

----------------------------------------------
Software requirements
----------------------------------------------

<b>Updating the apt Repo to have the latest versions</b>

<pre>
  sudo apt-get update
</pre>  

<b>Installation of the Java JDK 7:</b>

<pre>
 sudo apt-get install openjdk-7-jdk
</pre>

<b>Installation of the latest maven</b>

<pre>
 sudo apt-get install maven
</pre>

<b>Installation of the latest git client</b>

<pre>
 sudo apt-get install git
</pre>


<b>Cloning the latest shutter-control sources</b>

<pre>
 git clone https://github.com/Bendr0id/shutter-control.git
</pre>

<b>Building the shutter-control application</b>

<pre>
 cd shutter-control
 mvn clean install
</pre>

----------------------------------------------
Wiring Diagram
----------------------------------------------

<b>This diagram shows howto connect a 4-channel shutter remote control to your raspberrypi</b>

![alt tag](https://raw.githubusercontent.com/bendr0id/shutter-control/master/wiring_diagram_4_channel.png)

----------------------------------------------
Usage
----------------------------------------------
<b> You can use the shutter-control application by typing</b>

<pre>
  java -jar shutter-control.jar -s [shutter] -o [operation]
</pre>


<b>Options</b>

<pre>
 -h, --help
</pre>
Prints the help text and exits

<pre>
 -o, --operation [up/down/stop]
</pre>
Required parameter for setting the operation

<pre>
 -s, --shutter [1/2/3/4]
</pre>
Required parameter for setting the shutter to control
