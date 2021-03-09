SYSC 3303A - Assignment 3

Student Name: Jeong Won Kim
Student Number: 101094705

Files Included:		
Client.java: code for Client
Server.java: code for Server
IntermediateHost.java: code for intermediate host
a3_UML_diagrams.pdf: this includes UML class diagrams and sequence diagram

Setup Instructions:
The program was tested with Eclipse IDE for Java Developers Version: 2019-12 (4.14.0) and JDK 11.0.1
1. Unzip the archive file in your workspace, then you will see a project folder
2. Open Eclipse IDE and click File -> Open Projects from File System
3. Find the directory and import the project
4. Open 3 separate consoles
5. Right click and run as java application in following order (IntermediateHost.java -> Client.java -> Server.java)
6. Change the consoles for each to monitor the three programs
7. The output will be showing on the console
8. The iteration will stop at 11th iteration with invalid request exception in the server

Questions
1. Why did I suggest that you use more than one thread for the implementation of the Intermediate task?

The intermediate task needs to represent both the networking with the Client and the networking with the Server.
By having two threads, the system can easily achieve real-time computing with those threads handling a separate networking procedure from each other.

2. Is it necessary to use synchronized in the intermediate task? Explain.

It may be possible to achieve the goal without using synchronized in the intermediate task, but in general, there needs to secure the critical section in this system.
Specifically, the intermediate task uses more than one threads and the threads call the receive and reply procedure in common.
Therefore, synchronized is necessary to keep the critical section and manage the owrnership among threads.