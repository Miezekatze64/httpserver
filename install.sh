#!/bin/bash

INSTALL_DIR="/usr/lib/httpserver"

# checl if root
if [ $EUID == 0 ]; then
    echo "Don't run this as root, you will be prompted";
    exit 2;
fi

# compile webserver
./compile.sh
EXIT=$?

if [ $EXIT != 0 ]; then
    echo "Installation failed"
    exit $EXIT
fi
echo "Compiled sources"

ls ./httpserver.jar > /dev/null
if [ $? != 0 ]; then
    echo "File httpserver.jar not found"
    exit 3;
fi

ls ./page-src > /dev/null
if [ $? != 0 ]; then
    echo "page-src directory not found"
    exit 3;
fi

function installation() {
    install -d $INSTALL_DIR
    install ./httpserver.jar $INSTALL_DIR
    install -d $INSTALL_DIR/page-src/com/mieze/httpserver
    ls ./page-src/com/mieze/httpserver/* | while read LINE; do
        install $LINE $INSTALL_DIR/$LINE
    done
    echo "copied sources to $INSTALL_DIR"

cat<<EOF>/usr/bin/httpserver
#!/bin/bash
 
java -jar $INSTALL_DIR/httpserver.jar \$*
EOF
    
    EXIT=$?
    if [ $EXIT != 0 ]; then
        echo "Could not create /usr/bin/httpserver";
         exit $EXIT
    fi
    
    chmod +x /usr/bin/httpserver
    echo "created launch script"
}
echo ""
echo "Waiting for root permissions..."
sudo bash -c "$(declare -f installation); INSTALL_DIR=$INSTALL_DIR; installation"

echo ""
if [ $? == 0 ]; then
    echo "Sucessfully installed httpserver"
else
    echo "Installation FAILED"
fi
