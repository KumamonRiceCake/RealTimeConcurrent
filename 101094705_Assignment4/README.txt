SYSC 3303A - Assignment 4

Student Name: Jeong Won Kim
Student Number: 101094705

Files Included:		
Client.java: code for Client
Server.java: code for Server
IntermediateHost.java: code for intermediate host
a3_UML_diagrams.pdf: this includes UML class diagrams and sequence diagram
Instant - Intermediate Host for Client to Server.txt: 10 measured time using client to server delivery thread with Instant methods
Instant - Intermediate Host for Server to Client.txt: 10 measured time using server to client delivery thread with Instant methods
nanoTime - Intermediate Host for Client to Server.txt: 10 measured time using client to server delivery thread with nanoTime methods
nanoTime - Intermediate Host for Server to Client.txt: 10 measured time using server to client delivery thread with nanoTime methods

Setup Instructions:
The program was tested with Eclipse IDE for Java Developers Version: 2019-12 (4.14.0) and JDK 11.0.1
1. Unzip the archive file in your workspace, then you will see a project folder
2. Open Eclipse IDE and click File -> Open Projects from File System
3. Find the directory and import the project
4. Open 3 separate consoles
5. Right click and run as java application in following order (IntermediateHost.java -> Client.java -> Server.java)
6. Change the consoles for each to monitor the three programs
7. The output will be showing on the console
8. The iteration will stop after 1000 packet deliveries from the client to the server
9. The measured time is saved in two files named "Intermediate Host for Client to Server" and "Intermediate Host for Server to Client.txt"