#!/usr/bin/env bash
rm -f *.class
rm -rf _ChatRoom
idlj -fall  Server.idl
javac *.java _ChatRoom/*.java
orbd -ORBInitialPort 1050&
java Main -ORBInitialPort 1050 -ORBInitialHost localhost&



