This a very preliminary, pre-alpha, Stage 0 release of the Base Technology Agent Server software.

What is a software agent?
-------------------------

According to my "standard" definition:
(http://agtivity.com/agdef.htm)

"A Software Agent (or Autonomous Agent or Intelligent Agent) is a computer program which works toward
 goals (as opposed to discrete tasks) in a dynamic environment (where change is the norm) on behalf of 
 another entity (human or computational), possibly over an extended period of time, without continuous
 direct supervision or control, and exhibits a significant degree of flexibility and even creativity in
 how it seeks to transform goals into action tasks."

Stage 0 is a subset of the more grand vision and is simply a server environment in which small but
powerful "programs" can run continuously, accessing resources on the Web and bth using the outputs
of other agents and generating output that other agents can use.

In short, Stage 0 agents are a new model for "software components", smaller than full-blown processes
but larger and more autonomous than mere objects.

The agent server does not have a UI per se. You interact with the server using its REST API, most
typically with curl commands. See intro.txt.

----

The server is not yet suitable for production use and is intended for preliminary evaluation only.

See legal/LICENSE.txt for software licensing terms. Basically, it is the Apache ASL 2.0 -- open source.

See NOTICES.txt for licensing of software that is included with this software.

Full source code can be found on GitHub at https://github.com/jack-krupansky-BT/Agent-Server-Stage-0
It was developed in Java in Eclipse and can be built and tested using the ant build.xml script.

The zip file for the server can be downloaded from here:
https://github.com/jack-krupansky-BT/Agent-Server-Stage-0/blob/master/dist/AgentServer-0.1.zip

Instructions for installation and simple testing are here:
https://github.com/jack-krupansky-BT/Agent-Server-Stage-0/blob/master/doc/install.txt

For more information contact Jack Krupansky at Jack@BaseTechnology.com.

.
