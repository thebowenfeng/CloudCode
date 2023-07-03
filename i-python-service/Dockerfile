FROM python:3.10.12-bookworm

COPY . /runtime

RUN pip install pexpect

ENTRYPOINT ["python3", "/runtime/server.py"]