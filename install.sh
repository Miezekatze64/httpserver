#!/bin/bash

INSTALL_DIR="/usr/lib/httpserver"

# check if root
if [ $EUID == 0 ]; then
    echo "Don't run this as root, you will be prompted";
    exit 2;
fi

# compile webserver
echo "Compiling:"
./compile.sh
EXIT=$?

if [ $EXIT != 0 ]; then
    echo "[E] Installation failed"
    exit $EXIT
fi
echo -e "[-] Compiled sources\n"

function check_file() {
    if [ ! -e $1 ]; then
        echo "[EROOR] File $1 not found"
        exit 255;
    fi
}

check_file ./httpserver.jar
check_file ./page-src

function installation() {
    echo "[+] creating folders..."
    install -d $INSTALL_DIR || return 1
    install -d $INSTALL_DIR/page-src/com/mieze/httpserver || return 1
    echo "[+] installing files..."
    install ./httpserver.jar $INSTALL_DIR || return 1
    ls ./page-src/com/mieze/httpserver/* | while read LINE; do
        install $LINE $INSTALL_DIR/$LINE || return 1
    done
    echo "[+] copied sources to $INSTALL_DIR"

cat<<EOF>/usr/bin/httpserver
#!/bin/bash
 
java -jar $INSTALL_DIR/httpserver.jar \$*
EOF
    
    if [ $? != 0 ]; then
        echo "[E] Could not create /usr/bin/httpserver";
        exit 1
    fi
    
    chmod +x /usr/bin/httpserver
    echo "[+] created launch script"
}

echo "Requesting root permissions:"
sudo bash -c "$(declare -f installation); INSTALL_DIR=$INSTALL_DIR; installation"

if [ $? == 0 ]; then
    echo -e "\n[*] Sucessfully installed httpserver"
else
    echo -e "\n[E] Installation FAILED"
fi
