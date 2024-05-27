- # General
    - #### Team#: 29
    
    - #### Names: William Chu, Matthew Phan
    
    - #### Project 5 Video Demo Link: https://drive.google.com/file/d/1WEGwBcWXQplsEIk8569AKwvZxsmGHOYp/view?usp=sharing

    - #### Instruction of deployment: Compile using mvn package in the backend servers (master and slave). Deploy onto Apache Tomcat, on backend servers. Connect with Apache load balancer that redirects to deployed web apps.

    - #### Collaborations and Work Distribution:
      - William: AWS, Fuzzy Search
      - Matthew: Full text search, recording, AWS


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    Dashboard.java
    EmployeeLogin.java
    Genre.java
    Genres.java
    Login.java
    Movie.java
    Movies.java
    Pay.java
    SingleStarServlet.java
    Stars.java
    SearchEngine.java
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    Connection pooling allows us to not waste connections to our MySQL server, as we use an already existing pool of connections. This would decrease our creation of new connections, which would waste resources.
    
    - #### Explain how Connection Pooling works with two backend SQL.
    Each backend will have a separate connection pool, which allows us to distribute the requests evenly, and ensures that resources will be allocated and in an efficient manner that distributes it properly. This causes each access and need for a connection to be split from each other, yet also in a not wasteful way by pooling. Connection pooling is done and configured in "WebContent/META-INF/context.xml."

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
         -    WebContent/META-INF/context.xml
         -    WebContent/WEB-INF/web.xml
         -    aws-load-balancer.conf
         -    gcp-load-balancer.conf

    - #### How read/write requests were routed to Master/Slave SQL?
        - Requests were routed with the load balancer, which evenly distributes to the Master and Slave. Furthermore, only the Master is able to write, but both instances are able to read. The master's queries are replicated on the slave. Furthermore, sessions were kept by keeping a cookie of the backend server that holds the connection and "sticking" that client to that server.
