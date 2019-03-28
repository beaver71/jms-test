#!/bin/bash -eu
/usr/sbin/qpidd -v
echo ">starting server..."
/usr/sbin/qpidd --config /config/qpidd.conf

