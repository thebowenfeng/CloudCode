FROM python:3.10.12-bookworm

COPY . /runtime

RUN pip install pexpect
RUN pip install requests
RUN apt install gcc

EXPOSE 9000
ENTRYPOINT ["python3", "/runtime/server.py"]