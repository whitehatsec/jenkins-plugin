#!/bin/bash
/usr/bin/expect<<EOD

spawn sftp $USER@$HOST
expect "password:"
send "\\$PASSWORD\\r"
expect "sftp>"
send "cd $TARGET_DIR\r"
expect "sftp>"
send "put $SOURCE_FILE\r"
expect "sftp>"
send "bye\r"
exit
EOD