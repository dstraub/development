# Development Server


For development is often an infrastructure with several services such as ActiveMQ, LDAP, Mail required.
This can be provided by local services on a Linux machine or a bunch of docker containers. 
Since these solutions require some effort / knowledge or are difficult to install Windows, we use an JBoss and an application that offers these services. 

With this approach we have an target for our application and a fresh infrastructure with a single command : 

`mvn clean install ...`


This creates  an Wildfly or JBoss-EAP  (`-Pjboss-eap`) server with the following features :

- ActiveMQ Resourceadapter and integrated broker, listen on 61616
- LDAP server, listen on 3389 (based on [unboundid-ldap-sdk](https://www.ldap.com/unboundid-ldap-sdk-for-java) )
- Mail server, listen on 2525 (based on [subethasmtp](https://github.com/voodoodyne/subethasmtp) )


## Test the infrastructure

### LDAP

The LDAP server uses `src/test/resources/example.ldif` for initial setup.
The server could be tested with 

	ldapsearch -v -H ldap://localhost:3389 -x -D cn=manager,dc=sample,dc=de -w admin123 "(cn=*)"

### ActiveMQ

The installed development web app provides an servlet the following functions:

    curl http://localhost:8080/development/jms
    JMS-Test parameter:
      op [send,receive,browse]
      queue QUEUE_NAME
      data TEXT_TO_SEND
      
Sample: 
Send a message :
    curl http://localhost:8080/development/jms -d queue=sample -d op=send -d data="some text"
    Message sent ID:bigmac.local-52974-1475258349154-4:1:1:1:1

Browse queue : 
    curl http://localhost:8080/development/jms -d queue=sample -d op=browse
    Message ID:bigmac.local-52974-1475258349154-4:1:1:1:1 some text

Receive message : 
    curl http://localhost:8080/development/jms -d queue=sample -d op=receive
    Message received ID:bigmac.local-52974-1475258349154-4:1:1:1:1 some text


### Mail
Send a mail with telnet:

	$ telnet localhost 2525
	Connected to localhost.
	Escape character is '^]'.
	220 bigmac.fritz.box ESMTP SubEthaSMTP 3.1.7
	helo test@localhost.de
	250 bigmac.fritz.box
	mail from: ds@ctrlaltdel.de
	250 Ok
	rcpt to: test@localhost.local
	250 Ok
	data 
	354 End data with <CR><LF>.<CR><LF>
	here is some text
	.
	250 Ok
	quit
	221 Bye
	Connection closed by foreign host.
	
The mail will appears in the server log:

	21:50:05,418 INFO  [de.ctrlaltdel.development.LogMesssageHandler] (org.subethamail.smtp.server.Session-/127.0.0.1:53502) Mail from ds@ctrlaltdel.de, to test@localhost.local, content: 
	Received: from test@localhost.de (localhost [127.0.0.1])
	        by bigmac.local
	        with SMTP (SubEthaSMTP 3.1.7) id ITQ6NOQ7
	        for test@localhost.local;
	        Fri, 30 Sep 2016 21:49:56 +0200 (CEST)
	here is some text


It's also possible to test the mail function with the servlet `curl http://localhost:8080/development/mail`

	
