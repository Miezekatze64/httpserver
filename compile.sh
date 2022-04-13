#!/bin/bash

function gen_jar() {
    DIR=$PWD
    cd tmp
    jar --main-class com.mieze.httpserver.Server --create --file httpserver.jar com/mieze/httpserver/*.class
    mv httpserver.jar $DIR
    cd $DIR
    chmod +x httpserver.jar
}

echo "Starting compilation..."
cd ./src/
if javac com/mieze/httpserver/*.java -d ../tmp/; then
    echo "Compilation finished (.class files in ./bin/)"
    echo "run ./run.sh to run project"
	cd ..
	if ! gen_jar; then
		echo "jar creation FAILED. (See errors above...)"
		exit -1
	fi
else
    echo "Compilation FAILED. (See errors above...)"
    exit -1
fi
rm -rf tmp
exit 0
