FROM jokeswar/base-ctl

RUN echo "Hello from Docker"

RUN apt update

# tzdata is required by openjdk-21
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN	apt install -yqq openjdk-21-jdk tree

COPY ./checker ${CHECKER_DATA_DIRECTORY}

COPY ./antlr-4.13.0-complete.jar ${CHECKER_DATA_DIRECTORY}/..
