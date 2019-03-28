#!/bin/bash -eu
echo ">starting server..."
pushd ${QPID_WORK}

mkdir -p ./default/config
cp ${VHOST_FILE} ./default/config/default.json

/usr/local/qpid/bin/qpid-server