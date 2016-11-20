#!/usr/bin/python3
#
# Using this as a rest-ful server so that I can develop the android client
#  
# by Steve Mink
# Copyright 2016 Mink Technologies LLC
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
#
import argparse
from flask import Flask, jsonify, request, make_response
import subprocess
import time

HTTP_STATUS_NOT_FOUND = 404
FORTUNE_APP = '/usr/games/fortune'

version = 1.0
app = Flask(__name__)

def runCmd(cli):
    p = subprocess.Popen(cli, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    while(True):
        rc = p.poll()
        line = p.stdout.readline()
        yield line
        if(rc is not None):
            break

@app.route('/fortune', methods=['GET'])
def device_list():
    literal = 'fortune'
    f = ""
    try:
        # make me look more like this: {"id":1,"content":"Hello, World!"}
        for line in runCmd(FORTUNE_APP):
            f += line.strip().decode('utf-8')
    except FileNotFoundError:
        literal = 'error'
        f = 'application not found'

    time.sleep(5) # for testing different conditions
    return jsonify({literal: f})

@app.errorhandler(HTTP_STATUS_NOT_FOUND)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}),
                         HTTP_STATUS_NOT_FOUND)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("-b", "--bind", default='0.0.0.0', action="store", 
                        dest="bindaddr", help="only listen on specified address")
    parser.add_argument("-p", "--port", type=int, default=5100, action='store',
                        dest='portnum', help="port number server listens on")
    args = parser.parse_args()
    app.run(debug=True, host=args.bindaddr, port=int(args.portnum))

