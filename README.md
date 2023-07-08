
# CloudCode

This project aims to provide an API that allows users to safely execute arbitrary code on the cloud. 

### Table of contents
- [Running the project](#running-the-project)
  * [Requirements](#requirements)
    + [Docker](#docker)
    + [Backend](#backend)
    + [Load balancer](#load-balancer)
  * [Local development](#local-development)
    + [Backend](#backend-1)
    + [Load balancer](#load-balancer-1)
  * [Deploy to production](#deploy-to-production)
    + [Docker](#docker-1)
    + [Redis](#redis)
    + [Backend](#backend-2)
    + [Load balancer](#load-balancer-2)
- [Overview](#overview)
- [Documentation](#documentation)


## Running the project

The following section details how to run and deploy this application on your own machine or on cloud VMs. It is recommended to run all components under a Linux (preferably Ubuntu) based environment, other OSes has not been tested to work and may or may not function appropriately.

### Requirements

#### Docker

Docker engine must be installed running whilst the backend server is running. It is recommended to have the docker service run on system startup. It is also highly encouraged to have the docker daemon run, in order to ensure that containers can run properly.

The backend leverages the docker engine API in order to programatically manage docker container instances. The docker socket must be binded to the loopback address so that programs can access the engine API on localhost. [This tutorial](https://medium.com/@ssmak/how-to-enable-docker-remote-api-on-docker-host-7b73bd3278c6) shows the process to expose the docker socket. By default, the backend assumes docker is listening on port `4243`.

All docker services located under `/docker-services` must be built. By default, the backend server requires the following docker image names to exist:
- `ipython:latest` for `i-python-service`
- `python:latest` for `python-service`
- `c:latest` for `c-service`

These names can be customised in the backend. Details are in the backend README file.

#### Backend

Java 18 JRE and JDK is needed in order to build and run the backend. 

The docker service must be running.

Before starting the server, the following environment variables needs to be set:

- `CODECLOUD_DOCKER_HOST`: IP address of the docker engine API
- `CODECLOUD_API_KEY`: API key to access protected endpoints of the backend

#### Load balancer

Python flask and redis-py must be installed.

A redis server must be running with protected mode off by default.

### Local development

#### Backend

To run the server, simply run `./gradlew bootJar`, and run the compiled JAR file `java -jar COMPILED_JAR_FILE.jar`. The server will listen on port 8080 by default.

#### Load balancer

To run the load balancer, simply run `app.py` or `python -m flask run`. 

If the redis server is in protected mode, then the load balancer must be configured accordingly. By default, the load balancer assumes the redis server is running with protected mode turned off.

Add at least one instance of the backend server (e.g `add_server("localhost:8080")` for a locally running instance) in `app.py` or dynamically via the load balancer's API.

### Deploy to production

#### Docker

It is recommended to have one docker engine service per backend server, running on the same machine. This is to ensure lower network latency when creating and running containers. However, it is possible to manually allocate a remote machine to run docker related tasks, as long as the docker engine API is remotely accessible, and all required images are built under that machine.

#### Redis

It is recommended to have a singular, remote redis database running. **DO NOT** spin up a new redis database for each instance of a load balancer. The load balancer requires a central database to persist session related data and synchronization.

#### Backend

It is highly recommended to run the backend server as a Linux service and configure it to run on system startup, and restart on crash. Each individual machine/VM must only run at most one instance of the backend server.

#### Load balancer

The load balancer is not strictly needed when there is only one instance of the backend server. However, for large scale workload, where there are multiple backend servers running concurrently, it is highly recommended to utilize the load balancer. 

It is recommended to have the load balancer running under a PaaS service (like Heroku), connected to a single redis database. 

A possible configuration might look something like:![Possible architecture](https://i.imgur.com/KvnPd88.jpg)

## Overview

The project relies upon docker to ensure consistency and security of running arbitrary code on the host machine. 

Consistency is guaranteed as each instance of a program is ran on the same image (Ubuntu) and kernel (Ubuntu's Linux kernel), which more or less guarantees that if a program can be reasonably ran on a naked Ubuntu installation, then it can be ran on the server using docker, yielding comparable output. Since this service is designed for users to quickly mock up simple lines of code, any use of specialized libraries or niche features of other Linux distros will not work, and is not what the service is designed for.

Security is guaranteed by docker's application level sandboxing. In theory, processes that are ran within a docker container cannot access the host system's environment. However, such processes are still able to exploit any weaknesses in docker's sandboxing technology, or flaws in the underlying system kernel. That being said, any trivial attempts to "escape" the docker container will likely not work. Furthermore, docker is also able to limit the resource usage of each container, which largely prevents DoS attempts where users submit code that is designed to consume copious amount of system resources (e.g fork bombs).

In this sense, docker draws parallels with a VM. Some might argue that VMs offers more security (less susceptible to exploits that "escapes" the environment). However, the overhead of spinning up a guest system just to run a trivial program is, in my opinion, wasteful and not enough to justify substituting docker with raw VMs.  

The backend server simply needs to keep track and manage sessions, which refers to some user submitting and interacting with some program. In short, the server creates and destroys docker containers by utilizing docker engine's API, and also communicates with processes within each container using a simple TCP socket, forwarding any STDIN or STDOUT. It also nicely sanitizes any stale containers and removes any sessions whose container has unexpectedly crashed.

 As code execution is a resource heavy task in nature, the project is designed to allow horizontal scaling across multiple systems. Each backend server is treated as a single unit, able to concurrently run a finite number of sessions. The load balancer is then responsible for routing requests to the server with the least load (load is defined here as the server who is running the least amount of sessions), in turn evenly distributing the workload across all available units. The reason why pre-existing load balancers such as NGINX is not used, is due to session consistency. In other words, once a session is created, then all subsequent request to that session must be routed to the same server. The custom load balancer is also able to optimize even load balancing by promptly removing stale sessions from its internal records (see diagram of a possible system architecture using distributed systems and load balancing)

## Documentation
