ftp -n -v $HOST << EOT
ascii
user $USER $PASSWORD
cd $TARGET_DIR
put $SOURCE_FILE
EOT